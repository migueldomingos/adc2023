package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePWDData {

    public String username, currentPwd, newPwd, confNewPwd;

    public ChangePWDData() {}

    public ChangePWDData(String username, String currentPwd, String newPwd, String confNewPwd) {
        this.username = username;
        this.currentPwd = currentPwd;
        this.newPwd = newPwd;
        this.confNewPwd = confNewPwd;
    }
}
