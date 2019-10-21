package foundation.icon.iconex.menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.TTextInputLayout;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class WalletPwdChangeActivityNew extends AppCompatActivity {
    private static final String TAG = WalletPwdChangeActivityNew.class.getSimpleName();

    private Wallet mWallet;
    private byte[] mPrivateKey;
    private String beforePwd, beforeCheck;
    public static final int RESULT_CODE = 3333;

    // ui
    private CustomActionBar appbar;
    private TTextInputLayout editOldPwd, editPwd, editCheck;
    private Button btnChange;

    public interface OnResultListener {
        void onResult(Wallet wallet);
    }
    public static boolean getActivityResult(int result, Intent intent, OnResultListener listener) {
        if (result == RESULT_CODE) {
            Wallet wallet = (Wallet) intent.getSerializableExtra("result");
            listener.onResult(wallet);
            return true;
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_pwd_change_new);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        if (getIntent() != null) {
            mWallet = (Wallet) getIntent().getExtras().get("walletInfo");
        }

        // load ui
        appbar = findViewById(R.id.appbar);
        editOldPwd = findViewById(R.id.edit_old_pwd);
        editPwd = findViewById(R.id.edit_pwd);
        editCheck = findViewById(R.id.edit_check);
        btnChange = findViewById(R.id.btn_change);

        // init appbar
        appbar.setOnClickStartIcon(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // ---- input  layout event ----
        TTextInputLayout.OnKeyPreIme onKeyPreIme = new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                setChangeEnable(editOldPwd.getText(), editPwd.getText(), editCheck.getText());
            }
        };

        TTextInputLayout.OnEditorAction onEditorAction = new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                setChangeEnable(editOldPwd.getText(), editPwd.getText(), editCheck.getText());
            }
        };

        editOldPwd.setPastable(false);
        editPwd.setPastable(false);
        editCheck.setPastable(false);

        // ================= init current password
        editOldPwd.setOnKeyPreImeListener(onKeyPreIme);
        editOldPwd.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (!editOldPwd.getText().isEmpty()) {
                    boolean result = validateCurrentPwd(editOldPwd.getText());
                    if (!result) {
                        editOldPwd.setError(true, getString(R.string.errPassword));
                    } else {
                        editOldPwd.setError(false, null);
                    }
                }
            }
        });
        editOldPwd.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() <= 0) {
                    editOldPwd.setError(false, null);
                    btnChange.setEnabled(false);
                }
            }
        });
        editOldPwd.setOnEditorActionListener(onEditorAction);

        // ==================== init new password
        editPwd.setOnKeyPreImeListener(onKeyPreIme);
        editPwd.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                int result = PasswordValidator.validatePassword(editPwd.getText().toString());
                switch (result) {
                    case PasswordValidator.LEAST_8:
                        editPwd.setError(true, getString(R.string.errAtLeast));
                        break;

                    case PasswordValidator.NOT_MATCH_PATTERN:
                        editPwd.setError(true, getString(R.string.errPasswordPatternMatch));
                        break;

                    case PasswordValidator.HAS_WHITE_SPACE:
                        editPwd.setError(true, getString(R.string.errWhiteSpace));
                        break;

                    case PasswordValidator.SERIAL_CHAR:
                        editPwd.setError(true, getString(R.string.errSerialChar));
                        break;

                    default:
                        // if new password == old password
                        if (editOldPwd.getText().equals(editPwd.getText())) {
                            editPwd.setError(true, getString(R.string.msgNewPasswordSame));
                        } else {
                            editPwd.setError(false, null);
                        }
                        break;
                }

            }
        });
        editPwd.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.charAt(s.length() - 1) == ' ') {
                        editPwd.setText(s.subSequence(0, s.length() - 1).toString());
                        if (editPwd.getText().toString().length() > 0)
                            editPwd.setSelection(editPwd.getText().toString().length());
                    } else if (s.toString().contains(" ")) {
                        editPwd.setText(beforePwd);
                        editPwd.setSelection(beforePwd.length());
                    } else
                        beforePwd = s.toString();
                } else {
                    editPwd.setError(false, null);
                    btnChange.setEnabled(false);
                }
            }
        });
        editPwd.setOnEditorActionListener(onEditorAction);

        // ====================== init check password
        editCheck.setOnKeyPreImeListener(onKeyPreIme);
        editCheck.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (!editCheck.getText().toString().isEmpty()) {
                    if (editPwd.getText().toString().isEmpty()) {
                        editCheck.setError(true, getString(R.string.errPasswordNotMatched));
                    } else {
                        boolean result = PasswordValidator.checkPasswordMatch(editPwd.getText().toString(), editCheck.getText().toString());
                        if (!result) {
                            editCheck.setError(true, getString(R.string.errPasswordNotMatched));
                        } else {
                            editCheck.setError(false, null);
                        }
                    }
                }
            }
        });
        editCheck.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.charAt(s.length() - 1) == ' ') {
                        editCheck.setText(s.subSequence(0, s.length() - 1).toString());
                        if (editCheck.getText().toString().length() > 0)
                            editCheck.setSelection(editCheck.getText().toString().length());
                    } else if (s.toString().contains(" ")) {
                        editCheck.setText(beforeCheck);
                        editCheck.setSelection(beforeCheck.length());
                    } else
                        beforeCheck = s.toString();
                } else {
                    editCheck.setError(false, null);
                    btnChange.setEnabled(false);
                }
            }
        });
        editCheck.setOnEditorActionListener(onEditorAction);

        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = mWallet.getAddress();
                JsonObject oldKeyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);
                String id = oldKeyStore.get(("id")).getAsString();
                String newKeyStore = KeyStoreUtils.changePassword(mWallet.getCoinType(), id, address, mPrivateKey, editPwd.getText());
                if (newKeyStore != null) {
                    try {
                        RealmUtil.modWalletPassword(mWallet.getAddress(), newKeyStore);
                        mWallet.setKeyStore(newKeyStore);

                        MessageDialog messageDialog = new MessageDialog(WalletPwdChangeActivityNew.this);
                        messageDialog.setTitleText(getString(R.string.doneChangeWalletPwd));
                        messageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                setResult(RESULT_CODE, new Intent().putExtra("result", (Serializable) mWallet));
                                finish();
                            }
                        });
                        messageDialog.show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private boolean validateCurrentPwd(String pwd) {
        JsonObject keyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);

        JsonObject crypto;
        if (keyStore.has("crypto"))
            crypto = keyStore.get("crypto").getAsJsonObject();
        else
            crypto = keyStore.get("Crypto").getAsJsonObject();

        byte[] privKey = null;
        try {
            privKey = KeyStoreUtils.decryptPrivateKey(pwd, mWallet.getAddress(), crypto, mWallet.getCoinType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (privKey == null) {
            return false;
        } else {
            mPrivateKey = privKey;
            return true;
        }
    }

    private void setChangeEnable(String old, String pwd, String checkPwd) {
        boolean curResult = validateCurrentPwd(old);
        if (curResult) {
            editOldPwd.setError(false, null);
        } else {
            editOldPwd.setError(true, getString(R.string.errPassword));
        }

        int pwdResult = PasswordValidator.validatePassword(pwd);
        switch (pwdResult) {
            case PasswordValidator.EMPTY:
                editPwd.setError(true, getString(R.string.errPwdEmpty));
                break;

            case PasswordValidator.LEAST_8:
                editPwd.setError(true, getString(R.string.errAtLeast));
                break;

            case PasswordValidator.NOT_MATCH_PATTERN:
                editPwd.setError(true, getString(R.string.errPasswordPatternMatch));
                break;

            case PasswordValidator.HAS_WHITE_SPACE:
                editPwd.setError(true, getString(R.string.errWhiteSpace));
                break;

            case PasswordValidator.SERIAL_CHAR:
                editPwd.setError(true, getString(R.string.errSerialChar));
                break;

            default:
                // if new password == old password
                if (editOldPwd.getText().equals(editPwd.getText())) {
                    editPwd.setError(true, getString(R.string.msgNewPasswordSame));
                } else {
                    editPwd.setError(false, null);
                }
        }

        boolean checkResult = PasswordValidator.checkPasswordMatch(pwd, checkPwd);
        if (!checkResult) {
            editCheck.setError(true, getString(R.string.errPasswordNotMatched));
        } else {
            editCheck.setError(false, null);
        }

        if (curResult && checkResult && (pwdResult == PasswordValidator.OK) && !editOldPwd.getText().equals(editPwd.getText()))
            btnChange.setEnabled(true);
        else
            btnChange.setEnabled(false);
    }
}
