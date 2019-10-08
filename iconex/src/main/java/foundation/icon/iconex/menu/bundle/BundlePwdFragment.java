package foundation.icon.iconex.menu.bundle;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.util.PasswordValidator;
import foundation.icon.iconex.widgets.TTextInputLayout;
import kotlin.jvm.functions.Function1;

public class BundlePwdFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = BundlePwdFragment.class.getSimpleName();

    private TTextInputLayout editPwd, editCheck;

    private Button btnExport;

    private OnBundlePwdListener mListener;

    private String beforePwd, beforeCheck;

    public BundlePwdFragment() {
        // Required empty public constructor
    }

    public static BundlePwdFragment newInstance() {
        BundlePwdFragment fragment = new BundlePwdFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_bundle_pwd, container, false);

        editPwd = v.findViewById(R.id.edit_pwd);
        editPwd.setOnKeyPreImeListener(mKeyPreImeListener);
        editPwd.setOnFocusChangedListener(new TTextInputLayout.OnFocusReleased() {
            @Override
            public void onReleased() {
                int result = PasswordValidator.validatePassword(editPwd.getText());
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
                        editPwd.setError(false, null);
                }
            }
        });
        editPwd.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.charAt(s.length() - 1) == ' ') {
                        editPwd.setText(s.subSequence(0, s.length() - 1).toString());
                        if (editPwd.getText().length() > 0)
                            editPwd.setSelection(editPwd.getText().length());
                    } else if (s.toString().contains(" ")) {
                        editPwd.setText(beforePwd);
                        editPwd.setSelection(beforePwd.length());
                    } else {
                        beforePwd = s.toString();
                    }
                } else {
                    editPwd.setError(false, null);
                    btnExport.setEnabled(false);
                }
            }
        });

        editCheck = v.findViewById(R.id.edit_check);
        editCheck.setOnKeyPreImeListener(mKeyPreImeListener);
        editCheck.setOnFocusChangedListener(new TTextInputLayout.OnFocusReleased() {
            @Override
            public void onReleased() {
                if (editCheck.getText().isEmpty()) {
                    btnExport.setEnabled(false);
                } else {
                    if (editPwd.getText().isEmpty()) {
                        editCheck.setError(true, getString(R.string.errPasswordNotMatched));
                    } else {
                        boolean result = PasswordValidator.checkPasswordMatch(editPwd.getText(), editCheck.getText());
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
                        if (editCheck.getText().length() > 0)
                            editCheck.setSelection(editCheck.getText().length());
                    } else if (s.toString().contains(" ")) {
                        editCheck.setText(beforeCheck);
                        editCheck.setSelection(beforeCheck.length());
                    } else {
                        beforeCheck = s.toString();
                    }
                } else {
                    editCheck.setError(false, null);
                    btnExport.setEnabled(false);
                }
            }
        });
        editCheck.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                setDownEnabled();
            }
        });

        editPwd.setPastable(false);
        editPwd.setPastable(false);

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

            case R.id.btn_export:
                int pwdResult = PasswordValidator.validatePassword(editPwd.getText());
                if (pwdResult == PasswordValidator.EMPTY) {
                    editPwd.setError(true, getString(R.string.errPwdEmpty));
                } else if (pwdResult == PasswordValidator.LEAST_8) {
                    editPwd.setError(true, getString(R.string.errAtLeast));
                } else if (pwdResult == PasswordValidator.HAS_WHITE_SPACE) {
                    editPwd.setError(true, getString(R.string.errWhiteSpace));
                } else if (pwdResult == PasswordValidator.NOT_MATCH_PATTERN) {
                    editPwd.setError(true, getString(R.string.errPasswordPatternMatch));
                } else if (pwdResult == PasswordValidator.SERIAL_CHAR) {
                    editPwd.setError(true, getString(R.string.errSerialChar));
                } else {
                    editPwd.setError(false, null);
                }

                boolean checkResult = PasswordValidator.checkPasswordMatch(editPwd.getText(),
                        editCheck.getText());
                if (!checkResult)
                    editCheck.setError(true, getString(R.string.errPasswordNotMatched));
                else
                    editCheck.setError(false, null);

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

    private TTextInputLayout.OnKeyPreIme mKeyPreImeListener = new TTextInputLayout.OnKeyPreIme() {
        @Override
        public void onDone() {
            setDownEnabled();
        }
    };

    private void setDownEnabled() {
        int pwdResult = PasswordValidator.validatePassword(editPwd.getText());
        if (pwdResult == PasswordValidator.EMPTY) {
            editPwd.setError(true, getString(R.string.errPwdEmpty));
        } else if (pwdResult == PasswordValidator.LEAST_8) {
            editPwd.setError(true, getString(R.string.errAtLeast));
        } else if (pwdResult == PasswordValidator.HAS_WHITE_SPACE) {
            editPwd.setError(true, getString(R.string.errWhiteSpace));
        } else if (pwdResult == PasswordValidator.NOT_MATCH_PATTERN) {
            editPwd.setError(true, getString(R.string.errPasswordPatternMatch));
        } else if (pwdResult == PasswordValidator.SERIAL_CHAR) {
            editPwd.setError(true, getString(R.string.errSerialChar));
        } else {
            editPwd.setError(false, null);
        }

        boolean checkResult = PasswordValidator.checkPasswordMatch(editPwd.getText().toString(),
                editCheck.getText());
        if (editCheck.getText().trim().isEmpty()) {
            editCheck.setError(true, getString(R.string.errCheckEmpty));
        } else if (!checkResult)
            editCheck.setError(true, getString(R.string.errPasswordNotMatched));
        else
            editCheck.setError(false, null);

        if ((pwdResult == PasswordValidator.OK)
                && checkResult) {
            btnExport.setEnabled(true);
            editPwd.setError(false, null);
            editCheck.setError(false, null);
        } else {
            btnExport.setEnabled(false);
        }
    }

    private void checkPermission() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            MessageDialog messageDialog = new MessageDialog(getContext());
            messageDialog.setTitleText(getString(R.string.backupKeyStoreFileConfirm));
            messageDialog.setSingleButton(false);
            messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    mListener.onExport(editPwd.getText());
                    return true;
                }
            });
            messageDialog.show();
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
