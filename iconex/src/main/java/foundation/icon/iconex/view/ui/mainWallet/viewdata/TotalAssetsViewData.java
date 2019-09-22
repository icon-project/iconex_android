package foundation.icon.iconex.view.ui.mainWallet.viewdata;

import java.math.BigDecimal;

public class TotalAssetsViewData {

    private BigDecimal totalAsset = null;
    private BigDecimal votedPower = null;

    private String txtTotalAsset = null;
    private String txtVotedPower = null;
    private String txtExchangeUnit = null;

    public BigDecimal getTotalAsset() {
        return totalAsset;
    }

    public TotalAssetsViewData setTotalAsset(BigDecimal totalAsset) {
        this.totalAsset = totalAsset;
        return this;
    }

    public BigDecimal getVotedPower() {
        return votedPower;
    }

    public TotalAssetsViewData setVotedPower(BigDecimal votedPower) {
        this.votedPower = votedPower;
        return this;
    }

    public String getTxtTotalAsset() {
        return txtTotalAsset;
    }

    public TotalAssetsViewData setTxtTotalAsset(String txtTotalAsset) {
        this.txtTotalAsset = txtTotalAsset;
        return this;
    }

    public String getTxtVotedPower() {
        return txtVotedPower;
    }

    public TotalAssetsViewData setTxtVotedPower(String txtVotedPower) {
        this.txtVotedPower = txtVotedPower;
        return this;
    }

    public String getTxtExchangeUnit() {
        return txtExchangeUnit;
    }

    public TotalAssetsViewData setTxtExchangeUnit(String txtExchangeUnit) {
        this.txtExchangeUnit = txtExchangeUnit;
        return this;
    }
}
