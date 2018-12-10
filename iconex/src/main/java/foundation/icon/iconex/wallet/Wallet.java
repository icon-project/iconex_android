package foundation.icon.iconex.wallet;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by js on 2018. 3. 6..
 */

public class Wallet implements Serializable, Parcelable {

    private String coinType = "";
    private String alias = "";
    private String address = "";
    private String keyStore = "";
    private List<WalletEntry> walletEntries = null;
    private String createdAt = "";

    public String getCoinType() {
        return coinType;
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public List<WalletEntry> getWalletEntries() {
        return walletEntries;
    }

    public void setWalletEntries(List<WalletEntry> walletEntries) {
        this.walletEntries = walletEntries;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String toString() {
        return "coinType=" + coinType + " / "
                + "alias=" + alias + " / "
                + "address=" + address + " / "
                + "keyStore=" + keyStore
                + coinNTokeListToString();
    }

    private String coinNTokeListToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("+++ WalletEntry\n");
        String info = null;
        for (WalletEntry entry : walletEntries) {
            info = "Type=" + entry.getType() + "\n"
                    + "Name=" + entry.getName() + "\n"
                    + "Address=" + entry.getAddress() + "\n"
                    + "Symbol=" + entry.getSymbol();

            sb.append(info);
        }

        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(coinType);
        dest.writeString(alias);
        dest.writeString(address);
        dest.writeString(keyStore);
        dest.writeList(walletEntries);
        dest.writeString(createdAt);
    }

    public static final Parcelable.Creator<Wallet> CREATOR
            = new Parcelable.Creator<Wallet>() {
        public Wallet createFromParcel(Parcel in) {
            return new Wallet(in);
        }

        public Wallet[] newArray(int size) {
            return new Wallet[size];
        }
    };

    public Wallet() {
        super();
    }

    public Wallet(Parcel in) {
        coinType = in.readString();
        alias = in.readString();
        address = in.readString();
        keyStore = in.readString();
        walletEntries = new ArrayList<>();
        in.readList(walletEntries, WalletEntry.class.getClassLoader());
        createdAt = in.readString();
    }
}
