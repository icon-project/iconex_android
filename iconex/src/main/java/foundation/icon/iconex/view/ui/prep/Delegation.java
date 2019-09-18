package foundation.icon.iconex.view.ui.prep;

import java.io.Serializable;
import java.math.BigInteger;

public class Delegation implements Serializable {

    private String prepName, address;
    private PRep.Status status;
    private PRep.Grade grade;
    private BigInteger value;

    public String getPrepName() {
        return prepName;
    }

    public String getAddress() {
        return address;
    }

    public PRep.Status getStatus() {
        return status;
    }

    public PRep.Grade getGrade() {
        return grade;
    }

    public BigInteger getValue() {
        return value;
    }

    Delegation(Builder builder) {
        prepName = builder.prepName;
        address = builder.address;
        status = builder.status;
        grade = builder.grade;
        value = builder.value;
    }

    public Builder newBuilder() {
        return new Builder()
                .name(prepName)
                .address(address)
                .status(status)
                .grade(grade)
                .value(value);
    }

    public static class Builder {
        private String prepName, address;
        private PRep.Status status;
        private PRep.Grade grade;
        private BigInteger value;

        public Builder() {
        }

        public Builder name(String prepName) {
            this.prepName = prepName;
            return this;
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

        public Builder status(PRep.Status status) {
            this.status = status;
            return this;
        }

        public Delegation build() {
            return new Delegation(this);
        }
    }
}
