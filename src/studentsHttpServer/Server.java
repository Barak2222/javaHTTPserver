package studentsHttpServer;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class Server extends Thread {
	static final String RESP_NOTFOUND = "The Requested resource not found ...";
	static final String STATUSLINE_200 = "HTTP/1.1 200 OK" + "\r\n";
	static final String STATUSLINE_404 = "HTTP/1.1 404 Not Found" + "\r\n";
	static final String STATUSLINE_400 = "HTTP/1.1 400 Bad Request" + "\r\n";
	static final String SERVER_DETAILS = "Server: Java HTTPServer\r\n";
	static final String CONTENT_TYPE_HTML = "Content-Type: text/html" + "\r\n";
	static final String CLOSE_CONNECTION_MSG = "Connection: close\r\n";
	
	Socket connectedClient = null;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;

	public Server(Socket client) {
		this.connectedClient = client;
	}

	public void run() {
		try {
			HandleQuery();
		} catch (IOException e) {
			// Do nothing
		} finally {
			closeConnection();
		}
	}

	private void HandleQuery() throws IOException {
		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());
			String headerLine = inFromClient.readLine();
			if (headerLine == null || headerLine.length() < 1) {
				return;
			}
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			if (!httpMethod.equals("GET")) {
				sendResponse(404, RESP_NOTFOUND);
				return;
			}
			String[] info = httpQueryString.split("[?]");
			if (httpQueryString.indexOf("/students/add?") == 0) {
				Student s = new Student(info[1]);
				if (data.get(s.getId()) != null) {
					sendResponse(400, "Student already exists");
					return;
				}
				if (data.add(s)) {
					sendResponse(200, "Added succesfully");
				} else {
					sendResponse(400, "Data overflow");
				}
				return;
			}
			if (httpQueryString.indexOf("/students/remove?") == 0) {
				String id = (info[1].split("="))[1];
				int idNum = Integer.parseInt(id);
				if (data.get(idNum) == null) {
					sendResponse(400, "Student does not exist");
					return;
				}
				data.remove(idNum);
				sendResponse(200, "removed");
				return;
			}
			if (httpQueryString.indexOf("/students/find?") == 0) {
				String id = (info[1].split("="))[1];
				Student s = data.get(Integer.parseInt(id));
				sendResponse(200, s.toString());
				return;
			}
			sendResponse(404, RESP_NOTFOUND);
		} catch (NullPointerException e) {
			sendResponse(400, "Student was not found");
			return;
		} catch (NumberFormatException e) {
			sendResponse(400, "Illegal data");
			return;
		} catch (ArrayIndexOutOfBoundsException e) {
			sendResponse(400, "Illegal data");
		}
	}

	private void closeConnection() {
		try {
			if (inFromClient != null)
				inFromClient.close();
			if (outToClient != null)
				outToClient.close();
			if (connectedClient != null)
				connectedClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void illegalDataResponse(String responseText) throws IOException {
		sendResponse(400, responseText);
	}

	public void sendResponse(int statusCode, String responseString) throws IOException {
		String contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
		String statusLine = null;
		if (statusCode == 200)
			statusLine = STATUSLINE_200;
		else if (statusCode == 400) {
			statusLine = STATUSLINE_400;
		} else
			statusLine = STATUSLINE_404;

		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(SERVER_DETAILS);
		outToClient.writeBytes(CONTENT_TYPE_HTML);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes(CLOSE_CONNECTION_MSG);
		outToClient.writeBytes("\r\n");
		outToClient.writeBytes(responseString);
	}

	static DataHandler data;

	public static void main(String[] args) {
		try {
			ServerSocket Server = new ServerSocket(0);
			System.out.println("Server is listenning on port " + Server.getLocalPort());
			data = new DataHandler();
			while (true) {
				Socket connection = Server.accept();
				new Server(connection).start();
			}
		} catch (IOException e) {
			// do nothing
		}
	}

}
