package foundation.icon.iconex.wallet.transfer.data;

import java.io.Serializable;
import java.math.BigInteger;

import foundation.icon.iconex.wallet.transfer.EnterDataFragment;

public class InputData implements Serializable {

    private String address;
    private BigInteger balance;
    private BigInteger stepPrice;
    private BigInteger amount;
    private EnterDataFragment.DataType dataType;
    private String data;
    private int stepCost;

    public String getAddress() {
        return address;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
    }

    public BigInteger getStepPrice() {
        return stepPrice;
    }

    public void setStepPrice(BigInteger stepPrice) {
        this.stepPrice = stepPrice;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public EnterDataFragment.DataType getDataType() {
        return dataType;
    }

    public void setDataType(EnterDataFragment.DataType dataType) {
        this.dataType = dataType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getStepCost() {
        return stepCost;
    }

    public void setStepCost(int stepCost) {
        this.stepCost = stepCost;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
    }
}
