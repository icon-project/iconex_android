package foundation.icon.iconex.view.ui.mainWallet.viewdata;

import java.math.BigDecimal;

import foundation.icon.MyConstants;
import foundation.icon.iconex.util.DecimalFomatter;

public class TotalAssetsViewData {

    public boolean loadingTotalAssets = true;
    public boolean loadingVotedpower = true;

    private String txtTotalAsset = MyConstants.NO_BALANCE;
    private String txtVotedPower = MyConstants.NO_BALANCE;
    private String txtExchangeUnit = "USD";

    public TotalAssetsViewData setTotalAsset(BigDecimal totalAsset, String unit) {
        txtExchangeUnit = unit;
        int deci = "USD".equals(unit) ? 2 : 4;
        String strExchanged = totalAsset != null ? DecimalFomatter.format(totalAsset,deci) : MyConstants.NO_BALANCE;
        txtTotalAsset = strExchanged;
        return this;
    }

    public TotalAssetsViewData setVotedPower(BigDecimal votedPower) {
        txtVotedPower = votedPower != null ? votedPower.toString() : MyConstants.NO_BALANCE;
        return this;
    }

    public String getTxtTotalAsset() {
        return txtTotalAsset;
    }

    public String getTxtVotedPower() {
        return txtVotedPower;
    }


    public String getTxtExchangeUnit() {
        return txtExchangeUnit;
    }

    public TotalAssetsViewData setTxtExchangeUnit(String txtExchangeUnit) {
        this.txtExchangeUnit = txtExchangeUnit;
        return this;
    }
}
