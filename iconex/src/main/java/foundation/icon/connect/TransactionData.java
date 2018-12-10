package foundation.icon.connect;

import java.io.Serializable;
import java.math.BigInteger;

public class TransactionData implements Serializable {
    private String alias;

    private BigInteger stepPrice;
    private BigInteger defaultLimit;
    private BigInteger contractCall;
    private BigInteger input;
    private BigInteger maxLimit;

    private String symbol;
    private int decimals;
    private BigInteger balance;
    private BigInteger tokenBalance;

    public TransactionData(TransactionData.Builder builder) {
        this.alias = builder.alias;

        this.stepPrice = builder.stepPrice;
        this.defaultLimit = builder.defaultLimit;
        this.contractCall = builder.contractCall;
        this.input = builder.input;
        this.maxLimit = builder.maxLimit;

        this.balance = builder.balance;
        this.symbol = builder.symbol;
        this.decimals = builder.decimals;
        this.tokenBalance = builder.tokenBalance;
    }

    public String getAlias() {
        return alias;
    }

    public BigInteger getStepPrice() {
        return stepPrice;
    }

    public BigInteger getDefaultLimit() {
        return defaultLimit;
    }

    public BigInteger getContractCall() {
        return contractCall;
    }

    public BigInteger getInput() {
        return input;
    }

    public BigInteger getMaxLimit() {
        return maxLimit;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public BigInteger getTokenBalance() {
        return tokenBalance;
    }

    public BigInteger getBalance() {
        return balance;
    }

    public static class Builder {
        private String alias;

        private BigInteger stepPrice;
        private BigInteger defaultLimit;
        private BigInteger contractCall;
        private BigInteger input;
        private BigInteger maxLimit;

        private BigInteger balance;
        private String symbol;
        private int decimals;
        private BigInteger tokenBalance;

        public Builder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public Builder stepPrice(BigInteger stepPrice) {
            this.stepPrice = stepPrice;
            return this;
        }

        public Builder defaultLimit(BigInteger defaultLimit) {
            this.defaultLimit = defaultLimit;
            return this;
        }

        public Builder contractCall(BigInteger contractCall) {
            this.contractCall = contractCall;
            return this;
        }

        public Builder input(BigInteger input) {
            this.input = input;
            return this;
        }

        public Builder maxLimit(BigInteger maxLimit) {
            this.maxLimit = maxLimit;
            return this;
        }

        public Builder balance(BigInteger balance) {
            this.balance = balance;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder decimals(int decimals) {
            this.decimals = decimals;
            return this;
        }

        public Builder tokenBalance(BigInteger tokenBalance) {
            this.tokenBalance = tokenBalance;
            return this;
        }

        public TransactionData build() {
            return new TransactionData(this);
        }
    }
}
