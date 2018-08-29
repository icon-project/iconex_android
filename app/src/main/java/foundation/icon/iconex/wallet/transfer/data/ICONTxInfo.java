package foundation.icon.iconex.wallet.transfer.data;

public class ICONTxInfo extends TxInfo {

    private final String stepLimit;

    public ICONTxInfo(String to, String send, String fee, String stepLimit) {
        super(to, send, fee);

        this.stepLimit = "0x" + stepLimit;
    }

    public String getStepLimit() {
        return stepLimit;
    }
}
