package foundation.icon.iconex.wallet.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.TitleMsgDialog;
import foundation.icon.iconex.view.AuthActivity;
import foundation.icon.iconex.menu.DrawerMenuFragment;
import foundation.icon.iconex.menu.appInfo.AppInfoActivity;
import foundation.icon.iconex.menu.bundle.ExportWalletBundleActivity;
import foundation.icon.iconex.menu.lock.SettingLockActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.view.LoadWalletActivity;
import loopchain.icon.wallet.core.Constants;

import static foundation.icon.MyConstants.EXCHANGE_BTC;
import static foundation.icon.MyConstants.EXCHANGE_ETH;
import static foundation.icon.MyConstants.EXCHANGE_USD;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DrawerMenuFragment.OnMenuSelectListener,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private AppBarLayout appbar;
    private CollapsingToolbarLayout collapsing;

    private Button btnWalletsView, btnCoinsView;
    private Button btnMenu;

    private DrawerLayout mDrawerLayout;
    private ViewGroup menuView;

    private DrawerMenuFragment menuFragment;
    private FrameLayout menuFrameLayout;

    private FrameLayout addrFrameLayout;
    private AddressFragment addrFragment = null;

    private ViewGroup btnInfo;

    private TextView txtTotalAsset;
    private Button btnUSD, btnBTC, btnETH;

    private ViewGroup balanceLoading;

    private RecyclerView walletNameRecycler;
    private WalletNameRecyclerAdapter walletNameRecyclerAdapter = null;

    private ViewPager walletViewPager;
    private ViewPager coinsViewPager;
    private WalletViewPagerAdapter walletViewPagerAdapter = null;
    private CoinViewPagerAdapter coinViewPagerAdapter = null;

    private AppBarLayout appBarLayout;
    private int appbarPosition = 0;

    private ViewGroup layoutLoading;

    private List<String> walletNames;
    private List<String> coinNames;
    private HashMap<String, List<Wallet>> coinsMap;
    private List<CoinsViewItem> coinsList;

    private boolean mBound = false;
    private NetworkService mService;

    private int TOTAL_ENTRIES;
    private int REQUEST_COUNT;

    private String EXCHANGE_UNIT = EXCHANGE_USD;

    private ViewType mViewType = ViewType.WALLETS;

    private GestureDetectorCompat mDetector;

    private boolean isForeground = true;
    private boolean isFingerprintInvalidated = false;

    private boolean canPullDown = false;

    private LinearLayout layoutRefresh;

    private enum ViewType {
        WALLETS,
        COINS
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerExchangeCallback(mExchangeCallback);
            mService.registerBalanceCallback(mBalanceCallback);
            mBound = true;

            String exchangeList = makeExchangeList();


            if (mBound) {
                mService.requestExchangeList(exchangeList);

                final Handler localHandler = new Handler();
                localHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getBalance();
                    }
                }, 300);
            } else {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private NetworkService.ExchangeCallback mExchangeCallback = new NetworkService.ExchangeCallback() {
        @Override
        public void onReceiveExchangeList() {
            setTotalAsset();
        }

        @Override
        public void onReceiveError(String resCode) {
        }

        @Override
        public void onReceiveException(Throwable t) {
        }
    };

    private NetworkService.BalanceCallback mBalanceCallback = new NetworkService.BalanceCallback() {
        @Override
        public void onReceiveICXBalance(String id, String address, String result) {

            if (isForeground) {
                for (int i = 0; i < ICONexApp.wallets.size(); i++) {
                    if (address.equals(ICONexApp.wallets.get(i).getAddress())) {
                        for (int j = 0; j < ICONexApp.wallets.get(i).getWalletEntries().size(); j++) {
                            if (id.equals(Integer.toString(ICONexApp.wallets.get(i).getWalletEntries().get(j).getId()))) {
                                ICONexApp.wallets.get(i).getWalletEntries().get(j).setBalance(result);

                                if (isForeground) {
                                    if (mViewType == ViewType.WALLETS)
                                        walletViewPagerAdapter.notifyDataSetChanged();
                                    else
                                        coinViewPagerAdapter.notifyDataSetChanged();
                                }

                                break;
                            }
                        }
                    }
                }

                boolean isDone = isDoneRequest();
                if (isDone) {
                    balanceLoading.setVisibility(View.GONE);
                    setTotalAsset();
                }
            }
        }

        @Override
        public void onReceiveETHBalance(String id, String address, String result) {

            if (isForeground) {
                String ethAddress = address.substring(2);

                for (int i = 0; i < ICONexApp.wallets.size(); i++) {
                    if (ethAddress.equals(ICONexApp.wallets.get(i).getAddress())) {
                        for (int j = 0; j < ICONexApp.wallets.get(i).getWalletEntries().size(); j++) {
                            if (id.equals(Integer.toString(ICONexApp.wallets.get(i).getWalletEntries().get(j).getId()))) {
                                ICONexApp.wallets.get(i).getWalletEntries().get(j).setBalance(result);

                                if (isForeground) {
                                    if (mViewType == ViewType.WALLETS)
                                        walletViewPagerAdapter.notifyDataSetChanged();
                                    else
                                        coinViewPagerAdapter.notifyDataSetChanged();
                                }

                                break;
                            }
                        }
                    }
                }

                boolean isDone = isDoneRequest();
                if (isDone) {
                    balanceLoading.setVisibility(View.GONE);
                    setTotalAsset();
                }
            }
        }

        @Override
        public void onReceiveError(String id, String address, int code) {

            if (address.startsWith(MyConstants.PREFIX_HEX))
                address = address.substring(2);

            for (int i = 0; i < ICONexApp.wallets.size(); i++) {
                if (address.equals(ICONexApp.wallets.get(i).getAddress())) {
                    for (int j = 0; j < ICONexApp.wallets.get(i).getWalletEntries().size(); j++) {
                        if (id.equals(Integer.toString(ICONexApp.wallets.get(i).getWalletEntries().get(j).getId()))) {
                            ICONexApp.wallets.get(i).getWalletEntries().get(j).setBalance(MyConstants.NO_BALANCE);

                            if (isForeground) {
                                if (mViewType == ViewType.WALLETS)
                                    walletViewPagerAdapter.notifyDataSetChanged();
                                else
                                    coinViewPagerAdapter.notifyDataSetChanged();
                            }

                            break;
                        }
                    }
                }
            }

            boolean isDone = isDoneRequest();
            if (isDone) {
                balanceLoading.setVisibility(View.GONE);
                setTotalAsset();
            }
        }

        @Override
        public void onReceiveException(String id, String address, String msg) {

            if (address.startsWith(MyConstants.PREFIX_HEX))
                address = address.substring(2);

            for (int i = 0; i < ICONexApp.wallets.size(); i++) {
                if (address.equals(ICONexApp.wallets.get(i).getAddress())) {
                    for (int j = 0; j < ICONexApp.wallets.get(i).getWalletEntries().size(); j++) {
                        if (id.equals(Integer.toString(ICONexApp.wallets.get(i).getWalletEntries().get(j).getId()))) {
                            ICONexApp.wallets.get(i).getWalletEntries().get(j).setBalance(MyConstants.NO_BALANCE);

                            if (isForeground) {
                                if (mViewType == ViewType.WALLETS)
                                    walletViewPagerAdapter.notifyDataSetChanged();
                                else
                                    coinViewPagerAdapter.notifyDataSetChanged();
                            }

                            break;
                        }
                    }
                }
            }

            boolean isDone = isDoneRequest();
            if (isDone) {
                balanceLoading.setVisibility(View.GONE);
                setTotalAsset();
            }
        }
    };

    private synchronized boolean isDoneRequest() {
        if (REQUEST_COUNT > 0) {
            REQUEST_COUNT--;
        }

        if (REQUEST_COUNT == 0)
            return true;
        else
            return false;
    }

    private void setTotalRequestCount(int count) {
        REQUEST_COUNT = count;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isForeground = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mService != null)
            mService.stopGetBalance();

        // Unbind from the service
        isForeground = false;

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getIntent().getExtras() != null) {
            MyConstants.MainPopUp popUp = (MyConstants.MainPopUp) getIntent().getExtras().get("popup");
            if (popUp == MyConstants.MainPopUp.BUNDLE) {
                BasicDialog dialog = new BasicDialog(this);
                dialog.setMessage(getString(R.string.msgLoadBundle));
                dialog.show();
            }
        }

        isFingerprintInvalidated = getIntent().getBooleanExtra(AuthActivity.EXTRA_INVALIDATED, false);

        collapsing = findViewById(R.id.collapsing);

        appBarLayout = findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                int totalRange = appBarLayout.getTotalScrollRange();

                appbarPosition = verticalOffset;
                if (appbarPosition == 0)
                    canPullDown = false;
                else
                    canPullDown = true;
            }
        });

        layoutRefresh = findViewById(R.id.layout_refresh);

        btnInfo = findViewById(R.id.btn_info);
        btnInfo.setOnClickListener(this);

        btnWalletsView = findViewById(R.id.btn_selector_wallets);
        btnWalletsView.setOnClickListener(this);
        btnWalletsView.setSelected(true);
        btnCoinsView = findViewById(R.id.btn_selector_coins_n_tokens);
        btnCoinsView.setOnClickListener(this);

        btnMenu = findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(this);

        balanceLoading = findViewById(R.id.layout_asset_progress);

        txtTotalAsset = findViewById(R.id.txt_total_asset);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(txtTotalAsset, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        btnUSD = findViewById(R.id.btn_select_usd);
        btnUSD.setOnClickListener(this);
        btnUSD.setSelected(true);
        btnBTC = findViewById(R.id.btn_select_btc);
        btnBTC.setOnClickListener(this);
        btnETH = findViewById(R.id.btn_select_eth);
        btnETH.setOnClickListener(this);

        walletNameRecycler = findViewById(R.id.recycler_wallets);
        walletNameRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        walletViewPager = findViewById(R.id.view_pager_wallets);
        walletViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                walletNameRecyclerAdapter.setSelectedPosition(position);
                walletNameRecycler.smoothScrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        coinsViewPager = findViewById(R.id.view_pager_coins);
        coinsViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                walletNameRecyclerAdapter.setSelectedPosition(position);
                walletNameRecycler.smoothScrollToPosition(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        layoutLoading = findViewById(R.id.layout_loading);

        mDetector = new GestureDetectorCompat(this, this);
        mDetector.setOnDoubleTapListener(this);

//        menuFrameLayout = findViewById(R.id.side_menu_container);
        addrFrameLayout = findViewById(R.id.fragment_address);
        menuFrameLayout = findViewById(R.id.fragment_menu);

        setWalletsView();
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        showRecoverFingerprintAuth();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_info:
                BasicDialog dialog = new BasicDialog(this);
                dialog.setMessage(getString(R.string.infoTotalAsset));
                dialog.show();
                break;

            case R.id.btn_selector_wallets:
                setWalletsView();
                break;

            case R.id.btn_selector_coins_n_tokens:
                setCoinsView();
                break;

            case R.id.btn_select_usd:
                if (!btnUSD.isSelected())
                    setExchangeSelected(v.getId());
                break;

            case R.id.btn_select_btc:
                if (!btnBTC.isSelected())
                    setExchangeSelected(v.getId());
                break;

            case R.id.btn_select_eth:
                if (!btnETH.isSelected())
                    setExchangeSelected(v.getId());
                break;

            case R.id.btn_menu:
                showSideMenu();
                break;

        }
    }

    private void getBalance() {

        balanceLoading.setVisibility(View.VISIBLE);

        Object[] balanceList = makeGetBalanceList();
        HashMap<String, String> icxList = (HashMap<String, String>) balanceList[0];
        HashMap<String, String[]> ircList = (HashMap<String, String[]>) balanceList[1];
        HashMap<String, String> ethList = (HashMap<String, String>) balanceList[2];
        HashMap<String, String[]> ercList = (HashMap<String, String[]>) balanceList[3];

        TOTAL_ENTRIES = icxList.size() + ircList.size() + ethList.size() + ercList.size();
        setTotalRequestCount(icxList.size() + ircList.size() + ethList.size() + ercList.size());
        mService.getBalance(icxList, Constants.KS_COINTYPE_ICX);
        mService.getTokenBalance(ircList, Constants.KS_COINTYPE_ICX);
        mService.getBalance(ethList, Constants.KS_COINTYPE_ETH);
        mService.getTokenBalance(ercList, Constants.KS_COINTYPE_ETH);
    }

    public void notifyWalletChanged() {
        if (mViewType == ViewType.WALLETS)
            setWalletsView();
        else
            setCoinsView();

        getBalance();
    }

    public void refreshNameView() {
        if (mViewType == ViewType.WALLETS) {
            int position = walletViewPager.getCurrentItem();

            makeNameList();
            walletNameRecyclerAdapter.setNameList(walletNames);

            walletViewPagerAdapter = new WalletViewPagerAdapter(getSupportFragmentManager());
            walletViewPager.setAdapter(walletViewPagerAdapter);

            walletViewPager.setCurrentItem(position, true);
        } else {
            int position = coinsViewPager.getCurrentItem();

            coinsList = new ArrayList<>();
            coinsList = makeListFromCoins();
            makeCoinNameList(coinsList);

            walletNameRecyclerAdapter.setNameList(coinNames);

            coinViewPagerAdapter = new CoinViewPagerAdapter(getSupportFragmentManager(), coinsList);
            coinsViewPager.setAdapter(coinViewPagerAdapter);

            coinsViewPager.setCurrentItem(position, true);
        }
    }

    private void makeNameList() {
        walletNames = new ArrayList<>();
        for (Wallet wallet : ICONexApp.wallets) {
            walletNames.add(wallet.getAlias());
        }
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

    private void makeCoinNameList(List<CoinsViewItem> list) {
        coinNames = new ArrayList<>();

        for (CoinsViewItem item : list) {
            coinNames.add(item.getName());
        }
    }

    private List<CoinsViewItem> makeListFromCoins() {
        List<CoinsViewItem> list = new ArrayList<>();

        for (Wallet wallet : ICONexApp.wallets) {
            for (WalletEntry entry : wallet.getWalletEntries()) {
                String key;
                if (entry.getType().equals(MyConstants.TYPE_COIN))
                    key = entry.getSymbol();
                else {
                    key = entry.getContractAddress();
                }

                int itemPos = -1;
                for (int i = 0; i < list.size(); i++) {
                    CoinsViewItem tmp = list.get(i);
                    if (tmp.getType().equals(MyConstants.TYPE_COIN)) {
                        if (tmp.getSymbol().equals(key)) {
                            itemPos = i;
                            break;
                        }
                    } else {
                        if (tmp.getContractAddr().equals(key)) {
                            itemPos = i;
                            break;
                        }
                    }
                }

                if (itemPos >= 0) {
                    List<Wallet> wallets = list.get(itemPos).getWallets();
                    wallets.add(wallet);
                    list.get(itemPos).setWallets(wallets);
                } else {
                    CoinsViewItem item = new CoinsViewItem();
                    if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                        item.setType(MyConstants.TYPE_COIN);
                        item.setName(entry.getName());
                    } else {
                        item.setType(MyConstants.TYPE_TOKEN);
                        item.setContractAddr(entry.getContractAddress());
                        item.setName(entry.getName() + " Token");
                    }

                    item.setSymbol(entry.getSymbol());
                    item.setDec(entry.getDefaultDec());

                    List<Wallet> wallets = new ArrayList<>();
                    wallets.add(wallet);
                    item.setWallets(wallets);

                    if (item.getType().equals(MyConstants.TYPE_COIN)
                            && item.getName().equals(MyConstants.NAME_ICX))
                        list.add(0, item);
                    else
                        list.add(item);
                }
            }
        }

        return list;
    }

    private void setCoinsView() {
        mViewType = ViewType.COINS;

        coinsList = new ArrayList<>();
        coinsList = makeListFromCoins();
        makeCoinNameList(coinsList);

        walletNameRecyclerAdapter = new WalletNameRecyclerAdapter(this, coinNames);
        walletNameRecyclerAdapter.setClickListener(new WalletNameRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                coinsViewPager.setCurrentItem(position, true);
            }
        });
        walletNameRecycler.setAdapter(walletNameRecyclerAdapter);

        walletViewPager.setVisibility(View.GONE);
        coinsViewPager.setVisibility(View.VISIBLE);

        coinViewPagerAdapter = new CoinViewPagerAdapter(getSupportFragmentManager(), coinsList);
        coinsViewPager.setAdapter(coinViewPagerAdapter);

        btnWalletsView.setSelected(false);
        btnWalletsView.setEnabled(true);
        btnCoinsView.setSelected(true);
        btnCoinsView.setEnabled(false);
    }

    private void setWalletsView() {
        mViewType = ViewType.WALLETS;

        makeNameList();
        walletNameRecyclerAdapter = new WalletNameRecyclerAdapter(this, walletNames);
        walletNameRecyclerAdapter.setClickListener(new WalletNameRecyclerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                walletViewPager.setCurrentItem(position, true);
            }
        });
        walletNameRecycler.setAdapter(walletNameRecyclerAdapter);

        walletViewPager.setVisibility(View.VISIBLE);
        coinsViewPager.setVisibility(View.GONE);

        walletViewPagerAdapter = new WalletViewPagerAdapter(getSupportFragmentManager());
        walletViewPager.setAdapter(walletViewPagerAdapter);

        btnWalletsView.setSelected(true);
        btnWalletsView.setEnabled(false);
        btnCoinsView.setSelected(false);
        btnCoinsView.setEnabled(true);
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

    private void setTotalAsset() {
        Double totalAsset = 0.0;
        int cntNoBalance = 0;
        for (Wallet info : ICONexApp.wallets) {
            for (WalletEntry entry : info.getWalletEntries()) {

                if (!entry.getBalance().isEmpty()) {
                    if (entry.getBalance().equals(MyConstants.NO_BALANCE)) {
                        cntNoBalance++;
                    } else {
                        try {
                            BigInteger balance = new BigInteger(entry.getBalance());
                            String value = ConvertUtil.getValue(balance, entry.getDefaultDec());
                            Double doubBalance = Double.parseDouble(value);

                            String exchange = entry.getSymbol().toLowerCase() + getExchangeUnit().toLowerCase();
                            String strPrice;
                            if (exchange.equals("etheth"))
                                strPrice = "1";
                            else
                                strPrice = ICONexApp.EXCHANGE_TABLE.get(exchange);
                            if (strPrice != null) {
                                Double price = Double.parseDouble(strPrice);

                                totalAsset += doubBalance * price;
                            }
                        } catch (Exception e) {
                            // Do nothing.
                        }
                    }
                }
            }
        }

        if (cntNoBalance == TOTAL_ENTRIES) {
            txtTotalAsset.setText(MyConstants.NO_BALANCE);
        } else {
            if (EXCHANGE_UNIT.equals(EXCHANGE_USD)) {
                txtTotalAsset.setText(String.format(Locale.getDefault(), "%,.2f", totalAsset));
            } else {
                txtTotalAsset.setText(String.format(Locale.getDefault(), "%,.4f", totalAsset));
            }
        }
    }

    private void setExchangeSelected(int id) {
        switch (id) {
            case R.id.btn_select_usd:
                btnUSD.setSelected(true);
                btnBTC.setSelected(false);
                btnETH.setSelected(false);
                EXCHANGE_UNIT = EXCHANGE_USD;
                break;

            case R.id.btn_select_btc:
                btnUSD.setSelected(false);
                btnBTC.setSelected(true);
                btnETH.setSelected(false);
                EXCHANGE_UNIT = EXCHANGE_BTC;
                break;

            case R.id.btn_select_eth:
                btnUSD.setSelected(false);
                btnBTC.setSelected(false);
                btnETH.setSelected(true);
                EXCHANGE_UNIT = EXCHANGE_ETH;
                break;
        }

        if (mViewType == ViewType.WALLETS)
            walletViewPagerAdapter.notifyDataSetChanged();
        else
            coinViewPagerAdapter.notifyDataSetChanged();

        setTotalAsset();
    }

    public String getExchangeUnit() {
        return EXCHANGE_UNIT;
    }

    public void hideWalletAddress() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.remove(addrFragment);
        transaction.commit();

        addrFrameLayout.setVisibility(View.GONE);
    }

    public void showWalletAddress(String alias, String address) {
        addrFrameLayout.setVisibility(View.VISIBLE);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        addrFragment = AddressFragment.newInstance(alias, address);
        transaction.add(addrFrameLayout.getId(), addrFragment);
        transaction.commit();
    }

    private FragmentTransaction menuTransaction;

    private void showSideMenu() {
        menuFrameLayout.setVisibility(View.VISIBLE);
        menuTransaction = getSupportFragmentManager().beginTransaction();
        menuTransaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        menuFragment = DrawerMenuFragment.newInstance();
        menuTransaction.add(menuFrameLayout.getId(), menuFragment);
        menuTransaction.addToBackStack(null);
        menuTransaction.commit();
    }

    private void hideSideMenu() {
        getSupportFragmentManager().popBackStackImmediate();

        menuFrameLayout.setVisibility(View.GONE);
    }

    public void showLoading() {
        if (layoutLoading.getVisibility() != View.VISIBLE)
            layoutLoading.setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (layoutLoading.getVisibility() != View.GONE)
            layoutLoading.setVisibility(View.GONE);
    }

    private void showRecoverFingerprintAuth() {
        if (isFingerprintInvalidated) {
            Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
            dialog.setMessage(getString(R.string.authMsgRecoverFingerprintAuth));
            dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                @Override
                public void onOk() {
                    isFingerprintInvalidated = false;
                    startActivity(new Intent(MainActivity.this, SettingLockActivity.class)
                            .putExtra(SettingLockActivity.ARG_TYPE, MyConstants.TypeLock.RECOVER));
                }

                @Override
                public void onCancel() {
                    isFingerprintInvalidated = false;
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onClose() {
        hideSideMenu();
    }


    @Override
    public void onMenuClicked(DrawerMenuFragment.SIDE_MENU menu) {
        if (menu == DrawerMenuFragment.SIDE_MENU.CREATE_WALLET) {
//            startActivity(new Intent(this, CreateWalletActivity.class));
        } else if (menu == DrawerMenuFragment.SIDE_MENU.IMPORT_WALLET) {
            startActivity(new Intent(this, LoadWalletActivity.class));
        } else if (menu == DrawerMenuFragment.SIDE_MENU.EXPORT_WALLET_BUNDLE) {
            startActivity(new Intent(this, ExportWalletBundleActivity.class));
        } else if (menu == DrawerMenuFragment.SIDE_MENU.SETTING_LOCK) {
            startActivity(new Intent(this, SettingLockActivity.class)
                    .putExtra(SettingLockActivity.ARG_TYPE, MyConstants.TypeLock.DEFAULT));
        } else if (menu == DrawerMenuFragment.SIDE_MENU.APP_INFO) {
            startActivity(new Intent(this, AppInfoActivity.class));
        } else if (menu == DrawerMenuFragment.SIDE_MENU.ICONex_DISCLAIMER) {
            TitleMsgDialog dialog = new TitleMsgDialog(this);
            dialog.setTitle(getString(R.string.ICONexDisclaimers));
            SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.disclaimersHeader)
                    + "\n\n" + getString(R.string.disclaimersContents));
            builder.setSpan(new StyleSpan(Typeface.BOLD), 0, getString(R.string.disclaimersHeader).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            dialog.setMessage(builder.toString());
            dialog.show();
        }

        hideSideMenu();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                mDetector.onTouchEvent(ev);
                break;
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public void onBackPressed() {

        mService.stopGetBalance();

        finishAffinity();
    }
}
