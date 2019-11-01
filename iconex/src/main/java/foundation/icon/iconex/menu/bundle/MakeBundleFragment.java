package foundation.icon.iconex.menu.bundle;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.util.ConvertUtil;
import kotlin.jvm.functions.Function1;

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
                MessageDialog messageDialog = new MessageDialog(getContext());
                messageDialog.setSingleButton(false);
                messageDialog.setMessage(getString(R.string.msgBundleNotice));
                messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        mListener.onNext(adapter.getBundle(), privSet);
                        return true;
                    }
                });
                messageDialog.show();
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
        for (Wallet info : ICONexApp.wallets) {
            wallet = new BundleItem();
            wallet.setAlias(info.getAlias());
            wallet.setBalance(getBalance(info));
            wallet.setKeyStore(info.getKeyStore());
            wallet.setSymbol(info.getCoinType());
            wallet.setWallet(info);
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

    private BundleRecyclerAdapter.OnWalletClickListener mWalletClickListener = new BundleRecyclerAdapter.OnWalletClickListener() {
        @Override
        public void onWalletSelected(int position, BundleItem bundleItem) {
            Wallet wallet = bundleItem.getWallet();

            if (bundleItem.isSelected()) {
                adapter.setSelected(position, false);
                if (selectedCount != 0) {
                    selectedCount--;
                    privSet.remove(wallet.getAddress());
                    if (selectedCount == 0) {
                        btnNext.setEnabled(false);
                    }
                }
            } else {
                new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                    @Override
                    public void onPass(byte[] bytePrivateKey) {
                        adapter.setSelected(position, true);
                        selectedCount++;
                        privSet.put(wallet.getAddress(), Hex.toHexString(bytePrivateKey));
                        btnNext.setEnabled(true);
                    }
                }).show();
            }
        }
    };

    public interface OnMakeBundleListener {
        void onNext(List<Wallet> bundle, HashMap<String, String> privSet);
    }
}
