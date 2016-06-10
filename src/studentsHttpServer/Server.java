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
	
	Socket connectedClient = null;
	BufferedReader inFromClient = null;
	DataOutputStream outToClient = null;

	public Server(Socket client) {
		this.connectedClient = client;
	}
	public void run() {
		HandleQuery();
		closeConnection();
	}
	
	private void HandleQuery(){
		try {
			inFromClient = new BufferedReader(new InputStreamReader(connectedClient.getInputStream()));
			outToClient = new DataOutputStream(connectedClient.getOutputStream());
			String headerLine = inFromClient.readLine();;
			StringTokenizer tokenizer = new StringTokenizer(headerLine);
			String httpMethod = tokenizer.nextToken();
			String httpQueryString = tokenizer.nextToken();

			if (!httpMethod.equals("GET")){
				sendResponse(404, RESP_NOTFOUND);
				return ;
			}
			String[] info = httpQueryString.split("[?]");
			if(httpQueryString.indexOf("/students/add?") == 0){
				try{
					Student s = new Student(info[1]);
					if(data.get(s.getId()) != null){
						sendResponse(400, "Student already exists");
						return ;
					}
					if(data.add(s)){
						sendResponse(200, "Added succesfully");
					} else {
						sendResponse(400, "Data overflow");
					}
					return ;
				}catch(IllegalArgumentException e){
					sendResponse(400, "Illegal data");
					return ;
				}
			}
			if(httpQueryString.indexOf("/students/remove?") == 0){
				try{
					String id = (info[1].split("="))[1];
					int idNum = Integer.parseInt(id);
					if(data.get(idNum) == null){
						sendResponse(400, "Student does not exist");
						return ;
					}
					data.remove(idNum);
					sendResponse(200, "removed");
					return ;
				} catch (NullPointerException e){
					sendResponse(400, "Student was not found");
					return ;
				} catch (NumberFormatException e){
					sendResponse(400, "Illegal data");
					return ;
				}
			}
			if(httpQueryString.indexOf("/students/find?") == 0){
				try{
					String id = (info[1].split("="))[1];
					Student s = data.get(Integer.parseInt(id));
					sendResponse(200, s.toString());
					return ;
				} catch (NullPointerException e){
					sendResponse(400, "Student was not found");
					return ;
				} catch (NumberFormatException e){
					sendResponse(400, "Illegal data");
					return ;
				}
			}
			sendResponse(404, RESP_NOTFOUND);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnection(){
		try {
			if(inFromClient != null)
				inFromClient.close();
			if(outToClient != null)
				outToClient.close();
			if(connectedClient != null)
				connectedClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public void illegalDataResponse(String responseText) throws IOException{
		sendResponse(400, responseText);
	}
	
	public void sendResponse(int statusCode, String responseString) throws IOException {
		String statusLine = null;
		String serverdetails = "Server: Java HTTPServer\r\n";
		String contentLengthLine = null;
		String contentTypeLine = "Content-Type: text/html" + "\r\n";
		if (statusCode == 200)
			statusLine = "HTTP/1.1 200 OK" + "\r\n";
		else if(statusCode == 400){
			statusLine = "HTTP/1.1 400 Bad Request" + "\r\n";
		} else 
			statusLine = "HTTP/1.1 404 Not Found" + "\r\n";
		contentLengthLine = "Content-Length: " + responseString.length() + "\r\n";
		outToClient.writeBytes(statusLine);
		outToClient.writeBytes(serverdetails);
		outToClient.writeBytes(contentTypeLine);
		outToClient.writeBytes(contentLengthLine);
		outToClient.writeBytes("Connection: close\r\n");
		outToClient.writeBytes("\r\n");
		outToClient.writeBytes(responseString);
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
