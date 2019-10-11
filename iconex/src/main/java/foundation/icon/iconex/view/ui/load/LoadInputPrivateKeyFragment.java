package foundation.icon.iconex.view.ui.load;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.jetbrains.annotations.NotNull;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.TDropdownLayout;
import foundation.icon.iconex.widgets.TTextInputLayout;
import loopchain.icon.wallet.service.crypto.PKIUtils;

public class LoadInputPrivateKeyFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = LoadInputPrivateKeyFragment.class.getSimpleName();

    private OnLoadPrivateKeyListener mListener;
    private LoadViewModel vm;

    private TTextInputLayout inputPrivateKey;
    private TDropdownLayout dropDown;
    private Button btnNext;

    private List<String> coinList;
    private MyConstants.Coin mCoin;
    private String mPrivateKey;

    private final int RC_COIN = 1001;
    private final int RC_CAPTURE = 1002;

    public LoadInputPrivateKeyFragment() {
        // Required empty public constructor
    }

    public static LoadInputPrivateKeyFragment newInstance() {
        return new LoadInputPrivateKeyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(LoadViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_input_private_key, container, false);
        makeCoinList();
        initView(v);

        dropDown.setText(getString(R.string.coin_icx));

        return v;
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

    private void initView(View v) {
        dropDown = v.findViewById(R.id.drop_down);
        dropDown.setOnClickListener(new TDropdownLayout.OnDropDownClickListener() {
            @Override
            public void onClick() {
                BottomSheetMenuDialog dialog = new BottomSheetMenuDialog(getActivity(), getString(R.string.selectCoinNToken),
                        BottomSheetMenuDialog.SHEET_TYPE.BASIC);
                dialog.setBasicData(coinList);
                dialog.setOnItemClickListener(new BottomSheetMenuDialog.OnItemClickListener() {
                    @Override
                    public void onBasicItem(String item) {
                        dropDown.setSelected(false);
                        dropDown.setText(item);
                        mCoin = MyConstants.Coin.fromLabel(item);
                        if (!inputPrivateKey.getText().isEmpty()) {
                            checkPrivateKey(inputPrivateKey.getText());
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
                        dropDown.deactivate();
                    }
                });
                dialog.show();
            }
        });

        mCoin = MyConstants.Coin.ICX;
        dropDown.setText(coinList.get(0));

        inputPrivateKey = v.findViewById(R.id.input_private_key);
        inputPrivateKey.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() == 0)
                    btnNext.setEnabled(false);
            }
        });
        inputPrivateKey.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                checkPrivateKey(inputPrivateKey.getText());
            }
        });
        inputPrivateKey.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                checkPrivateKey(inputPrivateKey.getText());
            }
        });

        v.findViewById(R.id.btn_qr_scan).setOnClickListener(this);
        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        v.findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick:" + v.getId() + "//" + R.id.drop_down);
        switch (v.getId()) {
            case R.id.drop_down:

                break;

            case R.id.btn_qr_scan:
                startActivityForResult(new Intent(getActivity(), BarcodeCaptureActivity.class)
                        .putExtra(BarcodeCaptureActivity.UseFlash, false)
                        .putExtra(BarcodeCaptureActivity.AutoFocus, true)
                        .putExtra(BarcodeCaptureActivity.PARAM_SCANTYPE, BarcodeCaptureActivity.ScanType.PrivateKey.name()), RC_CAPTURE);
                break;

            case R.id.btn_next:
                vm.setCoinType(mCoin);
                vm.setPrivateKey(inputPrivateKey.getText());
                mListener.onPrivateKeyNext();
                break;

            case R.id.btn_back:
                mListener.onPrivateKeyBack();
                break;
        }
    }

    private void checkPrivateKey(String input) {
        boolean result;
        byte[] decode = null;

        if (input.trim().isEmpty()) {
            inputPrivateKey.setError(true, getString(R.string.loadByPrivateKeyHeader));

            btnNext.setEnabled(false);
            return;
        }

        try {
            decode = Hex.decode(input);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }

        if (result) {
            if (checkAddress(decode, mCoin.getSymbol())) {
                mPrivateKey = input;
                inputPrivateKey.setError(false, null);

                btnNext.setEnabled(true);
            } else {
                inputPrivateKey.setError(true, getString(R.string.duplicateWalletAddress));

                btnNext.setEnabled(false);
            }
        } else {
            inputPrivateKey.setError(true, getString(R.string.errPrivateKey));

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

        for (Wallet info : ICONexApp.wallets) {
            if (info.getAddress().equals(address))
                return false;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    inputPrivateKey.setText(barcode.displayValue);
                    checkPrivateKey(barcode.displayValue);
                } else {
                }
            } else {
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public interface OnLoadPrivateKeyListener {
        void onPrivateKeyNext();

        void onPrivateKeyBack();
    }

    private void makeCoinList() {
        coinList = new ArrayList<>();
        coinList.add(MyConstants.Coin.ICX.getLabel());
        coinList.add(MyConstants.Coin.ETH.getLabel());
    }

    public void clear(boolean inputEnabled) {
        dropDown.setText(getString(R.string.coin_icx));
        mCoin = MyConstants.Coin.ICX;
        inputPrivateKey.setText("");
        inputPrivateKey.setInputEnabled(inputEnabled);
    }
}
