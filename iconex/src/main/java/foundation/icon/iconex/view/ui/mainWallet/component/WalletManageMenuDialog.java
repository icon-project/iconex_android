package foundation.icon.iconex.view.ui.mainWallet.component;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.EditText2Dialog;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.menu.WalletBackupActivityNew;
import foundation.icon.iconex.menu.WalletPwdChangeActivityNew;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.token.manage.TokenManageActivity;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.IntroActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import io.realm.Realm;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;

public class WalletManageMenuDialog extends BottomSheetDialog implements View.OnClickListener {
    private static final String TAG = WalletManageMenuDialog.class.getSimpleName();

    private ImageButton btnClose;
    private Button btnRename;
    private Button btnManageToken;
    private Button btnBackupWallet;
    private Button btnChangeWalletPassword;
    private Button btnRemoveWallet;

    private Wallet wallet;

    private Activity activity;

    public enum UpdateDataType {
        Rename, Delete
    }

    public static final int REQ_PASSWORD_CHANGE = 14930;
    public static final int REQ_UPDATE_TOKEN = 23097;

    public interface OnNotifyWalletDataChangeListener {
        void onNotifyWalletDataChange(UpdateDataType updateDataType);
    }
    private OnNotifyWalletDataChangeListener mOnNotifyWalletDataChangeListener = null;

    public WalletManageMenuDialog(@NonNull Activity activity, Wallet wallet, OnNotifyWalletDataChangeListener listener) {
        super(activity, R.style.MyBottomSheetDialog);
        this.activity = activity;
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
                EditText2Dialog editText2Dialog = new EditText2Dialog(getContext(), getString(R.string.modWalletAlias));
                editText2Dialog.setHint(getString(R.string.hintWalletAlias));
                editText2Dialog.setPastable(false);
                editText2Dialog.setText(wallet.getAlias());
                final String[] beforeStr = {""};
                editText2Dialog.setOnTextChangedListener(new EditText2Dialog.OnTextChangedListener() {
                    @Override
                    public boolean onChangeText(String s) {
                        if (s.isEmpty()) {
                            return false;
                        } if (s.trim().isEmpty()) {
                            editText2Dialog.setText("");
                            return false;
                        } else if (s.charAt(0) == ' ') {
                            editText2Dialog.setText(beforeStr[0]);
                            editText2Dialog.setSelection(beforeStr[0].length());
                        } else {
                            if (Utils.checkByteLength(s) > 16) {
                                editText2Dialog.setText(beforeStr[0]);
                                editText2Dialog.setSelection(editText2Dialog.getText().length());
                            } else {
                                beforeStr[0] = s;
                            }
                        }
                        return true;
                    }
                });
                editText2Dialog.setOnConfirm(new EditText2Dialog.OnConfirmListener() {
                    @Override
                    public boolean onConfirm(String text) {
                        String alias = Utils.strip(text);

                        if (alias.isEmpty()) {
                            editText2Dialog.setError(getString(R.string.errWhiteSpace));
                            return false;
                        }

                        if (alias.trim().length() == 0) {
                            editText2Dialog.setError(getString(R.string.errWhiteSpace));
                            return false;
                        }

                        for (Wallet info : ICONexApp.wallets) {
                            if (info.getAlias().equals(alias)) {
                                editText2Dialog.setError(getString(R.string.duplicateWalletAlias));
                                return false;
                            }
                        }

                        alias = alias.trim();
                        RealmUtil.modWalletAlias(wallet.getAddress(), alias);
                        wallet.setAlias(alias);
                        for (int i = 0; ICONexApp.wallets.size() > i; i++) {
                            Wallet iWallet = ICONexApp.wallets.get(i);
                            if (iWallet.getAddress().equals(wallet.getAddress())) {
                                iWallet.setAlias(alias);
                                break;
                            }
                        }
                        mOnNotifyWalletDataChangeListener.onNotifyWalletDataChange(UpdateDataType.Rename);
                        return true;
                    }
                });
                editText2Dialog.show();
            } break;
            case R.id.btnManageToken: {
                Intent intent = new Intent(getContext(), TokenManageActivity.class);
                intent.putExtra("walletInfo", (Serializable) wallet);

                if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
                    intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.IRC);
                else
                    intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.ERC);

                activity.startActivityForResult(intent, REQ_UPDATE_TOKEN);
            } break;
            case R.id.btnBackupWallet:
                new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                    @Override
                    public void onPass(byte[] bytePrivateKey) {
                        getContext().startActivity(new Intent(getContext(), WalletBackupActivityNew.class)
                                .putExtra("walletInfo", (Serializable) wallet)
                                .putExtra("privateKey", Hex.toHexString(bytePrivateKey)));
                    }
                }).show();
                break;
            case R.id.btnChangeWalletPassword: {
                activity.startActivityForResult(
                        new Intent(getContext(), WalletPwdChangeActivityNew.class)
                        .putExtra("walletInfo", (Serializable) wallet),REQ_PASSWORD_CHANGE);
            } break;
            case R.id.btnRemoveWallet: {
                boolean isRemain = false;
                for (WalletEntry entry : wallet.getWalletEntries()) {
                    try {
                        if (new BigInteger(entry.getBalance())
                                .add(wallet.getStaked()
                                        .add(wallet.getUnstake())
                                .add(wallet.getiScore())).compareTo(BigInteger.ZERO) != 0)
                            isRemain = true;
                    } catch (Exception e) { }
                }

                final boolean fIsRemain = isRemain;
                MessageDialog messageDialog = new MessageDialog(getContext());
                messageDialog.setMessage(getString(isRemain ? R.string.warningRemoveWallet : R.string.removeWallet));
                messageDialog.setSingleButton(false);
                messageDialog.setConfirmButtonText(getString(R.string.yes));
                messageDialog.setCancelButtonText(getString(R.string.no));
                messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        if (fIsRemain) {
                            new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                                @Override
                                public void onPass(byte[] bytePrivateKey) {
                                    removeWallet();
                                }
                            }).show();
                        } else {
                            removeWallet();
                        }
                        return true;
                    }
                });
                messageDialog.show();
            } break;
        }
        this.dismiss();
    }

    private void removeWallet() {
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
    }

    private String getString(int resString) {
        return getContext().getString(resString);
    }
}
