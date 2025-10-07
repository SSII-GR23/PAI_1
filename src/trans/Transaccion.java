package trans;

import main.Main;
import utils.Generators;
import utils.Parser;

public class Transaccion {
	String message;
	byte[] nonse;
	String mac;
	
	public Transaccion(String message) {
		this.message = message;
		this.nonse = Generators.nonce(Main.NONCE_BYTES);
		
		String macMessage = message + Parser.bytesToHex(nonse);
		
		this.mac = Generators.mac(macMessage, Main.SECRET_KEY);
	}

}
