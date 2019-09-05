package foundation.icon.iconex.dev_mainWallet.viewdata;

import android.util.Log;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.WalletEntry;

public class WalletItemViewData {
    private static String TAG = WalletItemViewData.class.getSimpleName();
    public enum WalletItemType { ICXcoin, ETHcoin, Token, Wallet}

    // common flied
    private WalletItemType walletItemType;

    private String symbol;
    private String name;
    private String amount;
    private String exchanged;

    // only for icx coin
    private String stacked;
    private String votingPower;
    private String iScore;

    // has drawable resource id, maybe not use
    private int drawableSymbolresId;

    // for token
    private int bgSymbolColor;
    private char symbolLetter;

    public static WalletItemViewData convertWalletEntry2ViewItem(WalletEntry walletEntry) {
        WalletItemViewData itemViewData = new WalletItemViewData();

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

        switch (walletItemType) {
            case ICXcoin: {
                itemViewData
                        .setName(walletEntry.getName())
                        .setSymbol(walletEntry.getSymbol())
                        .setDrawableSymbolresId(R.drawable.img_logo_icon_sel)
                        .setAmount("0.00")
                        .setExchanged("0.00 USD")
                        .setStacked("12.5")
                        .setVotingPower("0.000")
                        .setiScore("0.000");
            } break;
            case ETHcoin: {
                itemViewData
                        .setName(walletEntry.getName())
                        .setSymbol(walletEntry.getSymbol())
                        .setDrawableSymbolresId(R.drawable.img_logo_ethereum_nor)
                        .setAmount("0.00")
                        .setExchanged("0.00 USD");
            } break;
            case Token: {
                itemViewData
                        .setName(walletEntry.getName())
                        .setSymbol(walletEntry.getSymbol())
                        .setSymbolLetter(walletEntry.getName().charAt(0))
//                        .setBgSymbolColor(tokenColor.getColor())
                        .setAmount("0.00")
                        .setExchanged("0.00 USD");
                // need init background color
            }
            case Wallet:
            default: {
                // never reach here.
            } break;
        }
        return itemViewData;
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

    public String getAmount() {
        return amount;
    }

    public WalletItemViewData setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public String getExchanged() {
        return exchanged;
    }

    public WalletItemViewData setExchanged(String exchanged) {
        this.exchanged = exchanged;
        return this;
    }

    public String getStacked() {
        return stacked;
    }

    public WalletItemViewData setStacked(String stacked) {
        this.stacked = stacked;
        return this;
    }

    public String getVotingPower() {
        return votingPower;
    }

    public WalletItemViewData setVotingPower(String votingPower) {
        this.votingPower = votingPower;
        return this;
    }

    public String getiScore() {
        return iScore;
    }

    public WalletItemViewData setiScore(String iScore) {
        this.iScore = iScore;
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
