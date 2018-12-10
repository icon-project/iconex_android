package foundation.icon.iconex.wallet.load;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.BottomItemSelectActivity;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.DropdownLayout;
import foundation.icon.iconex.widgets.MyEditText;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.PKIUtils;

public class LoadInputPrivateKeyFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LoadInputPrivateKeyFragment.class.getSimpleName();

    private OnLoadPrivateKeyListener mListener;

    private ViewGroup layoutInput;

    private DropdownLayout dropDown;
    private MyEditText editPriv;
    private ImageView btnScan;
    private View linePriv;
    private Button btnPrivDelete;
    private TextView txtPrivWarning;
    private Button btnNext, btnBack;

    private String mCoinType;
    private String mPrivateKey;

    private final int RC_COIN = 1001;
    private final int RC_CAPTURE = 1002;

    private ArrayList<String> coinList = new ArrayList<>();

    private InputMethodManager mImm;

    public LoadInputPrivateKeyFragment() {
        // Required empty public constructor
    }

    public static LoadInputPrivateKeyFragment newInstance() {
        LoadInputPrivateKeyFragment fragment = new LoadInputPrivateKeyFragment();
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
        View v = inflater.inflate(R.layout.fragment_load_input_private_key, container, false);

        makeCoinList();

        layoutInput = v.findViewById(R.id.layout_input);

        dropDown = v.findViewById(R.id.drop_down);
        dropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropDown.setSelected(true);
                BottomSheetMenuDialog dialog = new BottomSheetMenuDialog(getActivity(), getString(R.string.selectCoinNToken),
                        BottomSheetMenuDialog.SHEET_TYPE.BASIC);
                dialog.setBasicData(coinList);
                dialog.setOnItemClickListener(new BottomSheetMenuDialog.OnItemClickListener() {
                    @Override
                    public void onBasicItem(String item) {
                        dropDown.setSelected(false);
                        if (item.contains(Constants.KS_COINTYPE_ICX)) {
                            dropDown.setItem(getString(R.string.coin_icx));
                            mCoinType = Constants.KS_COINTYPE_ICX;
                            if (!editPriv.getText().toString().isEmpty()) {
                                checkPrivKey(editPriv.getText().toString());
                            }
                        } else if (item.contains(Constants.KS_COINTYPE_ETH)) {
                            dropDown.setItem(getString(R.string.coin_eth));
                            mCoinType = Constants.KS_COINTYPE_ETH;
                            if (!editPriv.getText().toString().isEmpty()) {
                                checkPrivKey(editPriv.getText().toString());
                            }
                        } else {
                            btnNext.setEnabled(false);
                        }
                    }

                    @Override
                    public void onCoinItem(int position) {

                    }

                    @Override
                    public void onMenuItem(String tag) {

                    }
                });
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dropDown.setSelected(false);
                    }
                });
                dialog.show();
            }
        });
        mCoinType = Constants.KS_COINTYPE_ICX;
        dropDown.setItem(getString(R.string.coin_icx));

        btnScan = v.findViewById(R.id.btn_qr_scan);
        btnScan.setOnClickListener(this);

        editPriv = v.findViewById(R.id.edit_priv);
        editPriv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    linePriv.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    linePriv.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });
        editPriv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnPrivDelete.setVisibility(View.VISIBLE);
                } else {
                    btnPrivDelete.setVisibility(View.INVISIBLE);
                    btnNext.setEnabled(false);
                }

//                int lines = editPriv.getLineCount();
//                if (lines > 2) {
//                    editPriv.getText().delete(editPriv.getSelectionEnd() - 1, editPriv.getSelectionStart());
//                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editPriv.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                hideInputMode();
                checkPrivKey(editPriv.getText().toString());
            }
        });
//        editPriv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
//                    hideInputMode();
//                    checkPrivKey(editPriv.getText().toString());
//                }
//                return false;
//            }
//        });
        editPriv.setOnEditTouchListener(new MyEditText.OnEditTouchListener() {
            @Override
            public void onTouch() {
                showInputMode(editPriv);
            }
        });

        linePriv = v.findViewById(R.id.line_priv);
        txtPrivWarning = v.findViewById(R.id.txt_priv_warning);
        btnPrivDelete = v.findViewById(R.id.btn_priv_delete);
        btnPrivDelete.setOnClickListener(this);

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_qr_scan:
                hideInputMode();
                startActivityForResult(new Intent(getActivity(), BarcodeCaptureActivity.class)
                        .putExtra(BarcodeCaptureActivity.UseFlash, false)
                        .putExtra(BarcodeCaptureActivity.AutoFocus, true), RC_CAPTURE);
                break;

            case R.id.btn_priv_delete:
                editPriv.setText("");
                txtPrivWarning.setVisibility(View.INVISIBLE);
                if (editPriv.isFocused())
                    linePriv.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    linePriv.setBackgroundColor(getResources().getColor(R.color.editNormal));
                break;

            case R.id.btn_next:
                mListener.onLoadPrivateKeyNext(mCoinType, mPrivateKey);
                clear();
                break;

            case R.id.btn_back:
                mListener.onPrivBack();
                clear();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLoadPrivateKeyListener) {
            mListener = (OnLoadPrivateKeyListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnLoadPrivateKeyListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void checkPrivKey(String privKey) {
        boolean result;
        byte[] decode = null;

        if (privKey.trim().isEmpty()) {
            linePriv.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtPrivWarning.setVisibility(View.VISIBLE);
            txtPrivWarning.setText(getString(R.string.loadByPrivateKeyHeader));

            btnNext.setEnabled(false);
            return;
        }

        try {
            decode = Hex.decode(privKey);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        if (result) {
            if (dropDown.getItem().isEmpty()) {
                btnNext.setEnabled(false);
                return;
            }

            if (checkAddress(decode, mCoinType)) {
                if (editPriv.hasFocus())
                    linePriv.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    linePriv.setBackgroundColor(getResources().getColor(R.color.editNormal));

                txtPrivWarning.setVisibility(View.INVISIBLE);
                mPrivateKey = privKey;

                btnNext.setEnabled(true);
            } else {
                linePriv.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtPrivWarning.setVisibility(View.VISIBLE);
                txtPrivWarning.setText(getString(R.string.duplicateWalletAddress));

                btnNext.setEnabled(false);
            }
        } else {
            linePriv.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtPrivWarning.setVisibility(View.VISIBLE);
            txtPrivWarning.setText(getString(R.string.errPrivateKey));

            btnNext.setEnabled(false);
        }
    }

    private boolean checkAddress(byte[] privateKey, String coinType) {

        String address;

        try {
            address = PKIUtils.makeAddressFromPrivateKey(privateKey, coinType);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        for (Wallet info : ICONexApp.mWallets) {
            if (info.getAddress().equals(address))
                return false;
        }

        return true;
    }

    private void showInputMode(View view) {
        view.requestFocus();
        mImm.showSoftInput(view, 0);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutInput.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.setMargins(0, (int) getResources().getDimension(R.dimen.ChangePwdMarginTopShow), 0, 0);
        layoutInput.setLayoutParams(layoutParams);
    }

    private void hideInputMode() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layoutInput.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.drop_down);
        layoutParams.setMargins(0, (int) getResources().getDimension(R.dimen.dp10), 0, 0);
        layoutInput.setLayoutParams(layoutParams);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_COIN) {
            dropDown.setSelected(false);
            if (resultCode == BottomItemSelectActivity.CODE_BASIC) {

                if (data.getExtras().getString("item").contains(Constants.KS_COINTYPE_ICX)) {
                    dropDown.setItem(getString(R.string.coin_icx));
                    mCoinType = Constants.KS_COINTYPE_ICX;
                    if (!editPriv.getText().toString().isEmpty()) {
                        checkPrivKey(editPriv.getText().toString());
                    }
                } else if (data.getExtras().getString("item").contains(Constants.KS_COINTYPE_ETH)) {
                    dropDown.setItem(getString(R.string.coin_eth));
                    mCoinType = Constants.KS_COINTYPE_ETH;
                    if (!editPriv.getText().toString().isEmpty()) {
                        checkPrivKey(editPriv.getText().toString());
                    }
                } else {
                    btnNext.setEnabled(false);
                }
            }
        } else if (requestCode == RC_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    editPriv.setText(barcode.displayValue);
                    editPriv.setSelection(editPriv.getText().toString().length());
                    checkPrivKey(editPriv.getText().toString());
                } else {
                }
            } else {
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface OnLoadPrivateKeyListener {
        void onLoadPrivateKeyNext(String coinType, String privKey);

        void onPrivBack();
    }

    private void makeCoinList() {
        coinList.add("ICON (ICX)");
        coinList.add("Ethereum (ETH)");
    }

    private void clear() {
        dropDown.setItem(getString(R.string.coin_icx));
        mCoinType = Constants.KS_COINTYPE_ICX;
        editPriv.setText("");
    }
}
