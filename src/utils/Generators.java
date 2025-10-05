package utils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Generators {

	/**
	 * Genera salt criptográficamente seguro.
	 * 
	 * @param length
	 * @return
	 */
	public static byte[] salt(int length) {
		SecureRandom rnd = new SecureRandom();
		byte[] salt = new byte[length];
		rnd.nextBytes(salt);
		return salt;
	}

	/**
	 * Hash simple SHA-256. Devuelve hex string. Para mayor seguridad usar
	 * PBKDF2/BCrypt/Argon2.
	 * 
	 * @param password
	 * @param salt
	 * @return
	 */
	public static String hashWithSalt(String password, byte[] salt) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			digest.update(salt); // incluir salt antes de la contraseña
			byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
			return utils.Parser.bytesToHex(hashed);

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Algoritmo de hash no disponible: " + e.getMessage(), e);
		}
	}

	/**
	 * Genera un Mac en función de los parámetros dados
	 * 
	 * @param message
	 * @param key
	 * @return
	 */
	public static String mac(String message, byte[] key) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec keySpec = new SecretKeySpec(key, "HmacSHA256");
			mac.init(keySpec);
			byte[] macBytes = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return utils.Parser.bytesToHex(macBytes);
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Genera un nonce
	 * 
	 * @param length
	 * @return
	 */
	public static byte[] nonce(int length) {
		SecureRandom rnd = new SecureRandom();
		byte[] n = new byte[length];
		rnd.nextBytes(n);
		return n;
	}

	/**
	 * Genera una nueva server Secret Key
	 * 
	 * @param lengthBytes
	 * @return
	 */
	public static byte[] serverSecretKey(int lengthBytes) {
		SecureRandom rnd = new SecureRandom();
		byte[] key = new byte[lengthBytes];
		rnd.nextBytes(key);
		return key;
	}

	/**
	 * Construye y devuelve un mensaje seguro:
	 * origen:destino:cantidad:nonce_hex:mac_hex El MAC se calcula sobre
	 * "origen:destino:cantidad:nonce_hex".
	 * 
	 * @param transferenciaBase
	 * @return
	 */
	public static String transferenciaSegura(String transferenciaBase, int nonce, byte[] key) {
		// transferenciaBase ejemplo: "ES8384:ES3476:1000"
		byte[] nonceBytes = utils.Generators.nonce(nonce);
		String nonceHex = utils.Parser.bytesToHex(nonceBytes);

		String mensajeParaMac = transferenciaBase + ":" + nonceHex;
		String mac = utils.Generators.mac(mensajeParaMac, key);
		if (mac == null)
			return null;

		String mensajeTotal = mensajeParaMac + ":" + mac;
		return mensajeTotal;
	}

}
