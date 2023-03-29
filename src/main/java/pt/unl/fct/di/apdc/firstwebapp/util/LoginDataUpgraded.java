package pt.unl.fct.di.apdc.firstwebapp.util;

public class LoginDataUpgraded {
	
	public String username, password, confirmation, email, name;
	
	public LoginDataUpgraded () {}
	
	public LoginDataUpgraded(String username, String password, String confirmation, String email, String name) {
		this.username = username;
		this.password = password;
		this.confirmation = confirmation;
		this.email = email;
		this.name = name;
	}

	public boolean validRegistration() {
		return !username.equals(null) && !password.equals(null) &&
				!confirmation.equals(null) && !email.equals(null) &&
				!name.equals(null);
	}
	

}
