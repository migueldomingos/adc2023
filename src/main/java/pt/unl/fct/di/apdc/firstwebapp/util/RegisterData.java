package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {
	
	public String username,mobileNum, phoneNum ,password, confirmation, email, name, ocupation, workspace, addr, postalCode, NIF;

	
	public RegisterData() {}

	public RegisterData(String username, String password, String confirmation, String email, String name, String phoneNum,
						String mobileNum, String ocupation, String workSpace, String addr, String postalCode,
						String NIF) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.name = name;

		if (confirmation == "")
			this.confirmation = "Not Defined";
		else
			this.confirmation = confirmation;

		if (phoneNum == "")
			this.phoneNum = "Not Defined";
		else
			this.phoneNum = phoneNum;

		if (mobileNum == "")
			this.mobileNum = "Not Defined";
		else
			this.mobileNum = mobileNum;

		if (ocupation == "")
			this.ocupation = "Not Defined";
		else
			this.ocupation = ocupation;

		if (workSpace == "")
			this.workspace = "Not Defined";
		else
			this.workspace = workSpace;

		if (addr == "")
			this.addr = "Not Defined";
		else
			this.addr = addr;

		if (postalCode == "")
			this.postalCode = "Not Defined";
		else
			this.postalCode = postalCode;

		if (NIF == "")
			this.NIF = "Not Defined";
		else
			this.NIF = NIF;
	}

	public boolean validRegistration() {
		return !username.equals(null) && !password.equals(null) &&
				!confirmation.equals(null) && !email.equals(null) &&
				!name.equals(null);
	}

}
