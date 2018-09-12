package foundation.icon.iconex.wallet.detail;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.BottomSheetMenu;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.BottomItemSelectActivity;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.dialogs.EditTextDialog;
import foundation.icon.iconex.dialogs.SearchConditionDialog;
import foundation.icon.iconex.dialogs.TrackerDialog;
import foundation.icon.iconex.intro.IntroActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.token.manage.TokenManageActivity;
import foundation.icon.iconex.token.swap.TokenSwapActivity;
import foundation.icon.iconex.wallet.menu.WalletAddressCodeActivity;
import foundation.icon.iconex.wallet.menu.WalletBackUpActivity;
import foundation.icon.iconex.wallet.menu.WalletPwdChangeActivity;
import foundation.icon.iconex.wallet.transfer.EtherTransferActivity;
import foundation.icon.iconex.wallet.transfer.ICONTransferActivity;
import foundation.icon.iconex.widgets.RefreshLayout.LoadingHeaderView;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.PKIUtils;

import static foundation.icon.iconex.MyConstants.EXCHANGE_BTC;
import static foundation.icon.iconex.MyConstants.EXCHANGE_ETH;
import static foundation.icon.iconex.MyConstants.EXCHANGE_USD;

public class WalletDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = WalletDetailActivity.class.getSimpleName();

    private Wallet mWallet;

    private ViewGroup appbar;
    private TextView txtWalletAlias;
    private Button btnBack, btnMenu;

    private RecyclerView recyclerTx;
    private TransactionListAdapter txAdapter;

    private RefreshLayout refreshLayout;

    private List<TxItem> txList = new ArrayList<>();

    private NetworkService mService;
    private boolean mBound;

    private int currentPage = 1;
    private int txTotalData = 0;

    private String EXCHANGE = EXCHANGE_USD;

    private String entryId = null;
    private WalletEntry selectedEntry;

    private MyConstants.TxState mState = MyConstants.TxState.DONE;
    private MyConstants.TxType mType = MyConstants.TxType.WHOLENESS;

    private static final int RC_BOTTOM_SHEET = 10001;
    private static final int RC_CHANGE_PWD = 10002;
    private final int RC_SWAP = 30001;

    public static final int RES_REFRESH = 4001;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerBalanceCallback(mBalanceCallback);
            mService.registerTxListCallback(mTxCallback);
            mBound = true;

            getTxList();
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
            if (refreshLayout.isRefreshing)
                refreshLayout.stopRefresh(true);

            if (Integer.toString(selectedEntry.getId()).equals(id)) {
                selectedEntry.setBalance(result);
                txAdapter.setWalletEntry(selectedEntry);
            }
        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {
            if (refreshLayout.isRefreshing)
                refreshLayout.stopRefresh(true);

            if (Integer.toString(selectedEntry.getId()).equals(id)) {
                selectedEntry.setBalance(result);
                txAdapter.setWalletEntry(selectedEntry);
            }
        }

        @Override
        public void onReceiveError(String id, String address, int code) {
            if (refreshLayout.isRefreshing)
                refreshLayout.stopRefresh(true);

            if (selectedEntry.getType().equals(MyConstants.TYPE_COIN)) {
                if (Integer.toString(selectedEntry.getId()).equals(id)) {
                    selectedEntry.setBalance(MyConstants.NO_BALANCE);
                    txAdapter.setWalletEntry(selectedEntry);
                }
            } else {
                for (int i = 0; i < mWallet.getWalletEntries().size(); i++) {
                    if (id.equals(mWallet.getWalletEntries().get(i).getId()))
                        mWallet.getWalletEntries().get(i).setBalance(MyConstants.NO_BALANCE);

                    if (id.equals(selectedEntry.getId()))
                        selectedEntry.setBalance(MyConstants.NO_BALANCE);
                }

                txAdapter.setWalletEntry(selectedEntry);
            }
        }

        @Override
        public void onReceiveException(String id, String address, String msg) {
            if (refreshLayout.isRefreshing)
                refreshLayout.stopRefresh(true);

            if (selectedEntry.getType().equals(MyConstants.TYPE_COIN)) {
                if (Integer.toString(selectedEntry.getId()).equals(id)) {
                    selectedEntry.setBalance(MyConstants.NO_BALANCE);
                    txAdapter.setWalletEntry(selectedEntry);
                }
            } else {
                for (int i = 0; i < mWallet.getWalletEntries().size(); i++) {
                    if (id.equals(mWallet.getWalletEntries().get(i).getId()))
                        mWallet.getWalletEntries().get(i).setBalance(MyConstants.NO_BALANCE);

                    if (id.equals(selectedEntry.getId()))
                        selectedEntry.setBalance(MyConstants.NO_BALANCE);
                }

                txAdapter.setWalletEntry(selectedEntry);
            }
        }
    };

    private NetworkService.TxListCallback mTxCallback = new NetworkService.TxListCallback() {
        @Override
        public void onReceiveTransactionList(int totalData, JsonArray txList) {
            txTotalData = totalData;
            Log.d(TAG, "TxTotal=" + totalData);
//            WalletDetailActivity.this.txList = new ArrayList<>();
            TxItem txItem;
            for (int i = 0; i < txList.size(); i++) {
                txItem = new TxItem();
                JsonObject tx = txList.get(i).getAsJsonObject();
                txItem.setTxHash(tx.get("txHash").getAsString());
                txItem.setDate(tx.get("createDate").getAsString());
                txItem.setFrom(tx.get("fromAddr").getAsString());
                txItem.setTo((tx.get("toAddr").getAsString()));
                txItem.setAmount(tx.get("amount").getAsString());
                txItem.setFee(tx.get("fee").getAsString());

                WalletDetailActivity.this.txList.add(txItem);
            }

            if (txAdapter != null && WalletDetailActivity.this.txList.size() > 0) {
                txAdapter.setMoreData(WalletDetailActivity.this.txList);
                txAdapter.moreLoading(false);
                txAdapter.notifyDataSetChanged();
            } else {
                txAdapter = new TransactionListAdapter(
                        WalletDetailActivity.this, selectedEntry, EXCHANGE, WalletDetailActivity.this.txList,
                        mState, mType, mWallet.getCoinType());
                txAdapter.setItemClickListener(mTxClickListener);
                txAdapter.setHeaderClickListener(mHeaderClickListener);
                recyclerTx.setAdapter(txAdapter);
            }

            txAdapter.showLoading(false);

            changeTxState(mWallet.getCoinType());
        }

        @Override
        public void onReceiveError(String resCode) {
            txAdapter = new TransactionListAdapter(
                    WalletDetailActivity.this, selectedEntry, EXCHANGE, WalletDetailActivity.this.txList,
                    mState, mType, mWallet.getCoinType());
            txAdapter.setItemClickListener(mTxClickListener);
            txAdapter.setHeaderClickListener(mHeaderClickListener);
            txAdapter.showLoading(false);
            recyclerTx.setAdapter(txAdapter);
        }

        @Override
        public void onReceiveException(Throwable t) {
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_detail);

        if (getIntent() != null) {
            mWallet = (Wallet) getIntent().getSerializableExtra("walletInfo");
            selectedEntry = (WalletEntry) getIntent().getSerializableExtra("walletEntry");
            entryId = Integer.toString(selectedEntry.getId());
        }

        appbar = findViewById(R.id.appbar);

        refreshLayout = findViewById(R.id.layout_refresh);
        final LoadingHeaderView loadingView = new LoadingHeaderView(this);
        refreshLayout.addHeader(loadingView);
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                txList = new ArrayList<>();
                currentPage = 1;
                getTxList();
            }

            @Override
            public void onLoadMore() {

            }
        });

        txtWalletAlias = findViewById(R.id.txt_wallet_alias);
        txtWalletAlias.setText(mWallet.getAlias());

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(this);

        recyclerTx = findViewById(R.id.recycler_tx);
        recyclerTx.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(-1)) {
                    appbar.setElevation(0);
                } else if (!recyclerView.canScrollVertically(1)) {
                    loadTxList();
                } else {
                    appbar.setElevation(getResources().getDimension(R.dimen.dp6));
                }
            }
        });

        txList = new ArrayList<>();
        currentPage = 1;
        txTotalData = 0;
    }

    @Override
    public void onResume() {
        super.onResume();

        txList = new ArrayList<>();
        currentPage = 1;
        txAdapter = new TransactionListAdapter(
                this, selectedEntry, EXCHANGE, txList, mState, mType, mWallet.getCoinType());
        txAdapter.setItemClickListener(mTxClickListener);
        txAdapter.setHeaderClickListener(mHeaderClickListener);
        recyclerTx.setAdapter(txAdapter);

        getTxList();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;

            case R.id.btn_menu:
                BottomSheetMenuDialog menuDialog = new BottomSheetMenuDialog(this, getString(R.string.manageWallet),
                        BottomSheetMenuDialog.SHEET_TYPE.MENU);
                menuDialog.setMenuData(makeMenus());
                menuDialog.setOnItemClickListener(menuListener);
                menuDialog.show();
                break;
        }
    }

    private void getTxList() {
        if (mBound) {
            txAdapter.showLoading(true);

            HashMap<String, String> rqWallet = new HashMap<>();

            if (mWallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                if (selectedEntry.getType().equals(MyConstants.TYPE_COIN)) {
                    rqWallet.put(Integer.toString(selectedEntry.getId()), mWallet.getAddress());
                    mService.getBalance(rqWallet, mWallet.getCoinType());
                    mService.requestICONTxList(mWallet.getAddress(), currentPage);
                } else {
                    HashMap<String, String[]> tokenList = new HashMap<>();
                    for (WalletEntry entry : mWallet.getWalletEntries()) {
                        if (entry.getType().equals(MyConstants.TYPE_TOKEN))
                            tokenList.put(Integer.toString(entry.getId()), new String[]{mWallet.getAddress(), entry.getContractAddress()});
                    }

                    mService.getTokenBalance(tokenList, Constants.KS_COINTYPE_ETH);
                    mService.requestICONTxList(mWallet.getAddress(), currentPage);
                }
            } else {
                if (selectedEntry.getType().equals(MyConstants.TYPE_COIN)) {
                    rqWallet.put(Integer.toString(selectedEntry.getId()), MyConstants.PREFIX_HEX + mWallet.getAddress());
                    mService.getBalance(rqWallet, mWallet.getCoinType());
                } else {
                    HashMap<String, String[]> tokenList = new HashMap<>();
                    for (WalletEntry entry : mWallet.getWalletEntries()) {
                        if (entry.getType().equals(MyConstants.TYPE_TOKEN))
                            tokenList.put(Integer.toString(entry.getId()),
                                    new String[]{MyConstants.PREFIX_HEX + mWallet.getAddress(), entry.getContractAddress()});
                    }

                    mService.getTokenBalance(tokenList, Constants.KS_COINTYPE_ETH);
                }
            }

        } else {
        }
    }

    private BigInteger getAsset() {
        BigInteger asset = new BigInteger("0");
        for (WalletEntry entry : mWallet.getWalletEntries()) {
            try {
                BigInteger balance = new BigInteger(entry.getBalance());
                asset = asset.add(balance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return asset;
    }

    private void changeTxState(String coinType) {
        String txHash;

        for (TxItem tx : txList) {
            txHash = tx.getTxHash();
            if (coinType.equals(Constants.KS_COINTYPE_ICX)) {
                for (int i = 0; i < ICONexApp.ICXSendInfo.size(); i++) {
                    if (txHash.equals(ICONexApp.ICXSendInfo.get(i).getTxHash())) {
                        ICONexApp.ICXSendInfo.get(i).setIsDone(MyConstants.TX_DONE);
                    }
                }
            } else {
                for (int i = 0; i < ICONexApp.ETHSendInfo.size(); i++) {
                    if (txHash.equals(ICONexApp.ETHSendInfo.get(i).getTxHash())) {
                        ICONexApp.ETHSendInfo.get(i).setIsDone(MyConstants.TX_DONE);
                    }
                }
            }
        }
    }

    private EditTextDialog editTextDialog;

    private EditTextDialog.OnPasswordCallback mPasswordDialogCallback = new EditTextDialog.OnPasswordCallback() {
        @Override
        public void onConfirm(EditTextDialog.RESULT_PWD result, String pwd) {
            JsonObject keyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);
            byte[] bytePrivKey;
            if (mWallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                try {
                    bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, mWallet.getAddress(),
                            keyStore.get("crypto").getAsJsonObject(), mWallet.getCoinType());
                    if (bytePrivKey != null) {
                        if (result == EditTextDialog.RESULT_PWD.TRANSFER) {
                            startActivity(new Intent(WalletDetailActivity.this, ICONTransferActivity.class)
                                    .putExtra("walletInfo", (Serializable) mWallet)
                                    .putExtra("walletEntry", (Serializable) selectedEntry)
                                    .putExtra("privateKey", Hex.toHexString(bytePrivKey)));
                        } else if (result == EditTextDialog.RESULT_PWD.BACKUP) {
                            startActivity(new Intent(WalletDetailActivity.this, WalletBackUpActivity.class)
                                    .putExtra("walletInfo", (Serializable) mWallet)
                                    .putExtra("privateKey", Hex.toHexString(bytePrivKey)));
                        } else {
                            RealmUtil.removeWallet(mWallet.getAddress());
                            try {
                                RealmUtil.loadWallet();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (ICONexApp.mWallets.size() == 0) {
                                startActivity(new Intent(WalletDetailActivity.this, IntroActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                setResult(RES_REFRESH);
                                finish();
                            }
                        }

                        editTextDialog.dismiss();
                    } else {
                        editTextDialog.setError(getString(R.string.errPassword));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    JsonObject crypto = null;
                    if (keyStore.has("crypto"))
                        crypto = keyStore.get("crypto").getAsJsonObject();
                    else
                        crypto = keyStore.get("Crypto").getAsJsonObject();

                    bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, mWallet.getAddress(), crypto, mWallet.getCoinType());
                    if (bytePrivKey != null) {
                        if (result == EditTextDialog.RESULT_PWD.TRANSFER) {
                            startActivity(new Intent(WalletDetailActivity.this, EtherTransferActivity.class)
                                    .putExtra("walletInfo", (Serializable) mWallet)
                                    .putExtra("walletEntry", (Serializable) selectedEntry)
                                    .putExtra("privateKey", Hex.toHexString(bytePrivKey)));
                        } else if (result == EditTextDialog.RESULT_PWD.BACKUP) {
                            startActivity(new Intent(WalletDetailActivity.this, WalletBackUpActivity.class)
                                    .putExtra("walletInfo", (Serializable) mWallet)
                                    .putExtra("privateKey", Hex.toHexString(bytePrivKey)));
                        } else if (result == EditTextDialog.RESULT_PWD.SWAP) {

                            try {
                                Intent swapIntent = new Intent(WalletDetailActivity.this, TokenSwapActivity.class);
                                swapIntent.putExtra(TokenSwapActivity.ARG_WALLET, (Serializable) mWallet);
                                swapIntent.putExtra(TokenSwapActivity.ARG_TOKEN, (Serializable) selectedEntry);
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
                        } else {
                            RealmUtil.removeWallet(mWallet.getAddress());
                            try {
                                RealmUtil.loadWallet();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (ICONexApp.mWallets.size() == 0) {
                                startActivity(new Intent(WalletDetailActivity.this, IntroActivity.class)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                            } else {
                                setResult(RES_REFRESH);
                                finish();
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
        }
    };

    private EditTextDialog.OnConfirmCallback mAliasDialogCallback = new EditTextDialog.OnConfirmCallback() {
        @Override
        public void onConfirm(String alias) {
            if (alias.isEmpty()) {
                editTextDialog.setError(getString(R.string.errWhiteSpace));
                return;
            }

            if (alias.trim().length() == 0) {
                editTextDialog.setError(getString(R.string.errWhiteSpace));
                return;
            }

            for (Wallet info : ICONexApp.mWallets) {
                if (info.getAlias().equals(alias)) {
                    editTextDialog.setError(getString(R.string.duplicateWalletAlias));
                    return;
                }
            }

            RealmUtil.modWalletAlias(mWallet.getAddress(), alias);
            try {
                RealmUtil.loadWallet();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mWallet.setAlias(alias);
            txtWalletAlias.setText(mWallet.getAlias());
            editTextDialog.dismiss();
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BOTTOM_SHEET) {
            if (resultCode == BottomItemSelectActivity.CODE_BASIC) {
                if (data != null) {
                    EXCHANGE = data.getStringExtra("item");
                    txAdapter.setExchange(EXCHANGE);
                }
            }
        } else if (requestCode == RC_CHANGE_PWD) {
            if (resultCode == WalletPwdChangeActivity.RESULT_CODE) {
                Wallet result = (Wallet) data.getExtras().get("result");
                mWallet = result;
            }
        } else if (requestCode == RC_SWAP) {
            if (resultCode == TokenSwapActivity.RES_CREATED)
                setResult(RES_REFRESH);
        }
    }

    private ArrayList<BottomSheetMenu> makeMenus() {
        ArrayList<BottomSheetMenu> menus = new ArrayList<>();
        BottomSheetMenu menu = new BottomSheetMenu(R.drawable.ic_edit, mWallet.getAlias());
        menu.setTag(MyConstants.TAG_MENU_ALIAS);
        menus.add(menu);

        if (mWallet.getCoinType().equals(Constants.KS_COINTYPE_ETH)) {
            menu = new BottomSheetMenu(R.drawable.ic_setting, getString(R.string.menuManageToken));
            menu.setTag(MyConstants.TAG_MENU_TOKEN);
            menus.add(menu);
        }

        menu = new BottomSheetMenu(R.drawable.ic_backup, getString(R.string.menuBackupWallet));
        menu.setTag(MyConstants.TAG_MENU_BACKUP);
        menus.add(menu);

        menu = new BottomSheetMenu(R.drawable.ic_side_lock, getString(R.string.menuChangePwd));
        menu.setTag(MyConstants.TAG_MENU_PWD);
        menus.add(menu);

        menu = new BottomSheetMenu(R.drawable.ic_delete, getString(R.string.menuDeleteWallet));
        menu.setTag(MyConstants.TAG_MENU_REMOVE);
        menus.add(menu);

        return menus;
    }

    private BottomSheetMenuDialog.OnItemClickListener menuListener = new BottomSheetMenuDialog.OnItemClickListener() {
        @Override
        public void onBasicItem(String item) {
            txAdapter.setExchange(item);
        }

        @Override
        public void onCoinItem(int position) {
            selectedEntry = mWallet.getWalletEntries().get(position);
            entryId = Integer.toString(selectedEntry.getId());
            txAdapter.setWalletEntry(selectedEntry);
        }

        @Override
        public void onMenuItem(String tag) {
            switch (tag) {
                case MyConstants.TAG_MENU_ALIAS:
                    editTextDialog = new EditTextDialog(WalletDetailActivity.this, getString(R.string.modWalletAlias));
                    editTextDialog.setHint(getString(R.string.hintWalletAlias));
                    editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.ALIAS);
                    editTextDialog.setAlias(mWallet.getAlias());
                    editTextDialog.setOnConfirmCallback(mAliasDialogCallback);
                    editTextDialog.show();
                    break;

                case MyConstants.TAG_MENU_TOKEN:
                    startActivity(new Intent(WalletDetailActivity.this, TokenManageActivity.class)
                            .putExtra("walletInfo", (Serializable) mWallet));
                    break;

                case MyConstants.TAG_MENU_BACKUP:
                    editTextDialog = new EditTextDialog(WalletDetailActivity.this, getString(R.string.enterWalletPassword));
                    editTextDialog.setHint(getString(R.string.hintWalletPassword));
                    editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                    editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.BACKUP);
                    editTextDialog.setOnPasswordCallback(mPasswordDialogCallback);
                    editTextDialog.show();
                    break;

                case MyConstants.TAG_MENU_PWD:
                    startActivityForResult(new Intent(WalletDetailActivity.this, WalletPwdChangeActivity.class)
                            .putExtra("walletInfo", (Serializable) mWallet), RC_CHANGE_PWD);
                    break;

                case MyConstants.TAG_MENU_REMOVE:
                    BigInteger asset = getAsset();
                    final Basic2ButtonDialog dialog = new Basic2ButtonDialog(WalletDetailActivity.this);
                    if (asset.compareTo(BigInteger.ZERO) == 0) {
                        dialog.setMessage(getString(R.string.removeWallet));
                        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                            @Override
                            public void onOk() {
                                RealmUtil.removeWallet(mWallet.getAddress());
                                try {
                                    RealmUtil.loadWallet();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (ICONexApp.mWallets.size() == 0) {
                                    dialog.dismiss();
                                    startActivity(new Intent(WalletDetailActivity.this, IntroActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                } else {
                                    dialog.dismiss();
                                    setResult(RES_REFRESH);
                                    finish();
                                }
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.show();
                    } else {
                        dialog.setMessage(getString(R.string.warningRemoveWallet));
                        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                            @Override
                            public void onOk() {
                                editTextDialog = new EditTextDialog(WalletDetailActivity.this, getString(R.string.enterWalletPassword));
                                editTextDialog.setHint(getString(R.string.hintWalletPassword));
                                editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                                editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.REMOVE);
                                editTextDialog.setOnPasswordCallback(mPasswordDialogCallback);
                                editTextDialog.show();
                                dialog.dismiss();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.show();
                    }
                    break;
            }
        }
    };

    private TransactionListAdapter.ItemClickListener mTxClickListener = new TransactionListAdapter.ItemClickListener() {
        @Override
        public void onItemClick(String txHash) {
            TrackerDialog dialog = new TrackerDialog(WalletDetailActivity.this, txHash);
            dialog.show();
        }
    };

    private TransactionListAdapter.HeaderClickListener mHeaderClickListener = new TransactionListAdapter.HeaderClickListener() {
        @Override
        public void onSelectCoin() {
            BottomSheetMenuDialog menuDialog = new BottomSheetMenuDialog(WalletDetailActivity.this,
                    getString(R.string.selectCoinNToken), BottomSheetMenuDialog.SHEET_TYPE.COIN_TOKEN);
            menuDialog.setEntriesData(mWallet);
            menuDialog.setOnItemClickListener(menuListener);
            menuDialog.show();
        }

        @Override
        public void onSelectExchange() {
            ArrayList<String> exchangeCodes = new ArrayList<>();
            exchangeCodes.add(EXCHANGE_USD);
            exchangeCodes.add(EXCHANGE_BTC);
            exchangeCodes.add(EXCHANGE_ETH);
            BottomSheetMenuDialog dialog = new BottomSheetMenuDialog(WalletDetailActivity.this,
                    getString(R.string.selectUnitOfEvaluation), BottomSheetMenuDialog.SHEET_TYPE.BASIC);
            dialog.setBasicData(exchangeCodes);
            dialog.setOnItemClickListener(menuListener);
            dialog.show();
        }

        @Override
        public void onSwap() {
            BasicDialog dialog = new BasicDialog(WalletDetailActivity.this);
            try {
                BigInteger tBalance = new BigInteger(selectedEntry.getBalance());
                if (tBalance.equals(BigInteger.ZERO)) {
                    dialog.setMessage(getString(R.string.swapMsgHasNoToken));
                    dialog.show();
                    return;
                }

                BigInteger eBalance = new BigInteger(mWallet.getWalletEntries().get(0).getBalance());
                if (eBalance.equals(BigInteger.ZERO)) {
                    dialog.setMessage(getString(R.string.swapMsgNotEnoughFee));
                    dialog.show();
                    return;
                }

                editTextDialog = new EditTextDialog(WalletDetailActivity.this, getString(R.string.enterWalletPassword));
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

        @Override
        public void onTransfer() {
            if (selectedEntry.getBalance().equals(MyConstants.NO_BALANCE)) {
                BasicDialog dialog = new BasicDialog(WalletDetailActivity.this);
                dialog.setMessage(getString(R.string.errCantWithdraw));
                dialog.show();

                return;
            }

            if (selectedEntry.getType().equals(MyConstants.TYPE_COIN)) {
                if (new BigInteger(selectedEntry.getBalance()).equals(BigInteger.ZERO)) {
                    BasicDialog dialog = new BasicDialog(WalletDetailActivity.this);
                    dialog.setMessage(getString(R.string.errCantWithdraw));
                    dialog.show();

                    return;
                }
            } else {
                if (new BigInteger(mWallet.getWalletEntries().get(0).getBalance()).equals(BigInteger.ZERO)) {
                    BasicDialog dialog = new BasicDialog(WalletDetailActivity.this);
                    dialog.setMessage(getString(R.string.errOwnNotEnough));
                    dialog.show();

                    return;
                }
            }

            editTextDialog = new EditTextDialog(WalletDetailActivity.this, getString(R.string.enterWalletPassword));
            editTextDialog.setHint(getString(R.string.hintWalletPassword));
            editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
            editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.TRANSFER);
            editTextDialog.setOnPasswordCallback(mPasswordDialogCallback);
            editTextDialog.show();
        }

        @Override
        public void onDeposit() {
            startActivity(new Intent(WalletDetailActivity.this, WalletAddressCodeActivity.class)
                    .putExtra("title", mWallet.getAlias())
                    .putExtra("address", mWallet.getAddress()));

        }

        @Override
        public void onSearchCondition() {
            SearchConditionDialog dialog = new SearchConditionDialog(WalletDetailActivity.this, mState, mType);
            dialog.setOnSearchConListener(new SearchConditionDialog.OnSearchConListener() {
                @Override
                public void onSelected(MyConstants.TxState state, MyConstants.TxType type) {
                    mState = state;
                    mType = type;

                    List<TxItem> result = makeTxList(txList, mState, mType);
                    txAdapter = new TransactionListAdapter(WalletDetailActivity.this, selectedEntry, EXCHANGE,
                            result, mState, mType, mWallet.getCoinType());
                    txAdapter.setHeaderClickListener(mHeaderClickListener);
                    txAdapter.setItemClickListener(mTxClickListener);
                    recyclerTx.setAdapter(txAdapter);
                }
            });
            dialog.show();
        }
    };

    private List<TxItem> makeTxList(List<TxItem> txList, MyConstants.TxState state, MyConstants.TxType type) {
        List<TxItem> result = new ArrayList<>();

        if (state == MyConstants.TxState.DONE) {
            for (TxItem tx : txList) {
                if (type == MyConstants.TxType.WHOLENESS) {
                    result.add(tx);
                } else if (type == MyConstants.TxType.REMITTANCE) {
                    if (tx.getFrom().equals(mWallet.getAddress()))
                        result.add(tx);
                } else {
                    if (tx.getTo().equals(mWallet.getAddress()))
                        result.add(tx);
                }
            }
        } else {
            if (mWallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                for (RecentSendInfo send : ICONexApp.ICXSendInfo) {
                    for (TxItem tx : txList) {
                        if (send.getTxHash().equals(tx.getTxHash())
                                && send.getIsDone() == MyConstants.TX_PENDING) {
                            result.add(tx);
                        }
                    }
                }
            }
        }

        return result;
    }

    private boolean hasSwapWallet(String address) throws Exception {
        for (Wallet wallet : ICONexApp.mWallets) {
            if (address.equals(wallet.getAddress()))
                return true;
        }

        return false;
    }

    private void loadTxList() {
        if (txList.size() < txTotalData) {
            txAdapter.moreLoading(true);
            txAdapter.notifyDataSetChanged();

            mService.requestICONTxList(mWallet.getAddress(), ++currentPage);
        }
    }
}
