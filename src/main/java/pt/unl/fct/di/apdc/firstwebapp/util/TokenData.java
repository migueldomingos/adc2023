package pt.unl.fct.di.apdc.firstwebapp.util;

public class TokenData {

    public String username, tokenID;
    public long creationData, expirationData;

    public TokenData(String username, String tokenID, long creationData, long expirationData) {
        this.username = username;
        this.tokenID = tokenID;
        this.creationData = creationData;
        this.expirationData = expirationData;
    }
}
