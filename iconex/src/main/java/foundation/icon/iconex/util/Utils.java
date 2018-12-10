package foundation.icon.iconex.util;

import android.content.Context;
import android.content.pm.PackageInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;

/**
 * Created by js on 2018. 5. 8..
 */

public class Utils {

    public static int checkByteLength(String msg) {
        int strCnt = 0;

        for (int i = 0; i < msg.length(); i++) {
            if (msg.charAt(i) == 91 || msg.charAt(i) == 92
                    || msg.charAt(i) == 93 || msg.charAt(i) == 94
                    || msg.charAt(i) == 123 || msg.charAt(i) == 124
                    || msg.charAt(i) == 125 || msg.charAt(i) == 126
                    || msg.charAt(i) >= 128) {
                strCnt += 2;
            } else if (msg.charAt(i) != 13) {
                strCnt++;
            }
        }

        return strCnt;
    }

    public static String readAssets(Context context, final String fileName) {
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open(fileName), "UTF-8"));

            // do reading, usually loop until end of file reading
            String mLine;
            while ((mLine = reader.readLine()) != null) {
                builder.append(mLine);
            }
        } catch (IOException e) {
            throw new RuntimeException("Read asset failed");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //log the exception
                }
            }
        }

        return builder.toString();
    }

    public static RES_VERSION versionCheck(Context context, String necessary) {
        String[] mVersion;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            mVersion = info.versionName.split("\\.");
        } catch (Exception e) {
            return RES_VERSION.NONE;
        }

        String[] all = ICONexApp.version.split("\\.");
        String[] hav2;
        if (necessary != null) {
            hav2 = necessary.split("\\.");

            if (Integer.parseInt(mVersion[0]) < Integer.parseInt(hav2[0]))
                return RES_VERSION.UPDATE;
            else if (Integer.parseInt(mVersion[1]) < Integer.parseInt(hav2[1]))
                return RES_VERSION.UPDATE;
        }

        if (Integer.parseInt(mVersion[0]) < Integer.parseInt(all[0]))
            return RES_VERSION.NEW;
        else if (Integer.parseInt(mVersion[1]) < Integer.parseInt(all[1])) {
            if (Integer.parseInt(mVersion[0]) <= Integer.parseInt(all[0]))
                return RES_VERSION.NEW;
            else {
                if (Integer.parseInt(mVersion[2]) < Integer.parseInt(all[2])) {
                    if (Integer.parseInt(mVersion[1]) <= Integer.parseInt(all[1]))
                        return RES_VERSION.NEW;
                }
            }
        } else {
            if (Integer.parseInt(mVersion[2]) < Integer.parseInt(all[2])) {
                if (Integer.parseInt(mVersion[1]) <= Integer.parseInt(all[1]))
                    return RES_VERSION.NEW;
            }
        }

        return RES_VERSION.LATEST;
    }

    public static String remove0x(String hex) {
        if (hex.startsWith(MyConstants.PREFIX_HEX))
            return hex.substring(2);
        else
            return hex;
    }

    public static String checkPrefix(String hex) {
        if (!hex.startsWith(MyConstants.PREFIX_HEX))
            return MyConstants.PREFIX_HEX + hex;
        else
            return hex;
    }

    public static String formatFloating(String target, int floating) {
        if (!target.contains("."))
            return target;
        else {
            String[] divide = target.split("\\.");
            BigInteger integer = new BigInteger(divide[0]);
            String trailing;
            if (divide[1].length() < floating)
                trailing = divide[1];
            else
                trailing = divide[1].substring(0, floating);

            return NumberFormat.getNumberInstance(Locale.getDefault()).format(integer) + "." + trailing;
        }
    }

    public static String strip(String target) {
        return target.replaceAll("^\\s++|\\s++$", "");
    }

    public enum RES_VERSION {
        NONE,
        LATEST,
        NEW,
        UPDATE
    }
}
