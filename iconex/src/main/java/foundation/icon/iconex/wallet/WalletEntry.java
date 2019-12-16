package foundation.icon.iconex.wallet;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by js on 2018. 3. 9..
 */

public class WalletEntry implements Serializable, Parcelable {
    private int id = 0;
    private String type = "";
    private String name = "";
    private String address = "";
    private String symbol = "";
    private String balance = "";

    private String contractAddress = "";
    private String userName = "";
    private String userSymbol = "";
    private int defaultDec = 0;
    private int userDec = 0;

    private String createdAt = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String toString() {
        return "id=" + id + "\n"
                + "type=" + type + "\n"
                + "name=" + name + "\n"
                + "address=" + address + "\n"
                + "symbol=" + symbol + "\n"
                + "defaultDec=" + defaultDec + "\n"
                + "userDec=" + userDec + "\n";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(type);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(symbol);
        dest.writeString(balance);
        dest.writeString(contractAddress);
        dest.writeString(userName);
        dest.writeString(userSymbol);
        dest.writeInt(defaultDec);
        dest.writeInt(userDec);
        dest.writeString(createdAt);
    }

    public static final Parcelable.Creator<WalletEntry> CREATOR
            = new Parcelable.Creator<WalletEntry>() {
        public WalletEntry createFromParcel(Parcel in) {
            return new WalletEntry(in);
        }

        public WalletEntry[] newArray(int size) {
            return new WalletEntry[size];
        }
    };

    public WalletEntry() {
        super();
    }

    public WalletEntry(Parcel in) {
        id = in.readInt();
        type = in.readString();
        name = in.readString();
        address = in.readString();
        symbol = in.readString();
        balance = in.readString();
        contractAddress = in.readString();
        userName = in.readString();
        userSymbol = in.readString();
        defaultDec = in.readInt();
        userDec = in.readInt();
        createdAt = in.readString();
    }
}
