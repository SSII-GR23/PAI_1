package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import utils.Generators;
import utils.Parser;

public class ClientSocket {
    private final String IP;
    private final Integer PORT;

    private final PrintWriter output;

    private final Socket socket;

    public static void main(String[] args) throws UnknownHostException, IOException {
        new ClientSocket("localhost", 4011).showMainWindow();
    }

    public ClientSocket(String ip, Integer port) throws UnknownHostException, IOException {
        this.IP = ip;
        this.PORT = port;
        this.socket = new Socket(IP, PORT);
        this.output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    // ======== Interfaz principal ========
    public void showMainWindow() {
        JFrame frame = new JFrame("Client Socket - Banking App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 150);
        frame.setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        JButton loginButton = new JButton("Log In");
        JButton signupButton = new JButton("Sign In");
        JButton exitButton = new JButton("Exit");

        // Acción: Log In
        loginButton.addActionListener(e -> {
            login();
        });

        // Acción: Sign In
        signupButton.addActionListener(e -> {
            sign();
        });
        
        exitButton.addActionListener(e -> {
            try {
                this.output.println("EXIT"); // notificar al servidor
                this.output.flush();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            frame.dispose(); // cierra solo la ventana
            // o usar System.exit(0) si quieres terminar la aplicación completa
        });


        panel.add(loginButton);
        panel.add(signupButton);
        panel.add(exitButton);

        frame.add(panel);
        frame.setVisible(true);
    }
    
    
	public void login() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String userName = JOptionPane.showInputDialog("Usuario:");
			this.output.println("SALT," + userName);
			
			String response = input.readLine();
			
			if(response.isEmpty()) {
				System.err.println("Usuario incorrecto.");
				JOptionPane.showMessageDialog(null, "Servidor: " + "Usuario incorrecto");
				return;
			}
			
			byte[] salt = Parser.hexToBytes(response);
			
			String password = JOptionPane.showInputDialog("Contraseña:");
			String hashPassword = Generators.hashWithSalt(password, salt);
			String mac = Generators.mac(hashPassword, Main.SECRET_KEY);
			
			this.output.println("LOGIN," + userName + "," + hashPassword + "," + mac);

			response = input.readLine();
			JOptionPane.showMessageDialog(null, "Servidor: " + response);

			if (!"OK".equals(response)) {
				JOptionPane.showMessageDialog(null, "Login fallido, cerrando cliente");
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sign() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String userName = JOptionPane.showInputDialog("Usuario:");
			String password = JOptionPane.showInputDialog("Contraseña:");
			
			byte[] salt = Generators.salt(Main.SALT_BYTES);
			String hashPassword = Generators.hashWithSalt(password, salt);
			String mac = Generators.mac(hashPassword, Main.SECRET_KEY);
			
			this.output.println("SIGN," + userName + "," + hashPassword + "," + mac + "," + utils.Parser.bytesToHex(salt));

			String response = input.readLine();
			JOptionPane.showMessageDialog(null, "Servidor: " + response);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
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
