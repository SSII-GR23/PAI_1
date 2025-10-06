package tests;

import java.nio.charset.StandardCharsets;

import main.BaseDatos;
import main.Main;

public class test_02 {
	public static final int SALT_BYTES = 16; // 128 bits
	public static final int NONCE_BYTES = 16; // 128 bits
	public static final byte[] SECRET_KEY = "ClaveSuperSecreta123!".getBytes(StandardCharsets.UTF_8);
	
	public static void main(String[] args) {
		
		
		
			// 2) Simular login y transferencias
			boolean acceso = BaseDatos.userLogin("usuarioPrueba", "password123");
			System.out.println("¿Acceso concedido? " + acceso);

			// ---------------- PRUEBA TRANSFERENCIA SEGURA VALIDA ----------------
			String transferenciaBase = "ES8384:ES3476:1000";
			String mensajeSeguro = utils.Generators.transferenciaSegura(transferenciaBase, NONCE_BYTES, SECRET_KEY);
			System.out.println("\nMensaje seguro generado: " + mensajeSeguro);

			String[] datosTransferenciaSegura = mensajeSeguro.split(":");

			System.out.println("\n--- PRUEBA 1: Transferencia SEGURA y VÁLIDA ---");
			Main.procesarComando(datosTransferenciaSegura, acceso, "usuarioPrueba");

		}

}
