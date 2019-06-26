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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;

import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;

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
        Address toAddress = new Address(SampleApp.to);
        BigInteger value = IconAmount.of("1", 18).toLoop();
        long timestamp = System.currentTimeMillis() * 1000L;
        BigInteger nonce = new BigInteger("1");
        String methodName = "transfer";

        RpcObject params = new RpcObject.Builder()
                .put("_to", new RpcValue(toAddress))
                .put("_value", new RpcValue(value))
                .build();

        // make a raw transaction without the stepLimit
        Transaction transaction = TransactionBuilder.newBuilder()
                .nid(networkId)
                .from(fromAddress)
                .to(new Address(SampleApp.score))
                .timestamp(new BigInteger(Long.toString(timestamp)))
                .nonce(nonce)
                .call(methodName)
                .params(params)
                .build();

        RpcObject object = getTransactionProperties(transaction);

        RpcObject.Builder builder = new RpcObject.Builder();
        for (String key : object.keySet()) {
            builder.put(key, object.getItem(key));
        }

        foundation.icon.icx.transport.jsonrpc.Request request = new foundation.icon.icx.transport.jsonrpc.Request(
                1234, "icx_sendTransaction", builder.build());

        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(foundation.icon.icx.transport.jsonrpc.Request.class, new RequestSerializer());
        mapper.registerModule(module);
        try {
            String jsonObject = mapper.writeValueAsString(request);
            String base64Encoded = Base64.encodeToString(jsonObject.getBytes(), Base64.NO_WRAP);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url + base64Encoded));
            startActivityForResult(intent, 2000);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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

    private RpcObject getTransactionProperties(Transaction transaction) {
        BigInteger timestamp = transaction.getTimestamp();
        if (timestamp == null) {
            timestamp = new BigInteger(Long.toString(System.currentTimeMillis() * 1000L));
        }

        RpcObject.Builder builder = new RpcObject.Builder();
        putTransactionPropertyToBuilder(builder, "version", transaction.getVersion());
        putTransactionPropertyToBuilder(builder, "from", transaction.getFrom());
        putTransactionPropertyToBuilder(builder, "to", transaction.getTo());
        putTransactionPropertyToBuilder(builder, "value", transaction.getValue());
        putTransactionPropertyToBuilder(builder, "timestamp", timestamp);
        putTransactionPropertyToBuilder(builder, "nid", transaction.getNid());
        putTransactionPropertyToBuilder(builder, "nonce", transaction.getNonce());
        putTransactionPropertyToBuilder(builder, "dataType", transaction.getDataType());
        putTransactionPropertyToBuilder(builder, "data", transaction.getData());
        return builder.build();
    }

    private void putTransactionPropertyToBuilder(RpcObject.Builder builder, String key, BigInteger value) {
        if (value != null) builder.put(key, new RpcValue(value));
    }

    private void putTransactionPropertyToBuilder(RpcObject.Builder builder, String key, String value) {
        if (value != null) builder.put(key, new RpcValue(value));
    }

    private void putTransactionPropertyToBuilder(RpcObject.Builder builder, String key, Address value) {
        if (value != null) builder.put(key, new RpcValue(value));
    }

    private void putTransactionPropertyToBuilder(RpcObject.Builder builder, String key, RpcItem item) {
        if (item != null) builder.put(key, item);
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

                SampleApp.from = result;
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
                if (data != null) {
                    Uri uri = data.getData();
                    JsonObject response = new Gson().fromJson(uri.toString(), JsonObject.class);
                    String result = response.get("result").getAsString();

                    txtIcxTxHash.setText(result);
                }
            }
        }
    }
}
