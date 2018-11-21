package foundation.icon.connect;

import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseData {

    private int resId;
    private int resCode;
    private String result;

    public ResponseData(int resId, int resCode, String result) {
        this.resId = resId;
        this.resCode = resCode;
        this.result = result;
    }

    public String getResponse() {
        JSONObject data = new JSONObject();
        try {
            data.put("id", resId);
            data.put("code", resCode);
            data.put("result", result);
        } catch (JSONException e) {

        }

        return Base64.encodeToString(data.toString().getBytes(), Base64.NO_WRAP);
    }
}
