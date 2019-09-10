package foundation.icon.iconex.dev2_detail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.dev2_detail.component.TransactionItemViewData;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class WalletDetailServiceHelper {
    public static final String TAG = WalletDetailServiceHelper.class.getSimpleName();

    private Context mContext;
    private WalletDetailViewModel mViewModle = null;

    private NetworkService mService = null;
    private boolean mIsBind = false;

    private List<TransactionItemViewData> mCacheTxData = new ArrayList<>();
    private int mCountTxData = 0;
    private int mLoadCursor = 1;

    public interface OnServiceReadyListener { void onReady(); }
    private OnServiceReadyListener mOnServiceReadyListener = null;

    public boolean isBind() {
        return mIsBind;
    }

    public WalletDetailServiceHelper(Context context, WalletDetailViewModel viewModel) {
        mContext = context;
        mViewModle = viewModel;
    }

    public void onStart() {
        Intent intent = new Intent(mContext, NetworkService.class);
        mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onStop() {
        if (mIsBind) {
            mContext.unbindService(mConnection);
            mIsBind = false;
        }
    }

    public void setOnServiceReadyListener(OnServiceReadyListener listener) {
        mOnServiceReadyListener = listener;
    }

    public void loadIcxTxList() {
        Log.d(TAG, "Load ICX Tx List");
        mCacheTxData = new ArrayList<>();
        Wallet wallet = mViewModle.wallet.getValue();
        WalletEntry walletEntry = mViewModle.walletEntry.getValue();
        mLoadCursor = 1;

        if ("ICX".equals(wallet.getCoinType())) {
            if ("COIN".equals(walletEntry.getType())) {
                // ICX COIN
                mService.requestICONTxList(wallet.getAddress(), 1);
            } else { // if ("TOKEN".equals(walletEntry.getType()))
                // ICX TOKEN
                mService.requestIrcTxList(wallet.getAddress(),
                        walletEntry.getContractAddress(), 1);
            }
        }
    }

    public void loadMoreIcxTxList() {
        Log.d(TAG, "check Load more");
        if (mCacheTxData.size() < mCountTxData) {
            Log.d(TAG, "load more");
            Wallet wallet = mViewModle.wallet.getValue();
            WalletEntry walletEntry = mViewModle.walletEntry.getValue();

            if ("ICX".equals(wallet.getCoinType())) {
                if ("COIN".equals(walletEntry.getType())) {
                    // ICX COIN
                    mService.requestICONTxList(wallet.getAddress(), ++mLoadCursor);
                } else { // if ("TOKEN".equals(walletEntry.getType()))
                    // ICX TOKEN
                    mService.requestIrcTxList(wallet.getAddress(),
                            walletEntry.getContractAddress(), ++mLoadCursor);
                }
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "ready!");
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerBalanceCallback(mBalanceCallback);
            mService.registerTxListCallback(mIcxTxCallback);
            mIsBind = true;

            if (mOnServiceReadyListener != null) {
                mOnServiceReadyListener.onReady();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mIsBind = false;
        }
    };

    private NetworkService.TxListCallback mIcxTxCallback = new NetworkService.TxListCallback() {
        @Override
        public void onReceiveTransactionList(int totalData, JsonArray txList) {
            Log.d(TAG, "receive list");
            mCountTxData = totalData;
            Wallet wallet = mViewModle.wallet.getValue();
            WalletEntry walletEntry = mViewModle.walletEntry.getValue();

            for (int i = 0; txList.size() > i; i ++) {
                TransactionItemViewData viewData = new TransactionItemViewData();
                JsonObject tx = txList.get(i).getAsJsonObject();

                viewData.setFrom(tx.get("fromAddr").getAsString());
                viewData.setTo(tx.get("toAddr").getAsString());
                viewData.setFee(tx.get("fee").getAsString());
                viewData.setState(Integer.parseInt(tx.get("state").getAsString()));

                if ("ICX".equals(wallet.getCoinType())) {
                    if ("COIN".equals(walletEntry.getType())) {
                        // ICX COIN
                        if ("0".equals(tx.get("txType").getAsString())) {
                            viewData.setTxHash(tx.get("txHash").getAsString());
                            viewData.setDate(tx.get("createDate").getAsString());
                            viewData.setAmount(tx.get("amount").getAsString());
                            mCacheTxData.add(viewData);
                        }
                    } else { // if ("TOKEN".equals(walletEntry.getType()))
                        // ICX TOKEN
                        viewData.setTxHash(tx.get("txHash").getAsString());
                        viewData.setDate(tx.get("age").getAsString());
                        viewData.setAmount(tx.get("quantity").getAsString());
                        mCacheTxData.add(viewData);
                    }
                }
            }

            mViewModle.lstTxData.postValue(mCacheTxData);
        }

        @Override
        public void onReceiveError(String resCode) {
            Log.d(TAG, "error: " + resCode);
            mLoadCursor--;
            mViewModle.lstTxData.postValue(mCacheTxData);
        }

        @Override
        public void onReceiveException(Throwable t) {
            t.printStackTrace();
            mViewModle.lstTxData.postValue(mCacheTxData);
        }
    };

    private NetworkService.BalanceCallback mBalanceCallback = new NetworkService.BalanceCallback() {
        @Override
        public void onReceiveICXBalance(String id, String address, String result) {

        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {

        }

        @Override
        public void onReceiveError(String id, String address, int code) {

        }

        @Override
        public void onReceiveException(String id, String address, String msg) {

        }
    };
}
