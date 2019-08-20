package foundation.icon.iconex.view.ui.load;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.MyEditText;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static foundation.icon.iconex.util.PasswordValidator.checkPasswordMatch;

public class LoadInputWalletInfoFragment extends Fragment implements View.OnClickListener {

    private OnInputWalletInfoListener mListener;

    private ViewGroup layoutHeader, inputArea;

    private MyEditText editAlias, editPwd, editCheck;
    private View lineAlias, linePwd, lineCheck;
    private TextView txtAliasWarning, txtPwdWarning, txtCheckWarnig;
    private Button btnAliasDel, btnPwdDel, btnCheckDel;

    private Button btnDone, btnBack;

    private InputMethodManager mImm;

    private String beforeStr, beforePwd, beforeCheck;

    private final int OK = 0;
    private final int ALIAS_DUP = 1;
    private final int ALIAS_EMPTY = 2;

    public LoadInputWalletInfoFragment() {
        // Required empty public constructor
    }

    public static LoadInputWalletInfoFragment newInstance() {
        LoadInputWalletInfoFragment fragment = new LoadInputWalletInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_input_wallet_info, container, false);

        layoutHeader = v.findViewById(R.id.layout_header);
        inputArea = v.findViewById(R.id.layout_input);

        editAlias = v.findViewById(R.id.edit_alias);
        editAlias.setOnKeyPreImeListener(mOnKeyPreImeListener);
        editAlias.setOnEditTouchListener(new MyEditText.OnEditTouchListener() {
            @Override
            public void onTouch() {
                showInputMode(editAlias);
            }
        });
        editAlias.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineAlias.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    // TODO: 2018. 3. 22. check password
                } else {
                    lineAlias.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    int aliasValidate = checkAlias(editAlias.getText().toString());
                    switch (aliasValidate) {
                        case ALIAS_DUP:
                            showWarning(lineAlias, txtAliasWarning, getString(R.string.duplicateWalletAlias));
                            break;
                        default:
                            hideWarning(editAlias, lineAlias, txtAliasWarning);
                    }
                }
            }
        });
        editAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (s.toString().trim().isEmpty()) {
                        editAlias.setText("");
                    } else if (s.charAt(0) == ' ') {
                        editAlias.setText(beforeStr);
                        editAlias.setSelection(beforeStr.length());
                    } else {
                        btnAliasDel.setVisibility(View.VISIBLE);
                        if (Utils.checkByteLength(s.toString()) > 16) {
                            editAlias.setText(beforeStr);
                            editAlias.setSelection(editAlias.getText().toString().length());
                        } else {
                            beforeStr = s.toString();
                        }
                    }
                } else {
                    btnAliasDel.setVisibility(View.INVISIBLE);
                    txtAliasWarning.setVisibility(View.GONE);
                    if (editAlias.isFocused())
                        lineAlias.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineAlias.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnDone.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editAlias.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    hideInputMode();
                    setDoneEnable(editAlias.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        editPwd = v.findViewById(R.id.edit_pwd);
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
                    txtPwdWarning.setVisibility(View.GONE);
                    if (editPwd.isFocused())
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnDone.setEnabled(false);
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
                    setDoneEnable(editAlias.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        editCheck = v.findViewById(R.id.edit_check);
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

                    if (!editCheck.getText().toString().isEmpty()) {
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
                    txtCheckWarnig.setVisibility(View.GONE);
                    if (editCheck.isFocused())
                        lineCheck.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineCheck.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnDone.setEnabled(false);
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
                    setDoneEnable(editAlias.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        txtAliasWarning = v.findViewById(R.id.txt_alias_warning);
        txtAliasWarning.setVisibility(View.GONE);
        txtPwdWarning = v.findViewById(R.id.txt_pwd_warning);
        txtPwdWarning.setVisibility(View.GONE);
        txtCheckWarnig = v.findViewById(R.id.txt_check_warning);
        txtCheckWarnig.setVisibility(View.GONE);

        lineAlias = v.findViewById(R.id.line_alias);
        linePwd = v.findViewById(R.id.line_pwd);
        lineCheck = v.findViewById(R.id.line_check);

        btnAliasDel = v.findViewById(R.id.btn_alias_delete);
        btnAliasDel.setOnClickListener(this);
        btnPwdDel = v.findViewById(R.id.btn_pwd_delete);
        btnPwdDel.setOnClickListener(this);
        btnCheckDel = v.findViewById(R.id.btn_check_delete);
        btnCheckDel.setOnClickListener(this);

        btnDone = v.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        mImm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_alias_delete:
                editAlias.setText("");
                break;

            case R.id.btn_pwd_delete:
                editPwd.setText("");
                break;

            case R.id.btn_check_delete:
                editCheck.setText("");
                break;

            case R.id.btn_done:
                mListener.onDoneInputWalletInfo(Utils.strip(editAlias.getText().toString()), editPwd.getText().toString());
                break;

            case R.id.btn_back:
                clear();
                mListener.onInfoBack();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInputWalletInfoListener) {
            mListener = (OnInputWalletInfoListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInputWalletInfoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private OnKeyPreImeListener mOnKeyPreImeListener = new OnKeyPreImeListener() {
        @Override
        public void onBackPressed() {
            hideInputMode();
            setDoneEnable(editAlias.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
        }
    };

    private void showInputMode(View view) {
        view.requestFocus();
        mImm.showSoftInput(view, 0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) inputArea.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        inputArea.setLayoutParams(layoutParams);
    }

    private void hideInputMode() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) inputArea.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.layout_header);
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

    private int checkAlias(String target) {
        String alias = Utils.strip(target);

        if (alias.isEmpty())
            return ALIAS_EMPTY;

        for (Wallet info : ICONexApp.mWallets) {
            if (info.getAlias().equals(alias)) {
                return ALIAS_DUP;
            }
        }

        return OK;
    }

    private void setDoneEnable(String alias, String pwd, String checkPwd) {
        int aliasValidate = checkAlias(alias);
        switch (aliasValidate) {
            case ALIAS_EMPTY:
                showWarning(lineAlias, txtAliasWarning, getString(R.string.errWhiteSpace));
                break;

            case ALIAS_DUP:
                showWarning(lineAlias, txtAliasWarning, getString(R.string.duplicateWalletAlias));
                break;
            default:
                hideWarning(editAlias, lineAlias, txtAliasWarning);
        }
        int pwdValidate = PasswordValidator.validatePassword(pwd);
        if (pwdValidate != 0) {
            switch (pwdValidate) {
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
        boolean matched = checkPasswordMatch(pwd, checkPwd);
        if (!matched) {
            showWarning(lineCheck, txtCheckWarnig, getString(R.string.errPasswordNotMatched));
        } else {
            hideWarning(editCheck, lineCheck, txtCheckWarnig);
        }

        if (aliasValidate == OK && matched && (pwdValidate == PasswordValidator.OK))
            btnDone.setEnabled(true);
        else
            btnDone.setEnabled(false);
    }

    public void clear() {
        editAlias.clearFocus();
        editPwd.clearFocus();
        editCheck.clearFocus();

        editAlias.setText("");
        editPwd.setText("");
        editCheck.setText("");

        txtAliasWarning.setVisibility(View.GONE);
        txtPwdWarning.setVisibility(View.GONE);
        txtCheckWarnig.setVisibility(View.GONE);
    }

    public interface OnInputWalletInfoListener {
        void onDoneInputWalletInfo(String name, String pwd);

        void onInfoBack();
    }
}
