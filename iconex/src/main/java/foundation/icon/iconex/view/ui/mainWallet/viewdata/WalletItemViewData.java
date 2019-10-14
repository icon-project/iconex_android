package foundation.icon.iconex.view.ui.mainWallet.viewdata;

import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.WalletEntry;

public class WalletItemViewData {
    private static String TAG = WalletItemViewData.class.getSimpleName();
    public enum WalletItemType { ICXcoin, ETHcoin, Token, Wallet}

    private int entryID;
    private BigDecimal amount = null;
    private BigDecimal exchanged = null;
    private BigInteger stacked = null;
    private BigInteger iScore = null;

    // common flied
    private WalletItemType walletItemType;

    private String symbol;
    private String name;
    private String txtAmount;
    private String txtExchanged;

    // only for icx coin
    private String txtStacked;
    private String txtIScore;

    // has drawable resource id, maybe not use
    private int drawableSymbolresId;

    // for token
    private int bgSymbolColor;
    private char symbolLetter;


    private WalletEntry entry;

    public WalletEntry getEntry() {
        return entry;
    }

    public void setEntry(WalletEntry entry) {
        this.entry = entry;
    }

    public WalletItemViewData () {}
    public WalletItemViewData (WalletItemViewData data) {
        entryID = data.entryID;
        amount = data.amount;
        exchanged = data.exchanged;
        walletItemType = data.walletItemType;
        symbol = data.symbol;
        name = data.name;
        txtAmount = data.txtAmount;
        txtExchanged = data.txtExchanged;
        txtStacked = data.txtStacked;
        txtIScore = data.txtIScore;
        drawableSymbolresId = data.drawableSymbolresId;
        bgSymbolColor = data.bgSymbolColor;
        symbolLetter = data.symbolLetter;
    }
    public static WalletItemViewData convertWalletEntry2ViewItem(WalletEntry walletEntry) {
        WalletItemViewData itemViewData = new WalletItemViewData();
        itemViewData.setEntry(walletEntry);

        // Wallet Item(Entry) Type check
        WalletItemViewData.WalletItemType walletItemType = null;
        if ("COIN".equals(walletEntry.getType().toUpperCase())) {
            if ("ICX".equals(walletEntry.getSymbol().toUpperCase())) {
                walletItemType = WalletItemViewData.WalletItemType.ICXcoin;
            } else if ("ETH".equals(walletEntry.getSymbol().toUpperCase())) {
                walletItemType = WalletItemViewData.WalletItemType.ETHcoin;
            } else {
                Log.e(TAG, "unknow coin(symbol) type " + walletEntry.getSymbol());
            }
        } else if ("TOKEN".equals(walletEntry.getType().toUpperCase())) {
            walletItemType = WalletItemViewData.WalletItemType.Token;
        } else {
            Log.e(TAG, "unknown token type " + walletEntry.getType());
        }
        itemViewData.setWalletItemType(walletItemType);
        itemViewData.setEntryID(walletEntry.getId());

        switch (walletItemType) {
            case ICXcoin: {
                itemViewData
                        .setName(walletEntry.getName())
                        .setSymbol(walletEntry.getSymbol())
                        .setDrawableSymbolresId(R.drawable.img_logo_icon_sel);
            } break;
            case ETHcoin: {
                itemViewData
                        .setName(walletEntry.getName())
                        .setSymbol(walletEntry.getSymbol())
                        .setDrawableSymbolresId(R.drawable.img_logo_ethereum_nor);
            } break;
            case Token: {
                itemViewData
                        .setName(walletEntry.getName())
                        .setSymbol(walletEntry.getSymbol())
                        .setSymbolLetter(walletEntry.getName().charAt(0));
            }
            case Wallet:
            default: {
                // never reach here.
            } break;
        }
        return itemViewData;
    }

    public int getEntryID() {
        return entryID;
    }

    public WalletItemViewData setEntryID(int entryID) {
        this.entryID = entryID;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public WalletItemViewData setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public BigDecimal getExchanged() {
        return exchanged;
    }

    public WalletItemViewData setExchanged(BigDecimal exchanged) {
        this.exchanged = exchanged;
        return this;
    }

    public BigInteger getStacked() {
        return stacked;
    }

    public WalletItemViewData setStacked(BigInteger stacked) {
        this.stacked = stacked;
        return this;
    }

    public BigInteger getiScore() {
        return iScore;
    }

    public WalletItemViewData setiScore(BigInteger iScore) {
        this.iScore = iScore;
        return this;
    }

    public WalletItemType getWalletItemType() {
        return walletItemType;
    }

    public WalletItemViewData setWalletItemType(WalletItemType walletItemType) {
        this.walletItemType = walletItemType;
        return this;
    }

    public String getSymbol() {
        return symbol;
    }

    public WalletItemViewData setSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public String getName() {
        return name;
    }

    public WalletItemViewData setName(String name) {
        this.name = name;
        return this;
    }

    public String getTxtAmount() {
        return txtAmount;
    }

    public WalletItemViewData setTxtAmount(String txtAmount) {
        this.txtAmount = txtAmount;
        return this;
    }

    public String getTxtExchanged() {
        return txtExchanged;
    }

    public WalletItemViewData setTxtExchanged(String txtExchanged) {
        this.txtExchanged = txtExchanged;
        return this;
    }

    public String getTxtStacked() {
        return txtStacked;
    }

    public WalletItemViewData setTxtStacked(String txtStacked) {
        this.txtStacked = txtStacked;
        return this;
    }

    public String getTxtIScore() {
        return txtIScore;
    }

    public WalletItemViewData setTxtIScore(String txtIScore) {
        this.txtIScore = txtIScore;
        return this;
    }

    public int getDrawableSymbolresId() {
        return drawableSymbolresId;
    }

    public WalletItemViewData setDrawableSymbolresId(int drawableSymbolresId) {
        this.drawableSymbolresId = drawableSymbolresId;
        return this;
    }

    public int getBgSymbolColor() {
        return bgSymbolColor;
    }

    public WalletItemViewData setBgSymbolColor(int bgSymbolColor) {
        this.bgSymbolColor = bgSymbolColor;
        return this;
    }

    public char getSymbolLetter() {
        return symbolLetter;
    }

    public WalletItemViewData setSymbolLetter(char symbolLetter) {
        this.symbolLetter = symbolLetter;
        return this;
    }
}
