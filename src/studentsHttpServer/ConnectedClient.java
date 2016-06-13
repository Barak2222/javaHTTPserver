package studentsHttpServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class contains all the logic to safely read the request from the client,
 * and respond with the needed response.
 * 
 * @author Barak
 *
 */
public class ConnectedClient implements Runnable {
	static final String RESP_NOTFOUND = "The Requested resource not found ...";
	static final String STATUSLINE_200 = "HTTP/1.1 200 OK" + "\r\n";
	static final String STATUSLINE_404 = "HTTP/1.1 404 Not Found" + "\r\n";
	static final String STATUSLINE_400 = "HTTP/1.1 400 Bad Request" + "\r\n";
	static final String SERVER_DETAILS = "Server: Java HTTPServer\r\n";
	static final String CONTENT_TYPE_HTML = "Content-Type: text/html" + "\r\n";
	static final String CLOSE_CONNECTION_MSG = "Connection: close\r\n";

	private DataHandler data;
	private Socket socket = null;
	private BufferedReader inFromClient = null;
	private DataOutputStream outToClient = null;
	private boolean isStreamsSet = true;

	/**
	 * Initialises this connection
	 * 
	 * @param Client
	 *            A given connection
	 * @param dh
	 *            Reference to the current data handler
	 */
	public ConnectedClient(Socket client, DataHandler dh) {
		this.socket = client;
		this.data = dh;
		try {
			inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			outToClient = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			isStreamsSet = false;
		}
	}

	/**
	 * Make sure the streams are set, Forward the request and finally close the
	 * connection
	 */
	public void run() {
		if (!isStreamsSet) {
			closeConnection();
			return;
		}
		try {
			HandleQuery();
		} catch (IOException e) {
			// Do nothing
		} finally {
			closeConnection();
		}
	}

	/**
	 * Parse the HTTP request and find out what needed to be done next
	 * 
	 * @throws IOException
	 */
	private void HandleQuery() throws IOException {
		String headerLine = inFromClient.readLine();
		if (headerLine == null || headerLine.length() < 1) {
			return;
		}
		StringTokenizer tokenizer = new StringTokenizer(headerLine);
		String httpMethod = tokenizer.nextToken();
		if (!httpMethod.equals("GET")) {
			send404();
			return;
		}
		String httpQueryString = tokenizer.nextToken();
		if (httpQueryString.indexOf("/stopserver") == 0) {
			stopServer();
			return;
		}
		String[] info = httpQueryString.split("[?]");
		if (httpQueryString.indexOf("/students/add?") == 0) {
			add(info[1]);
			return;
		}
		if (httpQueryString.indexOf("/students/remove?") == 0) {
			remove(info[1]);
			return;
		}
		if (httpQueryString.indexOf("/students/find?") == 0) {
			find(info[1]);
			return;
		}
		send404();
	}

	/**
	 * Search the data for the student with the asked ID
	 * 
	 * @param input
	 *            ID number of the needed student in URL param format
	 * @throws IOException
	 */
	private void find(String input) throws IOException {
		String id = parseId(input);
		if (id == null) {
			sendResponse(400, "Illegal data");
			return;
		}
		Student s = data.get(Integer.parseInt(id));
		if (s == null) {
			sendResponse(400, "Student does not exist");
		} else {
			sendResponse(200, s.toString());
		}
	}

	/**
	 * Removes the student corresponding to the ID number
	 * 
	 * @param input
	 *            A given ID number in URL param format
	 * @throws IOException
	 */
	private void remove(String input) throws IOException {
		String id = parseId(input);
		if (id == null) {
			sendResponse(400, "Illegal data");
			return;
		}
		int idNum = Integer.parseInt(id);
		if (data.get(idNum) == null) {
			sendResponse(400, "Student does not exist");
			return;
		}
		data.remove(idNum);
		sendResponse(200, "removed");
	}

	/**
	 * Add a Student to the data, and send a relevant response to the client
	 * 
	 * @param input
	 *            URL data of a student
	 * @throws IOException
	 */
	private void add(String input) throws IOException {
		if (!isLegalStudentPath(input)) {
			sendResponse(400, "Illegal data");
			return;
		}
		Student s = new Student(input);
		if (data.get(s.getId()) != null) {
			sendResponse(400, "Student already exists");
			return;
		}
		if (data.add(s)) {
			sendResponse(200, "Added succesfully");
		} else {
			sendResponse(400, "Data overflow");
		}
	}

	/**
	 * Stop the server
	 * 
	 * @throws IOException
	 */
	private void stopServer() throws IOException {
		sendResponse(200, "Closing server");
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Server.running = false;
		Server.closeServer();
	}
	
	/**
	 * Close this connection
	 */
	private void closeConnection() {
		try {
			if (socket != null)
				socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends a 404 error with a message
	 * 
	 * @throws IOException
	 */
	public void send404() throws IOException {
		sendResponse(404, STATUSLINE_404);
	}

	/**
	 * Sends HTTP response to the client
	 * 
	 * @param statusCode
	 *            HTTP status code
	 * @param responseString
	 *            String to be displayed to the client
	 * @throws IOException
	 */
	public void sendResponse(int statusCode, String responseString) throws IOException {
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
		outToClient.writeBytes(CLOSE_CONNECTION_MSG);
		outToClient.writeBytes("\r\n");
		outToClient.writeBytes(responseString);
	}

	/**
	 * Find out if a path defines a legal student data
	 * 
	 * @param student
	 *            as a given URL string
	 * @return true if the string matches a legal student data
	 */
	private static boolean isLegalStudentPath(String student) {
		String regex = "^(id\\=\\d+)(\\&name\\=\\w+)*(\\&gender\\=\\w+)*(\\&grade\\=\\d{1,3})*$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(student);
		return m.matches();
	}

	/**
	 * Find out if the given String defines a legal ID
	 * 
	 * @param s
	 *            Id to check
	 * @return The correct ID, or null if the data does not much a legal ID
	 */
	private static String parseId(String s) {
		String[] splited = s.split("=");
		if (splited.length < 2) {
			return null;
		}
		Pattern p = Pattern.compile("^\\d+$");
		Matcher m = p.matcher(splited[1]);
		if (m.matches()) {
			return splited[1];
		}
		return null;
	}
}
