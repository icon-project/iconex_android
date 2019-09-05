package foundation.icon.iconex.view.ui.prep;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.math.BigInteger;

import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.icx.transport.jsonrpc.RpcObject;

public class PRep implements Serializable {
    private static final String TAG = PRep.class.getSimpleName();

    private String name, country, city, address, irep, irepUpdated, irepGen;
    private Grade grade;
    private BigInteger stake, totalDelegated, delegated, totalBlocks, validatedBlocks;

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getIrep() {
        return irep;
    }

    public String getIrepUpdated() {
        return irepUpdated;
    }

    public String getIrepGen() {
        return irepGen;
    }

    public Grade getGrade() {
        return grade;
    }

    public BigInteger getStake() {
        return stake;
    }

    public void setTotalDelegated(BigInteger totalDelegated) {
        this.totalDelegated = totalDelegated;
    }

    public BigInteger getTotalDelegated() {
        return totalDelegated;
    }

    public BigInteger getDelegated() {
        return delegated;
    }

    public BigInteger getTotalBlocks() {
        return totalBlocks;
    }

    public BigInteger getValidatedBlocks() {
        return validatedBlocks;
    }

    PRep(Builder b) {
        name = b.name;
        country = b.country;
        city = b.city;
        grade = b.grade;
        address = b.address;
        irep = b.irep;
        irepUpdated = b.irepUpdated;
        irepGen = b.irepGen;
        stake = b.stake;
        totalDelegated = b.totalDelegated;
        delegated = b.delegated;
        totalBlocks = b.totalBlocks;
        validatedBlocks = b.validatedBlocks;
    }

    public static class Builder {

        private String name, country, city, address, irep, irepUpdated, irepGen;
        private Grade grade;
        private BigInteger stake, totalDelegated, delegated, totalBlocks, validatedBlocks;

        public Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder grade(String grade) {
            this.grade = Grade.fromGrade(
                    ConvertUtil.hexStringToBigInt(grade, 0).intValue());
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder irep(String irep) {
            this.irep = irep;
            return this;
        }

        public Builder irepUpdated(String irepUpdated) {
            this.irepUpdated = irepUpdated;
            return this;
        }

        public Builder irepGen(String irepGen) {
            this.irepGen = irepGen;
            return this;
        }

        public Builder stake(String stake) {
            this.stake = ConvertUtil.hexStringToBigInt(stake, 0);
            return this;
        }

        public Builder totalDelegated(String totalDelegated) {
            this.totalDelegated = ConvertUtil.hexStringToBigInt(totalDelegated, 0);
            return this;
        }

        public Builder delegated(String delegated) {
            this.delegated = ConvertUtil.hexStringToBigInt(delegated, 0);
            return this;
        }

        public Builder totalBlocks(String totalBlocks) {
            this.totalBlocks = ConvertUtil.hexStringToBigInt(totalBlocks, 0);
            return this;
        }

        public Builder validatedBlocks(String validatedBlocks) {
            this.validatedBlocks = ConvertUtil.hexStringToBigInt(validatedBlocks, 0);
            return this;
        }

        public PRep build() {
            return new PRep(this);
        }
    }

    public static PRep valueOf(RpcObject object) {
        return new Builder()
                .name(object.getItem("name").asString())
                .country(object.getItem("country").asString())
                .city(object.getItem("city").asString())
                .grade(object.getItem("grade").asString())
                .address(object.getItem("address").asString())
                .irep(object.getItem("irep").asString())
                .irepUpdated(object.getItem("irepUpdateBlockHeight").asString())
                .irepGen(object.getItem("lastGenerateBlockHeight").asString())
                .stake(object.getItem("stake").asString())
                .delegated(object.getItem("delegated").asString())
                .totalBlocks(object.getItem("totalBlocks").asString())
                .validatedBlocks(object.getItem("validatedBlocks").asString())
                .build();
    }

    public double delegatedPercent() {
        double total = totalDelegated.doubleValue();
        double del = delegated.doubleValue();

        return del / total * 100;
    }

    public enum Grade {
        PRep(0, "P-Rep"),
        SubPRep(1, "Sub P-Rep"),
        Candidate(2, "Candidate");

        private int grade;
        private String label;

        public int getGrade() {
            return grade;
        }

        public String getLabel() {
            return label;
        }

        Grade(int grade, String label) {
            this.grade = grade;
            this.label = label;
        }

        public static Grade fromGrade(int grade) {
            if (grade != -1) {
                for (Grade g : values()) {
                    if (g.getGrade() == grade)
                        return g;
                }
            }

            return null;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "{\n" +
                "\"name\": \"" + name + "\",\n" +
                "\"country\": \"" + country + "\",\n" +
                "\"city\": \"" + city + "\",\n" +
//                "\"grade\": \"" + grade.getGrade() + "\",\n" +
                "\"address\": \"" + address + "\",\n" +
                "\"irep\": \"" + irep + "\",\n" +
                "\"irepUpdateBlockHeight\": \"" + irepUpdated + "\",\n" +
                "\"lastGenerateBlockHeight\": \"" + irepGen + "\",\n" +
                "\"stake\": \"" + stake + "\",\n" +
                "\"delegated\": \"" + delegated + "\",\n" +
                "\"totalBlocks\": \"" + totalBlocks + "\",\n" +
                "\"validatedBlocks\": \"" + validatedBlocks + "\"\n" +
                "}";
    }
}
