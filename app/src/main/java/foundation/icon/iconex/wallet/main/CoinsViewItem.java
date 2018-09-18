package foundation.icon.iconex.wallet.main;

import java.io.Serializable;
import java.util.List;

import foundation.icon.iconex.wallet.Wallet;

/**
 * Created by js on 2018. 7. 6..
 */

public class CoinsViewItem implements Serializable {
    private String type;
    private String name;
    private String symbol;
    private int dec;
    private String contractAddr;
    private List<Wallet> wallets;

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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDec() {
        return dec;
    }

    public void setDec(int dec) {
        this.dec = dec;
    }

    public String getContractAddr() {
        return contractAddr;
    }

    public void setContractAddr(String contractAddr) {
        this.contractAddr = contractAddr;
    }

    public List<Wallet> getWallets() {
        return wallets;
    }

    public void setWallets(List<Wallet> wallets) {
        this.wallets = wallets;
    }

    @Override
    public String toString() {
        return "Type=" + type + ", name=" + name + ", symbol=" + symbol;
    }
}
