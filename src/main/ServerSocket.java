package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerSocket {
	java.net.ServerSocket serverSocket;
	
	public static void main(String[] args) throws IOException {
		new ServerSocket(4011).init();;
	}
	
	
	public ServerSocket(int port) throws IOException {
		this.serverSocket = new java.net.ServerSocket(port);
	}
	
	public void init() throws IOException {
		while (true) {

			// wait for client connection and check login information
			try {
				System.err.println("Waiting for connection...");

				Socket socket = serverSocket.accept();

				// open BufferedReader for reading data from client
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// open PrintWriter for writing data to client
				PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				String userName = input.readLine();
				String password = input.readLine();
				output.println("User, " + userName);
				output.println("Pass, " + password);

				output.close();
				input.close();
				socket.close();

			} // end try

			// handle exception communicating with client
			catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}
	
	public Socket accept() throws IOException {
		return this.serverSocket.accept();
	}

	public void close() throws IOException {
		this.serverSocket.close();
	}

}
