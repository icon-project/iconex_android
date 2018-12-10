package foundation.icon.iconex.realm.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by js on 2018. 3. 5..
 */

public class Wallet extends RealmObject {

    @PrimaryKey
    private long id;

    private String coinType;
    private String alias;
    private String address;
    private String keyStore;
    private RealmList<CoinNToken> coinNToken;
    private String createAt;

    public String getCoinType() {
        return coinType;
    }

    public void setCoinType(String coinType) {
        this.coinType = coinType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public RealmList<CoinNToken> getCoinNToken() {
        return coinNToken;
    }

    public void setCoinNToken(RealmList<CoinNToken> coinNToken) {
        this.coinNToken = coinNToken;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }
}
