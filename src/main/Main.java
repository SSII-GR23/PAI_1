package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import utils.Generators;

public class Main {

	public static final byte[] SECRET_KEY = "ClaveSuperSecreta123!".getBytes(StandardCharsets.UTF_8);
	public static final int SALT_BYTES = 16; // 128 bits
	public static final int NONCE_BYTES = 16; // 128 bits
	
	public static final String IP = "172.20.10.2"; // 128 bits
	public static final int PORT = 4012; // 128 bits

	private static final Set<String> noncesEmpleados = new HashSet<>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("=== Inicializando servidor (demo) ===");
		BaseDatos.initDemo();
		
		ServerSocket serverSocket = null;
		
	    try {
	        serverSocket = new ServerSocket(PORT);
	        System.out.println("Servidor escuchando en puerto 4011...");
	        
	        while (true) {
	            Socket socket = serverSocket.accept();
	            new Thread(() -> manejarCliente(socket)).start();
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    } finally {
	        if (serverSocket != null) {
	            try {
	                serverSocket.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	    }
//
//		// Le preguntamos al usuario que quiere hacer
//		Scanner opciones = new Scanner(System.in);
//		System.out.println("¿Que quiere hacer?:\n" + "1-Iniciar sesion\n" + "2-Registrar usuario");
//		int opcion = opciones.nextInt();
//		String user, passw;
//		boolean intentos = false;
//		boolean sesionIniciada = false;
//		int i = 0;
//		while (!intentos) {
//			if (opcion == 1) {
//				System.out.println("Introduce tu usuario:");
//				user = opciones.next();
//				System.out.println("Introduce tu contraa:");
//				passw = opciones.next();
//				Boolean login = BaseDatos.userLogin(user, passw);
//
//				if (login) {
//					System.out.println("Has iniciado sesion con exito");
//					intentos = true;
//				} else {
//					System.out.println("Usuario o contrasea incorrectos");
//					i++;
//				}
//			} else if (opcion == 2) {
//				System.out.println("Introduce el usuario que quieres registrar:");
//				user = opciones.next();
//				System.out.println("Introduce la contraseña que quieres registrar:");
//				passw = opciones.next();
//				boolean registro = BaseDatos.userSign(user, passw);
//				if (registro) {
//					System.out.println("Usuario registrado con exito");
//					intentos = true;
//				} else {
//					System.out.println("El usuario ya existe");
//				}
//			}
//
//			if (i == 3) {
//				System.out.println("Demasiados intentos fallidos");
//				intentos = true;
//
//			}
//		}
//		// 2) Simular login y transferencias
//		boolean acceso = BaseDatos.userLogin("usuarioPrueba", "password123");
//		System.out.println("¿Acceso concedido? " + acceso);
//
//		// ---------------- PRUEBA TRANSFERENCIA SEGURA VALIDA ----------------
//		String transferenciaBase = "ES8384:ES3476:1000";
//		String mensajeSeguro = utils.Generators.transferenciaSegura(transferenciaBase, NONCE_BYTES, SECRET_KEY);
//		System.out.println("\nMensaje seguro generado: " + mensajeSeguro);
//
//		String[] datosTransferenciaSegura = mensajeSeguro.split(":");
//
//		System.out.println("\n--- PRUEBA 1: Transferencia SEGURA y VÁLIDA ---");
//		procesarComando(datosTransferenciaSegura, acceso, "usuarioPrueba");
//
//		// ---------------- PRUEBA REPLAY ----------------
//		System.out.println("\n--- PRUEBA 2: Ataque de Repetición (Replay Attack) ---");
//		procesarComando(datosTransferenciaSegura, acceso, "usuarioPrueba");
//
//		// ---------------- PRUEBA ALTERACIÓN ----------------
//		System.out.println("\n--- PRUEBA 3: Ataque de Alteración de Mensaje (MAC Inválido) ---");
//		String nonce = datosTransferenciaSegura[3];
//		String mac = datosTransferenciaSegura[4];
//		String[] datosAlterados = { "ES8384", "ES3476", "99999", nonce, mac };
//		procesarComando(datosAlterados, acceso, "usuarioPrueba");
//
//		System.out.println("\n=== Demo finalizado ===");

	}
	
	private static void manejarCliente(Socket socket) {
	    try (
	        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        PrintWriter output = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
	    ) {
	        // Leer petición del cliente
	        String request = input.readLine(); // puede ser "LOGIN:user:pass" o "TRANSFER:origen:destino:..."
	        
	        String[] partes = request.split(":");
	        String response = "";

	        if (partes[0].equals("LOGIN")) {
	            boolean ok = BaseDatos.userLogin(partes[1], partes[2]);
	            response = ok ? "OK" : "ERROR";
	        } else if (partes[0].equals("TRANSFER")) {
	            // ejemplo: TRANSFER:ES8384:ES3476:1000:nonce:mac
	            String[] data = Arrays.copyOfRange(partes, 1, partes.length);
	            procesarComando(data, true, "cliente"); // logged = true para demo
	            response = "Transferencia procesada";
	        }

	        output.println(response);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	
	// =================== PROCESADOR DE COMANDOS =====================

	/**
	 * Procesa un comando separado en tokens (dataSplit). Protocolos: - [username,
	 * password] => login (len==2) - [origen, destino, cantidad] => transferencia
	 * simple (len==3) => requiere logged boolean externo - [origen, destino,
	 * cantidad, nonce, mac] => transferencia segura (len==5)
	 *
	 * Nota: Para el caso de demo, se asume logged==true cuando proceda. En la vida
	 * real habría sesión.
	 */
	public static void procesarComando(String[] dataSplit, boolean logged, String userValido) {
		int len = dataSplit.length;

		if (len == 2) {
			System.out.println("Se ha recibido un intento de login");
			String user = dataSplit[0];
			String passw = dataSplit[1];
			BaseDatos.userLogin(user, passw);

		} else if (len == 3) {
			System.out.println("Se ha recibido un intento de transferencia (Formato Antiguo)");
			String origen = dataSplit[0];
			String destino = dataSplit[1];
			String cantidad = dataSplit[2];
			if (logged) {
				System.out.println("El usuario está logueado, puede realizar esta operación");
				// No hay nonce/mac en este formato: registramos directamente
				BaseDatos.transaccion(origen, destino, cantidad, cantidad, userValido);
				System.out.println("Transferencia realizada (sin integridad MAC).");
			} else {
				System.out.println("El usuario no está logueado. No se permite transferencia.");
			}

		} else if (len == 5) {
			System.out.println("Se ha recibido un intento de transferencia SEGURA");
			String origen = dataSplit[0];
			String destino = dataSplit[1];
			String cantidad = dataSplit[2];
			String nonce = dataSplit[3];
			String mac = dataSplit[4];

			System.out.printf("Datos: Origen=%s, Destino=%s, Cantidad=%s, Nonce=%s, MAC=%s%n", origen, destino,
					cantidad, nonce, mac);

			manejoTransferencia(origen, destino, cantidad, nonce, mac, logged);

		} else {
			System.out.println("Error: formato de comando incorrecto.");
		}
	}

	/**
	 * Manejo de transferencia (completa) que exige estar logueado y verificar
	 * nonce+mac.
	 */
	public static void manejoTransferencia(String origen, String destino, String cantidad, String nonce, String mac,
			boolean logged) {
		System.out.println("\n*** INICIANDO VERIFICACIÓN DE TRANSFERENCIA ***");
		if (!logged) {
			System.out.println("El usuario no ha conseguido loguearse, no puede realizar esta operación");
			return;
		}

		// 1. comprobar si nonce ya usado (replay)
		if (noncesEmpleados.contains(nonce)) {
			System.err.println("ERROR: Nonce ya utilizado. Posible ataque de repetición.");
			return;
		}

		// 2. verificar mac
		String mensajeBase = String.join(":", origen, destino, cantidad, nonce);
		if (!BaseDatos.mac(mensajeBase, mac)) {
			System.err.println("ERROR: MAC inválido. El mensaje ha sido alterado o clave incorrecta.");
			System.out.println("MAC recibido: " + mac);
			String esperado = utils.Generators.mac(mensajeBase, SECRET_KEY);
			System.out.println("MAC esperado: " + esperado);
			return;
		}

		// 3. éxito: registrar nonce y transacción
		noncesEmpleados.add(nonce);
		System.out.println("ÉXITO: Transferencia verificada y MAC válido.");
		System.out.println("Procesando transferencia de " + cantidad + " de " + origen + " a " + destino);
		BaseDatos.transaccion(origen, destino, cantidad, nonce, mac);
	}
}
