package foundation.icon.iconex.view.ui.prep;

import java.io.Serializable;
import java.math.BigInteger;

public class Delegation implements Serializable {

    private PRep prep;
    private BigInteger value;
    private boolean isEdited = false;

    public PRep getPrep() {
        return prep;
    }

    public BigInteger getValue() {
        return value;
    }

    public void isEdited(boolean isEdited) {
        this.isEdited = isEdited;
    }

    public boolean isEdited() {
        return isEdited;
    }

    Delegation(Builder builder) {
        prep = builder.prep;

        if (builder.value == null)
            value = BigInteger.ZERO;
        else
            value = builder.value;
    }

    public Builder newBuilder() {
        return new Builder()
                .prep(prep)
                .value(value);
    }

    public static class Builder {
        private PRep prep;
        private BigInteger value;

        public Builder() {
        }

        public Builder prep(PRep prep) {
            this.prep = prep;
            return this;
        }

        public Builder value(BigInteger value) {
            this.value = value;
            return this;
        }

        public Delegation build() {
            return new Delegation(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Delegation) {
            Delegation d = (Delegation) obj;
            return d.getPrep().getAddress().equals(getPrep().getAddress());
        } else {
            return false;
        }
    }
}
