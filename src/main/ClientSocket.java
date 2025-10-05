package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JOptionPane;

public class ClientSocket {
	private final String IP;
	private final Integer PORT;
	
	public static void main(String[] args) {
		new ClientSocket("localhost", 4011).init();;
	}
	
	public ClientSocket(String ip, Integer port) {
		this.IP = ip;
		this.PORT = port;
	}
	
	public void init() {
		try (Socket socket = new Socket(IP, PORT);
				PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

			// Pedir login
			String userName = JOptionPane.showInputDialog("Usuario:");
			String password = JOptionPane.showInputDialog("Contraseña:");
			output.println("LOGIN:" + userName + ":" + password);

			String response = input.readLine();
			JOptionPane.showMessageDialog(null, "Servidor: " + response);

			if (!"OK".equals(response)) {
				JOptionPane.showMessageDialog(null, "Login fallido, cerrando cliente");
				return;
			}

			// Si login OK → menú de acciones
			while (true) {
				String opcion = JOptionPane.showInputDialog("Elige una opción:\n1. Hacer transferencia\n2. Salir");

				if ("1".equals(opcion)) {
					String origen = JOptionPane.showInputDialog("Cuenta origen:");
					String destino = JOptionPane.showInputDialog("Cuenta destino:");
					String cantidad = JOptionPane.showInputDialog("Cantidad:");
					// Aquí podrías generar nonce + mac usando utils.Generators
					String mensaje = "TRANSFER:" + origen + ":" + destino + ":" + cantidad + ":nonce:mac";
					output.println(mensaje);
					String respTransfer = input.readLine();
					JOptionPane.showMessageDialog(null, "Servidor: " + respTransfer);
				} else {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stop() {
		
	}

	/**
	 * @param args
	 * @throws IOException
	 */
//	public static void main(String[] args) throws IOException {
//		try {
//
//			// create Socket from factory
//			Socket socket = new Socket("172.20.10.2", 4011);
//
//			// create PrintWriter for sending login to server
//			PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
//			// prompt user for user name
//			String userName = JOptionPane.showInputDialog(null, "Enter User Name:");
//
//			// send user name to server
//			output.println(userName);
//
//			// prompt user for password
//			String password = JOptionPane.showInputDialog(null, "Enter Password:");
//
//			// send password to server
//			output.println(password);
//
//			output.flush();
//
//			// create BufferedReader for reading server response
//			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//			// read response from server
//			String response = input.readLine();
//
//			// display response to user
//			JOptionPane.showMessageDialog(null, response);
//
//			// clean up streams and Socket
//			output.close();
//			input.close();
//			socket.close();
//
//		} // end try
//
//		// handle exception communicating with server
//		catch (IOException ioException) {
//			ioException.printStackTrace();
//		}
//
//		// exit application
//		finally {
//			System.exit(0);
//		}
//
//	}
}
