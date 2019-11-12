package foundation.icon.iconex.menu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.CustomToast;
import foundation.icon.iconex.widgets.TTextInputLayout;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class WalletPwdChangeActivityNew extends AppCompatActivity {
    private static final String TAG = WalletPwdChangeActivityNew.class.getSimpleName();

    private Wallet mWallet;
    private byte[] mPrivateKey;
    private String beforePwd;
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
        editOldPwd.setPastable(false);
        editPwd.setPastable(false);
        editCheck.setPastable(false);

        // ================= init current password
        editOldPwd.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateAll(true, false, false);
            }
        });
        editOldPwd.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                validateAll(true, false, false);
            }
        });
        editOldPwd.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                Log.d(TAG, "onChanged() called with: s = [" + s.length() + "]");
                if (s.length() == 0) validateAll(false, false, false);
            }
        });
        editOldPwd.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                validateAll(true, false, false);
            }
        });

        // ==================== init new password
        editPwd.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateAll(false, true, false);
            }
        });
        editPwd.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                validateAll(false, true, false);
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
                    validateAll(false, false, false);
                }
            }
        });
        editPwd.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                validateAll(false, true, false);
            }
        });

        // ====================== init check password
        editCheck.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateAll(false, false, true);
            }
        });
        editCheck.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                validateAll(false, false, true);
            }
        });
        editCheck.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() == 0) validateAll(false, false, false);
            }
        });
        editCheck.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                validateAll(false, false, true);
            }
        });

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
                        CustomToast.makeText(WalletPwdChangeActivityNew.this, getString(R.string.doneChangeWalletPwd), Toast.LENGTH_SHORT).show();
                        setResult(RESULT_CODE, new Intent().putExtra("result", (Serializable) mWallet));
                        finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        editOldPwd.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editOldPwd.getEditView(), InputMethodManager.SHOW_FORCED);
            }
        }, 100);
    }

    private boolean validateOldPwd(boolean showErr) {
        String pwd = editOldPwd.getText();
        Log.d(TAG, "validateOldPwd() called with: showErr = [" + pwd + "]");

        if (pwd.isEmpty()) {
            editOldPwd.setError(false, null);
            return false;
        }

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
            if (showErr) editOldPwd.setError(true, getString(R.string.errPassword));
            return false;
        } else {
            mPrivateKey = privKey;
            editOldPwd.setError(false, null);
            return true;
        }
    }

    private boolean validateNewPwd(boolean showErr) {
        String pwd = editPwd.getText();
        int pwdResult = PasswordValidator.validatePassword(pwd);
        switch (pwdResult) {
            case PasswordValidator.EMPTY:
                // if (showErr) editPwd.setError(true, getString(R.string.errPwdEmpty));
//                editPwd.setError(false, null);
                return false;

            case PasswordValidator.LEAST_8:
                if (showErr) editPwd.setError(true, getString(R.string.errAtLeast));
                return false;

            case PasswordValidator.NOT_MATCH_PATTERN:
                if (showErr) editPwd.setError(true, getString(R.string.errPasswordPatternMatch));
                return false;

            case PasswordValidator.HAS_WHITE_SPACE:
                if (showErr) editPwd.setError(true, getString(R.string.errWhiteSpace));
                return false;

            case PasswordValidator.SERIAL_CHAR:
                if (showErr) editPwd.setError(true, getString(R.string.errSerialChar));
                return false;

            case PasswordValidator.ILLEGAL_CHAR:
                editPwd.setError(true, getString(R.string.errAllowSpecialCharacters));
                return false;

            default:
                // if new password == old password
                if (editOldPwd.getText().equals(editPwd.getText())) {
                    if (showErr) editPwd.setError(true, getString(R.string.msgNewPasswordSame));
                    return false;
                } else {
                    editPwd.setError(false, null);
                    return true;
                }
        }
    }

    private boolean validateCheckPwd(boolean errCheckPwd) {
        String pwd = editPwd.getText();
        String checkPwd = editCheck.getText();

        if (checkPwd.isEmpty()) {
            // editCheck.setError(false, null);
            editCheck.setError(false, null);
            return false;
        }

        boolean checkResult = PasswordValidator.checkPasswordMatch(pwd, checkPwd);
        if (!checkResult) {
            if (errCheckPwd) editCheck.setError(true, getString(R.string.errPasswordNotMatched));
            return false;
        } else {
            editCheck.setError(false, null);
            return true;
        }
    }

    private void validateAll(boolean showErrOldPwd, boolean showErrNewPwd, boolean showErrCheckPwd) {
        boolean oldPwd = validateOldPwd(showErrOldPwd);
        boolean newPwd = validateNewPwd(showErrNewPwd);
        boolean checkPwd = validateCheckPwd(showErrCheckPwd);

        btnChange.setEnabled(oldPwd && newPwd && checkPwd);
    }
}
