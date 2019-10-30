package foundation.icon.iconex.view.ui.prep;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.icx.transport.jsonrpc.RpcObject;

public class PRep implements Serializable {
    private static final String TAG = PRep.class.getSimpleName();

    private int rank;
    private String name, country, city, address, irep, irepUpdated, irepGen;
    private Grade grade;
    private BigInteger stake, totalDelegated, delegated, totalBlocks, validatedBlocks;
    private String penalty, email, website, details, p2pEndPoint;

    public int getRank() {
        return rank;
    }

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

    public String getPenalty() {
        return penalty;
    }

    public String getEmail() {
        return email;
    }

    public String getWebsite() {
        return website;
    }

    public String getDetails() {
        return details;
    }

    public String getP2pEndPoint() {
        return p2pEndPoint;
    }

    PRep(Builder b) {
        rank = b.rank;
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

        penalty = b.penalty;
        email = b.email;
        website = b.website;
        details = b.details;
        p2pEndPoint = b.p2pEndPoint;
    }

    public static class Builder {
        private int rank;
        private String name, country, city, address, irep, irepUpdated, irepGen;
        private Grade grade;
        private BigInteger stake, totalDelegated, delegated, totalBlocks, validatedBlocks;
        private String penalty, email, website, details, p2pEndPoint;

        public Builder() {
        }

        public Builder rank(int rank) {
            this.rank = rank;
            return this;
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

        public Builder grade(PRep.Grade grade) {
            this.grade = grade;
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

        public Builder stake(BigInteger stake) {
            this.stake = stake;
            return this;
        }

        public Builder totalDelegated(String totalDelegated) {
            this.totalDelegated = ConvertUtil.hexStringToBigInt(totalDelegated, 0);
            return this;
        }

        public Builder totalDelegated(BigInteger totalDelegated) {
            this.totalDelegated = totalDelegated;
            return this;
        }

        public Builder delegated(String delegated) {
            this.delegated = ConvertUtil.hexStringToBigInt(delegated, 0);
            return this;
        }

        public Builder delegated(BigInteger delegated) {
            this.delegated = delegated;
            return this;
        }

        public Builder totalBlocks(String totalBlocks) {
            this.totalBlocks = ConvertUtil.hexStringToBigInt(totalBlocks, 0);
            return this;
        }

        public Builder totalBlocks(BigInteger totalBlocks) {
            this.totalBlocks = totalBlocks;
            return this;
        }

        public Builder validatedBlocks(String validatedBlocks) {
            this.validatedBlocks = ConvertUtil.hexStringToBigInt(validatedBlocks, 0);
            return this;
        }

        public Builder validatedBlocks(BigInteger validatedBlocks) {
            this.validatedBlocks = validatedBlocks;
            return this;
        }

        public Builder penalty(String penalty) {
            this.penalty = penalty;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder p2pEndPoint(String p2pEndPoint) {
            this.p2pEndPoint = p2pEndPoint;
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

    public PRep setDetails(RpcObject object) {
        PRep pRep = newBuilder()
                .email(object.getItem("email").asString())
                .website(object.getItem("website").asString())
                .p2pEndPoint(object.getItem("p2pEndpoint").asString())
                .build();
        Log.d(TAG, "PRep=" + pRep);
        return pRep;
    }

    public Builder newBuilder() {
        return new Builder()
                .rank(rank)
                .name(name)
                .country(country)
                .city(city)
                .grade(grade)
                .address(address)
                .irep(irep)
                .irepUpdated(irepUpdated)
                .irepGen(irepGen)
                .stake(stake)
                .totalDelegated(totalDelegated)
                .delegated(delegated)
                .totalBlocks(totalBlocks)
                .validatedBlocks(validatedBlocks);
    }

    public double delegatedPercent() {
        BigDecimal total = new BigDecimal(totalDelegated).scaleByPowerOfTen(-18);
        BigDecimal delegation = new BigDecimal(delegated).scaleByPowerOfTen(-18);

        return Double.parseDouble(delegation.divide(total, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).setScale(1, BigDecimal.ROUND_HALF_UP).toString());
    }

    public enum Grade {
        PRep(0, "Main P-Rep"),
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

    public enum Status {
        NORMAL(0),
        SLASHED(1);

        private int status;

        Status(int status) {
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public static Status fromStatus(String hex) {
            if (hex != null) {
                int status = ConvertUtil.hexStringToBigInt(hex, 0).intValue();

                for (Status s : values()) {
                    if (s.getStatus() == status)
                        return s;
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
                "\"website\": \"" + website + "\"\n" +
                "}";
    }
}
