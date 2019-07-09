package foundation.icon.connect;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import loopchain.icon.wallet.core.Constants;

public class SelectWalletActivity extends AppCompatActivity implements View.OnClickListener, SelectWalletAdapter.OnWalletSelectListener {
    private static final String TAG = SelectWalletAdapter.class.getSimpleName();

    private Button btnConfirm;
    private RecyclerView listWallets;
    private SelectWalletAdapter listAdapter;

    private NetworkService mService;
    private boolean mBound = false;

    private List<Wallet> mList;

    private int reqId;
    private RequestData request;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerBalanceCallback(mBalanceCallback);
            mBound = true;


            if (mBound) {
                getBalance();
            } else {
                Toast.makeText(SelectWalletActivity.this, "Bind Failed.", Toast.LENGTH_SHORT).show();
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
            for (int i = 0; i < mList.size(); i++) {
                WalletEntry entry = mList.get(i).getWalletEntries().get(0);
                if (Integer.toString(entry.getId()).equals(id)) {
                    mList.get(i).getWalletEntries().get(0).setBalance(result);
                    listAdapter.setBalance(mList);
                }
            }
        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {

        }

        @Override
        public void onReceiveError(String id, String address, int code) {
            for (int i = 0; i < mList.size(); i++) {
                WalletEntry entry = mList.get(i).getWalletEntries().get(0);
                if (Integer.toString(entry.getId()).equals(id)) {
                    mList.get(0).getWalletEntries().get(0).setBalance(MyConstants.NO_BALANCE);
                }
            }
        }

        @Override
        public void onReceiveException(String id, String address, String msg) {
            for (int i = 0; i < mList.size(); i++) {
                WalletEntry entry = mList.get(i).getWalletEntries().get(0);
                if (Integer.toString(entry.getId()).equals(id)) {
                    mList.get(0).getWalletEntries().get(0).setBalance(MyConstants.NO_BALANCE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_wallet);

        if (getIntent() != null) {
            reqId = getIntent().getIntExtra("id", -1);
            request = (RequestData) getIntent().getExtras().get("request");
        }

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.selectWallet));
        findViewById(R.id.btn_close).setOnClickListener(this);
        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        listWallets = findViewById(R.id.list_wallets);

        mList = makeWalletList();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        listAdapter = new SelectWalletAdapter(this, mList, this);
        listWallets.setAdapter(listAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mService != null)
            unbindService(mConnection);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        ResponseData resData;

        switch (v.getId()) {
            case R.id.btn_close:
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
                dialog.setMessage(getString(R.string.msgCancelBind));
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        IconexConnect.sendError(SelectWalletActivity.this, request,
                                new ErrorCodes.Error(ErrorCodes.ERR_USER_CANCEL, ErrorCodes.MSG_USER_CANCEL));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.show();
                break;
            case R.id.btn_confirm:
                String address = listAdapter.getSelected();
                IconexConnect.sendResponse(SelectWalletActivity.this, request, address);
                break;
        }
    }

    private List<Wallet> makeWalletList() {
        List<Wallet> list = new ArrayList<>();

        for (Wallet wallet : ICONexApp.mWallets) {
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
                list.add(wallet);
        }

        return list;
    }

    private void getBalance() {
        HashMap<String, String> icxList = makeGetBalanceList();
        mService.getBalance(icxList, Constants.KS_COINTYPE_ICX);
    }

    private HashMap<String, String> makeGetBalanceList() {

        HashMap<String, String> icxList = new HashMap<>();

        for (Wallet wallet : mList) {
            WalletEntry entry = wallet.getWalletEntries().get(0);
            icxList.put(Integer.toString(entry.getId()), entry.getAddress());
        }

        return icxList;
    }

    @Override
    public void onSelect() {
        btnConfirm.setEnabled(true);
    }

    @Override
    public void onBackPressed() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
        dialog.setMessage(getString(R.string.msgCancelBind));
        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
            @Override
            public void onOk() {
                IconexConnect.sendError(SelectWalletActivity.this, request,
                        new ErrorCodes.Error(ErrorCodes.ERR_USER_CANCEL, ErrorCodes.MSG_USER_CANCEL));
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.show();
    }
}
