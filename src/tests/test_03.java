package tests;

import java.nio.charset.StandardCharsets;

import main.BaseDatos;
import main.Main;

public class test_03 {
	public static final int SALT_BYTES = 16; // 128 bits
	public static final int NONCE_BYTES = 16; // 128 bits
	public static final byte[] SECRET_KEY = "ClaveSuperSecreta123!".getBytes(StandardCharsets.UTF_8);
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		boolean acceso = BaseDatos.userLogin("usuarioPrueba", "password123");
		System.out.println("¿Acceso concedido? " + acceso);

		String transferenciaBase = "ES8384:ES3476:1000";
		String mensajeSeguro = utils.Generators.transferenciaSegura(transferenciaBase, NONCE_BYTES, SECRET_KEY);
		System.out.println("\nMensaje seguro generado: " + mensajeSeguro);

		String[] datosTransferenciaSegura = mensajeSeguro.split(":");
		// ---------------- PRUEBA REPLAY ----------------
		System.out.println("\n--- PRUEBA 2: Ataque de Repetición (Replay Attack) ---");
		Main.procesarComando(datosTransferenciaSegura, acceso, "usuarioPrueba");
	}

}
