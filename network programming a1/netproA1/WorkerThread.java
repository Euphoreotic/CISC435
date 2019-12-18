package netproA1;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WorkerThread extends Thread {

	public Socket clientSocket;
	public String name;
	public String acceptedDate;
	public String finishedDate;
	public PrintWriter out = null;
	public BufferedReader in = null;
	
	public WorkerThread(Socket clientSocket, String name, BufferedReader in, PrintWriter out) {
		this.clientSocket = clientSocket;
		this.name = name;
		this.in = in;
		this.out = out;
	}
	
	// override run() to execute our own logic
	@Override
	public void run() {
		System.out.println("Client connection accepted for: " + name);

		XServer.numClients++;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date accDate = new Date();
		acceptedDate = dateFormat.format(accDate);
		System.out.println("Connection for " + name + " accept at: " + acceptedDate);
		
		// notify the client that it is now running in the thread pool 
		out.println("Connected to server.");
		out.flush();
		
		// while there is a connection to the client
		while (true) {
			
			// receive the input steam from the client
			String data = null;
			try {
				data = in.readLine();
			} catch (IOException e) {
				System.out.println("IO Exception");
			}
			System.out.println(name + " sent: " + data);
			
			// if the exit command is given, attempt to close the connection
			if (data.equals("exit")) {
				Date finDate = new Date();
				finishedDate = dateFormat.format(finDate);
				System.out.println("Exit received at: " + finishedDate + ", terminating connection");
				out.println("Connection to server ended.");
				break;
				
			} else if (data.equals("list")) { // list command is sent by the client
				out.println("Here are the files:");
				out.flush();
				
				//initialize the repository using the supplied file path
				final File[] folder = new File(XServer.repoPath).listFiles();
				
				//sends each files name separately
				try {
					for (File fileEntry : folder) {
						out.println(fileEntry.getName());
						out.flush();
				    }
				} catch(NullPointerException e) {
					System.out.println("folder not found");
				}
				
				// let the client know that there are no more files
				out.println("End of Repo.");
				out.flush();
				
			} else {
				// else, return the same string with ACK attached
				out.println(data + " ACK");
				out.flush();
			}
			
		}

		// close the connection to the client
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
		XServer.numClients--;
	}
}
