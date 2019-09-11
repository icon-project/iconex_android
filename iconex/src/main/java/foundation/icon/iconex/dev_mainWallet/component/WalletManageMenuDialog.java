package foundation.icon.iconex.dev_mainWallet.component;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.MainWalletActivity;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.EditTextDialog;
import foundation.icon.iconex.menu.WalletBackUpActivity;
import foundation.icon.iconex.menu.WalletPwdChangeActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.token.manage.TokenManageActivity;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.IntroActivity;
import foundation.icon.iconex.wallet.Wallet;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class WalletManageMenuDialog extends BottomSheetDialog implements View.OnClickListener {

    private ImageButton btnClose;
    private Button btnRename;
    private Button btnManageToken;
    private Button btnBackupWallet;
    private Button btnChangeWalletPassword;
    private Button btnRemoveWallet;

    private Wallet wallet;

    public enum UpdateDataType {
        Rename, Delete
    }

    public interface OnNotifyWalletDataChangeListener {
        void onNotifyWalletDataChange(UpdateDataType updateDataType);
    }
    private OnNotifyWalletDataChangeListener mOnNotifyWalletDataChangeListener = null;

    public WalletManageMenuDialog(@NonNull Context context, Wallet wallet, OnNotifyWalletDataChangeListener listener) {
        super(context, R.style.MyBottomSheetDialog);
        this.wallet = wallet;
        mOnNotifyWalletDataChangeListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_wallet_manage_menu);

        btnClose = findViewById(R.id.btnClose);
        btnRename = findViewById(R.id.btnRename);
        btnManageToken = findViewById(R.id.btnManageToken);
        btnBackupWallet = findViewById(R.id.btnBackupWallet);
        btnChangeWalletPassword = findViewById(R.id.btnChangeWalletPassword);
        btnRemoveWallet = findViewById(R.id.btnRemoveWallet);

        btnClose.setOnClickListener(this);
        btnRename.setOnClickListener(this);
        btnManageToken.setOnClickListener(this);
        btnBackupWallet.setOnClickListener(this);
        btnChangeWalletPassword.setOnClickListener(this);
        btnRemoveWallet.setOnClickListener(this);

        btnRename.setText(wallet.getAlias());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                break;
            case R.id.btnRename: {
                EditTextDialog editTextDialog = new EditTextDialog(getContext(), getString(R.string.modWalletAlias));
                editTextDialog.setHint(getString(R.string.hintWalletAlias));
                editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.ALIAS);
                editTextDialog.setAlias(wallet.getAlias());
                editTextDialog.setOnConfirmCallback(new EditTextDialog.OnConfirmCallback() {
                    @Override
                    public void onConfirm(String text) {
                        String alias = Utils.strip(text);

                        if (alias.isEmpty()) {
                            editTextDialog.setError(getString(R.string.errWhiteSpace));
                            return;
                        }

                        if (alias.trim().length() == 0) {
                            editTextDialog.setError(getString(R.string.errWhiteSpace));
                            return;
                        }

                        for (Wallet info : ICONexApp.wallets) {
                            if (info.getAlias().equals(alias)) {
                                editTextDialog.setError(getString(R.string.duplicateWalletAlias));
                                return;
                            }
                        }

                        RealmUtil.modWalletAlias(wallet.getAddress(), alias);
                        wallet.setAlias(alias);
                        mOnNotifyWalletDataChangeListener.onNotifyWalletDataChange(UpdateDataType.Rename);
                        editTextDialog.dismiss();
                    }
                });
                editTextDialog.show();
            } break;
            case R.id.btnManageToken: {
                Intent intent = new Intent(getContext(), TokenManageActivity.class);
                intent.putExtra("walletInfo", (Serializable) wallet);

                if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
                    intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.IRC);
                else
                    intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.ERC);

                getContext().startActivity(intent);
            } break;
            case R.id.btnBackupWallet:
                EditTextDialog editTextDialog = new EditTextDialog(getContext(), getString(R.string.enterWalletPassword));
                editTextDialog.setHint(getString(R.string.hintWalletPassword));
                editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.BACKUP);
                editTextDialog.setOnPasswordCallback(new EditTextDialog.OnPasswordCallback() {
                    @Override
                    public void onConfirm(EditTextDialog.RESULT_PWD result, String pwd) {
                        JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);
                        byte[] bytePrivKey;
                        try {
                            JsonObject crypto = null;
                            if (keyStore.has("crypto"))
                                crypto = keyStore.get("crypto").getAsJsonObject();
                            else
                                crypto = keyStore.get("Crypto").getAsJsonObject();

                            bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, wallet.getAddress(), crypto, wallet.getCoinType());
                            if (bytePrivKey != null) {
                                getContext().startActivity(new Intent(getContext(), WalletBackUpActivity.class)
                                        .putExtra("walletInfo", (Serializable) wallet)
                                        .putExtra("privateKey", Hex.toHexString(bytePrivKey)));

                                editTextDialog.dismiss();
                            } else {
                                editTextDialog.setError(getString(R.string.errPassword));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                editTextDialog.show();
                break;
            case R.id.btnChangeWalletPassword: {
                getContext().startActivity(new Intent(getContext(), WalletPwdChangeActivity.class)
                        .putExtra("walletInfo", (Serializable) wallet));
            } break;
            case R.id.btnRemoveWallet: {
                final Basic2ButtonDialog dialog = new Basic2ButtonDialog(getContext());
                dialog.setMessage(getString(R.string.warningRemoveWallet));
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        EditTextDialog editTextDialog = new EditTextDialog(getContext(), getString(R.string.enterWalletPassword));
                        editTextDialog.setHint(getString(R.string.hintWalletPassword));
                        editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                        editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.REMOVE);
                        editTextDialog.setOnPasswordCallback(new EditTextDialog.OnPasswordCallback() {
                            @Override
                            public void onConfirm(EditTextDialog.RESULT_PWD result, String pwd) {
                                JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);
                                byte[] bytePrivKey;
                                try {
                                    JsonObject crypto = null;
                                    if (keyStore.has("crypto"))
                                        crypto = keyStore.get("crypto").getAsJsonObject();
                                    else
                                        crypto = keyStore.get("Crypto").getAsJsonObject();

                                    bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, wallet.getAddress(), crypto, wallet.getCoinType());
                                    if (bytePrivKey != null) {

                                        RealmUtil.removeWallet(wallet.getAddress());
                                        try {
                                            RealmUtil.loadWallet();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        if (ICONexApp.wallets.size() == 0) {
                                            getContext().startActivity(new Intent(getContext(), IntroActivity.class)
                                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                        } else {
                                            mOnNotifyWalletDataChangeListener.onNotifyWalletDataChange(UpdateDataType.Delete);
                                        }

                                        editTextDialog.dismiss();
                                    } else {
                                        editTextDialog.setError(getString(R.string.errPassword));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        editTextDialog.show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.show();
            } break;
        }
        this.dismiss();
    }

    private String getString(int resString) {
        return getContext().getString(resString);
    }
}
