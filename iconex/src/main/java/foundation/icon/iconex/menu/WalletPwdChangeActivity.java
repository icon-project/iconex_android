package foundation.icon.iconex.menu;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.MyEditText;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class WalletPwdChangeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = WalletPwdChangeActivity.class.getSimpleName();

    private Wallet mWallet;

    private Button btnClose;
    private MyEditText editOldPwd, editPwd, editCheck;
    private View lineOld, linePwd, lineCheck;
    private TextView txtOldWarning, txtPwdWarning, txtCheckWarnig;
    private Button btnOldDel, btnPwdDel, btnCheckDel;
    private Button btnChange;

    private ViewGroup appbar;
    private ViewGroup headerView;
    private ViewGroup inputArea;

    private InputMethodManager mImm;
    private byte[] mPrivateKey;

    private String beforePwd, beforeCheck;

    public static final int RESULT_CODE = 3333;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_pwd_change);

        if (getIntent() != null) {
            mWallet = (Wallet) getIntent().getExtras().get("walletInfo");
        }

        appbar = findViewById(R.id.appbar);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) ((TextView) findViewById(R.id.txt_title)).getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        ((TextView) findViewById(R.id.txt_title)).setLayoutParams(layoutParams);
        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleChangeWalletPwd));

        headerView = findViewById(R.id.layout_header);
        inputArea = findViewById(R.id.layout_input);

        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);

        editOldPwd = findViewById(R.id.edit_old_pwd);
        editOldPwd.setOnKeyPreImeListener(mOnKeyPreImeListener);
        editOldPwd.setOnEditTouchListener(new MyEditText.OnEditTouchListener() {
            @Override
            public void onTouch() {
                showInputMode(editOldPwd);
            }
        });
        editOldPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineOld.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    // TODO: 2018. 3. 22. check password
                } else {
                    lineOld.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    if (editOldPwd.getText().toString().isEmpty()) {
                        showWarning(lineOld, txtOldWarning, getString(R.string.errPwdEmpty));
                    } else {
                        boolean result = validateCurrentPwd(editOldPwd.getText().toString());
                        if (!result) {
                            showWarning(lineOld, txtOldWarning, getString(R.string.errPassword));
                        } else {
                            hideWarning(editOldPwd, lineOld, txtOldWarning);
                        }
                    }
                }
            }
        });
        editOldPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnOldDel.setVisibility(View.VISIBLE);
                } else {
                    btnOldDel.setVisibility(View.INVISIBLE);
                    hideWarning(editOldPwd, lineOld, txtOldWarning);

                    btnChange.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editOldPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideInputMode();
                    setChangeEnable(editOldPwd.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        editPwd = findViewById(R.id.edit_pwd);
        editPwd.setOnKeyPreImeListener(mOnKeyPreImeListener);
        editPwd.setOnEditTouchListener(new MyEditText.OnEditTouchListener() {
            @Override
            public void onTouch() {
                showInputMode(editPwd);
            }
        });
        editPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    int result = PasswordValidator.validatePassword(editPwd.getText().toString());
                    switch (result) {
                        case PasswordValidator.EMPTY:
                            showWarning(linePwd, txtPwdWarning, getString(R.string.errPwdEmpty));
                            break;

                        case PasswordValidator.LEAST_8:
                            showWarning(linePwd, txtPwdWarning, getString(R.string.errAtLeast));
                            break;

                        case PasswordValidator.NOT_MATCH_PATTERN:
                            showWarning(linePwd, txtPwdWarning, getString(R.string.errPasswordPatternMatch));
                            break;

                        case PasswordValidator.HAS_WHITE_SPACE:
                            showWarning(linePwd, txtPwdWarning, getString(R.string.errWhiteSpace));
                            break;

                        case PasswordValidator.SERIAL_CHAR:
                            showWarning(linePwd, txtPwdWarning, getString(R.string.errSerialChar));
                            break;

                        default:
                            hideWarning(editPwd, linePwd, txtPwdWarning);
                    }
                }
            }
        });
        editPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnPwdDel.setVisibility(View.VISIBLE);
                    if (s.charAt(s.length() - 1) == ' ') {
                        editPwd.setText(s.subSequence(0, s.length() - 1));
                        if (editPwd.getText().toString().length() > 0)
                            editPwd.setSelection(editPwd.getText().toString().length());
                    } else if (s.toString().contains(" ")) {
                        editPwd.setText(beforePwd);
                        editPwd.setSelection(beforePwd.length());
                    } else
                        beforePwd = s.toString();
                } else {
                    btnPwdDel.setVisibility(View.INVISIBLE);
                    hideWarning(editPwd, linePwd, txtPwdWarning);

                    btnChange.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideInputMode();
                    setChangeEnable(editOldPwd.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        editCheck = findViewById(R.id.edit_check);
        editCheck.setOnKeyPreImeListener(mOnKeyPreImeListener);
        editCheck.setOnEditTouchListener(new MyEditText.OnEditTouchListener() {
            @Override
            public void onTouch() {
                showInputMode(editCheck);
            }
        });
        editCheck.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineCheck.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineCheck.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    if (editCheck.getText().toString().isEmpty()) {
                        showWarning(lineCheck, txtCheckWarnig, getString(R.string.errWhiteSpace));
                    } else {
                        if (editPwd.getText().toString().isEmpty()) {
                            showWarning(lineCheck, txtCheckWarnig, getString(R.string.errPasswordNotMatched));
                        } else {
                            boolean result = PasswordValidator.checkPasswordMatch(editPwd.getText().toString(), editCheck.getText().toString());
                            if (!result) {
                                showWarning(lineCheck, txtCheckWarnig, getString(R.string.errPasswordNotMatched));
                            } else {
                                hideWarning(editCheck, lineCheck, txtCheckWarnig);
                            }
                        }
                    }
                }
            }
        });
        editCheck.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnCheckDel.setVisibility(View.VISIBLE);
                    if (s.charAt(s.length() - 1) == ' ') {
                        editCheck.setText(s.subSequence(0, s.length() - 1));
                        if (editCheck.getText().toString().length() > 0)
                            editCheck.setSelection(editCheck.getText().toString().length());
                    } else if (s.toString().contains(" ")) {
                        editCheck.setText(beforeCheck);
                        editCheck.setSelection(beforeCheck.length());
                    } else
                        beforeCheck = s.toString();
                } else {
                    btnCheckDel.setVisibility(View.INVISIBLE);
                    hideWarning(editCheck, lineCheck, txtCheckWarnig);

                    btnChange.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editCheck.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideInputMode();
                    setChangeEnable(editOldPwd.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        txtOldWarning = findViewById(R.id.txt_old_pwd_warning);
        txtPwdWarning = findViewById(R.id.txt_pwd_warning);
        txtCheckWarnig = findViewById(R.id.txt_check_warning);

        lineOld = findViewById(R.id.line_old_pwd);
        linePwd = findViewById(R.id.line_pwd);
        lineCheck = findViewById(R.id.line_check);

        btnOldDel = findViewById(R.id.btn_old_delete);
        btnOldDel.setOnClickListener(this);
        btnPwdDel = findViewById(R.id.btn_pwd_delete);
        btnPwdDel.setOnClickListener(this);
        btnCheckDel = findViewById(R.id.btn_check_delete);
        btnCheckDel.setOnClickListener(this);

        btnChange = findViewById(R.id.btn_change);
        btnChange.setOnClickListener(this);

        mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                finish();
                break;

            case R.id.edit_old_pwd:
                showInputMode(editOldPwd);
                break;

            case R.id.edit_pwd:
                showInputMode(editPwd);
                break;

            case R.id.edit_check:
                showInputMode(editCheck);
                break;

            case R.id.btn_old_delete:
                editOldPwd.setText("");
                break;

            case R.id.btn_pwd_delete:
                editPwd.setText("");
                break;

            case R.id.btn_check_delete:
                editCheck.setText("");
                break;

            case R.id.btn_change:
                String address = mWallet.getAddress();
                JsonObject oldKeyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);
                String id = oldKeyStore.get(("id")).getAsString();
                String newKeyStore = getKeyStore(mWallet.getCoinType(), id, address, mPrivateKey, editPwd.getText().toString());
                if (newKeyStore != null) {
                    try {
                        RealmUtil.modWalletPassword(mWallet.getAddress(), newKeyStore);
                        mWallet.setKeyStore(newKeyStore);
                        BasicDialog dialog = new BasicDialog(this);
                        dialog.setMessage(getString(R.string.doneChangeWalletPwd));
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                setResult(RESULT_CODE, new Intent().putExtra("result", (Serializable) mWallet));
                                finish();
                            }
                        });
                        dialog.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private OnKeyPreImeListener mOnKeyPreImeListener = new OnKeyPreImeListener() {
        @Override
        public void onBackPressed() {
            hideInputMode();
            setChangeEnable(editOldPwd.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
        }
    };

    private void showInputMode(View view) {
        view.requestFocus();
        mImm.showSoftInput(view, 0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) inputArea.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.appbar);
        layoutParams.setMargins(0, (int) getResources().getDimension(R.dimen.ChangePwdMarginTopShow), 0, 0);
        inputArea.setLayoutParams(layoutParams);
    }

    private void hideInputMode() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) inputArea.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.layout_header);
        layoutParams.setMargins(0, (int) getResources().getDimension(R.dimen.ChangePwdMarginTopHide), 0, 0);
        inputArea.setLayoutParams(layoutParams);
    }

    private void showWarning(View line, TextView txtView, String msg) {
        line.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        txtView.setVisibility(View.VISIBLE);
        txtView.setText(msg);
    }

    private void hideWarning(EditText editView, View line, TextView txtView) {
        if (editView.hasFocus()) {
            line.setBackgroundColor(getResources().getColor(R.color.editActivated));
        } else {
            line.setBackgroundColor(getResources().getColor(R.color.editNormal));
        }

        txtView.setVisibility(View.GONE);
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

    private String getKeyStore(String coinType, String id, String address, byte[] privKey, String pwd) {
        return KeyStoreUtils.changePassword(coinType, id, address, privKey, pwd);
    }


    private void setChangeEnable(String old, String pwd, String checkPwd) {
        boolean curResult = validateCurrentPwd(old);
        if (curResult) {
            hideWarning(editOldPwd, lineOld, txtOldWarning);
        } else {
            showWarning(lineOld, txtOldWarning, getString(R.string.errPassword));
        }
        int pwdResult = PasswordValidator.validatePassword(pwd);
        switch (pwdResult) {
            case PasswordValidator.EMPTY:
                showWarning(linePwd, txtPwdWarning, getString(R.string.errPwdEmpty));
                break;

            case PasswordValidator.LEAST_8:
                showWarning(linePwd, txtPwdWarning, getString(R.string.errAtLeast));
                break;

            case PasswordValidator.NOT_MATCH_PATTERN:
                showWarning(linePwd, txtPwdWarning, getString(R.string.errPasswordPatternMatch));
                break;

            case PasswordValidator.HAS_WHITE_SPACE:
                showWarning(linePwd, txtPwdWarning, getString(R.string.errWhiteSpace));
                break;

            case PasswordValidator.SERIAL_CHAR:
                showWarning(linePwd, txtPwdWarning, getString(R.string.errSerialChar));
                break;

            default:
                hideWarning(editPwd, linePwd, txtPwdWarning);
        }

        boolean checkResult = PasswordValidator.checkPasswordMatch(pwd, checkPwd);
        if (!checkResult) {
            showWarning(lineCheck, txtCheckWarnig, getString(R.string.errPasswordNotMatched));
        } else {
            hideWarning(editCheck, lineCheck, txtCheckWarnig);
        }

        if (curResult && checkResult && (pwdResult == PasswordValidator.OK))
            btnChange.setEnabled(true);
        else
            btnChange.setEnabled(false);
    }
}
