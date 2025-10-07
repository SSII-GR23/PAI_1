package trans;

import java.util.Arrays;

import main.Main;
import sun.nio.cs.Surrogate.Generator;
import utils.Generators;
import utils.Parser;

public class Transaccion {
	String message;
	byte[] nonse;
	String mac;
	
	public Transaccion(String message) {
		this.message = Generators.hashWithSalt(message, Generators.salt(Main.SALT_BYTES)) ;
		this.nonse = Generators.nonce(Main.NONCE_BYTES);
		
		String macMessage = message + Parser.bytesToHex(nonse);
		
		this.mac = Generators.mac(macMessage, Main.SECRET_KEY);
	}

	@Override
	public String toString() {
		return "Transaccion," + message + "," + Arrays.toString(nonse) + "," + mac;
	}
	
	

}
