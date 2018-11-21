package foundation.icon.iconex.wallet.menu.bundle;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.EditTextDialog;
import foundation.icon.iconex.util.ConvertUtil;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class MakeBundleFragment extends Fragment {

    private static final String TAG = MakeBundleFragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private BundleRecyclerAdapter adapter;

    private Button btnNext;

    private OnMakeBundleListener mListener;

    private List<BundleItem> mList;
    private int selectedCount = 0;

    private HashMap<String, String> privSet = new HashMap<>();

    public MakeBundleFragment() {
        // Required empty public constructor
    }

    public static MakeBundleFragment newInstance() {
        MakeBundleFragment fragment = new MakeBundleFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mList = makeList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_make_bundle, container, false);

        recyclerView = v.findViewById(R.id.recycler_wallets);

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        mListener.onNext(adapter.getBundle(), privSet);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.setMessage(getString(R.string.msgBundleNotice));
                dialog.show();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        selectedCount = 0;
        adapter = new BundleRecyclerAdapter(getActivity(), mList);
        adapter.setOnWalletClickListener(mWalletClickListener);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMakeBundleListener) {
            mListener = (OnMakeBundleListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMakeBundleListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private List<BundleItem> makeList() {
        List<BundleItem> list = new ArrayList<>();

        BundleItem wallet;
        for (Wallet info : ICONexApp.mWallets) {
            wallet = new BundleItem();
            wallet.setAlias(info.getAlias());
            wallet.setBalance(getBalance(info));
            wallet.setKeyStore(info.getKeyStore());
            wallet.setSymbol(info.getCoinType());
            wallet.setSelected(false);

            list.add(wallet);
        }

        return list;
    }

    private String getBalance(Wallet info) {
        BigInteger balance;

        try {
            balance = new BigInteger(info.getWalletEntries().get(0).getBalance());
            return ConvertUtil.getValue(balance, info.getWalletEntries().get(0).getDefaultDec());
        } catch (Exception e) {
            return MyConstants.NO_BALANCE;
        }
    }

    private EditTextDialog dialog;

    private int mPosition;
    private String address;
    private JsonObject crypto;
    private String coinType;

    private BundleRecyclerAdapter.OnWalletClickListener mWalletClickListener = new BundleRecyclerAdapter.OnWalletClickListener() {
        @Override
        public void onWalletSelected(int position, BundleItem wallet) {
            JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);
            address = keyStore.get("address").getAsString();

            if (wallet.isSelected()) {
                adapter.setSelected(position, false);
                if (selectedCount != 0) {
                    selectedCount--;
                    privSet.remove(address);
                    if (selectedCount == 0) {
                        btnNext.setEnabled(false);
                    }
                }
            } else {
                mPosition = position;

                if (keyStore.has("coinType"))
                    coinType = Constants.KS_COINTYPE_ICX;
                else
                    coinType = Constants.KS_COINTYPE_ETH;

                if (keyStore.has("crypto"))
                    crypto = keyStore.get("crypto").getAsJsonObject();
                else if (keyStore.has("Crypto"))
                    crypto = keyStore.get("Crypto").getAsJsonObject();


                dialog = new EditTextDialog(getActivity(), getString(R.string.enterWalletPassword));
                dialog.setOnPasswordCallback(mPasswordCallback);
                dialog.setHint(getString(R.string.hintWalletPassword));
                dialog.show();
            }
        }
    };

    private EditTextDialog.OnPasswordCallback mPasswordCallback = new EditTextDialog.OnPasswordCallback() {
        @Override
        public void onConfirm(EditTextDialog.RESULT_PWD result, String text) {
            byte[] privKey = null;
            try {
                privKey = KeyStoreUtils.decryptPrivateKey(text, address, crypto, coinType);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (privKey != null) {
                adapter.setSelected(mPosition, true);
                selectedCount++;
                privSet.put(address, Hex.toHexString(privKey));
                btnNext.setEnabled(true);
                dialog.dismiss();
            } else {
                dialog.setError(getString(R.string.errPassword));
            }
        }
    };

    public interface OnMakeBundleListener {
        void onNext(List<Wallet> bundle, HashMap<String, String> privSet);
    }
}
