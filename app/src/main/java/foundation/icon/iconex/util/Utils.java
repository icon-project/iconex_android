package foundation.icon.iconex.util;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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
}
