package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.widgets.MyEditText;

/**
 * Created by js on 2018. 2. 19..
 */

public class EditTextDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = EditTextDialog.class.getSimpleName();

    private final Context mContext;
    private OnConfirmCallback mCallback;
    private OnPasswordCallback mPasswordCallback;
    private final String mTitle;
    private String mHint;
    private TYPE_INPUT mType;
    private RESULT_PWD mPwdType;

    private MyEditText editText;
    private View lineText;
    private TextView txtWarning;
    private Button btnDel;
    private Button btnCancel, btnConfirm;

    private String mAlias;

    private String beforeStr;

    public EditTextDialog(@NonNull Context context, @NonNull String title) {
        super(context);

        mContext = context;
        mTitle = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_edit_text);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.txt_title)).setText(mTitle);

        btnDel = findViewById(R.id.del_input);
        btnDel.setOnClickListener(this);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        lineText = findViewById(R.id.line_pwd);
        txtWarning = findViewById(R.id.txt_file_warning);

        editText = findViewById(R.id.edit_text);
        editText.setHint(mHint);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineText.setBackgroundColor(mContext.getResources().getColor(R.color.editActivated));
                } else {
                    lineText.setBackgroundColor(mContext.getResources().getColor(R.color.editNormal));
                }
            }
        });
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnDel.setVisibility(View.VISIBLE);
                    btnConfirm.setEnabled(true);

                    if (mType == TYPE_INPUT.ALIAS) {
                        if (s.toString().trim().isEmpty()) {
                            editText.setText("");
                        } else {
                            if (Utils.checkByteLength(s.toString()) > 16) {
                                editText.setText(beforeStr);
                                editText.setSelection(editText.getText().toString().length());
                            } else {
                                beforeStr = s.toString();
                            }
                        }
                    }
                } else {
                    btnDel.setVisibility(View.INVISIBLE);
                    btnConfirm.setEnabled(false);
                    txtWarning.setVisibility(View.INVISIBLE);
                    if (editText.isFocused())
                        lineText.setBackgroundColor(mContext.getResources().getColor(R.color.editActivated));
                    else
                        lineText.setBackgroundColor(mContext.getResources().getColor(R.color.editNormal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editText.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                if (editText.getText().toString().length() == 0) {
                    lineText.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                    txtWarning.setVisibility(View.VISIBLE);
                    if (mType == TYPE_INPUT.ALIAS) {
                        txtWarning.setText(mContext.getString(R.string.errAliasEmpty));
                    } else {
                        txtWarning.setText(mContext.getString(R.string.errPwdEmpty));
                    }
                }
            }
        });

        if (mType == TYPE_INPUT.ALIAS) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setText(mAlias);
        } else
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        Handler localHandler = new Handler();
        localHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                editText.requestFocus();
                editText.setSelection(editText.getText().toString().length());
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, 0);
            }
        }, 100);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;

            case R.id.del_input:
                editText.setText("");
                break;

            case R.id.btn_confirm:
                if (mType == TYPE_INPUT.ALIAS) {
                    if (mCallback != null)
                        mCallback.onConfirm(editText.getText().toString());
                } else {
                    if (mPasswordCallback != null)
                        mPasswordCallback.onConfirm(mPwdType, editText.getText().toString());
                }
                break;
        }
    }

    public void setInputType(TYPE_INPUT type) {
        mType = type;
    }

    public void setPasswordType(RESULT_PWD type) {
        mPwdType = type;
    }

    public void setAlias(String alias) {
        mAlias = alias;
    }

    public void setHint(String msg) {
        mHint = msg;
    }

    public void setError(String msg) {
        lineText.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
        txtWarning.setVisibility(View.VISIBLE);
        txtWarning.setText(msg);
    }

    public void setOnConfirmCallback(OnConfirmCallback callback) {
        mCallback = callback;
    }

    public void setOnPasswordCallback(OnPasswordCallback callback) {
        mPasswordCallback = callback;
    }

    public interface OnConfirmCallback {
        void onConfirm(String text);
    }

    public interface OnPasswordCallback {
        void onConfirm(RESULT_PWD result, String text);
    }

    public enum TYPE_INPUT {
        ALIAS,
        PASSWORD
    }

    public enum RESULT_PWD {
        TRANSFER,
        BACKUP,
        SWAP,
        REMOVE
    }
}
