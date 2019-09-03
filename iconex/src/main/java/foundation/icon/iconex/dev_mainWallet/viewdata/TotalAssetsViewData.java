package foundation.icon.iconex.dev_mainWallet.viewdata;

import java.math.BigInteger;

public class TotalAssetsViewData {
    private BigInteger totalAsset;
    private float votedPower;

    public BigInteger getTotalAsset() {
        return totalAsset;
    }

    public TotalAssetsViewData setTotalAsset(BigInteger totalAsset) {
        this.totalAsset = totalAsset;
        return this;
    }

    public float getVotedPower() {
        return votedPower;
    }

    public TotalAssetsViewData setVotedPower(float votedPower) {
        this.votedPower = votedPower;
        return this;
    }
}
