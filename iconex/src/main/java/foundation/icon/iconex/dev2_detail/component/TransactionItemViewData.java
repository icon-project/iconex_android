package foundation.icon.iconex.dev2_detail.component;

public class TransactionItemViewData {

    private String txHash;
    private String date;
    private String from;
    private String to;
    private String amount;
    private String fee;
    private int state;

    private String txtName;
    private String txtDate;
    private String txtAddress;
    private String txtAmount;
    private boolean isDark;

    public String getTxHash() {
        return txHash;
    }

    public TransactionItemViewData setTxHash(String txHash) {
        this.txHash = txHash;
        return this;
    }

    public String getDate() {
        return date;
    }

    public TransactionItemViewData setDate(String date) {
        this.date = date;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public TransactionItemViewData setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public TransactionItemViewData setTo(String to) {
        this.to = to;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public TransactionItemViewData setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public String getFee() {
        return fee;
    }

    public TransactionItemViewData setFee(String fee) {
        this.fee = fee;
        return this;
    }

    public int getState() {
        return state;
    }

    public TransactionItemViewData setState(int state) {
        this.state = state;
        return this;
    }

    public String getTxtName() {
        return txtName;
    }

    public TransactionItemViewData setTxtName(String txtName) {
        this.txtName = txtName;
        return this;
    }

    public String getTxtDate() {
        return txtDate;
    }

    public TransactionItemViewData setTxtDate(String txtDate) {
        this.txtDate = txtDate;
        return this;
    }

    public String getTxtAddress() {
        return txtAddress;
    }

    public TransactionItemViewData setTxtAddress(String txtAddress) {
        this.txtAddress = txtAddress;
        return this;
    }

    public String getTxtAmount() {
        return txtAmount;
    }

    public TransactionItemViewData setTxtAmount(String txtAmount) {
        this.txtAmount = txtAmount;
        return this;
    }

    public boolean isDark() {
        return isDark;
    }

    public TransactionItemViewData setDark(boolean dark) {
        isDark = dark;
        return this;
    }

    @Override
    public String toString() {
        return "TransactionItemViewData{" +
                "txHash='" + txHash + '\'' +
                ", date='" + date + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", amount='" + amount + '\'' +
                ", fee='" + fee + '\'' +
                ", state=" + state +
                ", txtName='" + txtName + '\'' +
                ", txtDate='" + txtDate + '\'' +
                ", txtAddress='" + txtAddress + '\'' +
                ", txtAmount='" + txtAmount + '\'' +
                ", isDark=" + isDark +
                '}';
    }
}
