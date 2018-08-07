package foundation.icon.iconex.wallet.transfer.data;

import org.web3j.crypto.Credentials;

/**
 * Created by js on 2018. 5. 14..
 */

public class ErcTxInfo extends EthTxInfo {

    private Credentials credentials;
    private String contract;
    private int decimals;
    private String symbol;

    public ErcTxInfo(String send, String fee, String to) {
        super(send, fee, to);
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
