package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import auth.Login;
import auth.Signin;
import trans.Transaccion;
import utils.Generators;
import utils.Parser;


public class ServerSocket {
    private final java.net.ServerSocket serverSocket;

    public static void main(String[] args) throws IOException {
        new ServerSocket(4011).init();
    }

    public ServerSocket(int port) throws IOException {
        this.serverSocket = new java.net.ServerSocket(port);
    }

    public void init() throws IOException {
    	BaseDatos.initDemo();
    	
    	
//    	Signin user1 = new Signin("test1", "test");
//    	Signin user2 = new Signin("test2", "test");
//    	Signin user3 = new Signin("test3", "test");
//    	
//    	setUserDataBase(user1);
//    	setUserDataBase(user2);
//    	setUserDataBase(user3);
    	
    	
        System.out.println("Servidor iniciado. Esperando conexiones...");

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado desde " + socket.getInetAddress());

                // Lanzamos un hilo por cliente, para no bloquear el servidor
                new Thread(() -> handleClient(socket)).start();

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleClient(Socket socket) {
        try (
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            String line;

            // Bucle persistente mientras el cliente siga conectado
            while ((line = input.readLine()) != null) {
                System.out.println("Recibido: " + line);

                String[] userData = line.split(",");
                String command = userData[0];
                String response;
                
                switch (command) {
                    case "LOGIN":
                    	Login login = Login.of(userData);
                    	
                    	//Si los mac no son iguales erro de mensaje modificado;
                    	if(!compareMac(login.message, login.mac)) {
                    		response = "ERROR: El mensaje ha sido modificado.";
                    		break;
                    	}
                    	
                        boolean ok = getUserDataBase(login);
                        response = ok ? "OK" : "ERROR: Usuario o contraseña incorrectos.";
                        break;

                    case "SIGNIN":
                    	Signin sign = Signin.of(userData);
                    	
                    	//Si los mac no son iguales erro de mensaje modificado;
                    	if(!compareMac(sign.message, sign.mac)) {
                    		response = "ERROR: El mensaje ha sido modificado.";
                    		break;
                    	}
                    	
                        response = setUserDataBase(sign);
                        break;

                    case "EXIT":
                        response = "Desconectado del servidor.";
                        output.println(response);
                        System.out.println("Cliente desconectado.");
                        socket.close();
                        return; // 🔚 Sale del hilo

                    case "Transaccion":
                    	Transaccion trans = Transaccion.of(userData);
                    	
                    	if(!compareMac(trans.macMessage, trans.mac)) {
                    		response = "ERROR: El mensaje ha sido modificado.";
                    		break;
                    	}
                    	
                        response = transaccion(trans);
                        break; // 🔚 Sale del hilo
                    default:
                        response = "ERROR: Comando no reconocido.";
                        break;
                }

                output.println(response);
            }

        } catch (IOException e) {
            System.out.println("Cliente desconectado abruptamente: " + e.getMessage());
        }
    }

    public Boolean getUserDataBase(Login login) {
        return BaseDatos.userLogin(login.user, login.hashpassword);
    }

    public String setUserDataBase(Signin sign) {
        Boolean exists = BaseDatos.userExist(sign.user);

        if (exists) {
            String out = String.format("Usuario '%s' ya existe. Prueba un nombre diferente.", sign.user);
            System.err.println(out);
            return out;
        }

        BaseDatos.userSign(sign.user, sign.hashpassword);
        String out = String.format("Usuario '%s' creado correctamente.", sign.user);
        System.out.println(out);
        return out;
    }
    
    public boolean compareMac(String message, String mac) {
    	return mac.equals(Generators.mac(message, Main.SECRET_KEY));
    }
    
    public String transaccion(Transaccion trans) {
    	String res = "ERROR: Mensaje duplicado";
    	
    	if(!BaseDatos.nonceExiste(trans.nonse)) {
    		res = "Transacción añadida a la base de datos";
    		
    		BaseDatos.transaccion(trans.message, trans.nonse, trans.mac);
    		BaseDatos.registrarNonce(trans.nonse);
    	}
    	
    	return res;
    }
}
