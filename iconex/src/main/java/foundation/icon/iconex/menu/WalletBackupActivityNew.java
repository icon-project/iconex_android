package foundation.icon.iconex.menu;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.util.KeyStoreIO;
import foundation.icon.iconex.view.ui.wallet.ViewWalletInfoActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.TTextInputLayout;
import loopchain.icon.wallet.core.Constants;

public class WalletBackupActivityNew extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = WalletBackUpActivity.class.getSimpleName();

    private Wallet mWallet;
    private String mPrivKey;

    private final int STORAGE_PERMISSION_REQUEST = 10001;

    private CustomActionBar appbar;
    private TTextInputLayout inputPrivateKey;
    private Button btnBackup;
    private Button btnCopy;
    private Button btnView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_wallet_backup_new);

        // get intente
        if (getIntent() != null) {
            mWallet = (Wallet) getIntent().getSerializableExtra("walletInfo");
            mPrivKey = getIntent().getStringExtra("privateKey");
        }

        // load ui
        appbar = findViewById(R.id.appbar);
        inputPrivateKey = findViewById(R.id.input_private_key);
        btnBackup = findViewById(R.id.btn_back_up);
        btnCopy = findViewById(R.id.btn_copy);
        btnView = findViewById(R.id.btn_view_info);

        // init appbar
        appbar.setOnClickStartIcon(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // init input private key
        inputPrivateKey.setText(mPrivKey);

        // init backup, copy, view
        btnBackup.setOnClickListener(this);
        btnCopy.setOnClickListener(this);
        btnView.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back_up: {
                checkPermission();
            } break;
            case R.id.btn_copy: {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("privateKey", mPrivKey);
                clipboard.setPrimaryClip(data);
                Toast.makeText(this, getString(R.string.msgCopyPrivateKey), Toast.LENGTH_SHORT).show();
            } break;
            case R.id.btn_view_info: {
                String coinName;
                String format = "%1$s(%2$s)";
                if (mWallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
                    coinName = String.format(format, MyConstants.NAME_ICX, mWallet.getCoinType());
                else
                    coinName = String.format(format, MyConstants.NAME_ETH, mWallet.getCoinType());

                startActivity(new Intent(this, ViewWalletInfoActivity.class)
                        .putExtra("alias", mWallet.getAlias())
                        .putExtra("coinName", coinName)
                        .putExtra("address", mWallet.getAddress())
                        .putExtra("privateKey", mPrivKey)
                        .putExtra("date", mWallet.getCreatedAt()));
            } break;
        }
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
                        BasicDialog dialog = new BasicDialog(WalletBackupActivityNew.this);
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

                    Basic2ButtonDialog dialog = new Basic2ButtonDialog(WalletBackupActivityNew.this);
                    dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm));
                    dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                        @Override
                        public void onOk() {
                            boolean result = downloadKeystore();
                            if (result) {
                                BasicDialog dialog = new BasicDialog(WalletBackupActivityNew.this);
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
                    BasicDialog dialog = new BasicDialog(WalletBackupActivityNew.this);
                    dialog.setMessage(getString(R.string.permissionStorageDenied));
                    dialog.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
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
}
