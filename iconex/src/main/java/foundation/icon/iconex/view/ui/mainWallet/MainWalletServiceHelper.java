package foundation.icon.iconex.view.ui.mainWallet;

import android.os.Handler;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;

import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import ethereum.contract.MyContract;
import ethereum.contract.MyTransactionManager;
import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Call;
import retrofit2.Response;

public class MainWalletServiceHelper {

    public static String TAG = MainWalletServiceHelper.class.getSimpleName();
    private String LogWallet(Wallet wallet, boolean wrapping) {
        if (wrapping) {
            return " wallet = [" + wallet.getAlias() + ", " + wallet.getAddress() + "]";
        } else {
            return wallet.getAlias() + ", " + wallet.getAddress();
        }
    }
    private String LogEntry(WalletEntry entry, boolean wrapping) {
        if (wrapping) {
            return " entry = [" + entry.getSymbol() + ", " + entry.getAddress() + "]";
        } else {
            return entry.getSymbol() + ", " + entry.getAddress();
        }
    }

    public interface OnLoadListener {
        void onLoadNextBalance(WalletEntry entry, int walletPosition, int entryPosition);
        void onLoadCompleteBalance();

        void onLoadCompleteExchangeTable();

        void onLoadNextiScore(Wallet wallet, int walletPosition);
        void onLoadNextDelegation(Wallet wallet, int walletPosition);
        void onLoadCompletePReps();

        void onLoadCompleteAll();
        void onNetworkError();
    }

    public OnLoadListener listener[] = null;
    public void setListener(OnLoadListener listener) {
        this.listener = new OnLoadListener[] {listener};
    }
    public void clearListener() {
        if (listener != null) {
            listener[0] = null;
        }
        listener = null;
    }

    public void requestAllData() {
        Log.d(TAG, "requestAllData() called");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;

        action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                merge(new ArrayList<Completable>() {{

                    add(requestBalance());
                    add(requestExchangeTable());
                    add(requestRReps());

                }}, new SimpleObserver() {
                    @Override
                    public void onDone() {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "onDone() called in requestAllData!");

                                if (listener != null) {
                                    listener.onLoadCompleteAll();
                                }
                            }
                        }, 100);
                    }

                    @Override
                    public void onNetworkError(UnknownHostException e) {
                        Log.d(TAG, "onNetworkError() called in requestAllData");
                        e.printStackTrace();
                        if (listener != null) {
                            listener.onNetworkError();
                        }
                    }
                });
            }

            @Override
            public void onOtherError(Throwable e) {

            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                Log.d(TAG, "onDone() called request complete!");
            }
        });
    }

    private Completable requestBalance() {
        Log.d(TAG, "requestBalance() called");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;

        return merge(new ArrayList<Completable>() {{
            int szWallets = ICONexApp.wallets.size();
            for (int i = 0; szWallets > i; i++) {
                Wallet wallet = ICONexApp.wallets.get(i);
                int szEntries = wallet.getWalletEntries().size();
                for (int j = 0; szEntries > j; j++){
                    WalletEntry entry = wallet.getWalletEntries().get(j);

                    if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                        if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                            add(getIcxCoinBalance(entry, i, j));
                        } else {
                            add(getIcxTokenBalance(entry, i, j));
                        }
                    } else {
                        if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                            add(getEthCoinBalance(entry, i, j));
                        } else {
                            add(getEthTokenBalance(entry, i, j));
                        }
                    }
                }
            }
        }}, new SimpleObserver() {
            @Override
            public void onDone() {
                if (listener != null) {
                    listener.onLoadCompleteBalance();
                }
            }
        });
    }

    private Completable getIcxCoinBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        Log.d(TAG, "getIcxCoinBalance() called with: entry = [" + LogEntry(entry,false) + "], walletPosition = [" + walletPosition + "], entryPosition = [" + entryPosition + "]");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;
        int entryId = entry.getId();
        String address = entry.getAddress();
        final String[] balance = {null};

        return action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                LoopChainClient client = new LoopChainClient(getIcxHost());
                Response<LCResponse> response = client.getBalance(entryId, address).execute();
                if (response.errorBody() != null) throw new Exception(response.message());
                String hexBalance = response.body().getResult().getAsString();
                balance[0] = ConvertUtil.hexStringToBigInt(hexBalance, 18).toString();
            }

            @Override
            public void onOtherError(Throwable e) {
                Log.d(TAG, "onOtherError() called with: e = [" + e.getMessage() + "]," + LogEntry(entry, true));
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);
                Log.d(TAG, "onDone() called in getIcxCoinBalance with: balance = [" + balance[0] + "], " + LogEntry(entry, true));
                if (listener != null) {
                    listener.onLoadNextBalance(entry, walletPosition, entryPosition);
                }
            }
        });
    }

    private Completable getIcxTokenBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        Log.d(TAG, "getIcxTokenBalance() called with: entry = [" + LogEntry(entry,false) + "], walletPosition = [" + walletPosition + "], entryPosition = [" + entryPosition + "]");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;
        int entryId = entry.getId();
        String address = entry.getAddress();
        String contractAddress = entry.getContractAddress();
        final String[] balance = {null};

        return action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                LoopChainClient client = new LoopChainClient(getIcxHost());
                Response<LCResponse> response = client.getTokenBalance(entryId, address, contractAddress).execute();
                if (response.errorBody() != null) throw new Exception(response.message());
                String hexBalance = response.body().getResult().getAsString();
                balance[0] = ConvertUtil.hexStringToBigInt(hexBalance, 18).toString();
            }

            @Override
            public void onOtherError(Throwable e) {
                Log.d(TAG, "onOtherError() called in getIcxTokenBalance with: e = [" + e.getMessage() + "], " + LogEntry(entry, true));
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);
                Log.d(TAG, "onDone() called in getIcxTokenBalance() with: balance = [" + balance[0] + "], " + LogEntry(entry, true));
                if (listener != null) {
                    listener.onLoadNextBalance(entry, walletPosition, entryPosition);
                }
            }
        });
    }

    private Completable getEthCoinBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        Log.d(TAG, "getEthCoinBalance() called with: entry = [" + LogEntry(entry, false) + "], walletPosition = [" + walletPosition + "], entryPosition = [" + entryPosition + "]");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;
        String address = entry.getAddress();
        final String[] balance = {null};

        return action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                Web3j web3j = Web3jFactory.build(new HttpService(getEthHost()));
                EthGetBalance getBalance = web3j.ethGetBalance("0x" + address, DefaultBlockParameterName.LATEST).send();
                if (getBalance.getError() != null)
                    throw new Exception(getBalance.getError().getMessage());
                balance[0] = getBalance.getBalance().toString();
            }

            @Override
            public void onOtherError(Throwable e) {
                Log.d(TAG, "onOtherError() called int getEthCoinBalance with: e = [" + e.getMessage() + "], " + LogEntry(entry, true));
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);
                Log.d(TAG, "onDone() called in getEthCoinBalance with: balance = [" + balance[0] + "], " + LogEntry(entry, true));
                if (listener != null) {
                    listener.onLoadNextBalance(entry, walletPosition, entryPosition);
                }
            }
        });
    }

    private Completable getEthTokenBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        Log.d(TAG, "getEthTokenBalance() called with: entry = [" + LogEntry(entry, false) + "], walletPosition = [" + walletPosition + "], entryPosition = [" + entryPosition + "]");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;
        String address = entry.getAddress();
        String contractAddress = entry.getContractAddress();
        final String[] balance = {null};

        return action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                Web3j web3j = Web3jFactory.build(new HttpService(getEthHost()));
                MyTransactionManager txManager = new MyTransactionManager(web3j, contractAddress, Collections.EMPTY_LIST);
                MyContract myContract = MyContract.load(contractAddress, web3j, txManager, Contract.GAS_PRICE, Contract.GAS_LIMIT);
                balance[0] = myContract.balanceOf(address).send().toString();
            }

            @Override
            public void onOtherError(Throwable e) {
                Log.d(TAG, "onOtherError() called with: e = [" + e.getMessage() + "], " + LogEntry(entry, true));
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);
                Log.d(TAG, "onDone() called in getEthTokenBalance with: balance =[" + balance[0] + "], " + LogEntry(entry, true));
                if (listener != null) {
                    listener.onLoadNextBalance(entry, walletPosition, entryPosition);
                }
            }
        });
    }

    private Completable requestExchangeTable() {
        Log.d(TAG, "requestExchangeTable() called");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;
        HashMap<String, String> exchangeTable = new HashMap<>();

        return action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                StringBuilder lstExchange = new StringBuilder();
                for (int i = 0; ICONexApp.EXCHANGES.size() > i; i++) {
                    String sym = ICONexApp.EXCHANGES.get(i);
                    lstExchange.append(sym+"usd,"+sym+"btc,"+sym+"eth,"+sym+"icx");
                    if (i < ICONexApp.EXCHANGES.size() - 1) lstExchange.append(",");
                }

                LoopChainClient client = new LoopChainClient(getVersionHost());
                Call<TRResponse> responseCall = client.getExchangeRates(lstExchange.toString());
                Response<TRResponse> response = responseCall.execute();
                JsonArray list = response.body().getData().getAsJsonArray();
                for (int i = 0; list.size() > i; i++) {
                    JsonObject item = list.get(i).getAsJsonObject();
                    String tradeName = item.get("tradeName").getAsString();
                    String price = item.get("price").getAsString();
                    exchangeTable.put(tradeName, price);
                }
            }

            @Override
            public void onOtherError(Throwable e) {
                Log.d(TAG, "onOtherError() called in requestExchangeTable  with: e = [" + e.getMessage() + "]");
            }
        }, new SimpleObserver() {
            @Override
            public void onDone() {
                Log.d(TAG, "onDone() called in requestExchangeTable with: exchageTable = [" + exchangeTable +"]");
                ICONexApp.EXCHANGE_TABLE = exchangeTable;

                if (listener != null) {
                    listener.onLoadCompleteExchangeTable();
                }
            }
        });
    }
    private Completable requestRReps() {
        Log.d(TAG, "requestRReps() called");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;

        return merge(new ArrayList<Completable>() {{

            int size = ICONexApp.wallets.size();
            for (int i = 0;  size > i; i++) {
                Wallet wallet = ICONexApp.wallets.get(i);
                if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                    add(getIScore(wallet, i));
                    add(getDelegation(wallet, i));
                }
            }

        }}, new SimpleObserver() {
            @Override
            public void onDone() {
                Log.d(TAG, "onDone() called in requestPReps");

                if (listener != null) {
                    listener.onLoadCompletePReps();
                }
            }
        });
    }

    private Completable getIScore(Wallet wallet, int walletPosition) {
        Log.d(TAG, "getIScore() called with: wallet = [" + LogWallet(wallet,false) + "], walletPosition = [" + walletPosition + "]");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;
        String address = wallet.getAddress();
        String url = ICONexApp.NETWORK.getUrl();
        final BigInteger[] iscore = {null};

        return action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                PRepService service = new PRepService(url);
                RpcObject rpcObject = service.getIScore(address).asObject();
                iscore[0] = rpcObject.getItem("iscore").asInteger();
            }

            @Override
            public void onOtherError(Throwable e) {
                Log.d(TAG, "onOtherError() in getIScore called with: e = [" + e.getMessage() + "]," + LogWallet(wallet, true));
            }
        }, new SimpleObserver() {
            @Override
            public void onDone() {
                Log.d(TAG, "onDone() called in getIScore with: wallet: = [" + LogWallet(wallet, false) + "], iscore = [" + iscore[0] + "]");
                wallet.setiScore(iscore[0]);
                if (listener != null) {
                    listener.onLoadNextiScore(wallet, walletPosition);
                }
            }
        });
    }
    private Completable getDelegation(Wallet wallet, int walletPosition) {
        Log.d(TAG, "getDelegation() called with: wallet = [" + LogWallet(wallet,false) + "], walletPosition = [" + walletPosition + "]");
        final OnLoadListener listener = this.listener != null ? this.listener[0] : null;
        String address = wallet.getAddress();
        String url = ICONexApp.NETWORK.getUrl();
        final BigInteger[] stake = new BigInteger[1];
        final BigInteger[] votingPower = new BigInteger[1];

        return action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                PRepService service = new PRepService(url);
                RpcObject rpcObject = service.getDelegation(address).asObject();
                votingPower[0] = rpcObject.getItem("votingPower").asInteger();
                stake[0] = rpcObject.getItem("totalDelegated")
                        .asInteger()
                        .add(votingPower[0]);
            }

            @Override
            public void onOtherError(Throwable e) {
                Log.d(TAG, "onOtherError() in getDelegation called with: e = [" + e.getMessage() + "]," + LogWallet(wallet, true));
            }
        }, new SimpleObserver() {
            @Override
            public void onDone() {
                Log.d(TAG, "onDone() called in getDelegation with = [" + LogWallet(wallet,false) + "], stake,votingpower=("+ stake[0] + ", " + votingPower[0] + ")");
                wallet.setStaked(stake[0]);
                wallet.setVotingPower(votingPower[0]);
                if (listener != null) {
                    listener.onLoadNextDelegation(wallet, walletPosition);
                }
            }
        });
    }

    private String getVersionHost() {
        switch (ICONexApp.network) {
            default:
            case MyConstants.NETWORK_MAIN: return ServiceConstants.URL_VERSION_MAIN;
            case MyConstants.NETWORK_TEST: return ServiceConstants.URL_VERSION_TEST;
            case MyConstants.NETWORK_DEV: return ServiceConstants.DEV_TRACKER;
        }
    }

    private String getIcxHost() {
        switch (ICONexApp.network) {
            default:
            case MyConstants.NETWORK_MAIN: return ServiceConstants.TRUSTED_HOST_MAIN;
            case MyConstants.NETWORK_TEST: return ServiceConstants.TRUSTED_HOST_TEST;
            case MyConstants.NETWORK_DEV: return ServiceConstants.DEV_HOST;
        }
    }

    public String getEthHost() {
        switch (ICONexApp.network) {
            case MyConstants.NETWORK_MAIN:
                return ServiceConstants.ETH_HOST;
            default:
                return ServiceConstants.ETH_ROP_HOST;
        }
    }

    abstract class SimpleObserver {
        abstract void onDone();
        void onNetworkError(UnknownHostException e) { }
    }

    abstract class NetworkErrorAction implements Action{
        public void run() throws UnknownHostException {
            try {
                 action();
            } catch (UnknownHostException e) {
                throw e;
            } catch (Throwable e) {
                onOtherError(e);
            }
        }
        abstract public void action() throws Throwable;
        abstract public void onOtherError(Throwable e);
    }

    private Completable action(NetworkErrorAction act, SimpleObserver ob) {
        Completable completable = Completable.fromAction(act)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                ob.onDone();
            }

            @Override
            public void onError(Throwable e) {
                ob.onDone();
                ob.onNetworkError(((UnknownHostException) e));
            }
        });

        return completable;
    }

    private Completable merge(ArrayList<Completable> lst, SimpleObserver ob) {
        Completable completable = Completable.merge(lst)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                ob.onDone();
            }

            @Override
            public void onError(Throwable e) {
                ob.onDone();
                ob.onNetworkError(((UnknownHostException) e));
            }
        });

        return completable;
    }
}
