package foundation.icon.iconex.realm.data;

import java.math.BigInteger;

import foundation.icon.iconex.view.ui.prep.PRep;
import io.realm.RealmObject;

public class MyVotes extends RealmObject {

    private String owner;
    private String prepName;
    private String prepAddress;
    private int prepGrade;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPrepName() {
        return prepName;
    }

    public void setPrepName(String prepName) {
        this.prepName = prepName;
    }

    public String getPrepAddress() {
        return prepAddress;
    }

    public void setPrepAddress(String prepAddress) {
        this.prepAddress = prepAddress;
    }

    public int getPrepGrade() {
        return prepGrade;
    }

    public void setPrepGrade(int prepGrade) {
        this.prepGrade = prepGrade;
    }
}
