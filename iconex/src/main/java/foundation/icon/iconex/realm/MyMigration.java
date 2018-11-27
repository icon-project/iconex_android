package foundation.icon.iconex.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by js on 2018. 3. 7..
 */

public class MyMigration implements RealmMigration {

    private static final String TAG = MyMigration.class.getSimpleName();

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

//        if (oldVersion == 1) {
//            Log.d(TAG, "oldVersion=" + oldVersion + ", newVersion" + newVersion);
//            RealmObjectSchema coinNToken = schema.get("CoinNToken");
//            RealmObjectSchema wallet = schema.get("Wallet");
//
//            coinNToken.addField("createAt", String.class);
//            wallet.addField("createAt", String.class);
//
//            oldVersion++;
//        }
    }
}
