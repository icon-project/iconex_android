package foundation.icon.iconex.view.ui.load;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.view.MainWalletActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.DividerItemDecorator;
import loopchain.icon.wallet.core.Constants;

public class LoadBundleFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = LoadBundleFragment.class.getSimpleName();

    public static LoadBundleFragment newInstance() {
        return new LoadBundleFragment();
    }

    private OnLoadBundleListener mListener;
    private LoadViewModel vm;

    private RecyclerView list;
    private BundleListAdapter adapter;

    private List<BundleItem> bundle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(LoadViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_bundle, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnLoadBundleListener) {
            mListener = (OnLoadBundleListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnLoadBundleListener");
        }

        Intent intent = new Intent(getContext(), NetworkService.class);
        getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;

        isForeground = false;

        if (mBound) {
            getContext().unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    private void initView(View v) {
        list = v.findViewById(R.id.list);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecorator(getContext(),
                        ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        list.addItemDecoration(itemDecoration);

        adapter = new BundleListAdapter(getContext(), makeList());
        list.setAdapter(adapter);

        v.findViewById(R.id.btn_complete).setOnClickListener(this);
        v.findViewById(R.id.btn_back).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_complete:
                try {
                    saveWallets();
                    startActivity(new Intent(getActivity(), MainWalletActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("bundleDone", true));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case R.id.btn_back:
                mListener.onBundleBack();
                break;
        }
    }

    private List<BundleItem> makeList() {
        bundle = new ArrayList<>();
        List<Wallet> wallets = vm.getBundle().getValue();

        for (Wallet w : wallets) {
            BundleItem item = new BundleItem();

            item.setId(new Random().nextInt(999999) + 100000);
            item.setAddress(w.getAddress());
            item.setAlias(w.getAlias());
            item.setBalance(MyConstants.NO_BALANCE);
            item.setSymbol(w.getWalletEntries().get(0).getSymbol());
            item.setCoinType(w.getCoinType());
            item.setRegistered(checkRegistered(w.getAddress()));

            bundle.add(item);
        }

        return bundle;
    }

    private boolean checkRegistered(String address) {
        for (Wallet w : ICONexApp.wallets) {
            if (w.getAddress().equals(address))
                return true;
        }

        return false;
    }

    private void saveWallets() throws Exception {
        for (Wallet w : vm.getBundle().getValue()) {
            if (checkRegistered(w.getAddress()))
                RealmUtil.overwriteWallet(w.getAddress(), w);
            else
                RealmUtil.addWallet(w);
        }

        RealmUtil.loadWallet();
    }

    public interface OnLoadBundleListener {
        void onBundleBack();
    }

    private boolean mBound = false;
    private NetworkService mService;
    private boolean isForeground = true;

    private ServiceConnection mConnection = new ServiceConnection() {
        @SuppressWarnings("unchecked")
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerBalanceCallback(mBalanceCallback);
            mBound = true;

            if (mBound) {
                HashMap[] addrMap = makeRequestList();
                HashMap<String, String> icxAddresses = addrMap[0];
                HashMap<String, String> ethAddresses = addrMap[1];

                mService.getBalance(icxAddresses, Constants.KS_COINTYPE_ICX);
                mService.getBalance(ethAddresses, Constants.KS_COINTYPE_ETH);
            } else {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private NetworkService.BalanceCallback mBalanceCallback = new NetworkService.BalanceCallback() {
        @Override
        public void onReceiveICXBalance(String id, String address, String result) {

            for (int i = 0; i < bundle.size(); i++) {
                if (id.equals(Integer.toString(bundle.get(i).getId()))) {
                    bundle.get(i).setBalance(result);

                    adapter.setData(bundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {

            for (int i = 0; i < bundle.size(); i++) {
                if (id.equals(Integer.toString(bundle.get(i).getId()))) {
                    bundle.get(i).setBalance(result);

                    adapter.setData(bundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onReceiveError(String id, String address, int code) {

            for (int i = 0; i < bundle.size(); i++) {
                if (id.equals(Integer.toString(bundle.get(i).getId()))) {
                    bundle.get(i).setBalance(MyConstants.NO_BALANCE);

                    adapter.setData(bundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }

        }

        @Override
        public void onReceiveException(String id, String address, String msg) {

            for (int i = 0; i < bundle.size(); i++) {
                if (id.equals(Integer.toString(bundle.get(i).getId()))) {
                    bundle.get(i).setBalance(MyConstants.NO_BALANCE);

                    adapter.setData(bundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @SuppressWarnings("unchecked")
    private HashMap<String, String>[] makeRequestList() {
        HashMap<String, String> icxAddresses = new HashMap<>();
        HashMap<String, String> ethAddresses = new HashMap<>();

        for (BundleItem item : bundle) {
            if (item.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                icxAddresses.put(Integer.toString(item.getId()), item.getAddress());
            } else {
                ethAddresses.put(Integer.toString(item.getId()), MyConstants.PREFIX_HEX + item.getAddress());
            }
        }

        return new HashMap[]{icxAddresses, ethAddresses};
    }
}
