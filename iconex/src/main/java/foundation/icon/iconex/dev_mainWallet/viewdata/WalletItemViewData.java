package foundation.icon.iconex.dev_mainWallet.viewdata;

public class WalletItemViewData {
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
