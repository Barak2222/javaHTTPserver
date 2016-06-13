package studentsHttpServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * This class initialises all the logic needed for the server to work
 * 
 * @author Barak
 *
 */
public class Server {
	static DataHandler data;
	public static boolean running = true;
	private static ServerSocket httpServer = null;
	
	public static void main(String[] args) {
		try {
			httpServer = new ServerSocket(0);
			System.out.println("Server is listenning on port " + httpServer.getLocalPort());
			data = new DataHandler();
			while (running) {
				Socket connection = httpServer.accept();
				ConnectedClient client = new ConnectedClient(connection, data);
				new Thread(client).start();
			}
		} catch (SocketException e) {
			// Server was closed
		} catch(IOException e){
			// do nothing
		}
		try{
			if(httpServer != null){
				httpServer.close();
			}
		} catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("Server closed");
	}
	
	/**
	 * function that upon called, interrupts the server.accept()
	 */
	public static void closeServer(){
		try{
			httpServer.close();
		} catch(IOException e){
			e.printStackTrace();
		}
	}
}
