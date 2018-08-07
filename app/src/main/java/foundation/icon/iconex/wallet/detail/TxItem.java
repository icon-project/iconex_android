package foundation.icon.iconex.wallet.detail;

/**
 * Created by js on 2018. 3. 13..
 */

public class TxItem {

    private String txHash;
    private String date;
    private String from;
    private String to;
    private String amount;
    private String fee;

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String toString() {
        return "txHash=" + txHash + "\n"
                + "date=" + date + "\n"
                + "from=" + from + "\n"
                + "to=" + to + "\n"
                + "amount" + amount + "\n"
                + "fee=" + fee;

    }
}
