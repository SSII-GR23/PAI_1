package auth;

public class Signin extends Auth{

	public Signin(String user, String password) {
		super(user, password,Command.SIGNIN);
	}
	
	Signin(String[] line) {
		super(Command.SIGNIN, line);
	}
	
	
	public static Signin of(String[] line) {
		return new Signin(line);
	}
	
}
