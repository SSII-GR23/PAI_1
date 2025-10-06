package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

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
                	case "SALT":
                		byte[] salt = BaseDatos.getUserSalt(userData[1]);
                		System.err.println(String.format("Salt del usuario %s:\t %s",userData[1],Parser.bytesToHex(salt)));
                		response = salt == null ? "" : Parser.bytesToHex(salt);
                		break;
                    case "LOGIN":
                        boolean ok = getUserDataBase(userData);
                        response = ok ? "OK" : "ERROR: Usuario o contraseña incorrectos.";
                        break;

                    case "SIGN":
                        response = setUserDataBase(userData);
                        break;

                    case "EXIT":
                        response = "Desconectado del servidor.";
                        output.println(response);
                        System.out.println("Cliente desconectado.");
                        socket.close();
                        return; // 🔚 Sale del hilo

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

    public Boolean getUserDataBase(String... data) {
        return BaseDatos.userLogin(data[1], data[2]);
    }

    public String setUserDataBase(String... data) {
        Boolean exists = BaseDatos.userExist(data[1]);

        if (exists) {
            String out = String.format("Usuario '%s' ya existe. Prueba un nombre diferente.", data[1]);
            System.err.println(out);
            return out;
        }

        BaseDatos.userSign(data[1], data[2], Parser.hexToBytes(data[4]));
        String out = String.format("Usuario '%s' creado correctamente.", data[1]);
        System.out.println(out);
        return out;
    }
}
