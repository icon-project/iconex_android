package foundation.icon.sample_iconex_connect;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.IconAmount;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Transaction tx;
    private TextView txtAddress, txtIcxTxHash;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_bind).setOnClickListener(view -> bind());

        findViewById(R.id.btn_json_rpc).setOnClickListener(view -> jsonRpc());

        findViewById(R.id.btn_developer).setOnClickListener(view -> setDeveloperMode());

        txtAddress = findViewById(R.id.txt_bound);
        txtAddress.setTextIsSelectable(true);
        if (SampleApp.from != null) {
            txtAddress.setText(SampleApp.from);
            txtAddress.setVisibility(View.VISIBLE);
        } else
            txtAddress.setVisibility(View.GONE);

        txtIcxTxHash = findViewById(R.id.txt_icx_tx_hash);
    }

    private void bind() {
        String url = "iconex://bind";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivityForResult(intent, 1000);
    }

    private void jsonRpc() {
        String url = "iconex://JSON-RPC?data=";

        BigInteger networkId = new BigInteger("2");
        Address fromAddress = new Address(SampleApp.from);
        Address toAddress = new Address("hx4873b94352c8c1f3b2f09aaeccea31ce9e90bd31");

        BigInteger value = IconAmount.of("1", IconAmount.Unit.ICX).toLoop();
        BigInteger stepLimit = new BigInteger("100000");
        long timestamp = System.currentTimeMillis() * 1000L;
        BigInteger nonce = new BigInteger("1");

        Transaction transaction = TransactionBuilder.newBuilder()
                .nid(networkId)
                .from(fromAddress)
                .to(toAddress)
                .value(value)
                .stepLimit(stepLimit)
                .timestamp(new BigInteger(Long.toString(timestamp)))
                .nonce(nonce)
                .build();

        String data = Base64.encodeToString(new Gson().toJson(transaction).getBytes(), Base64.NO_WRAP);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + data));
        startActivityForResult(intent, 2000);
    }

    private void setDeveloperMode() {
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
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES | Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);

        sendBroadcast(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Handle bind response
        if (requestCode == 1000) {
            if (resultCode < 0) {
                Uri uri = data.getData();
                JsonObject response = new Gson().fromJson(uri.toString(), JsonObject.class);
                String message = response.get("message").getAsString();

                Toast.makeText(this, "Get response:" + resultCode + " : " + message, Toast.LENGTH_SHORT).show();
            } else {
                Uri uri = data.getData();
                JsonObject response = new Gson().fromJson(uri.toString(), JsonObject.class);
                String result = response.get("result").getAsString();

                txtAddress.setText(result);
            }
        }
        // Handle JSON-RPC response
        else if (requestCode == 2000) {
            if (resultCode < 0) {
                Uri uri = data.getData();
                JsonObject response = new Gson().fromJson(uri.toString(), JsonObject.class);
                String message = response.get("message").getAsString();

                Toast.makeText(this, "Get response:" + resultCode + " : " + message, Toast.LENGTH_SHORT).show();
            } else {
                Uri uri = data.getData();
                JsonObject response = new Gson().fromJson(uri.toString(), JsonObject.class);
                String result = response.get("result").getAsString();

                txtIcxTxHash.setText(result);
            }
        }
    }
}
