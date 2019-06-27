package foundation.icon.connect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.Objects;

import foundation.icon.ICONexApp;
import foundation.icon.SplashActivity;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.data.Bytes;
import loopchain.icon.wallet.core.Constants;

public class ICONexConnectActivity extends AppCompatActivity
        implements SelectWalletFragment.SelectWalletListener, PasswordFragment.PasswordFragmentListener,
        SendTransactionFragment.SendTransactionFragmentListener {
    private static final String TAG = ICONexConnectActivity.class.getSimpleName();

    private static final String CMD_BIND = "bind";
    private static final String CMD_TX = "JSON-RPC";

    private static final String PARAM_DATA = "data";

    private Uri request;
    private String txString;
    private Wallet wallet;
    private Transaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iconex_connect);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            request = intent.getData();

            startActivityForResult(new Intent(this, SplashActivity.class)
                            .putExtra("icon_connect", true),
                    9999);
        }
    }

    private void parseRequest() {
        try {
            String requestData = request.getQueryParameter(PARAM_DATA);

            txString = new String(Base64.decode(requestData, Base64.NO_WRAP));
            JsonObject jsonObject = new Gson().fromJson(txString, JsonObject.class);
            Log.d(TAG, txString);

            JsonObject params = jsonObject.get("params").getAsJsonObject();
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();
            JsonParser parser = factory.createParser(params.toString());

            transaction = RequestDeserializer.deserialize(parser);

            checkWallet(transaction.getFrom().toString());
        } catch (NullPointerException e) {
            e.printStackTrace();
            Intent err = new Intent();
            err.setData(Uri.parse(
                    new Gson().toJson(new Response.Builder()
                            .code(-1)
                            .message("Parse error")
                            .build())));
            setResult(-1001, err);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkWallet(String address) {

        for (Wallet wallet : ICONexApp.mWallets) {
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                if (wallet.getAddress().equals(address)) {
                    this.wallet = wallet;
                    commitPasswordFragment();
                    return;
                }
            }
        }

        Intent err = new Intent();
        err.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(-3000)
                        .message("Not found wallet")
                        .build())));
        setResult(-3000, err);
        finish();
    }

    private void commitPasswordFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, PasswordFragment.newInstance(wallet))
                .commitAllowingStateLoss();
    }

    @Override
    public void onSelected(String address) {
        Intent response = new Intent();
        response.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(0)
                        .result(address)
                        .build())));
        setResult(0, response);
        finish();
    }

    @Override
    public void onSelectClose() {
        Intent err = new Intent();
        err.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(-1)
                        .message("User cancel")
                        .build())));
        setResult(-1, err);
        finish();
    }

    @Override
    public void hasNoWallet() {
        Intent err = new Intent();
        err.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(-2000)
                        .message("Has no wallet")
                        .build())));
        setResult(-2000, err);
        finish();
    }

    @Override
    public void onValidatedPassword(byte[] privateKey) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, SendTransactionFragment.newInstance(
                        txString, privateKey, wallet.getAlias()))
                .commit();
    }

    @Override
    public void onPasswordCancel() {
        Intent err = new Intent();
        err.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(-1)
                        .message("User cancel")
                        .build())));
        setResult(-1, err);
        finish();
    }

    @Override
    public void sendTransaction(Bytes txHash) {
        Intent response = new Intent();
        response.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(0)
                        .result(txHash.toHexString(true))
                        .build())));
        setResult(0, response);
        finish();
    }

    @Override
    public void parseError() {
        Intent err = new Intent();
        err.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(-1003)
                        .message("Invalid JSON-RPC syntax")
                        .build())));
        setResult(-1003, err);
        finish();
    }

    @Override
    public void onSendTxCancel() {
        Intent err = new Intent();
        err.setData(Uri.parse(
                new Gson().toJson(new Response.Builder()
                        .code(-1)
                        .message("User cancel")
                        .build())));
        setResult(-1, err);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String command;
        String requestData = null;

        try {
            command = Objects.requireNonNull(request).getHost();

            if (Objects.requireNonNull(command).equalsIgnoreCase(CMD_BIND)) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, SelectWalletFragment.newInstance())
                        .commitAllowingStateLoss();
            } else if (command.equalsIgnoreCase(CMD_TX)) {
                parseRequest();
            } else {
                Intent err = new Intent();
                err.setData(Uri.parse(
                        new Gson().toJson(new Response.Builder()
                                .code(-1000)
                                .message("Command not found")
                                .build())));
                setResult(-1000, err);
                finish();
            }
        } catch (NullPointerException e) {
            Intent err = new Intent();
            err.setData(Uri.parse(
                    new Gson().toJson(new Response.Builder()
                            .code(-1000)
                            .message("Command not found")
                            .build())));
            setResult(-1000, err);
            finish();
        }
    }
}
