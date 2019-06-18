package foundation.icon.connect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import java.util.Objects;

import foundation.icon.iconex.R;
import foundation.icon.icx.Transaction;

public class ICONexConnectActivity extends AppCompatActivity
        implements SelectWalletFragment.SelectWalletListener {
    private static final String TAG = ICONexConnectActivity.class.getSimpleName();

    private static final String CMD_BIND = "bind";
    private static final String CMD_TX = "JSON-RPC";

    private static final String PARAM_DATA = "data";

    private String transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iconex_connect);

        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            String command;
            String data = null;

            try {
                command = Objects.requireNonNull(uri).getHost();

                if (Objects.requireNonNull(command).equalsIgnoreCase(CMD_BIND)) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, SelectWalletFragment.newInstance())
                            .commitNow();
                } else if (command.equalsIgnoreCase(CMD_TX)) {
                    try {
                        data = uri.getQueryParameter(PARAM_DATA);

                        transaction = new String(Base64.decode(data, Base64.NO_WRAP));
                        Log.d(TAG, transaction);
                        Transaction tmp = new Gson().fromJson(transaction, Transaction.class);
                    } catch (NullPointerException e) {
                        setResult(-1001);
                        finish();
                    }
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
}
