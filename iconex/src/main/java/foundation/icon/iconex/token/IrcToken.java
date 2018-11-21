package foundation.icon.iconex.token;

public class IrcToken extends Token {

    private boolean checked = false;
    private boolean opened = false;

    public IrcToken(String address, String score, String name, String symbol, int dec) {
        super();

        this.address = address;
        this.contractAddress = score;
        this.defaultName = name;
        this.userName = name;
        this.defaultSymbol = symbol;
        this.userSymbol = symbol;
        this.defaultDec = dec;
        this.userDec = dec;
    }

    public String getName() {
        return defaultName;
    }

    public String getSymbol() {
        return defaultSymbol;
    }

    public int getDecimal() {
        return defaultDec;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
