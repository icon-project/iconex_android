package foundation.icon.iconex.dev_mainWallet.viewdata;

import java.math.BigInteger;

public class TotalAssetsViewData {
    private String totalAsset;
    private String votedPower;
    private String exchangeUnit;

    public String getTotalAsset() {
        return totalAsset;
    }

    public TotalAssetsViewData setTotalAsset(String totalAsset) {
        this.totalAsset = totalAsset;
        return this;
    }

    public String getVotedPower() {
        return votedPower;
    }

    public TotalAssetsViewData setVotedPower(String votedPower) {
        this.votedPower = votedPower;
        return this;
    }

    public String getExchangeUnit() {
        return exchangeUnit;
    }

    public TotalAssetsViewData setExchangeUnit(String exchangeUnit) {
        this.exchangeUnit = exchangeUnit;
        return this;
    }
}
