package foundation.icon.iconex.menu;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.util.KeyStoreIO;
import foundation.icon.iconex.view.ui.wallet.ViewWalletInfoActivity;
import foundation.icon.iconex.wallet.Wallet;
import loopchain.icon.wallet.core.Constants;

public class WalletBackUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = WalletBackUpActivity.class.getSimpleName();

    private Button btnClose, btnDown, btnCopy, btnView;
    private TextView txtPrivKey;
    private Button btnVisibility;

    private Wallet mWallet;
    private String mPrivKey;

    private final int STORAGE_PERMISSION_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_wallet_back_up);

        if (getIntent() != null) {
            mWallet = (Wallet) getIntent().getSerializableExtra("walletInfo");
            mPrivKey = getIntent().getStringExtra("privateKey");
        }

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleBackup));

        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);

        btnDown = findViewById(R.id.btn_download);
        btnDown.setOnClickListener(this);

        btnCopy = findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(this);
        btnView = findViewById(R.id.btn_view_info);
        btnView.setOnClickListener(this);

        txtPrivKey = findViewById(R.id.txt_private_key);
        txtPrivKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        txtPrivKey.setText(mPrivKey);
        btnVisibility = findViewById(R.id.btn_eye);
        btnVisibility.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;

            case R.id.btn_download:
                checkPermission();
                break;

            case R.id.btn_copy:
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("privateKey", mPrivKey);
                clipboard.setPrimaryClip(data);
                Toast.makeText(this, getString(R.string.msgCopyPrivateKey), Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_eye:
                if (btnVisibility.isSelected()) {
                    btnVisibility.setSelected(false);
                    txtPrivKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                } else {
                    btnVisibility.setSelected(true);
                    txtPrivKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                }
                break;

            case R.id.btn_view_info:
                startActivity(new Intent(this, ViewWalletInfoActivity.class)
                        .putExtra("wallet", ((Serializable) mWallet))
                        .putExtra("privateKey", mPrivKey));

                break;
        }
    }

    private boolean downloadKeystore() {
        JsonObject keyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);
        try {
            KeyStoreIO.exportKeyStore(keyStore, mWallet.getCoinType());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void checkPermission() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm));
            dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                @Override
                public void onOk() {
                    boolean result = downloadKeystore();
                    if (result) {
                        BasicDialog dialog = new BasicDialog(WalletBackUpActivity.this);
                        dialog.setMessage(String.format(getString(R.string.keyStoreDownloadAccomplished), KeyStoreIO.DIR_PATH));
                        dialog.show();
                    }
                }

                @Override
                public void onCancel() {

                }
            });
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Basic2ButtonDialog dialog = new Basic2ButtonDialog(WalletBackUpActivity.this);
                    dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm));
                    dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                        @Override
                        public void onOk() {
                            boolean result = downloadKeystore();
                            if (result) {
                                BasicDialog dialog = new BasicDialog(WalletBackUpActivity.this);
                                dialog.setMessage(String.format(getString(R.string.keyStoreDownloadAccomplished), KeyStoreIO.DIR_PATH));
                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    dialog.show();

                } else {
                    BasicDialog dialog = new BasicDialog(WalletBackUpActivity.this);
                    dialog.setMessage(getString(R.string.permissionStorageDenied));
                    dialog.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
