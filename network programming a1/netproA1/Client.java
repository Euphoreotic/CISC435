package netproA1;

import java.io.*;
import java.net.*;

public class Client {

	public static void main(String[] args) throws UnknownHostException, IOException {
		System.out.println("Attempting to connect to server on port 6969");
		Socket socket = new Socket("127.0.0.1", 6969);
		
		// establish an input and output stream to the server socket
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		
		// wait for a response from the server
		String serverResponse = in.readLine();
		System.out.println(serverResponse);
		
		// if thread capacity is already reached, close the connection
		if (serverResponse.equals("Server capacity reached")){
			System.out.println("Closing connection.");
			out.close();
			in.close();
			socket.close();
		} else { // else server is ready to receive messages
			System.out.println("Client ready to write to server");
			System.out.println("Enter \"exit\" to close connections to the server.");
			System.out.println("Enter \"list\" to list all files in the repository.");
			
			// while there is a connection to the server
			while (true) {
				// get the users input on CLI and sends it to the server
				BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
				String message = userInput.readLine();
				out.println(message);
				out.flush();
				
				// receives a response from the server
				String data = in.readLine();
				System.out.println("Server: " + data);
				
				// if list command is entered, the client will read file names sent by the server until
				// the string "End of Repo." is received which indicates that there are no more files to list
				if (message.equals("list")) {
					while (true) {
						String fileName = in.readLine();
						System.out.println(fileName);
						if (fileName.equals("End of Repo.")) {
							break;
						}
					}
				} else if (message.equals("exit")) {// if exit command was entered, attempt to close the connection to the server
					break;
				}
			}
			
			// close the input stream, output stream, and connection to the server
			out.close();
			in.close();
			socket.close();
		}
	}
}
