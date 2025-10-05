package tests;

public class test_01 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

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

}
