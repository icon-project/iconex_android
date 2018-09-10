package foundation.icon.iconex.wallet.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.EditTextDialog;
import foundation.icon.iconex.token.swap.TokenSwapActivity;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.detail.WalletDetailActivity;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.PKIUtils;

import static foundation.icon.iconex.MyConstants.EXCHANGE_USD;
import static foundation.icon.iconex.MyConstants.NO_BALANCE;

public class CoinFragment extends Fragment {

    private static final String TAG = CoinFragment.class.getSimpleName();

    private String mName;
    private List<Wallet> mList;
    private int mDecimals;
    private String mSymbol;

    private String mType;
    private CoinsViewItem mItem;

    private static final String ARG_NAME = "ARG_NAME";
    private static final String ARG_LIST = "ARG_LIST";
    private static final String ARG_ITEM = "ARG_ITEM";

    private TextView txtName;
    private TextView txtAsset, txtSym;
    private RecyclerView recyclerView;
    private CoinRecyclerAdapter adapter;

    private ViewGroup loadingBalance;

    private Wallet mWallet;
    private WalletEntry mToken;

    private final int RC_SWAP = 301;

    public CoinFragment() {
        // Required empty public constructor
    }

    public static CoinFragment newInstance(CoinsViewItem item) {
        CoinFragment fragment = new CoinFragment();
        Bundle bundle = new Bundle();
//        bundle.putString(ARG_NAME, name);
//        bundle.putParcelableArrayList(ARG_LIST, list);
        bundle.putSerializable(ARG_ITEM, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mItem = (CoinsViewItem) getArguments().getSerializable(ARG_ITEM);
//            mName = getArguments().getString(ARG_NAME);
//            mList = (ArrayList<Wallet>) getArguments().get(ARG_LIST);
            mType = mItem.getType();
            mName = mItem.getName();
            mSymbol = mItem.getSymbol();
            mList = mItem.getWallets();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_coin, container, false);

        txtName = v.findViewById(R.id.txt_name);
        txtName.setText(mName);
        txtAsset = v.findViewById(R.id.txt_total_balance);
        txtSym = v.findViewById(R.id.txt_balance_unit);

        recyclerView = v.findViewById(R.id.recycler_wallets);

        loadingBalance = v.findViewById(R.id.loading_balance);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        coinsNotifyDataChanged();
    }

    private void setTotalAsset() {
        Double totalBalance = 0.0;
        int cntNoBalance = 0;
        int cntTarget = 0;
        String unit = ((MainActivity) getActivity()).getExchangeUnit();

        for (Wallet wallet : mList) {
            for (WalletEntry entry : wallet.getWalletEntries()) {
                if (entry.getType().equals(mType)) {
                    if (entry.getSymbol().equals(mSymbol)) {
                        if (!entry.getBalance().isEmpty()) {

                            if (entry.getBalance().equals(NO_BALANCE)) {
                                cntNoBalance++;
                            } else {
                                try {
                                    String value = ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec());
                                    Double doubBalance = Double.parseDouble(value);
                                    String exchange = entry.getSymbol().toLowerCase() + ((MainActivity) getActivity()).getExchangeUnit().toLowerCase();
                                    String strPrice;
                                    if (exchange.equals("etheth"))
                                        strPrice = "1";
                                    else
                                        strPrice = ICONexApp.EXCHANGE_TABLE.get(exchange);
                                    if (strPrice != null) {
                                        totalBalance += doubBalance * Double.parseDouble(strPrice);
                                        if (strPrice.equals("0"))
                                            cntNoBalance++;
                                    }
                                } catch (Exception e) {
                                    // Do nothing.
                                }
                            }
                        }

                        cntTarget++;
                    }
                }
            }
        }

        if (cntNoBalance == cntTarget) {
            txtAsset.setText(NO_BALANCE);
        } else {
            if (unit.equals(EXCHANGE_USD)) {
                txtAsset.setText(String.format(Locale.getDefault(), "%,.2f", totalBalance));
            } else {
                txtAsset.setText(String.format(Locale.getDefault(), "%,.4f", totalBalance));
            }
        }

        txtSym.setText(unit);
    }

    public void coinsNotifyDataChanged() {

        loadingBalance.setVisibility(View.VISIBLE);


        List<Wallet> list = new ArrayList<>();
        mList = new ArrayList<>();

        for (Wallet wallet : ICONexApp.mWallets) {
            for (WalletEntry entry : wallet.getWalletEntries()) {
                if (entry.getType().equals(mType)) {
                    if (entry.getSymbol().equals(mSymbol)) {
                        list.add(wallet);
                        mDecimals = entry.getDefaultDec();
                    }
                }
            }
        }
        mList.addAll(list);

        if (loadingBalance.getVisibility() == View.VISIBLE) {

            boolean isDone = true;
            for (Wallet wallet : mList) {
                for (WalletEntry entry : wallet.getWalletEntries()) {
                    if (entry.getBalance().isEmpty()) {
                        isDone = isDone && false;
                    } else {
                        isDone = isDone && true;
                    }
                }
            }

            if (isDone) {
                loadingBalance.setVisibility(View.GONE);
            }
        }

        setTotalAsset();

        adapter = new CoinRecyclerAdapter(getActivity(), mType, mList, mDecimals, mSymbol);
        adapter.setClickListener(new CoinRecyclerAdapter.WalletClickListener() {
            @Override
            public void onWalletClick(Wallet wallet, String symbol) {
                WalletEntry target = null;
                for (WalletEntry entry : wallet.getWalletEntries())
                    if (entry.getSymbol().equals(symbol))
                        target = entry;
                startActivity(new Intent(getActivity(), WalletDetailActivity.class)
                        .putExtra("walletInfo", (Serializable) wallet)
                        .putExtra("walletEntry", (Serializable) target));
            }

            @Override
            public void onRequestSwap(Wallet wallet) {
                mWallet = wallet;
                WalletEntry own = wallet.getWalletEntries().get(0);

                for (WalletEntry entry : wallet.getWalletEntries()) {
                    if (entry.getSymbol().equals(Constants.KS_COINTYPE_ICX))
                        mToken = entry;
                }

                BasicDialog dialog = new BasicDialog(getActivity());
                try {
                    BigInteger tBalance = new BigInteger(mToken.getBalance());
                    if (tBalance.equals(BigInteger.ZERO)) {
                        dialog.setMessage(getString(R.string.swapMsgHasNoToken));
                        dialog.show();
                        return;
                    }

                    BigInteger eBalance = new BigInteger(own.getBalance());
                    if (eBalance.equals(BigInteger.ZERO)) {
                        dialog.setMessage(getString(R.string.swapMsgNotEnoughFee));
                        dialog.show();
                        return;
                    }

                    editTextDialog = new EditTextDialog(getActivity(), getString(R.string.enterWalletPassword));
                    editTextDialog.setHint(getString(R.string.hintWalletPassword));
                    editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                    editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.SWAP);
                    editTextDialog.setOnPasswordCallback(mPasswordDialogCallback);
                    editTextDialog.show();
                } catch (Exception e) {
                    dialog.setMessage(getString(R.string.swapMsgHasNoToken));
                    dialog.show();
                }
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private EditTextDialog editTextDialog;
    private EditTextDialog.OnPasswordCallback mPasswordDialogCallback = new EditTextDialog.OnPasswordCallback() {
        @Override
        public void onConfirm(EditTextDialog.RESULT_PWD result, String pwd) {
            JsonObject keyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);
            byte[] bytePrivKey;
            try {
                JsonObject crypto = null;
                if (keyStore.has("crypto"))
                    crypto = keyStore.get("crypto").getAsJsonObject();
                else
                    crypto = keyStore.get("Crypto").getAsJsonObject();

                bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, mWallet.getAddress(), crypto, mWallet.getCoinType());
                if (bytePrivKey != null) {
                    if (result == EditTextDialog.RESULT_PWD.SWAP) {

                        try {
                            Intent swapIntent = new Intent(getActivity(), TokenSwapActivity.class);
                            swapIntent.putExtra(TokenSwapActivity.ARG_WALLET, (Serializable) mWallet);
                            swapIntent.putExtra(TokenSwapActivity.ARG_TOKEN, (Serializable) mToken);
                            String address = PKIUtils.makeAddressFromPrivateKey(bytePrivKey, Constants.KS_COINTYPE_ICX);
                            if (hasSwapWallet(address))
                                swapIntent.putExtra(TokenSwapActivity.ARG_TYPE, TokenSwapActivity.TYPE_SWAP.EXIST);
                            else
                                swapIntent.putExtra(TokenSwapActivity.ARG_TYPE, TokenSwapActivity.TYPE_SWAP.NO_WALLET);
                            swapIntent.putExtra(TokenSwapActivity.ARG_ICX_ADDR, address);
                            swapIntent.putExtra(TokenSwapActivity.ARG_PRIV, Hex.toHexString(bytePrivKey));

                            startActivityForResult(swapIntent, RC_SWAP);
                        } catch (Exception e) {
                            // TODO: 2018. 5. 16. Notice error
                        }
                    }

                    editTextDialog.dismiss();
                } else {
                    editTextDialog.setError(getString(R.string.errPassword));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private boolean hasSwapWallet(String address) throws Exception {
        for (Wallet wallet : ICONexApp.mWallets) {
            if (address.equals(wallet.getAddress()))
                return true;
        }

        return false;
    }
}
