package pt.unl.fct.di.apdc.firstwebapp.util;

public class UserClass {

    public String username, phoneNum, mobileNum, password, email, name, perfil, ocupation, workspace, addr, postalCode, NIF, role;
    public long state;

    public UserClass() {}

    public UserClass(String username, String email, String name) {
        this.username = username;
        this.email = email;
        this.name = name;
    }

    public UserClass(String username, String password, String email, String name, String phoneNum, String perfil,
                     String mobileNum, long state, String ocupation, String workSpace, String addr, String postalCode,
                     String NIF, String role) {
        this.username = username;
        this.password = password;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getOcupation() {
        return ocupation;
    }

    public void setOcupation(String ocupation) {
        this.ocupation = ocupation;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getNIF() {
        return NIF;
    }

    public void setNIF(String NIF) {
        this.NIF = NIF;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getPwd() {
        return password;
    }

    public void setPwd(String pwd) {
        this.password = pwd;
    }
}
