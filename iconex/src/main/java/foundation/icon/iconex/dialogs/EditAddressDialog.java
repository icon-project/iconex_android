package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.jetbrains.annotations.NotNull;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.Contacts;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.widgets.TTextInputLayout;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;

public class EditAddressDialog extends MessageDialog implements View.OnClickListener {
    private static String TAG = EditAddressDialog.class.getSimpleName();

    TTextInputLayout editName, editAddress;
    ImageButton btnScan;

    boolean isICX;
    String address2edit = null;

    String beforName = "";

    public interface OnCompleteEditListener {
        void onQRCodeScan();
        void onCompleteEdit(boolean isAddMode, String name, String address);
    }
    OnCompleteEditListener listener;


    public void setAddress(String address) {
        Log.d(TAG, "setAddress() called with: address = [" + address + "]");
        editAddress.setText(address);
        validateAddress();
    }

    public EditAddressDialog(@NotNull Context context, boolean isICX, OnCompleteEditListener listener) {
        super(context);
        this.isICX = isICX;
        this.listener = listener;
        buildDialog();
    }

    public EditAddressDialog(@NotNull Context context, boolean isICX, String address2edit, OnCompleteEditListener listener) {
        super(context);
        this.isICX = isICX;
        this.address2edit = address2edit;
        this.listener = listener;
        buildDialog();
    }

    private void buildDialog() {
        // set title
        setTitle(getContext().getString(address2edit == null ? R.string.addContacts : R.string.modContacts));

        // set Content
        View content = View.inflate(getContext(), R.layout.dlg_add_address, null);
        setContent(content);

        // load content ui
        editName = content.findViewById(R.id.edit_name);
        editAddress = content.findViewById(R.id.edit_address);
        btnScan = content.findViewById(R.id.btn_scan);
        editAddress.syncTopHeight(content.findViewById(R.id.con_sync));

        // set Button
        setSingleButton(false);

        // set confirm button
        setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                String name = editName.getText().trim();
                String address = editAddress.getText();

                boolean isAddMode = address2edit == null;
                if (validateName() && (isAddMode ? validateAddress() : true)) {
                    listener.onCompleteEdit(isAddMode, name, address);
                    return true;
                }

                return false;
            }
        });

        initView();
    }

    private void initView() {

        // init data
        editName.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        editName.setPastable(false);

        editAddress.setHint(getContext().getString(isICX ? R.string.hintICXAddress : R.string.hintETHAddress));
        editAddress.setInputType(InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        btnScan.setOnClickListener(this);

        if (address2edit != null) {
            editAddress.setText(address2edit);
            editAddress.setInputEnabled(false);
            btnScan.setVisibility(View.GONE);
        }

        // set edit name------- filter, validation
        editName.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    // filtering name
                    if (s.toString().trim().isEmpty()) {
                        editName.setText("");
                    } else if (s.charAt(0) == ' ') {
                        editName.setText(beforName);
                        editName.setSelection(beforName.length());
                    } else if (Utils.checkByteLength(s.toString()) > 16) {
                        editName.setText(beforName);
                        editName.setSelection(editName.getText().toString().length());
                    } else {
                        beforName = s.toString();
                        validateButtonEnable();
                    }
                } else {
                    validateButtonEnable();
                }
            }
        });
        editName.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                validateName();
            }
        });

        // set edit address------------ filter, validation
        editAddress.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                validateButtonEnable();
            }
        });
        editAddress.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                validateAddress();
            }
        });
    }

    @Override // on btn scan click
    public void onClick(View v) {
        listener.onQRCodeScan();
    }


    private void validateButtonEnable() {
        String name = editName.getText();
        String address = editAddress.getText();

        if (name.isEmpty()) editName.setError(false, null);
        if (address.isEmpty()) editAddress.setError(false, null);

        if (address2edit == null) {
            setConfirmEnable(!name.isEmpty() && !address.isEmpty());
        } else {
            setConfirmEnable(!name.isEmpty());
        }
    }

    private boolean validateName() {
        String name = editName.getText();

        if (name.isEmpty()) {
            return false;
        }

        if (isICX) {
            for (Contacts contacts : ICONexApp.ICXContacts) {
                if (contacts.getName().equals(name)) {
                    editName.setError(true, getContext().getString(R.string.errDuplicateContactsName));
                    return false;
                }
            }
        } else {
            for (Contacts contacts : ICONexApp.ETHContacts) {
                if (contacts.getName().equals(name)) {
                    editName.setError(true, getContext().getString(R.string.errDuplicateContactsName));
                    return false;
                }
            }
        }

        editName.setError(false, null);
        return true;
    }

    private boolean validateAddress() {
        String address = editAddress.getText();
        Log.d(TAG, "validateAddress() called with: address = [\" + address + \"]");

        if (address.isEmpty()) {
            return false;
        }

        if (isICX) {
            if (address.startsWith("hx") || address.startsWith("cx")) {
                String temp = address.substring(2);
                if (temp.length() != 40) {
                    editAddress.setError(true, String.format(
                            getContext().getString(R.string.errWrongAddress), Constants.KS_COINTYPE_ICX));
                    return false;
                } else {
                    for (Contacts contacts : ICONexApp.ICXContacts) {
                        if (contacts.getAddress().equals(address)) {
                            editAddress.setError(true, getContext().getString(R.string.errDupICXAddress));
                            return false;
                        }
                    }
                }
            } else {
                editAddress.setError(true, String.format(
                        getContext().getString(R.string.errWrongAddress), Constants.KS_COINTYPE_ICX));
                return false;
            }
        } else {
            if (address.startsWith("0x")) {
                String temp = address.substring(2);
                if (temp.length() != 40) {
                    editAddress.setError(true, String.format(
                            getContext().getString(R.string.errWrongAddress), Constants.KS_COINTYPE_ETH));
                    return false;
                } else {
                    for (Contacts contacts : ICONexApp.ETHContacts) {
                        if (contacts.getAddress().equals(address)) {
                            editAddress.setError(true, getContext().getString(R.string.errDupETHAddress));
                            return false;
                        }
                    }
                }
            } else {
                editAddress.setError(true, String.format(
                        getContext().getString(R.string.errWrongAddress), Constants.KS_COINTYPE_ETH));
                return false;
            }
        }

        editAddress.setError(false, null);
        return true;
    }

    @Override
    public void show() {
        super.show();
        editName.requestFocus();
    }
}
