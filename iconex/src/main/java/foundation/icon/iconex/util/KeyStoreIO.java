package foundation.icon.iconex.util;

import android.content.Context;
import android.os.Environment;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import loopchain.icon.wallet.core.Constants;

/**
 * Created by js on 2018. 2. 8..
 */

public class KeyStoreIO {

    private static final String TAG = KeyStoreIO.class.getSimpleName();

    private final Context mContext;

    public static final String DIR_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ICONex";

    public KeyStoreIO(Context context) {

        mContext = context;
    }

    private static void makeDir() throws Exception {
        File dir = new File(Environment.getExternalStorageDirectory(), "ICONex");
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static void exportKeyStore(JsonObject keyStore, String coinType) throws Exception {
        makeDir();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH_mm_ss.SSS", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime()) + "T" + timeFormat.format(calendar.getTime()) + "Z";

        String keyStoreName;
        if (coinType.equals(Constants.KS_COINTYPE_ICX))
            keyStoreName = "UTC--" + date + "--" + keyStore.get("address").getAsString();
        else
            keyStoreName = "UTC--" + date + "--0x" + keyStore.get("address").getAsString();

        File mKeyStore = new File(DIR_PATH + "/" + keyStoreName);
        boolean result = mKeyStore.createNewFile();

        if (result) {
            FileWriter writer = new FileWriter(mKeyStore);
            writer.write(keyStore.toString());

            writer.close();
        } else {
            throw new RuntimeException("Create file is failed.");
        }
    }

    public static void exportBundle(JsonArray bundle) throws Exception {
        makeDir();

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH_mm_ss.SSS", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime()) + "T" + timeFormat.format(calendar.getTime()) + "Z";

        String bundleName = "iconex_" + date;

        File mKeyStore = new File(DIR_PATH + "/" + bundleName);
        boolean result = mKeyStore.createNewFile();

        if (result) {
            FileWriter writer = new FileWriter(mKeyStore);
            writer.write(bundle.toString());

            writer.close();
        } else {
            throw new RuntimeException("Create file is failed.");
        }
    }
}
