package trans;

import main.Main;
import utils.Generators;
import utils.Parser;

public class Transaccion {
	public String message;
	public String macMessage;
	public String nonse;
	public String mac;
	
	public Transaccion(String message) {
		this.message = Generators.hashWithSalt(message, Generators.salt(Main.SALT_BYTES)) ;
		this.nonse = Parser.bytesToHex(Generators.nonce(Main.NONCE_BYTES));
		
		this.macMessage = message + nonse;
		this.mac = Generators.mac(macMessage, Main.SECRET_KEY);
	}
	
	public Transaccion(String[] line) {
		this.message = line[1];
		this.nonse =line[2];
		this.macMessage = line[3];
		this.mac = line[4];
	}
	
	public static Transaccion of(String[] line) {
		return new Transaccion(line);
	}

	@Override
	public String toString() {
		return "Transaccion," + message + "," + nonse + "," + macMessage + "," + mac;
	}
	
	

}
