package foundation.icon.iconex.dev_mainWallet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import loopchain.icon.wallet.core.Constants;

public class MainWalletServiceHelper {
    private static final String TAG = MainWalletServiceHelper.class.getSimpleName();
    private void Log(String log) { Log.d(TAG, log); }

    public interface OnLoadRemoteDataListener { void onLoadRemoteData(List<String[]> icxBalance, List<String[]> ethBalance, List<String[]> errBalance); }

    private Context mContext;
    private NetworkService mService;
    private OnLoadRemoteDataListener mListener = null;
    private boolean mIsBound = false;

    private Vector<String[]> mIcxBalance = new Vector<>();
    private Vector<String[]> mEthBalance = new Vector<>();
    private Vector<String[]> mErrBalance = new Vector<>();

    private int requestCount;
    private boolean isReceveExchange = false;

    public MainWalletServiceHelper(Context context, OnLoadRemoteDataListener listener) {
        mContext = context;
        mListener = listener;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerBalanceCallback(mBalanceCallback);
            mService.registerExchangeCallback(mExchangeCallback);
            mIsBound = true;
            Log("service connected");

            requestRemoteData();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBound = false;
            Log("service disconnected");
        }
    };

    private NetworkService.BalanceCallback mBalanceCallback = new NetworkService.BalanceCallback() {
        @Override
        public void onReceiveICXBalance(String id, String address, String result) {
            Log("Receive icx balance");
            mIcxBalance.add(new String[] { id, address, result});
            if(isDoneRequest(true, false)) completeRequest();
        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {
            Log("Receive eth balance");
            address = address.substring(2);
            mEthBalance.add(new String[] { id, address, result });

            if(isDoneRequest(true, false)) completeRequest();
        }

        @Override
        public void onReceiveError(String id, String address, int code) {
            Log("Receive balance err code:" + code);
            if (address.startsWith(MyConstants.PREFIX_HEX)) {
                address = address.substring(2);
            }

            mErrBalance.add(new String[] { id, address, MyConstants.NO_BALANCE });

            if(isDoneRequest(true, false)) completeRequest();
        }

        @Override
        public void onReceiveException(String id, String address, String msg) {
            Log("Receive balance exception " + msg);
            if (address.startsWith(MyConstants.PREFIX_HEX)) {
                address = address.substring(2);
            }

            mErrBalance.add(new String[] { id, address, MyConstants.NO_BALANCE });

            if(isDoneRequest(true,false)) completeRequest();
        }
    };

    private NetworkService.ExchangeCallback mExchangeCallback = new NetworkService.ExchangeCallback() {
        @Override
        public void onReceiveExchangeList() {
            Log("Receive Exchange list");
            if(isDoneRequest(false,true)) completeRequest();
        }

        @Override
        public void onReceiveError(String resCode) {
            Log("Receive Exchange error: " + resCode);
            if(isDoneRequest(false,true)) completeRequest();
        }

        @Override
        public void onReceiveException(Throwable t) {
            t.printStackTrace();
            if(isDoneRequest(false,true)) completeRequest();
        }
    };

    private void completeRequest () {
        if (mListener != null) {
            mListener.onLoadRemoteData(
                    new ArrayList<String[]>() {{ addAll(mIcxBalance); }},
                    new ArrayList<String[]>() {{ addAll(mEthBalance); }},
                    new ArrayList<String[]>() {{ addAll(mErrBalance); }}
            );

            mIcxBalance.clear();
            mEthBalance.clear();
            mErrBalance.clear();
        }
    }

    private synchronized boolean isDoneRequest(boolean countingRequest, boolean markingExchange) {
        if (requestCount > 0 && countingRequest) {
            requestCount--;
        }

        if (markingExchange) {
            isReceveExchange = true;
        }

        return requestCount == 0 && isReceveExchange;
    }

    private void cancleRequest() {
        if (!isDoneRequest(false,false)) {
            mService.stopGetBalance();
            mIcxBalance.clear();
            mIcxBalance.clear();
            isReceveExchange = false;
        }
    }

    public void requestRemoteData() {
        cancleRequest();

        Object[] balanceList = makeGetBalanceList();
        HashMap<String, String> icxList = (HashMap<String, String>) balanceList[0];
        HashMap<String, String[]> ircList = (HashMap<String, String[]>) balanceList[1];
        HashMap<String, String> ethList = (HashMap<String, String>) balanceList[2];
        HashMap<String, String[]> ercList = (HashMap<String, String[]>) balanceList[3];

        requestCount = icxList.size() + ircList.size() + ethList.size() + ercList.size();
        mService.getBalance(icxList, Constants.KS_COINTYPE_ICX);
        mService.getTokenBalance(ircList, Constants.KS_COINTYPE_ICX);
        mService.getBalance(ethList, Constants.KS_COINTYPE_ETH);
        mService.getTokenBalance(ercList, Constants.KS_COINTYPE_ETH);

        String exchangeList = makeExchangeList();
        mService.requestExchangeList(exchangeList);
    }

    private Object[] makeGetBalanceList() {
        HashMap<String, String> icxList = new HashMap<>();
        HashMap<String, String> ethList = new HashMap<>();
        HashMap<String, String[]> ercList = new HashMap<>();
        HashMap<String, String[]> ircList = new HashMap<>();

        for (Wallet info : ICONexApp.wallets) {
            if (info.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                List<WalletEntry> entries = info.getWalletEntries();
                for (WalletEntry entry : entries) {
                    if (entry.getType().equals(MyConstants.TYPE_COIN))
                        icxList.put(Integer.toString(entry.getId()), entry.getAddress());
                    else
                        ircList.put(Integer.toString(entry.getId()), new String[]{entry.getAddress(), entry.getContractAddress()});
                }
            } else {
                List<WalletEntry> entries = info.getWalletEntries();
                for (WalletEntry entry : entries) {
                    if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                        ethList.put(Integer.toString(entry.getId()), MyConstants.PREFIX_HEX + entry.getAddress());
                    } else {
                        ercList.put(Integer.toString(entry.getId()), new String[]{MyConstants.PREFIX_HEX + entry.getAddress(), entry.getContractAddress()});
                    }
                }
            }
        }

        return new Object[]{icxList, ircList, ethList, ercList};
    }

    private String makeExchangeList() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < ICONexApp.EXCHANGES.size(); i++) {
            String symbol = ICONexApp.EXCHANGES.get(i);
            sb.append(symbol + MyConstants.EXCHANGE_USD.toLowerCase());
            sb.append(",");
            sb.append(symbol + MyConstants.EXCHANGE_BTC.toLowerCase());
            sb.append(",");
            sb.append(symbol + MyConstants.EXCHANGE_ETH.toLowerCase());

            if (i < ICONexApp.EXCHANGES.size() - 1) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    public void resume() {
        Log("resume");
        Intent intent = new Intent(mContext, NetworkService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        if (mIsBound) requestRemoteData();
    }

    public void stop () {
        Log("stop");
        cancleRequest();
        if (mIsBound) {
            mContext.unbindService(mConnection);
            mIsBound = false;
        }
    }
}
