package foundation.icon.iconex.token;

import java.io.Serializable;

/**
 * Created by js on 2018. 4. 7..
 */

public class Token implements Serializable{

    protected String address;
    protected String contractAddress;
    protected String defaultName;
    protected String userName;
    protected String defaultSymbol;
    protected String userSymbol;
    protected int defaultDec;
    protected int userDec;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDefaultSymbol() {
        return defaultSymbol;
    }

    public void setDefaultSymbol(String defaultSymbol) {
        this.defaultSymbol = defaultSymbol;
    }

    public String getUserSymbol() {
        return userSymbol;
    }

    public void setUserSymbol(String userSymbol) {
        this.userSymbol = userSymbol;
    }

    public int getDefaultDec() {
        return defaultDec;
    }

    public void setDefaultDec(int defaultDec) {
        this.defaultDec = defaultDec;
    }

    public int getUserDec() {
        return userDec;
    }

    public void setUserDec(int userDec) {
        this.userDec = userDec;
    }
}
