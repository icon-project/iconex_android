package foundation.icon.sample_icon_connect;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import foundation.icon.icx.Transaction;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Transaction tx;
    private TextView txtAddress, txtSigned, txtIcxTxHash, txtTokenTxHash;

    private MyBroadcastReceiver receiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_bind).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendBind();
            }
        });

        findViewById(R.id.btn_sign).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputDialog dialog = new InputDialog(MainActivity.this, SampleApp.Method.Sign);
                dialog.setListener(new InputDialog.OnClickListener() {
                    @Override
                    public void onConfirm(RequestData requestData) {
                        String params = new Gson().toJson(requestData, RequestData.class);

                        sendSign(params);
                    }
                });
                dialog.show();
            }
        });

        findViewById(R.id.btn_send_icx).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputDialog dialog = new InputDialog(MainActivity.this, SampleApp.Method.SendIcx);
                dialog.setListener(new InputDialog.OnClickListener() {
                    @Override
                    public void onConfirm(RequestData requestData) {
                        String params = new Gson().toJson(requestData, RequestData.class);

                        sendICX(params);
                    }
                });
                dialog.show();
            }
        });

        findViewById(R.id.btn_send_irc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputDialog dialog = new InputDialog(MainActivity.this, SampleApp.Method.SendToken);
                dialog.setListener(new InputDialog.OnClickListener() {
                    @Override
                    public void onConfirm(RequestData requestData) {
                        String params = new Gson().toJson(requestData, RequestData.class);

                        sendToken(params);
                    }
                });
                dialog.show();
            }
        });

        findViewById(R.id.btn_developer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDeveloper();
            }
        });

        txtAddress = findViewById(R.id.txt_bound);
        txtAddress.setTextIsSelectable(true);
        if (SampleApp.from != null) {
            txtAddress.setText(SampleApp.from);
            txtAddress.setVisibility(View.VISIBLE);
        } else
            txtAddress.setVisibility(View.GONE);

        txtSigned = findViewById(R.id.txt_signed);
        txtIcxTxHash = findViewById(R.id.txt_icx_tx_hash);
        txtTokenTxHash = findViewById(R.id.txt_token_tx_hash);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (receiver == null)
            receiver = new MyBroadcastReceiver();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, new IntentFilter(SampleApp.LOCAL_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (receiver != null) {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void sendBind() {
        JSONObject params = new JSONObject();

        try {
            params.put("id", 1234);
            params.put("method", "bind");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = Base64.encodeToString(params.toString().getBytes(), Base64.NO_WRAP);
        Intent intent = new Intent()
                .setClassName("foundation.icon.iconex", "foundation.icon.connect.ConnectReceiver")
                .setAction(SampleApp.ACTION_CONNECT)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES|Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
                .putExtra("data", data)
                .putExtra("caller", "foundation.icon.sample_icon_connect")
                .putExtra("receiver", "foundation.icon.sample_icon_connect.ResponseReceiver");

        sendBroadcast(intent);
    }

    private void sendSign(String params) {
        JSONObject action = new JSONObject();
        try {
            action.put("id", 2234)
                    .put("method", "sign")
                    .put("params", params);
        } catch (JSONException e) {

        }

        String data = Base64.encodeToString(action.toString().getBytes(), Base64.NO_WRAP);
        Intent intent = new Intent()
                .setClassName("foundation.icon.iconex", "foundation.icon.connect.ConnectReceiver")
                .setAction(SampleApp.ACTION_CONNECT)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES|Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
                .putExtra("data", data)
                .putExtra("caller", "foundation.icon.sample_icon_connect")
                .putExtra("receiver", "foundation.icon.sample_icon_connect.ResponseReceiver");

        sendBroadcast(intent);
    }

    private void sendICX(String params) {
        JSONObject action = new JSONObject();

        try {
            action.put("id", 3234)
                    .put("method", "sendICX")
                    .put("params", params);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = Base64.encodeToString(action.toString().getBytes(), Base64.NO_WRAP);
        Intent intent = new Intent()
                .setClassName("foundation.icon.iconex", "foundation.icon.connect.ConnectReceiver")
                .setAction(SampleApp.ACTION_CONNECT)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES|Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
                .putExtra("data", data)
                .putExtra("caller", "foundation.icon.sample_icon_connect")
                .putExtra("receiver", "foundation.icon.sample_icon_connect.ResponseReceiver");

        sendBroadcast(intent);
    }

    private void sendToken(String params) {
        JSONObject action = new JSONObject();

        try {
            action.put("id", 4234)
                    .put("method", "sendToken")
                    .put("params", params);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = Base64.encodeToString(action.toString().getBytes(), Base64.NO_WRAP);
        Intent intent = new Intent()
                .setClassName("foundation.icon.iconex", "foundation.icon.connect.ConnectReceiver")
                .setAction(SampleApp.ACTION_CONNECT)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES|Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
                .putExtra("data", data)
                .putExtra("caller", "foundation.icon.sample_icon_connect")
                .putExtra("receiver", "foundation.icon.sample_icon_connect.ResponseReceiver");

        sendBroadcast(intent);
    }

    private void sendDeveloper() {
        JSONObject params = new JSONObject();

        try {
            params.put("id", 9999);
            params.put("method", "bind");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String data = Base64.encodeToString(params.toString().getBytes(), Base64.NO_WRAP);
        Intent intent = new Intent()
                .setClassName("foundation.icon.iconex", "foundation.icon.connect.ConnectReceiver")
                .setAction(SampleApp.ACTION_DEVELOPER)
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES|Intent.FLAG_EXCLUDE_STOPPED_PACKAGES)
                .putExtra("data", data)
                .putExtra("caller", "foundation.icon.sample_icon_connect")
                .putExtra("receiver", "foundation.icon.sample_icon_connect.ResponseReceiver");

        sendBroadcast(intent);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("MainActivity", "onReceive!!");
            if (intent.getAction().equals(SampleApp.LOCAL_ACTION)) {
                int id = intent.getIntExtra("id", -1);
                switch (id) {
                    case 1234:
                        txtAddress.setText(SampleApp.from);
                        txtAddress.setVisibility(View.VISIBLE);
                        break;

                    case 2234:
                        txtSigned.setText(intent.getStringExtra("result"));
                        txtSigned.setVisibility(View.VISIBLE);
                        break;

                    case 3234:
                        txtIcxTxHash.setText(intent.getStringExtra("result"));
                        txtIcxTxHash.setVisibility(View.VISIBLE);
                        break;

                    case 4234:
                        txtTokenTxHash.setText(intent.getStringExtra("result"));
                        txtTokenTxHash.setVisibility(View.VISIBLE);
                        break;
                }
            }
        }
    }
}
