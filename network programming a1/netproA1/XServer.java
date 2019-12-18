package netproA1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XServer {

	// initialization variables
	static final int MAX_THREADS = 3;
	static public int clientId = 1;
	static public int numClients = 0;
	static public String repoPath;
	
	public static void main(String[] args) throws IOException {
		
		// create a max thread pool size of MAX_THREADS
		ExecutorService threadPool = Executors.newFixedThreadPool(MAX_THREADS);
		
		// creates a server socket with port 6969
		ServerSocket serverSocket = new ServerSocket(6969);
		System.out.println("Opening server on port 6969");
		
		repoPath = "./src/netproA1/interesting_files";
		
		// while the server is running, accept connection requests
		while (true) {
			Socket clientSocket = serverSocket.accept();
			String name = "Client_" + clientId;
			clientId++;
			
			PrintWriter out = null;
			// establish an input and output stream to the client socket
			try {
				out = new PrintWriter(clientSocket.getOutputStream(), true);
			} catch (IOException e) {
				System.out.println("IO Exception");
			}
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				System.out.println("IO Exception");
			}
			
			// if there are already 3 clients, close the connection
			if (XServer.numClients > 2) {
				System.out.println("Server capacity reached. Connection for " + name + " rejected.");
				out.println("Server capacity reached");
				out.close();
				try {
					in.close();
				} catch (IOException e) {
					System.out.println("IO Exception");
				}
				try {
					clientSocket.close();
				} catch (IOException e) {
					System.out.println("IO Exception");
				}
			} else { // else run the thread
				// execute the newly created WorkerThread
				// if there are already MAX_THREADS WorkerThreads in the thread pool,
				// it is placed in a queue until a thread in the pool becomes available
				threadPool.execute(new WorkerThread(clientSocket, name, in, out));
			}
		}
		
	}
}
