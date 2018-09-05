package foundation.icon.iconex.wallet.load;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.NetworkService;
import loopchain.icon.wallet.core.Constants;

public class LoadBundleActivity extends AppCompatActivity {

    private static final String TAG = LoadBundleActivity.class.getSimpleName();

    private RecyclerView recyclerView;
    private BundleListAdapter adapter;

    private ArrayList<BundleItem> mBundle;

    public static final String EXTRA_LIST = "EXTRA_LIST";
    public static final int RES_LOAD = 200;

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

            for (int i = 0; i < mBundle.size(); i++) {
                if (id.equals(Integer.toString(mBundle.get(i).getId()))) {
                    mBundle.get(i).setBalance(result);

                    adapter.setData(mBundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {

            for (int i = 0; i < mBundle.size(); i++) {
                if (id.equals(Integer.toString(mBundle.get(i).getId()))) {
                    mBundle.get(i).setBalance(result);

                    adapter.setData(mBundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onReceiveError(String id, String address, int code) {

            for (int i = 0; i < mBundle.size(); i++) {
                if (id.equals(Integer.toString(mBundle.get(i).getId()))) {
                    mBundle.get(i).setBalance(MyConstants.NO_BALANCE);

                    adapter.setData(mBundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }

        }

        @Override
        public void onReceiveException(String id, String address, String msg) {

            for (int i = 0; i < mBundle.size(); i++) {
                if (id.equals(Integer.toString(mBundle.get(i).getId()))) {
                    mBundle.get(i).setBalance(MyConstants.NO_BALANCE);

                    adapter.setData(mBundle);
                    if (isForeground)
                        adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_bundle);

        mBundle = (ArrayList<BundleItem>) getIntent().getSerializableExtra(EXTRA_LIST);

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleLoadBundle));
        findViewById(R.id.btn_back).setVisibility(View.GONE);

        recyclerView = findViewById(R.id.recycler);
        adapter = new BundleListAdapter(this, mBundle);
        recyclerView.setAdapter(adapter);

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button btnLoad = findViewById(R.id.btn_load);
        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RES_LOAD);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        isForeground = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        isForeground = false;

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, String>[] makeRequestList() {
        HashMap<String, String> icxAddresses = new HashMap<>();
        HashMap<String, String> ethAddresses = new HashMap<>();

        for (BundleItem item : mBundle) {
            if (item.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                icxAddresses.put(Integer.toString(item.getId()), item.getAddress());
            } else {
                ethAddresses.put(Integer.toString(item.getId()), MyConstants.PREFIX_ETH + item.getAddress());
            }
        }

        return new HashMap[]{icxAddresses, ethAddresses};
    }
}
