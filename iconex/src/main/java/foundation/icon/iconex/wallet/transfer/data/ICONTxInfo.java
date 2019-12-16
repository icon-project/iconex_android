package foundation.icon.iconex.wallet.transfer.data;

public class ICONTxInfo extends TxInfo {

    private final String stepLimit;
    private final String symbol;

    private String limitPrice;

    public ICONTxInfo(String to, String send, String fee, String stepLimit, String symbol) {
        super(to, send, fee);
        this.stepLimit = "0x" + stepLimit;
        this.symbol = symbol;
    }

    public String getStepLimit() {
        return stepLimit;
    }

    public String getLimitPrice() {
        return limitPrice;
    }

    public void setLimitPrice(String limitPrice) {
        this.limitPrice = limitPrice;
    }

    public String getSymbol() {
        return symbol;
    }
}
