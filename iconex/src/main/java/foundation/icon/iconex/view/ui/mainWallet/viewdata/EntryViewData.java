package foundation.icon.iconex.view.ui.mainWallet.viewdata;

import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.DecimalFomatter;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class EntryViewData {
    private static String TAG = EntryViewData.class.getSimpleName();

    private Wallet wallet;
    private WalletEntry entry;
    private BigDecimal exchanged;
    public BigInteger unstake;

    public int pos0;
    public int pos1;

    private String symbol;
    private String name;
    private String txtAmount;
    private String txtExchanged;
    public boolean amountLoading;
    public boolean exchageLoading;

    // only for icx coin
    private String txtStacked;
    private String txtIScore;
    public boolean prepsLoading;
    public boolean iscoreLoading;

    // for token
    private int bgSymbolColor;
    private int drawableSymbolresId = -1;

    public EntryViewData(Wallet wallet, WalletEntry entry) {
        this.wallet = wallet;
        this.entry = entry;

        symbol = entry.getSymbol();
        name = entry.getName();
        txtAmount = MyConstants.NO_BALANCE;
        txtExchanged = MyConstants.NO_BALANCE;

        txtStacked = MyConstants.NO_BALANCE;
        txtIScore = MyConstants.NO_BALANCE;

        amountLoading = true;
        exchageLoading = true;
        prepsLoading = true;
        iscoreLoading = true;
    }

    public EntryViewData(String name, String symbol) {
        this.symbol = name;
        this.name = symbol;
        txtAmount = MyConstants.NO_BALANCE;
        txtExchanged = MyConstants.NO_BALANCE;

        txtStacked = MyConstants.NO_BALANCE;
        txtIScore = MyConstants.NO_BALANCE;

        amountLoading = true;
        exchageLoading = true;
        prepsLoading = true;
    }

    public Wallet getWallet() { return wallet; }

    public WalletEntry getEntry() {
        return entry;
    }

    public String getSymbol() {
        return symbol;
    }

    public EntryViewData setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getName() {
        return name;
    }

    public EntryViewData setName(String name) {
        this.name = name;
        return this;
    }

    public String getTxtAmount() {
        return txtAmount;
    }

    public EntryViewData setTxtAmount(String txtAmount) {
        this.txtAmount = txtAmount;
        return this;
    }

    public String getTxtExchanged() {
        return txtExchanged;
    }

    public BigDecimal getExchanged() {
        return exchanged;
    }

    public void setExchanged(BigDecimal exchanged, String unit) {
        this.exchanged = exchanged;
        int deci = "USD".equals(unit) ? 2 : 4;
        String strExchanged = exchanged != null ? DecimalFomatter.format(exchanged,deci) : MyConstants.NO_BALANCE;
        this.txtExchanged = strExchanged + " " + unit;
    }

    public String getTxtStacked() {
        return txtStacked;
    }

    public EntryViewData setTxtStacked(String txtStacked) {
        this.txtStacked = txtStacked;
        return this;
    }

    public String getTxtIScore() {
        return txtIScore;
    }

    public EntryViewData setTxtIScore(String txtIScore) {
        this.txtIScore = txtIScore;
        return this;
    }

    public int getDrawableSymbolresId() {
        return drawableSymbolresId;
    }

    public EntryViewData setDrawableSymbolresId(int drawableSymbolresId) {
        this.drawableSymbolresId = drawableSymbolresId;
        return this;
    }

    public int getBgSymbolColor() {
        return bgSymbolColor;
    }

    public EntryViewData setBgSymbolColor(int bgSymbolColor) {
        this.bgSymbolColor = bgSymbolColor;
        return this;
    }
}
