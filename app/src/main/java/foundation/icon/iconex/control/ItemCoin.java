package foundation.icon.iconex.control;

import android.support.annotation.NonNull;

/**
 * Created by js on 2018. 2. 22..
 */

public class ItemCoin {

    String name;
    boolean isChecked;
    String coinType;

    public ItemCoin(@NonNull String name, @NonNull String coinType) {
        setName(name);
        setChecked(false);
        setCoinType(coinType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getCoinType() {
        return coinType;
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType;
    }
}
