package foundation.icon.iconex.wallet.transfer.data;

public class ICONTxInfo extends TxInfo {

    private final String stepLimit;
    private final String symbol;

    public ICONTxInfo(String to, String send, String fee, String stepLimit, String symbol) {
        super(to, send, fee);

        this.stepLimit = "0x" + stepLimit;
        this.symbol = symbol;
    }

    public String getStepLimit() {
        return stepLimit;
    }

    public String getSymbol() {
        return symbol;
    }
}
