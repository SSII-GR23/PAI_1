package auth;

import main.Main;
import utils.Generators;
import utils.Parser;

public abstract class Auth {
	
	public static final byte[] SALT = Parser.hexToBytes("8E0182FDED366B81FF7E790056C778E2"); // 128 bits
	
	public final String message;
	public String mac;

	public final String user;
	public final String hashpassword;
	public final Command command;

	public Auth(String user, String password, Command command) {
		this.command = command;
		this.user = user.trim();
		this.hashpassword = Generators.hashWithSalt(password.trim(), SALT);

		this.message = String.format("%s,%s", user, this.hashpassword).trim();
		this.mac = Generators.mac(message, Main.SECRET_KEY);
	}
	
	public Auth(Command command, String[] message) {
		this.command = command;
		this.user = message[1].trim();
		this.hashpassword = message[2].trim();

		this.message = String.format("%s,%s", this.user, this.hashpassword).trim();
		this.mac = message[3].trim();
	}
	
	@Override
	public String toString() {
		return command + "," + message + "," + mac;
	}

}
