package foundation.icon.iconex.realm.data;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by js on 2018. 3. 19..
 */

public class ICXContacts extends RealmObject {

    @PrimaryKey
    private int id;

    private String name;
    private String address;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
