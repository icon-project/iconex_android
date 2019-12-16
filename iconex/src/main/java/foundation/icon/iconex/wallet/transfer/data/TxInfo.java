package foundation.icon.iconex.wallet.transfer.data;

/**
 * Created by js on 2018. 5. 14..
 */

public class TxInfo {

    private String toAddress;
    private String sendAmount;
    private String fee;
    private String transFee;

    public TxInfo(String to, String send, String fee) {
        this.toAddress = to;
        this.sendAmount = send;
        this.fee = fee;
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

    public String getTransFee() {
        return transFee;
    }

    public void setTransFee(String transFee) {
        this.transFee = transFee;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }
}
