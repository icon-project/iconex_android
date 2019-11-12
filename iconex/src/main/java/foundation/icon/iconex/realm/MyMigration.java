package foundation.icon.iconex.realm;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

/**
 * Created by js on 2018. 3. 7..
 */

public class MyMigration implements RealmMigration {

    private static final String TAG = MyMigration.class.getSimpleName();

    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        Log.wtf(TAG, "oldVersion=" + oldVersion);
        RealmSchema schema = realm.getSchema();

        if (oldVersion == 0) {
            schema.create("MyVotes")
                    .addField("owner", String.class)
                    .addField("prepAddress", String.class)
                    .addField("prepName", String.class)
                    .addField("prepGrade", Integer.class, FieldAttribute.REQUIRED);

            oldVersion++;
        }

        if (oldVersion == 1) {
            RealmObjectSchema recentETHSendSchema = schema.get("RecentETHSend");
            recentETHSendSchema.addField("nid", int.class);
            recentETHSendSchema.transform(new RealmObjectSchema.Function() {
                @Override
                public void apply(DynamicRealmObject obj) {
                    obj.setInt("nid", 1);
                }
            });

            oldVersion++;
        }
    }
}
