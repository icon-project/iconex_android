package foundation.icon.iconex.wallet.create;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.control.PasswordValidator;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.MyEditText;

import static foundation.icon.iconex.control.PasswordValidator.checkPasswordMatch;

public class CreateWalletStep2Fragment extends Fragment implements View.OnClickListener {

    private static final String TAG = CreateWalletStep2Fragment.class.getSimpleName();

    private OnStep2Listener mListener;

    private View view;

    private MyEditText editAlias, editPwd, editCheck;
    private View lineAlias, linePwd, lineCheck;
    private TextView txtAliasWarning, txtPwdWarning, txtCheckWarnig;
    private Button btnAliasDel, btnPwdDel, btnCheckDel;
    private Button btnPwdView, btnCheckView;

    private Button btnPrev, btnNext;
    private ProgressBar progress;

    private InputMethodManager mImm;
    private OnKeyPreImeListener mOnKeyPreImeListener;

    private String beforeStr;

    private final int OK = 0;
    private final int ALIAS_DUP = 1;
    private final int ALIAS_EMPTY = 2;

    public CreateWalletStep2Fragment() {
        // Required empty public constructor
    }

    public static CreateWalletStep2Fragment newInstance() {
        CreateWalletStep2Fragment fragment = new CreateWalletStep2Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mOnKeyPreImeListener = new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                hideInputMode();
                setNextEnable(editAlias.getText().toString(),
                        editPwd.getText().toString(), editCheck.getText().toString());
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_create_wallet_step2, container, false);
        btnPrev = view.findViewById(R.id.btn_prev);
        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearEdit();
                mListener.onStep2Back();
            }
        });
        btnNext = view.findViewById(R.id.btn_next);
        btnNext.setEnabled(false);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                btnNext.setEnabled(false);
                progress.setVisibility(View.VISIBLE);
                mListener.onStep2Done(Utils.strip(editAlias.getText().toString()), editPwd.getText().toString());
            }
        });

        progress = view.findViewById(R.id.progress);

        editAlias = view.findViewById(R.id.edit_alias);
//        editAlias.setFilters(new InputFilter[]{new ByteLengthFilter()});
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
                        case ALIAS_EMPTY:
                            showWarning(lineAlias, txtAliasWarning, getString(R.string.errWhiteSpace));
                            break;

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
                    btnAliasDel.setVisibility(View.VISIBLE);
                    if (Utils.checkByteLength(s.toString()) > 16) {
                        editAlias.setText(beforeStr);
                        editAlias.setSelection(editAlias.getText().toString().length());
                    } else {
                        beforeStr = s.toString();
                    }
                } else {
                    btnAliasDel.setVisibility(View.INVISIBLE);
                    txtAliasWarning.setVisibility(View.INVISIBLE);

                    if (editAlias.isFocused())
                        lineAlias.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineAlias.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnNext.setEnabled(false);
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
                    setNextEnable(editAlias.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        editPwd = view.findViewById(R.id.edit_pwd);
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
                } else {
                    btnPwdDel.setVisibility(View.INVISIBLE);
                    txtPwdWarning.setVisibility(View.INVISIBLE);

                    if (editPwd.isFocused())
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnNext.setEnabled(false);
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
                    setNextEnable(editAlias.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        editCheck = view.findViewById(R.id.edit_check);
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
                        showWarning(lineCheck, txtCheckWarnig, getString(R.string.errCheckEmpty));
                        btnNext.setEnabled(false);
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
                } else {
                    btnCheckDel.setVisibility(View.INVISIBLE);

                    txtCheckWarnig.setVisibility(View.INVISIBLE);
                    if (editCheck.isFocused())
                        lineCheck.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineCheck.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnNext.setEnabled(false);
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
                    setNextEnable(editAlias.getText().toString(), editPwd.getText().toString(), editCheck.getText().toString());
                }
                return false;
            }
        });

        txtAliasWarning = view.findViewById(R.id.txt_alias_warning);
        txtPwdWarning = view.findViewById(R.id.txt_pwd_warning);
        txtCheckWarnig = view.findViewById(R.id.txt_check_warning);

        lineAlias = view.findViewById(R.id.line_alias);
        linePwd = view.findViewById(R.id.line_pwd);
        lineCheck = view.findViewById(R.id.line_check);

        btnAliasDel = view.findViewById(R.id.btn_alias_delete);
        btnAliasDel.setOnClickListener(this);
        btnPwdDel = view.findViewById(R.id.btn_pwd_delete);
        btnPwdDel.setOnClickListener(this);
        btnCheckDel = view.findViewById(R.id.btn_check_delete);
        btnCheckDel.setOnClickListener(this);

        btnPwdView = view.findViewById(R.id.btn_pwd_view);
        btnPwdView.setOnClickListener(this);
        btnCheckView = view.findViewById(R.id.btn_check_view);
        btnCheckView.setOnClickListener(this);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStep2Listener) {
            mListener = (OnStep2Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStep2Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

            case R.id.btn_pwd_view:
                if (btnPwdView.isSelected()) {
                    btnPwdView.setSelected(false);
                    editPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    btnPwdView.setSelected(true);
                    editPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                editPwd.setSelection(editPwd.getText().toString().length());
                break;

            case R.id.btn_check_view:
                if (btnCheckView.isSelected()) {
                    btnCheckView.setSelected(false);
                    editCheck.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                } else {
                    btnCheckView.setSelected(true);
                    editCheck.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                }
                editCheck.setSelection(editCheck.getText().toString().length());
                break;
        }
    }

    private void showInputMode(View view) {
        view.requestFocus();
        mImm.showSoftInput(view, 0);
        ViewGroup layoutInputArea = this.view.findViewById(R.id.layout_input_area);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutInputArea.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutInputArea.setLayoutParams(layoutParams);
        mListener.onShowInputMode();
    }

    private void hideInputMode() {
        ViewGroup layoutInputArea = this.view.findViewById(R.id.layout_input_area);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutInputArea.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.layout_header);
        layoutParams.addRule(RelativeLayout.ABOVE, R.id.layout_buttons);
        layoutInputArea.setLayoutParams(layoutParams);
        mListener.onHideInputMode();
    }

    private void showWarning(View line, TextView txtView, String msg) {
        line.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        txtView.setVisibility(View.VISIBLE);
        txtView.setText(msg);
    }

    private void hideWarning(View edit, View line, View txtView) {
        txtView.setVisibility(View.INVISIBLE);
        if (edit.isFocused())
            line.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            line.setBackgroundColor(getResources().getColor(R.color.editNormal));
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

    private void setNextEnable(String alias, String pwd, String checkPwd) {
        int aliasValidate = 0;
        int pwdValidate = 0;
        boolean matched = true;

        if (!editAlias.getText().toString().isEmpty()) {
            aliasValidate = checkAlias(alias);
            switch (aliasValidate) {
                case ALIAS_EMPTY:
                    showWarning(lineAlias, txtAliasWarning, getString(R.string.errAliasEmpty));
                    break;

                case ALIAS_DUP:
                    showWarning(lineAlias, txtAliasWarning, getString(R.string.duplicateWalletAlias));
                    break;
                default:
                    hideWarning(editAlias, lineAlias, txtAliasWarning);
            }
        } else {
            showWarning(lineAlias, txtAliasWarning, getString(R.string.errAliasEmpty));
            btnNext.setEnabled(false);
            return;
        }

        if (!editPwd.getText().toString().isEmpty()) {
            pwdValidate = PasswordValidator.validatePassword(pwd);
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
        } else {
            showWarning(linePwd, txtPwdWarning, getString(R.string.errPwdEmpty));
            btnNext.setEnabled(false);
            return;
        }

        if (!editCheck.getText().toString().isEmpty()) {
            matched = checkPasswordMatch(pwd, checkPwd);
            if (!matched) {
                showWarning(lineCheck, txtCheckWarnig, getString(R.string.errPasswordNotMatched));
            } else {
                hideWarning(editCheck, lineCheck, txtCheckWarnig);
            }
        } else {
            showWarning(lineCheck, txtCheckWarnig, getString(R.string.errCheckEmpty));
            btnNext.setEnabled(false);
            return;
        }

        if (aliasValidate == OK && matched && (pwdValidate == PasswordValidator.OK))
            btnNext.setEnabled(true);
        else
            btnNext.setEnabled(false);
    }

    public void clearEdit() {
        editAlias.requestFocus();

        editAlias.setText("");
        editPwd.setText("");
        editCheck.setText("");

        lineAlias.setBackgroundColor(getResources().getColor(R.color.editActivated));
        linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));
        lineCheck.setBackgroundColor(getResources().getColor(R.color.editNormal));

        txtAliasWarning.setVisibility(View.INVISIBLE);
        txtPwdWarning.setVisibility(View.INVISIBLE);
        txtCheckWarnig.setVisibility(View.INVISIBLE);

        progress.setVisibility(View.GONE);
        btnNext.setEnabled(false);
    }

    public interface OnStep2Listener {
        void onStep2Done(String name, String pwd);

        void onStep2Back();

        void onShowInputMode();

        void onHideInputMode();
    }
}
