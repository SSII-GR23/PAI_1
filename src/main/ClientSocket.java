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

import auth.Auth;
import auth.Login;
import auth.Signin;
import trans.Transaccion;
import utils.Generators;

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
			
			String userName = JOptionPane.showInputDialog("Usuario:").strip();
			String password = JOptionPane.showInputDialog("Contraseña:").strip();
			
			Auth message = new Login(userName.trim(), password.trim());
			
			//	Mensaje al servidor
			this.output.println(message);

			//	Respuesta del servidor
			String response = input.readLine();
			JOptionPane.showMessageDialog(null, "Servidor: " + response);

			if (!"OK".equals(response)) {
				JOptionPane.showMessageDialog(null, "Login fallido, cerrando cliente");
				return;
			}
			
			showTransitionsWindow();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sign() {
		try {
			BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			String userName = JOptionPane.showInputDialog("Usuario:");
			String password = JOptionPane.showInputDialog("Contraseña:");
			Auth message = new Signin(userName.trim(), password.trim());
			
			System.out.println("Mensaje:" + message.toString());
			
			//	Mensaje al servidor
			this.output.println(message);

			String response = input.readLine();
			JOptionPane.showMessageDialog(null, "Servidor: " + response);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showTransitionsWindow() {
	    JFrame frame = new JFrame("Transferencia Bancaria");
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    frame.setSize(400, 250);
	    frame.setLocationRelativeTo(null);
	    frame.setLayout(new java.awt.GridBagLayout());

	    java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
	    gbc.insets = new java.awt.Insets(5, 5, 5, 5);
	    gbc.fill = java.awt.GridBagConstraints.HORIZONTAL;

	    // Campos de entrada
	    javax.swing.JLabel origenLabel = new javax.swing.JLabel("Número de cuenta origen:");
	    javax.swing.JTextField origenField = new javax.swing.JTextField(20);

	    javax.swing.JLabel destinoLabel = new javax.swing.JLabel("Número de cuenta destino:");
	    javax.swing.JTextField destinoField = new javax.swing.JTextField(20);

	    javax.swing.JLabel cantidadLabel = new javax.swing.JLabel("Cantidad a transferir:");
	    javax.swing.JTextField cantidadField = new javax.swing.JTextField(10);

	    // Botones
	    JButton cancelarButton = new JButton("Cerrar Sesión");
	    JButton aceptarButton = new JButton("Aceptar");

	    // Posicionamiento
	    gbc.gridx = 0; gbc.gridy = 0;
	    frame.add(origenLabel, gbc);
	    gbc.gridx = 1; gbc.gridy = 0;
	    frame.add(origenField, gbc);

	    gbc.gridx = 0; gbc.gridy = 1;
	    frame.add(destinoLabel, gbc);
	    gbc.gridx = 1; gbc.gridy = 1;
	    frame.add(destinoField, gbc);

	    gbc.gridx = 0; gbc.gridy = 2;
	    frame.add(cantidadLabel, gbc);
	    gbc.gridx = 1; gbc.gridy = 2;
	    frame.add(cantidadField, gbc);

	    // Botones en la parte inferior
	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(cancelarButton);
	    buttonPanel.add(aceptarButton);

	    gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
	    frame.add(buttonPanel, gbc);

	    // === Eventos ===

	    // Cancelar: cerrar ventana
	    cancelarButton.addActionListener(e -> frame.dispose());

	    // Aceptar: procesar datos
	    aceptarButton.addActionListener(e -> {
	        String origen = origenField.getText().trim();
	        String destino = destinoField.getText().trim();
	        String cantidad = cantidadField.getText().trim();

	        if (origen.isEmpty() || destino.isEmpty() || cantidad.isEmpty()) {
	            JOptionPane.showMessageDialog(frame, "Por favor, rellena todos los campos.", "Error", JOptionPane.ERROR_MESSAGE);
	            return;
	        }
	        
	        Transaccion trans = new Transaccion(origen + destino + cantidad);
	        
	        sendTransaction(trans);

	        frame.dispose();
	    });

	    frame.setVisible(true);
	}
	
	void sendTransaction(Transaccion trans){
			try {
				BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				//	Mensaje al servidor
				this.output.println(trans);

				//	Respuesta del servidor
				String response = input.readLine();
				JOptionPane.showMessageDialog(null, "Servidor: " + response);

				if (!"OK".equals(response)) {
					JOptionPane.showMessageDialog(null, "Transacción finalizada");
					showTransitionsWindow();
					return;
				}
				
				showTransitionsWindow();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}