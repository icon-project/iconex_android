package foundation.icon.iconex.menu.bundle;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.widgets.MyEditText;

public class BundlePwdFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = BundlePwdFragment.class.getSimpleName();

    private ViewGroup layoutHeader, layoutInput;

    private MyEditText editPwd, editCheck;
    private View linePwd, lineCheck;
    private TextView txtPwdWarning, txtCheckWarning;
    private Button btnPwdDel, btnCheckDel;
    private Button btnPwdView, btnCheckView;

    private Button btnExport;

    private InputMethodManager mImm;

    private OnBundlePwdListener mListener;

    public BundlePwdFragment() {
        // Required empty public constructor
    }

    public static BundlePwdFragment newInstance() {
        BundlePwdFragment fragment = new BundlePwdFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bundle_pwd, container, false);

        layoutHeader = v.findViewById(R.id.layout_header);
        layoutInput = v.findViewById(R.id.layout_input);

        editPwd = v.findViewById(R.id.edit_pwd);
        editPwd.setOnKeyPreImeListener(mKeyPreImeListener);
        editPwd.setLongClickable(false);
        editPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    int pwdResult = PasswordValidator.validatePassword(editPwd.getText().toString());
                    if (pwdResult == PasswordValidator.EMPTY) {
                        showWarning(linePwd, txtPwdWarning, getString(R.string.errPwdEmpty));
                    } else if (pwdResult == PasswordValidator.LEAST_8) {
                        showWarning(linePwd, txtPwdWarning, getString(R.string.errAtLeast));
                    } else if (pwdResult == PasswordValidator.HAS_WHITE_SPACE) {
                        showWarning(linePwd, txtPwdWarning, getString(R.string.errWhiteSpace));
                    } else if (pwdResult == PasswordValidator.NOT_MATCH_PATTERN) {
                        showWarning(linePwd, txtPwdWarning, getString(R.string.errPasswordPatternMatch));
                    } else if (pwdResult == PasswordValidator.SERIAL_CHAR) {
                        showWarning(linePwd, txtPwdWarning, getString(R.string.errSerialChar));
                    } else {
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
                    txtPwdWarning.setVisibility(View.GONE);
                    if (editPwd.isFocused())
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnExport.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editPwd.setOnEditTouchListener(new MyEditText.OnEditTouchListener() {
            @Override
            public void onTouch() {
                showInputMode(editPwd);
            }
        });

        editCheck = v.findViewById(R.id.edit_check);
        editCheck.setOnKeyPreImeListener(mKeyPreImeListener);
        editCheck.setLongClickable(false);
        editCheck.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineCheck.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineCheck.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    if (editCheck.getText().toString().isEmpty()) {
                        showWarning(lineCheck, txtCheckWarning, getString(R.string.errCheckEmpty));
                    } else {
                        boolean checkResult = PasswordValidator.checkPasswordMatch(editPwd.getText().toString(), editCheck.getText().toString());
                        if (!checkResult)
                            showWarning(lineCheck, txtCheckWarning, getString(R.string.errPasswordNotMatched));
                        else {
                            hideWarning(editCheck, lineCheck, txtCheckWarning);
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
                    txtCheckWarning.setVisibility(View.GONE);
                    if (editCheck.isFocused())
                        lineCheck.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineCheck.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    btnExport.setEnabled(false);
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
                    setDownEnabled();
                }
                return false;
            }
        });
        editCheck.setOnEditTouchListener(new MyEditText.OnEditTouchListener() {
            @Override
            public void onTouch() {
                showInputMode(editCheck);
            }
        });

        linePwd = v.findViewById(R.id.line_pwd);
        lineCheck = v.findViewById(R.id.line_check);
        txtPwdWarning = v.findViewById(R.id.txt_pwd_warning);
        txtCheckWarning = v.findViewById(R.id.txt_check_warning);

        btnPwdDel = v.findViewById(R.id.btn_pwd_delete);
        btnPwdDel.setOnClickListener(this);
        btnCheckDel = v.findViewById(R.id.btn_check_delete);
        btnCheckDel.setOnClickListener(this);

        btnPwdView = v.findViewById(R.id.btn_pwd_view);
        btnPwdView.setOnClickListener(this);
        btnCheckView = v.findViewById(R.id.btn_check_view);
        btnCheckView.setOnClickListener(this);

        btnExport = v.findViewById(R.id.btn_export);
        btnExport.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

            case R.id.btn_export:
                int pwdResult = PasswordValidator.validatePassword(editPwd.getText().toString());
                if (pwdResult == PasswordValidator.EMPTY) {
                    showWarning(linePwd, txtPwdWarning, getString(R.string.errPwdEmpty));
                } else if (pwdResult == PasswordValidator.LEAST_8) {
                    showWarning(linePwd, txtPwdWarning, getString(R.string.errAtLeast));
                } else if (pwdResult == PasswordValidator.HAS_WHITE_SPACE) {
                    showWarning(linePwd, txtPwdWarning, getString(R.string.errWhiteSpace));
                } else if (pwdResult == PasswordValidator.NOT_MATCH_PATTERN) {
                    showWarning(linePwd, txtPwdWarning, getString(R.string.errPasswordPatternMatch));
                } else if (pwdResult == PasswordValidator.SERIAL_CHAR) {
                    showWarning(linePwd, txtPwdWarning, getString(R.string.errSerialChar));
                } else {
                    hideWarning(editPwd, linePwd, txtPwdWarning);
                }

                boolean checkResult = PasswordValidator.checkPasswordMatch(editPwd.getText().toString(),
                        editCheck.getText().toString());
                if (!checkResult)
                    showWarning(lineCheck, txtCheckWarning, getString(R.string.errPasswordNotMatched));
                else
                    hideWarning(editCheck, lineCheck, txtCheckWarning);

                if ((pwdResult == PasswordValidator.OK)
                        && checkResult) {
                    checkPermission();
                } else {
                    btnExport.setEnabled(false);
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnBundlePwdListener) {
            mListener = (OnBundlePwdListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnBundlePwdListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private OnKeyPreImeListener mKeyPreImeListener = new OnKeyPreImeListener() {
        @Override
        public void onBackPressed() {
            hideInputMode();
            setDownEnabled();
        }
    };

    private void showInputMode(View view) {
        view.requestFocus();
        mImm.showSoftInput(view, 0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutInput.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutInput.setLayoutParams(layoutParams);
    }

    private void hideInputMode() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutInput.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.layout_header);
        layoutInput.setLayoutParams(layoutParams);
    }

    private void showWarning(View line, TextView txtView, String msg) {
        line.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        txtView.setVisibility(View.VISIBLE);
        txtView.setText(msg);
    }

    private void hideWarning(MyEditText edit, View line, TextView txtView) {
        txtView.setVisibility(View.GONE);
        if (edit.isFocused())
            line.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            line.setBackgroundColor(getResources().getColor(R.color.editNormal));
    }

    private void setDownEnabled() {
        int pwdResult = PasswordValidator.validatePassword(editPwd.getText().toString());
        if (pwdResult == PasswordValidator.EMPTY) {
            showWarning(linePwd, txtPwdWarning, getString(R.string.errPwdEmpty));
        } else if (pwdResult == PasswordValidator.LEAST_8) {
            showWarning(linePwd, txtPwdWarning, getString(R.string.errAtLeast));
        } else if (pwdResult == PasswordValidator.HAS_WHITE_SPACE) {
            showWarning(linePwd, txtPwdWarning, getString(R.string.errWhiteSpace));
        } else if (pwdResult == PasswordValidator.NOT_MATCH_PATTERN) {
            showWarning(linePwd, txtPwdWarning, getString(R.string.errPasswordPatternMatch));
        } else if (pwdResult == PasswordValidator.SERIAL_CHAR) {
            showWarning(linePwd, txtPwdWarning, getString(R.string.errSerialChar));
        } else {
            hideWarning(editPwd, linePwd, txtPwdWarning);
        }

        boolean checkResult = PasswordValidator.checkPasswordMatch(editPwd.getText().toString(),
                editCheck.getText().toString());
        if (editCheck.getText().toString().trim().isEmpty()) {
            showWarning(lineCheck, txtCheckWarning, getString(R.string.errCheckEmpty));
        } else if (!checkResult)
            showWarning(lineCheck, txtCheckWarning, getString(R.string.errPasswordNotMatched));
        else
            hideWarning(editCheck, lineCheck, txtCheckWarning);

        if ((pwdResult == PasswordValidator.OK)
                && checkResult) {
            btnExport.setEnabled(true);
            txtPwdWarning.setVisibility(View.GONE);
            txtCheckWarning.setVisibility(View.GONE);
        } else {
            btnExport.setEnabled(false);
        }
    }

    private void checkPermission() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm));
            dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                @Override
                public void onOk() {
                    mListener.onExport(editPwd.getText().toString());
                }

                @Override
                public void onCancel() {

                }
            });
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ExportWalletBundleActivity.STORAGE_PERMISSION_REQUEST);
        }
    }

    public interface OnBundlePwdListener {
        void onExport(String pwd);
    }
}
