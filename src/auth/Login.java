package auth;

public class Login extends Auth{

	public Login(String user, String password) {
		super(user, password,Command.LOGIN);
	}
	
	Login(String[] line) {
		super(Command.LOGIN,line);
	}
	
	
	public static Login of(String[] line) {
		return new Login(line);
	}
	
}
