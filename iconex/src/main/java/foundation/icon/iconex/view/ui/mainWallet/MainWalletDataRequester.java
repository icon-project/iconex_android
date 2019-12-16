package foundation.icon.iconex.view.ui.mainWallet;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import ethereum.contract.MyContract;
import ethereum.contract.MyTransactionManager;
import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.functions.Action;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Call;
import retrofit2.Response;

public class MainWalletDataRequester {

    private Hashtable<String, Integer> completeChecker = new Hashtable<>();
    private Set<String> nextChecker = new HashSet<>();

    public interface OnLoadListener {
        void onLoadNextBalance(WalletEntry entry, int walletPosition, int entryPosition);

        void onLoadCompleteBalance();

        void onLoadCompleteExchangeTable();

        void onLoadNextiScore(Wallet wallet, int walletPosition);

        void onLoadNextStake(Wallet wallet, int walletPosition, BigInteger unstake);

        void onLoadNextDelegation(Wallet wallet, int walletPosition);

        void onLoadCompletePReps();

        void onLoadCompleteAll();

        void onNetworkError();
    }

    OnLoadListener mListener = null;

    private static String KEY_BALANCE = "balance";
    private static String KEY_EXCHANGE = "exchange";
    private static String KEY_ISCORE = "iscore";
    private static String KEY_STAKE = "stake";
    private static String KEY_DELEGATION = "delegation";
    private static String KEY_NETWORK_ERR = "network err";

    public void setListener(OnLoadListener listener) {
        mListener = listener;
    }

    private void completeChecking(String key, boolean check) {
        Integer count = completeChecker.get(key) == null ? 0 : completeChecker.get(key);
        completeChecker.put(key, count + (check ? +1 : -1));
    }

    private void nextChecking(String key) {
        nextChecker.add(key);
    }

    public void requestAllData() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                requestExchangeTable();
                requestBalance();
                requestRReps();

                boolean loading = true;
                do {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    synchronized (MainWalletDataRequester.this) {
                        if (mListener == null) break;
                        final OnLoadListener listener = MainWalletDataRequester.this.mListener;

                        for (String key : nextChecker) {
                            String[] update = key.split(",");
                            String cmd = update[0];
                            int walletPosition = Integer.parseInt(update[1]);
                            Wallet wallet = ICONexApp.wallets.get(walletPosition);
                            if (KEY_BALANCE.equals(cmd)) {
                                int entryPosition = Integer.parseInt(update[2]);
                                WalletEntry entry = wallet.getWalletEntries().get(entryPosition);
                                listener.onLoadNextBalance(entry, walletPosition, entryPosition);
                            } else if (KEY_ISCORE.equals(cmd)) {
                                listener.onLoadNextiScore(wallet, walletPosition);
                            } else if (KEY_STAKE.equals(cmd)) {
                                BigInteger unstake = new BigInteger(update[2]);
                                listener.onLoadNextStake(wallet, walletPosition, unstake);
                            } else if (KEY_DELEGATION.equals(cmd)) {
                                listener.onLoadNextDelegation(wallet, walletPosition);
                            }
                        }
                        nextChecker.clear();

                        int networkErr = completeChecker.get(KEY_NETWORK_ERR) == null ? 0 : completeChecker.get(KEY_NETWORK_ERR);
                        if (networkErr < 0) {
                            listener.onNetworkError();
                            break;
                        }

                        int balance = completeChecker.get(KEY_BALANCE) == null ? 0 : completeChecker.get(KEY_BALANCE);
                        if (balance == 0) {
                            listener.onLoadCompleteBalance();
                            completeChecker.put(KEY_BALANCE, -1);
                            balance = -1;
                        }

                        int exchange = completeChecker.get(KEY_EXCHANGE) == null ? 0 : completeChecker.get(KEY_EXCHANGE);
                        if (exchange == 0) {
                            listener.onLoadCompleteExchangeTable();
                            completeChecker.put(KEY_EXCHANGE, -1);
                            exchange = -1;
                        }

                        int iscore = completeChecker.get(KEY_ISCORE) == null ? 0 : completeChecker.get(KEY_ISCORE);
                        int stake = completeChecker.get(KEY_STAKE) == null ? 0 : completeChecker.get(KEY_STAKE);
                        int delegation = completeChecker.get(KEY_DELEGATION) == null ? 0 : completeChecker.get(KEY_DELEGATION);
                        if (iscore == 0 && stake == 0 && delegation == 0) {
                            listener.onLoadCompletePReps();
                            completeChecker.put(KEY_ISCORE, -1);
                            completeChecker.put(KEY_STAKE, -1);
                            completeChecker.put(KEY_DELEGATION, -1);
                            iscore = -1;
                            stake = -1;
                            delegation = -1;
                        }

                        if (balance == -1 && exchange == -1 && iscore == -1 && stake == -1 && delegation == -1) {
                            listener.onLoadCompleteAll();
                            loading = false;
                            break;
                        }

                    }
                } while (loading);
            }
        }).start();
    }

    private void requestBalance() {

        int szWallets = ICONexApp.wallets.size();
        for (int i = 0; szWallets > i; i++) {
            Wallet wallet = ICONexApp.wallets.get(i);
            int szEntries = wallet.getWalletEntries().size();
            for (int j = 0; szEntries > j; j++) {
                WalletEntry entry = wallet.getWalletEntries().get(j);

                if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                    if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                        getIcxCoinBalance(entry, i, j);
                    } else {
                        getIcxTokenBalance(entry, i, j);
                    }
                } else {
                    if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                        getEthCoinBalance(entry, i, j);
                    } else {
                        getEthTokenBalance(entry, i, j);
                    }
                }
            }
        }
    }

    private void getIcxCoinBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        completeChecking(KEY_BALANCE, true);
        int entryId = entry.getId();
        String address = entry.getAddress();
        final String[] balance = {null};

        action(new NetworkErrorAction() {
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
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);

                synchronized (MainWalletDataRequester.this) {
                    nextChecking(KEY_BALANCE + "," + walletPosition + "," + entryPosition);
                    completeChecking(KEY_BALANCE, false);
                }
            }
        });
    }

    private void getIcxTokenBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        completeChecking(KEY_BALANCE, true);
        int entryId = entry.getId();
        String address = entry.getAddress();
        String contractAddress = entry.getContractAddress();
        final String[] balance = {null};

        action(new NetworkErrorAction() {
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
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);
                synchronized (MainWalletDataRequester.this) {
                    nextChecking(KEY_BALANCE + "," + walletPosition + "," + entryPosition);
                    completeChecking(KEY_BALANCE, false);
                }
            }
        });
    }

    private void getEthCoinBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        completeChecking(KEY_BALANCE, true);
        String address = entry.getAddress();
        final String[] balance = {null};

        action(new NetworkErrorAction() {
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
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);
                synchronized (MainWalletDataRequester.this) {
                    nextChecking(KEY_BALANCE + "," + walletPosition + "," + entryPosition);
                    completeChecking(KEY_BALANCE, false);
                }
            }
        });
    }

    private void getEthTokenBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        completeChecking(KEY_BALANCE, true);
        String address = entry.getAddress();
        String contractAddress = entry.getContractAddress();
        final String[] balance = {null};

        action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                Web3j web3j = Web3jFactory.build(new HttpService(getEthHost()));
                MyTransactionManager txManager = new MyTransactionManager(web3j, contractAddress, Collections.EMPTY_LIST);
                MyContract myContract = MyContract.load(contractAddress, web3j, txManager, Contract.GAS_PRICE, Contract.GAS_LIMIT);
                balance[0] = myContract.balanceOf(address).send().toString();
            }

            @Override
            public void onOtherError(Throwable e) {
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                entry.setBalance(balance[0] == null ? MyConstants.NO_BALANCE : balance[0]);
                synchronized (MainWalletDataRequester.this) {
                    nextChecking(KEY_BALANCE + "," + walletPosition + "," + entryPosition);
                    completeChecking(KEY_BALANCE, false);
                }
            }
        });
    }

    private void requestExchangeTable() {
        completeChecking(KEY_EXCHANGE, true);
        HashMap<String, String> exchangeTable = new HashMap<>();

        action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                StringBuilder lstExchange = new StringBuilder();
                for (int i = 0; ICONexApp.EXCHANGES.size() > i; i++) {
                    String sym = ICONexApp.EXCHANGES.get(i);
                    lstExchange.append(sym + "usd," + sym + "btc," + sym + "eth," + sym + "icx");
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
                    if (!price.equals("0"))
                        exchangeTable.put(tradeName, price);
                }
            }

            @Override
            public void onOtherError(Throwable e) {
            }
        }, new SimpleObserver() {
            @Override
            public void onDone() {
                ICONexApp.EXCHANGE_TABLE = exchangeTable;
                synchronized (MainWalletDataRequester.this) {
                    completeChecking(KEY_EXCHANGE, false);
                }
            }
        });
    }

    private void requestRReps() {
        int size = ICONexApp.wallets.size();
        for (int i = 0; size > i; i++) {
            Wallet wallet = ICONexApp.wallets.get(i);
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                getIScore(wallet, i);
                getStake(wallet, i);
                getDelegation(wallet, i);
            }
        }
    }

    private void getIScore(Wallet wallet, int walletPosition) {
        completeChecking(KEY_ISCORE, true);
        String address = wallet.getAddress();
        String url = ICONexApp.NETWORK.getUrl();
        final BigInteger[] iscore = {null};

        action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                PRepService service = new PRepService(url);
                RpcObject rpcObject = service.getIScore(address).asObject();
                iscore[0] = rpcObject.getItem("iscore").asInteger();
            }

            @Override
            public void onOtherError(Throwable e) {
            }
        }, new SimpleObserver() {
            @Override
            public void onDone() {
                wallet.setiScore(iscore[0]);
                synchronized (MainWalletDataRequester.this) {
                    nextChecking(KEY_ISCORE + "," + walletPosition);
                    completeChecking(KEY_ISCORE, false);
                }
            }
        });
    }

    private void getStake(Wallet wallet, int walletPosition) {
        completeChecking(KEY_STAKE, true);
        String address = wallet.getAddress();
        String url = ICONexApp.NETWORK.getUrl();
        final BigInteger[] stake = new BigInteger[1];
        final BigInteger[] unstake = new BigInteger[1];

        action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                PRepService service = new PRepService(url);
                RpcObject rpcObject = service.getStake(address).asObject();
                stake[0] = rpcObject.getItem("stake").asInteger();
                RpcItem item = rpcObject.getItem("unstake");
                if (item != null)
                    unstake[0] = item.asInteger();
            }

            @Override
            public void onOtherError(Throwable e) {
            }
        }, new SimpleObserver() {
            @Override
            void onDone() {
                wallet.setStaked(stake[0]);
                synchronized (MainWalletDataRequester.this) {
                    nextChecking(KEY_STAKE + "," + walletPosition + "," + (unstake[0] == null ? "0" : unstake[0]));
                    completeChecking(KEY_STAKE, false);
                }
            }
        });
    }

    private void getDelegation(Wallet wallet, int walletPosition) {
        completeChecking(KEY_DELEGATION, true);
        String address = wallet.getAddress();
        String url = ICONexApp.NETWORK.getUrl();
        final BigInteger[] votingPower = new BigInteger[1];

        action(new NetworkErrorAction() {
            @Override
            public void action() throws Throwable {
                PRepService service = new PRepService(url);
                RpcObject rpcObject = service.getDelegation(address).asObject();
                votingPower[0] = rpcObject.getItem("votingPower").asInteger();
            }

            @Override
            public void onOtherError(Throwable e) {
            }
        }, new SimpleObserver() {
            @Override
            public void onDone() {
                wallet.setVotingPower(votingPower[0]);
                synchronized (MainWalletDataRequester.this) {
                    nextChecking(KEY_DELEGATION + "," + walletPosition);
                    completeChecking(KEY_DELEGATION, false);
                }
            }
        });
    }

    private String getVersionHost() {
        switch (ICONexApp.NETWORK.getNid().intValue()) {
            default:
            case MyConstants.NETWORK_MAIN:
                return ServiceConstants.URL_VERSION_MAIN;
            case MyConstants.NETWORK_TEST:
                return ServiceConstants.URL_VERSION_TEST;
            case MyConstants.NETWORK_DEV:
                return ServiceConstants.DEV_TRACKER;
        }
    }

    private String getIcxHost() {
        switch (ICONexApp.NETWORK.getNid().intValue()) {
            default:
            case MyConstants.NETWORK_MAIN:
                return ServiceConstants.TRUSTED_HOST_MAIN;
            case MyConstants.NETWORK_TEST:
                return ServiceConstants.TRUSTED_HOST_TEST;
            case MyConstants.NETWORK_DEV:
                return ServiceConstants.DEV_HOST;
        }
    }

    public String getEthHost() {
        switch (ICONexApp.NETWORK.getNid().intValue()) {
            case MyConstants.NETWORK_MAIN:
                return ServiceConstants.ETH_HOST;
            default:
                return ServiceConstants.ETH_ROP_HOST;
        }
    }

    abstract class SimpleObserver {
        abstract void onDone();

        void onNetworkError(UnknownHostException e) {
        }
    }

    abstract class NetworkErrorAction implements Action {
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

    private void action(NetworkErrorAction act, SimpleObserver ob) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    act.run();
                } catch (UnknownHostException e) {
                    completeChecking(KEY_NETWORK_ERR, false);
                } finally {
                    ob.onDone();
                }
            }
        }).start();
    }
}
