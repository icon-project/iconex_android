package foundation.icon.iconex.wallet.transfer.data;

/**
 * Created by js on 2018. 5. 14..
 */

public class TxInfo {

    private String sendAmount;
    private String fee;
    private String toAddress;

    public TxInfo(String send, String fee, String to) {
        this.sendAmount = send;
        this.fee = fee;
        this.toAddress = to;
    }

    public String getSendAmount() {
        return sendAmount;
    }

    public void setSendAmount(String sendAmount) {
        this.sendAmount = sendAmount;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }
}
