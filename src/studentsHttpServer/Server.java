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

	Socket connectedClient = null;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;

	public Server(Socket client) {
		this.connectedClient = client;
	}
	public void run() {
		
		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());
			String headerLine = inFromClient.readLine();;
			StringTokenizer tokenizer = new StringTokenizer(headerLine);// WTF??
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			if (httpMethod.equals("GET")) {
				String[] info = httpQueryString.split("[?]");
				if(httpQueryString.indexOf("/students/add?") >= 0){
					System.out.println("new student:" + info[1]);
					try{
						Student s = new Student(info[1]);
						data.add(s);
						sendResponse(200, "Added succesfully");
					}catch(IllegalArgumentException e){
						sendResponse(400, "Illegal data");
					}
				} else if(httpQueryString.indexOf("/students/remove?") >= 0){
					try{
						String id = (info[1].split("="))[1];
						System.out.println("remove: " + id);
						data.remove(Integer.parseInt(id));
						sendResponse(200, "removed");
					} catch (NullPointerException e){
						sendResponse(400, "Student was not found");
					} catch (NumberFormatException e){
						sendResponse(400, "Illegal data");
					}
				} else if(httpQueryString.indexOf("/students/find?") >= 0){
					try{
						String id = (info[1].split("="))[1];
						Student s = data.get(Integer.parseInt(id));
						sendResponse(200, s.toString());
					} catch (NullPointerException e){
						sendResponse(400, "Student was not found");
					} catch (NumberFormatException e){
						sendResponse(400, "Illegal data");
					}
				} else {
					sendResponse(404, "<b>The Requested resource not found ...."
				+ "Usage: http://127.0.0.1:5000</b>");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendResponse(int statusCode, String responseString) throws IOException {

		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer";
		String contentLengthLine = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";

		if (statusCode == 200)//add new classs trhat handles /r/n
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
		
		contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";

		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");

		outToClient.writeBytes(responseString);
		outToClient.close();
	}

	static DataHandler data;
	public static void main(String[] args) throws Exception {
		ServerSocket Server = new ServerSocket(0);
		System.out.println("Server is listenning on port " + Server.getLocalPort());
		data = new DataHandler();
		while (true) {
			Socket connection = Server.accept();
			new Server(connection).start();
		}
	}

}
