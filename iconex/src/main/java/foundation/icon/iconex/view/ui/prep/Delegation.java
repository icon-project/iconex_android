package foundation.icon.iconex.view.ui.prep;

import java.math.BigInteger;

public class Delegation {

    private String address, status;
    private PRep.Grade grade;
    private BigInteger value;

    public String getAddress() {
        return address;
    }

    public String getStatus() {
        return status;
    }

    public PRep.Grade getGrade() {
        return grade;
    }

    public BigInteger getValue() {
        return value;
    }

    public Delegation(Builder builder) {
        address = builder.address;
        status = builder.status;
        grade = builder.grade;
        value = builder.value;
    }

    public static class Builder {
        private String address, status;
        private PRep.Grade grade;
        private BigInteger value;

        Builder() {

        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }
        public Builder grade(PRep.Grade grade) {
            this.grade = grade;
            return this;
        }

        public Builder value(BigInteger value) {
            this.value = value;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Delegation build() {
            return new Delegation(this);
        }
    }
}
