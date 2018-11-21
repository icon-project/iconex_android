package foundation.icon.sample_icon_connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class ResponseReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        JSONObject response = getResponse(intent.getStringExtra("data"));

        if (action.equals("ICON_CONNECT")) {
            int id = -1;
            int code = -1;
            String result;
            try {
                id = response.getInt("id");
                code = response.getInt("code");
                result = response.getString("result");
            } catch (JSONException e) {
                return;
            }

            switch (id) {
                case 1234:
                    if (code > 0)
                        SampleApp.from = result;

                    LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(new Intent(SampleApp.LOCAL_ACTION)
                            .putExtra("id", id));
                    break;
                default:
                    LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(new Intent(SampleApp.LOCAL_ACTION)
                            .putExtra("id", id)
                            .putExtra("result", result));
                    Toast.makeText(context, code + " : " + result, Toast.LENGTH_LONG).show();
            }
        }
    }

    private JSONObject getResponse(String extra) {
        byte[] base64Response = Base64.decode(extra, Base64.NO_WRAP);
        JSONObject response = null;
        try {
            response = new JSONObject(new String(base64Response));
        } catch (JSONException e) {

        }

        return response;
    }
}
