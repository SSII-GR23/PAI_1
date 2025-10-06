package tests;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import main.BaseDatos;

public class test_01 {
	public static final int SALT_BYTES = 16; // 128 bits
	public static final int NONCE_BYTES = 16; // 128 bits
	public static final byte[] SECRET_KEY = "ClaveSuperSecreta123!".getBytes(StandardCharsets.UTF_8);

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//Test 1 uso normal del cliente
		Scanner opciones = new Scanner(System.in);
		System.out.println("¿Que quiere hacer?:\n" + "1-Iniciar sesion\n" + "2-Registrar usuario");
		int opcion = opciones.nextInt();
		String user, passw;
		String cuentaOrigen, cuentaDestino;
		Integer cantidad;
		boolean intentos = false;
		boolean sesionIniciada = false;
		boolean login = false;
		int i = 0;
		while (!intentos) {
			if (opcion == 1) {
				System.out.println("Introduce tu usuario:");
				user = opciones.next();
				System.out.println("Introduce tu contraa:");
				passw = opciones.next();
				login = BaseDatos.userLogin(user, passw);

				if (login) {
					System.out.println("Has iniciado sesion con exito");
					intentos = true;
				} else {
					System.out.println("Usuario o contrasea incorrectos");
					i++;
				}
			} else if (opcion == 2) {
				System.out.println("Introduce el usuario que quieres registrar:");
				user = opciones.next();
				System.out.println("Introduce la contraseña que quieres registrar:");
				passw = opciones.next();
				boolean registro = BaseDatos.userSign(user, passw);
				if (registro) {
					System.out.println("Usuario registrado con exito");
					intentos = true;
				} else {
					System.out.println("El usuario ya existe");
				}
			}

			if (i == 3) {
				System.out.println("Demasiados intentos fallidos");
				intentos = true;
			}
		}
		
		while(login) {
		/*Cuando el usuario este logeado podrá realizar una transferencia o cerrar sesion*/
			System.out.println("¿Que quiere hacer?:\n" + "1-Realizar transferencia\n" + "2-Cerrar sesion");
			int opcion1 = opciones.nextInt();

			if (opcion1 == 1) {
				
				System.out.println("Introduce la cuenta origen:");
				cuentaOrigen = opciones.next();
				System.out.println("Introduce la cuenta destino:");
				cuentaDestino = opciones.next();
				System.out.println("Introduce la cantidad que desea transferir:");
				cantidad = Integer.parseInt(opciones.next());
				String transferenciaBase = cuentaOrigen + ":" + cuentaDestino + ":" + cantidad;
				String mensajeSeguro = utils.Generators.transferenciaSegura(transferenciaBase, NONCE_BYTES, SECRET_KEY);
				System.out.println("\nMensaje seguro generado: " + mensajeSeguro);
			} else if (opcion1 == 2) {
				login = false;
				System.out.println("Sesión cerrada");
				break;
			}			
		}
	}
	}

