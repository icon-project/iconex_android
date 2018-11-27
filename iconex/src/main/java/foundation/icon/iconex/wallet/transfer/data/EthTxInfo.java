package foundation.icon.iconex.wallet.transfer.data;

/**
 * Created by js on 2018. 5. 14..
 */

public class EthTxInfo extends TxInfo {

    private String fromAddress;
    private String price;
    private String limit;
    private String data;

    public EthTxInfo(String to, String send, String fee) {
        super(to, send, fee);
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getLimit() {
        return limit;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
