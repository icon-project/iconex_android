package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.Contacts;
import foundation.icon.iconex.util.Utils;
import loopchain.icon.wallet.core.Constants;

/**
 * Created by js on 2018. 3. 20..
 */

public class ContactsDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private MODE mMode;
    private String mCoinType;
    private String mAddress = null;
    private OnClickListener mListener;

    private EditText editName, editAddress;
    private View lineName, lineAddress;
    private Button btnNameDelete, btnAddrDelete;
    private TextView txtNameWarning, txtAddrWarning;

    private ImageView btnScan;

    private Button btnCancel, btnConfirm;

    private int OK = 0;
    private int ERR_LEN_NAME = 1;
    private int ERR_DUP_NAME = 2;

    private InputMethodManager imm;

    private Handler localHandler = new Handler();

    public ContactsDialog(@NonNull Context context, @NonNull String coinType,
                          @NonNull MODE mode, @Nullable String address, @NonNull OnClickListener listener) {
        super(context);

        mContext = context;
        mCoinType = coinType;
        mMode = mode;
        mListener = listener;
        mAddress = address;
        imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_contacts);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        if (mMode == MODE.ADD)
            ((TextView) findViewById(R.id.txt_title)).setText(mContext.getString(R.string.addContacts));
        else
            ((TextView) findViewById(R.id.txt_title)).setText(mContext.getString(R.string.modContacts));

        editName = findViewById(R.id.edit_name);
        editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineName.setBackgroundColor(mContext.getResources().getColor(R.color.editActivated));
                } else {
                    lineName.setBackgroundColor(mContext.getResources().getColor(R.color.editNormal));
                    validateName(editName.getText().toString());
                }
            }
        });
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (Utils.checkByteLength(s.toString()) > 16) {
                        editName.setText(s.subSequence(0, s.length() - 1));
                        editName.setSelection(editName.getText().toString().length());
                    }

                    btnNameDelete.setVisibility(View.VISIBLE);
                } else {
                    btnNameDelete.setVisibility(View.INVISIBLE);
                    txtNameWarning.setVisibility(View.GONE);

                    if (editName.isFocused())
                        lineName.setBackgroundColor(mContext.getResources().getColor(R.color.editActivated));
                    else
                        lineName.setBackgroundColor(mContext.getResources().getColor(R.color.editNormal));

                    btnConfirm.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editAddress = findViewById(R.id.edit_address);
        editAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.editActivated));
                } else {
                    lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.editNormal));
                    validateAddress(editAddress.getText().toString());
                }
            }
        });
        editAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnAddrDelete.setVisibility(View.VISIBLE);

                    if (!editName.getText().toString().trim().isEmpty()
                            && !editAddress.getText().toString().trim().isEmpty())
                        btnConfirm.setEnabled(true);
                } else {
                    btnAddrDelete.setVisibility(View.INVISIBLE);
                    txtAddrWarning.setVisibility(View.GONE);
                    if (editAddress.isFocused())
                        lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.editActivated));
                    else
                        lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.editNormal));

                    btnConfirm.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (mCoinType.equals(Constants.KS_COINTYPE_ICX))
            editAddress.setHint(mContext.getString(R.string.hintICXAddress));
        else
            editAddress.setHint(mContext.getString(R.string.hintETHAddress));

        btnScan = findViewById(R.id.btn_qr_scan);
        btnScan.setOnClickListener(this);

        lineName = findViewById(R.id.line_name);
        lineAddress = findViewById(R.id.line_address);

        txtNameWarning = findViewById(R.id.txt_name_warning);
        txtAddrWarning = findViewById(R.id.txt_addr_warning);

        btnNameDelete = findViewById(R.id.btn_name_delete);
        btnNameDelete.setOnClickListener(this);
        btnAddrDelete = findViewById(R.id.btn_addr_delete);
        btnAddrDelete.setOnClickListener(this);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        if (mMode == MODE.MOD) {
            editAddress.setText(mAddress);
            editAddress.setEnabled(false);
            editAddress.setFocusable(false);
            btnAddrDelete.setVisibility(View.GONE);
            lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.editReadOnly));
            btnScan.setVisibility(View.GONE);
        }

        localHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                editName.requestFocus();
                imm.showSoftInput(editName, 0);
            }
        }, 200);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_name_delete:
                editName.setText("");
                break;

            case R.id.btn_addr_delete:
                editAddress.setText("");
                break;

            case R.id.btn_cancel:
                imm.toggleSoftInput(0, 0);
                dismiss();
                break;

            case R.id.btn_qr_scan:
                mListener.scanQRCode();
                break;

            case R.id.btn_confirm:
                if (mMode == MODE.ADD) {
                    if (validateName(editName.getText().toString())
                            && validateAddress(editAddress.getText().toString())
                            && mListener != null) {
                        imm.toggleSoftInput(0, 0);
                        mListener.onConfirm(mMode, editName.getText().toString(),
                                editAddress.getText().toString());
                        dismiss();
                    }
                } else {
                    if (validateName(editName.getText().toString())
                            && mListener != null) {
                        imm.toggleSoftInput(0, 0);
                        mListener.onConfirm(mMode, editName.getText().toString(),
                                editAddress.getText().toString());
                        dismiss();
                    }
                }
                break;
        }
    }

    private boolean validateName(String name) {
        if (name.trim().length() == 0) {
            lineName.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
            txtNameWarning.setText(mContext.getString(R.string.errWhiteSpace));
            txtNameWarning.setVisibility(View.VISIBLE);

            return false;
        }

        if (mCoinType.equals(loopchain.icon.wallet.core.Constants.KS_COINTYPE_ICX)) {
            for (Contacts contacts : ICONexApp.ICXContacts) {
                if (contacts.getName().equals(name)) {
                    lineName.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                    txtNameWarning.setText(mContext.getString(R.string.errDuplicateContactsName));
                    txtNameWarning.setVisibility(View.VISIBLE);
                    return false;
                }
            }
        } else {
            for (Contacts contacts : ICONexApp.ETHContacts) {
                if (contacts.getName().equals(name)) {
                    lineName.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                    txtNameWarning.setText(mContext.getString(R.string.errDuplicateContactsName));
                    txtNameWarning.setVisibility(View.VISIBLE);
                    return false;
                }
            }
        }

        return true;
    }

    private boolean validateAddress(String address) {
        if (address.trim().isEmpty()) {
            lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(mContext.getString(R.string.errNoAddress));

            return false;
        }

        if (mCoinType.equals(loopchain.icon.wallet.core.Constants.KS_COINTYPE_ICX)) {
            if (address.startsWith("hx")) {
                String temp = address.substring(2);
                if (temp.length() != 40) {
                    lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                    txtAddrWarning.setVisibility(View.VISIBLE);
                    txtAddrWarning.setText(String.format(mContext.getString(R.string.errWrongAddress), mCoinType.toUpperCase()));
                    return false;
                } else {
                    for (Contacts contacts : ICONexApp.ICXContacts) {
                        if (contacts.getAddress().equals(address)) {
                            lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                            txtAddrWarning.setVisibility(View.VISIBLE);
                            txtAddrWarning.setText(mContext.getString(R.string.errDuplicateContactsAddr));
                            return false;
                        }
                    }
                }
            } else {
                lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                txtAddrWarning.setVisibility(View.VISIBLE);
                txtAddrWarning.setText(String.format(mContext.getString(R.string.errWrongAddress), mCoinType.toUpperCase()));
                return false;
            }
        } else {
            if (address.startsWith("0x")) {
                String temp = address.substring(2);
                if (temp.length() != 40) {
                    lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                    txtAddrWarning.setVisibility(View.VISIBLE);
                    txtAddrWarning.setText(String.format(mContext.getString(R.string.errWrongAddress), mCoinType.toUpperCase()));
                    return false;
                } else {
                    for (Contacts contacts : ICONexApp.ETHContacts) {
                        if (contacts.getAddress().equals(address)) {
                            lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                            txtAddrWarning.setVisibility(View.VISIBLE);
                            txtAddrWarning.setText(mContext.getString(R.string.errDuplicateContactsAddr));
                            return false;
                        }
                    }
                }
            } else {
                lineAddress.setBackgroundColor(mContext.getResources().getColor(R.color.colorWarning));
                txtAddrWarning.setVisibility(View.VISIBLE);
                txtAddrWarning.setText(String.format(mContext.getString(R.string.errWrongAddress), mCoinType.toUpperCase()));
                return false;
            }
        }

        return true;
    }

    public void setAddress(String address) {
        editAddress.setText(address);
    }

    public enum MODE {
        ADD,
        MOD
    }

    public interface OnClickListener {
        void onConfirm(MODE mode, String name, String address);

        void scanQRCode();
    }
}
