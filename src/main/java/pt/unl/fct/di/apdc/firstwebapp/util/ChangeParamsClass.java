package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeParamsClass {
    public String username, phoneNum, mobileNum, email, name, perfil, ocupation, workspace, addr, postalCode, NIF, role;
    public long state;

    public ChangeParamsClass() {}

    public ChangeParamsClass(String username, String email, String name, String phoneNum, String perfil,
                     String mobileNum, long state, String ocupation, String workSpace, String addr, String postalCode,
                     String NIF, String role) {
        this.username = username;
        this.email = email;
        this.name = name;
        this.phoneNum = phoneNum;
        this.mobileNum = mobileNum;
        this.state = state;
        this.perfil = perfil;
        this.ocupation = ocupation;
        this.workspace = workSpace;
        this.addr = addr;
        this.postalCode = postalCode;
        this.NIF = NIF;
        this.role = role;
    }
}