package foundation.icon.iconex.view.ui.detailWallet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.DecimalFomatter;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionItemViewData;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import loopchain.icon.wallet.core.Constants;

public class WalletDetailServiceHelper {
    public static final String TAG = WalletDetailServiceHelper.class.getSimpleName();

    private Context mContext;
    private WalletDetailViewModel mViewModle = null;

    private NetworkService mService = null;
    private boolean mIsBind = false;

    private List<TransactionItemViewData> mCacheTxData = new ArrayList<>();
    private int mCountTxData = 0;
    private int mLoadCursor = 1;

    private Vector<String[]> mIcxBalance = new Vector<>();
    private Vector<String[]> mEthBalance = new Vector<>();
    private Vector<String[]> mErrBalance = new Vector<>();
    private int requestCount;

    public interface OnServiceReadyListener {
        void onReady();
    }

    private OnServiceReadyListener mOnServiceReadyListener = null;

    public boolean isBind() {
        return mIsBind;
    }

    public WalletDetailServiceHelper(Context context, WalletDetailViewModel viewModel) {
        mContext = context;
        mViewModle = viewModel;
        viewModel.isNoLoadMore.setValue(true);
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

    public void loadTxList() {
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
        } else { // ether
            RealmUtil.loadRecents();
            List<TransactionItemViewData> viewDataList = new ArrayList<>();
            for (RecentSendInfo tx : ICONexApp.ETHSendInfo) {
                if (walletEntry.getAddress().equals(tx.getAddress()) && walletEntry.getSymbol().equals(tx.getSymbol())
                        && ICONexApp.NETWORK.getNid().intValue() == tx.getNid()) {
                    TransactionItemViewData viewData = new TransactionItemViewData();
                    viewData.setState(1);
                    viewData.setDate(tx.getDate());
                    viewData.setFrom(tx.getAddress());
                    viewData.setTxHash(tx.getTxHash());
                    try {
                        viewData.setAmount(DecimalFomatter.format(new BigDecimal(tx.getAmount())));
                    } catch (Exception e) {
                        viewData.setAmount("-");
                    }
                    viewDataList.add(viewData);
                }
            }

            mViewModle.lstTxData.setValue(viewDataList);
            mViewModle.isNoLoadMore.setValue(false);
            mViewModle.isRefreshing.setValue(false);
        }
    }

    public void loadMoreIcxTxList() {
        mViewModle.isNoLoadMore.setValue(mCacheTxData.size() >= mCountTxData);
        if (mCacheTxData.size() < mCountTxData) {
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
            mCountTxData = totalData;
            Wallet wallet = mViewModle.wallet.getValue();
            WalletEntry walletEntry = mViewModle.walletEntry.getValue();

            for (int i = 0; txList.size() > i; i++) {
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
                            viewData.setAmount(DecimalFomatter.format(new BigDecimal(tx.get("amount").getAsString())));
                            mCacheTxData.add(viewData);
                        }
                    } else { // if ("TOKEN".equals(walletEntry.getType()))
                        // ICX TOKEN
                        viewData.setTxHash(tx.get("txHash").getAsString());
                        viewData.setDate(tx.get("age").getAsString());
                        viewData.setAmount(DecimalFomatter.format(new BigDecimal(tx.get("quantity").getAsString())));
                        mCacheTxData.add(viewData);
                    }
                }
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mViewModle.isNoLoadMore.setValue(mCacheTxData.size() >= mCountTxData);
                    mViewModle.lstTxData.postValue(mCacheTxData);
                }
            }, 1000);
        }

        @Override
        public void onReceiveError(String resCode) {
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
            mIcxBalance.add(new String[]{id, address, result});
            if (isDoneRequest(true)) completeRequest();
        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {
            address = address.substring(2);
            mEthBalance.add(new String[]{id, address, result});

            if (isDoneRequest(true)) completeRequest();
        }

        @Override
        public void onReceiveError(String id, String address, int code) {
            if (address.startsWith(MyConstants.PREFIX_HEX)) {
                address = address.substring(2);
            }

            mErrBalance.add(new String[]{id, address, MyConstants.NO_BALANCE});

            if (isDoneRequest(true)) completeRequest();
        }

        @Override
        public void onReceiveException(String id, String address, String msg) {
            if (address.startsWith(MyConstants.PREFIX_HEX)) {
                address = address.substring(2);
            }

            mErrBalance.add(new String[]{id, address, MyConstants.NO_BALANCE});

            if (isDoneRequest(true)) completeRequest();
        }
    };

    private void completeRequest() {
        mViewModle.lstBalanceResults.setValue(
                new ArrayList<String[]>() {{
                    addAll(mIcxBalance);
                    addAll(mEthBalance);
                    addAll(mErrBalance);
                }});

        mIcxBalance.clear();
        mEthBalance.clear();
        mErrBalance.clear();
    }

    private synchronized boolean isDoneRequest(boolean countingRequest) {
        if (requestCount > 0 && countingRequest) {
            requestCount--;
        }

        return requestCount == 0;
    }

    private void cancleRequest() {
        if (!isDoneRequest(true)) {
            mService.stopGetBalance();
            mIcxBalance.clear();
            mIcxBalance.clear();
        }
    }

    public void requestBalance() {
        cancleRequest();

        mViewModle.loadingBalance.setValue(true);
        mViewModle.loadingSatke.setValue(true);
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


        WalletEntry entry = mViewModle.walletEntry.getValue();
        if (entry.getType().equals(MyConstants.TYPE_COIN))
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String address = mViewModle.wallet.getValue().getAddress();
                        RpcObject rpcObject = new PRepService(ICONexApp.NETWORK.getUrl()).getStake(address).asObject();
                        BigDecimal stake = new BigDecimal(ConvertUtil.getValue(rpcObject.getItem("stake").asInteger(), 18));
                        BigDecimal unstake = rpcObject.getItem("unstake") == null ? BigDecimal.ZERO :
                                new BigDecimal(ConvertUtil.getValue(rpcObject.getItem("unstake").asInteger(), 18));
                        mViewModle.stake.postValue(new BigDecimal[]{stake, unstake});
                    } catch (IOException e) {
                    }
                }
            }).start();
    }

    private Object[] makeGetBalanceList() {
        HashMap<String, String> icxList = new HashMap<>();
        HashMap<String, String> ethList = new HashMap<>();
        HashMap<String, String[]> ercList = new HashMap<>();
        HashMap<String, String[]> ircList = new HashMap<>();

        Wallet info = mViewModle.wallet.getValue();
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


        return new Object[]{icxList, ircList, ethList, ercList};
    }
}
