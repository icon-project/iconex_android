package foundation.icon.iconex.view.ui.mainWallet;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.view.AboutActivity;
import foundation.icon.iconex.view.ui.mainWallet.component.ExpanableViewPager;
import foundation.icon.iconex.view.ui.mainWallet.component.RefreshLoadingView;
import foundation.icon.iconex.view.ui.mainWallet.component.TotalAssetInfoView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletCardView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletIndicator;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletManageMenuDialog;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.util.ScreenUnit;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.transfer.ICONTransferActivity;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;
import jnr.ffi.annotations.In;

public class MainWalletFragment extends Fragment {

    private enum LoadState {loadedWalletData, loadedTokenData, unloaded}

    // UI field
    private DrawerLayout drawer;
    private CustomActionBar actionBar;
    private RefreshLayout refresh;
    private TotalAssetInfoView totalAssetInfoView;
    private ExpanableViewPager walletViewPager;
    private WalletIndicator walletIndicator;

    private ImageButton btnAction;
    private ViewGroup bubbleMenuModal;
    private ViewGroup bubbleMenu;
    private Button btnPReps;
    private Button btnStake;
    private Button btnVote;
    private Button btnIScore;

    private Button btnCreateWallet;
    private Button btnLoadWallet;
    private Button btnExportWalletBundle;
    private Button btnScreenLock;
    private Button btnAppVer;
    private Button btnICONexDisclaimers;

    // UI side field
    private PagerAdapter pagerAdapter = null;
    private LoadState mLoadState = LoadState.unloaded;
    private Runnable totalAssetInfoLooper = null;
    private int LOOPING_TIME_INTERVAL = 5000;
    private Handler asyncUpdater = new Handler();

    // Data field
    public enum ExchangeUnit {
        USD, BTC, ETH
    }

    private List<WalletCardViewData> mShownWalletDataList = new ArrayList<>();
    private TotalAssetsViewData mTotalAssetsData = new TotalAssetsViewData();
    private ExchangeUnit currentExchangeUnit = ExchangeUnit.USD;

    // cached data
    private List<WalletCardViewData> mWalletDataList = new ArrayList<>();
    private List<WalletCardViewData> mTokenDataList = new ArrayList<>();

    // data loader
    public interface AsyncRequester {
        void asyncRequestInitData();

        void asyncRequestRefreshData();

        void asyncRequestChangeExchangeUnit(ExchangeUnit exchangeUnit);

        void notifyWalletDatachage();
    }

    // prep menu
    public interface PRepsMenu {
        void pReps(WalletCardViewData viewData);

        void stake(WalletCardViewData viewData);

        void vote(WalletCardViewData viewData);

        void iScore(WalletCardViewData viewData);
    }

    // side menu item
    public interface SideMenu {
        void createWallet();

        void loadWallet();

        void exportWalletBundle();

        void screenLock();

        void appVer();

        void iconexDisclamers();
    }

    public static MainWalletFragment newInstance() {
        MainWalletFragment fragment = new MainWalletFragment();
        return fragment;
    }

    public ExchangeUnit getCurrentExchangeUnit() {
        return currentExchangeUnit;
    }

    public void asyncResponseInit(List<WalletCardViewData> walletDataList) {
        asyncUpdater.post(new Runnable() {
            @Override
            public void run() {
                updateWalletData(walletDataList);
                updateWalletView();
                updateTotalAssetsView(new TotalAssetsViewData());
            }
        });
    }

    public void asyncResponseRefreash(List<WalletCardViewData> walletDataList, TotalAssetsViewData totalAssetsViewData) {
        asyncUpdater.post(new Runnable() {
            @Override
            public void run() {
                updateWalletData(walletDataList);
                updateWalletView();
                updateTotalAssetsView(totalAssetsViewData);
                refresh.stopRefresh(true);
            }
        });
    }

    public void asyncResponseChangeExchangeUnit(ExchangeUnit exchangeUnit, TotalAssetsViewData totalAssetsViewData) {
        asyncUpdater.post(new Runnable() {
            @Override
            public void run() {
                currentExchangeUnit = exchangeUnit;
                updateWalletData(mWalletDataList);
                updateWalletView();
                updateTotalAssetsView(totalAssetsViewData);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.main_wallet_frgment, container, false);

        // load UI
        drawer = v.findViewById(R.id.drawer);
        actionBar = v.findViewById(R.id.actionbar);
        refresh = v.findViewById(R.id.refresh);
        totalAssetInfoView = v.findViewById(R.id.info_total_asset);
        walletViewPager = v.findViewById(R.id.wallet_viewpager);
        walletIndicator = v.findViewById(R.id.wallet_indicator);

        btnAction = v.findViewById(R.id.btn_action);
        bubbleMenuModal = v.findViewById(R.id.bubble_menu_modal);
        bubbleMenu = v.findViewById(R.id.bubble_menu);
        btnPReps = v.findViewById(R.id.btn_preps);
        btnStake = v.findViewById(R.id.btn_stake);
        btnVote = v.findViewById(R.id.btn_vote);
        btnIScore = v.findViewById(R.id.btn_iscore);

        btnCreateWallet = v.findViewById(R.id.menu_createWallet);
        btnLoadWallet = v.findViewById(R.id.menu_loadWallet);
        btnExportWalletBundle = v.findViewById(R.id.menu_exportWalletBundle);
        btnScreenLock = v.findViewById(R.id.menu_screenLock);
        btnAppVer = v.findViewById(R.id.menu_AppVer);
        btnICONexDisclaimers = v.findViewById(R.id.menu_iconexDiscalimers);


        initUI(v);
        ((AsyncRequester) getActivity()).asyncRequestInitData();

        return v;
    }

    private void initUI(View content) {
        // init drawer
        // noting.

        // init side menu
        btnCreateWallet.setOnClickListener(sideMenuListener);
        btnLoadWallet.setOnClickListener(sideMenuListener);
        btnExportWalletBundle.setOnClickListener(sideMenuListener);
        btnScreenLock.setOnClickListener(sideMenuListener);
        btnAppVer.setOnClickListener(sideMenuListener);
        btnICONexDisclaimers.setOnClickListener(sideMenuListener);

        try {
            String version = getActivity()
                    .getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0)
                    .versionName;

            btnAppVer.setText(getText(R.string.appVer) + " " + version);
        } catch (PackageManager.NameNotFoundException e) {
            btnAppVer.setText(getText(R.string.appVer) + " -");
        }

        // init actiobar
        actionBar.setOnActionClickListener(new CustomActionBar.OnActionClickListener() {
            @Override
            public void onClickAction(CustomActionBar.ClickAction action) {
                switch (action) {
                    case btnStart: {
                        drawer.openDrawer(Gravity.LEFT);
                    }
                    break;
                    case btnEnd: {
                        showInfo();
                    }
                    break;
                    case btnToggle: {
                        toggleWalletDataLoad();
                    }
                    break;
                }
            }
        });

        // init RefreshLayout
        refresh.addHeader(new RefreshLoadingView(getContext()) {
            @Override // for fix ExpanableViewpager bug
            public void onRefreshBefore(int scrollY, int headerHeight) {
                super.onRefreshBefore(scrollY, headerHeight);
                walletViewPager.setIsExpanable(false);
            }

            @Override
            public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {
                super.onRefreshComplete(scrollY, headerHeight, isRefreshSuccess);
                walletViewPager.setIsExpanable(true);
            }

            @Override
            public void onRefreshCancel(int scrollY, int headerHeight) {
                super.onRefreshCancel(scrollY, headerHeight);
                walletViewPager.setIsExpanable(true);
            }
        });
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshViewData();
            }

            @Override
            public void onLoadMore() {
            }
        });
        refresh.setRefreshEnable(true);

        // init totalAssetInfoView.
        totalAssetInfoLooper = new Runnable() {
            @Override
            public void run() {
                int inversedIdx = 1 - totalAssetInfoView.getIndex();
                totalAssetInfoView.setIndex(inversedIdx);
            }
        };
        totalAssetInfoView.postDelayed(totalAssetInfoLooper, LOOPING_TIME_INTERVAL);
        totalAssetInfoView.setOnClickExchangeUnitButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeExchnageUnit();
                totalAssetInfoView.removeCallbacks(totalAssetInfoLooper);
                totalAssetInfoView.postDelayed(totalAssetInfoLooper, LOOPING_TIME_INTERVAL);
            }
        });
        totalAssetInfoView.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                totalAssetInfoView.removeCallbacks(totalAssetInfoLooper);
                totalAssetInfoView.postDelayed(totalAssetInfoLooper, LOOPING_TIME_INTERVAL);
            }
        });


        // init wallet viewpager.
        initWalletViewPager(content);

        // init wallet indicator.
        // noting.

        // init btnAction

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = bubbleMenuModal.getVisibility();
                setBubbleMenuShow(visibility != ViewGroup.VISIBLE);
            }
        });

        bubbleMenuModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBubbleMenuShow(false);
            }
        });

        View.OnClickListener bublemenuListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletCardViewData viewData = getCurrentWalletCardData();

                switch (v.getId()) {
                    case R.id.btn_preps: {
                        ((PRepsMenu) getActivity()).pReps(viewData);
                        setBubbleMenuShow(false);
                    }
                    break;
                    case R.id.btn_stake: {
                        ((PRepsMenu) getActivity()).stake(viewData);
                        setBubbleMenuShow(false);
                    }
                    break;
                    case R.id.btn_vote: {
                        ((PRepsMenu) getActivity()).vote(viewData);
                        setBubbleMenuShow(false);
                    }
                    break;
                    case R.id.btn_iscore: {
                        ((PRepsMenu) getActivity()).iScore(viewData);
                        setBubbleMenuShow(false);
                    }
                    break;
                }
            }
        };

        btnPReps.setOnClickListener(bublemenuListener);
        btnStake.setOnClickListener(bublemenuListener);
        btnVote.setOnClickListener(bublemenuListener);
        btnIScore.setOnClickListener(bublemenuListener);

        setBubbleMenuShow(false);
    }

    private void setBubbleMenuShow(boolean isShow) {
        if (!isShow) {
            bubbleMenuModal.setVisibility(View.GONE);
            bubbleMenu.setVisibility(View.GONE);
            btnAction.setImageResource(R.drawable.ic_vote_menu);
        } else {
            bubbleMenuModal.setVisibility(View.VISIBLE);
            bubbleMenu.setVisibility(View.VISIBLE);
            btnAction.setImageResource(R.drawable.ic_close_menu);
        }
    }

    private void initWalletViewPager(View content) {
        int dp10 = ScreenUnit.dp2px(getContext(), 10);
        walletViewPager.setClipToPadding(false);
        walletViewPager.setPadding(dp10, 0, dp10, 0);
        walletViewPager.setPageMargin(dp10);
        walletViewPager.setOffscreenPageLimit(5);
        walletViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateCollapsable();
                walletIndicator.setIndex(position);
            }
        });
        walletViewPager.setOnStateChangeListener(new ExpanableViewPager.OnStateChangeListener() {
            @Override
            public void onChangeState(ExpanableViewPager.State state) {
                switch (state) {
                    case Expaned: {
                        refresh.setRefreshEnable(false);
                    }
                    break;
                    case Collapsed: {
                        refresh.setRefreshEnable(true);
                    }
                    break;
                }
            }
        });
        pagerAdapter = new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                WalletCardView walletCardView = onGenWalletCardView(container);
                container.addView(walletCardView);

                WalletCardViewData data = mShownWalletDataList.get(position);
                walletCardView.bindData(data);
                return walletCardView;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(((View) object));
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @Override
            public int getCount() {
                return mShownWalletDataList.size();
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return POSITION_NONE;
            }
        };
        walletViewPager.setAdapter(pagerAdapter);

        content.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                content.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Rect pagerBound = new Rect();
                Rect infoBound = new Rect();
                walletViewPager.getGlobalVisibleRect(pagerBound);
                totalAssetInfoView.getGlobalVisibleRect(infoBound);
                walletViewPager.setExpandedHeight(pagerBound.bottom - infoBound.top);
                walletViewPager.setCollapseHeight(pagerBound.bottom - infoBound.bottom);
            }
        });
    }

    private void updateCollapsable() {
        int position = walletViewPager.getCurrentItem();
        WalletCardView walletCardView = ((WalletCardView) walletViewPager.getChildAt(position));
        boolean collapsable = walletCardView == null || walletCardView.getIsScrollTop();
        walletViewPager.setIsCollapsable(collapsable);
    }

    private WalletCardView onGenWalletCardView(ViewGroup container) {
        WalletCardView walletCardView = new WalletCardView(container.getContext());
        walletCardView.setOnChagneIsScrollTopListener(new WalletCardView.OnChangeIsScrollTopListener() {
            @Override
            public void onChangeIsScrollTop(boolean isScrollTop) {
                updateCollapsable();
            }
        });

        walletCardView.setOnClickWalletItemListner(new WalletCardView.OnClickWalletItemListner() {
            @Override
            public void onClickWalletItem(WalletItemViewData itemViewData) {
                ((WalletCardView.OnClickWalletItemListner) getActivity()).onClickWalletItem(itemViewData);
            }
        });

        walletCardView.setOnClickQrScanListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletCardViewData viewData = getCurrentWalletCardData();
                Wallet wallet = findWalletByViewData(viewData);
                new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                    @Override
                    public void onPass(byte[] bytePrivateKey) {
                        getContext() // only icx wallet, icx coin wallet entry.
                                .startActivity(new Intent(getContext(), ICONTransferActivity.class)
                                        .putExtra("walletInfo", (Serializable) wallet)
                                        .putExtra("walletEntry", (Serializable) wallet.getWalletEntries().get(0))
                                        .putExtra("privateKey", Hex.toHexString(bytePrivateKey))
                                        .putExtra("qr code scan start", true)
                                );
                    }
                }).show();
            }
        });

        walletCardView.setOnClickQrCodeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "not implement qr code", Toast.LENGTH_SHORT).show();
            }
        });

        walletCardView.setOnClickMoreListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletCardViewData viewData = getCurrentWalletCardData();
                Wallet wallet = findWalletByViewData(viewData);
                new WalletManageMenuDialog(getActivity(), wallet, new WalletManageMenuDialog.OnNotifyWalletDataChangeListener() {
                    @Override
                    public void onNotifyWalletDataChange(WalletManageMenuDialog.UpdateDataType updateDataType) {
                        ((AsyncRequester) getActivity()).notifyWalletDatachage();
                    }
                }).show();
            }
        });

        return walletCardView;
    }

    private Wallet findWalletByViewData(WalletCardViewData viewData) {
        String address = viewData.getAddress();
        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getAddress().equals(address)) {
                return wallet;
            }
        }

        return null;
    }

    private void updateWalletView() {
        mShownWalletDataList.clear();
        switch (mLoadState) {
            case unloaded:
            case loadedWalletData: {
                mShownWalletDataList.addAll(mWalletDataList);
                actionBar.setTitle(getString(R.string.appbarSelectorWallets));
            }
            break;
            case loadedTokenData: {
                mShownWalletDataList.addAll(mTokenDataList);
                actionBar.setTitle(getString(R.string.appbarSelectorCoinsNTokens));
            }
            break;
        }
        pagerAdapter.notifyDataSetChanged();
        walletIndicator.setSize(mShownWalletDataList.size());
    }

    private void updateTotalAssetsView(TotalAssetsViewData viewData) {
        int exchangeRound = currentExchangeUnit == MainWalletFragment.ExchangeUnit.USD ? 2 : 4;
        String txtTotalAsset = viewData.getTotalAsset() == null ? "-" :
                viewData.getTotalAsset().setScale(exchangeRound, BigDecimal.ROUND_FLOOR) + "";
        String txtVotingPower = viewData.getVotedPower() + " %";
        viewData.setTxtExchangeUnit(currentExchangeUnit.name())
                .setTxtTotalAsset(txtTotalAsset).setTxtVotedPower(txtVotingPower);
        mTotalAssetsData = viewData;
        totalAssetInfoView.bind(mTotalAssetsData);
    }

    private void refreshViewData() {
        ((AsyncRequester) getActivity()).asyncRequestRefreshData();
    }

    private void changeExchnageUnit() {
        ExchangeUnit exchangeUnit = null;
        switch (currentExchangeUnit) {
            case USD:
                exchangeUnit = ExchangeUnit.BTC;
                break;
            case BTC:
                exchangeUnit = ExchangeUnit.ETH;
                break;
            case ETH:
                exchangeUnit = ExchangeUnit.USD;
                break;
        }

        ((AsyncRequester) getActivity()).asyncRequestChangeExchangeUnit(exchangeUnit);
    }

    private void toggleWalletDataLoad() {
        // inverse state
        switch (mLoadState) {
            case loadedTokenData: {
                mLoadState = LoadState.loadedWalletData;
            }
            break;
            case unloaded:
            case loadedWalletData: {
                mLoadState = LoadState.loadedTokenData;
            }
            break;
        }
        updateWalletView();
    }

    private void updateWalletData(List<WalletCardViewData> walletDataList) {
        int exchangeRound = currentExchangeUnit == MainWalletFragment.ExchangeUnit.USD ? 2 : 4;
        mWalletDataList = walletDataList;


        Map<String, WalletCardViewData> mapTokenViewData = new HashMap<>();
        for (WalletCardViewData walletViewData : mWalletDataList) {
            for (WalletItemViewData itemViewData : walletViewData.getLstWallet()) {
                // update balance, exchange (double -> string)
                String txtAmount = itemViewData.getAmount() == null ? "-" : // decimal rounding 4
                        itemViewData.getAmount().setScale(4, BigDecimal.ROUND_FLOOR) + "";

                String txtExchanged = itemViewData.getExchanged() == null ? "-" :
                        itemViewData.getExchanged().setScale(exchangeRound, BigDecimal.ROUND_FLOOR)
                                + " " + currentExchangeUnit.name();

                itemViewData.setTxtAmount(txtAmount).setTxtExchanged(txtExchanged);


                String tokenName = itemViewData.getName();
                WalletItemViewData topToken = null;

                if (!mapTokenViewData.containsKey(tokenName)) {
                    topToken = new WalletItemViewData(itemViewData);
                    final WalletItemViewData _topToken = topToken;
                    mapTokenViewData.put(tokenName,
                            // create wallet card
                            new WalletCardViewData()
                                    .setWalletType(WalletCardViewData.WalletType.TokenList)
                                    .setTitle(tokenName)
                                    .setLstWallet(new ArrayList<WalletItemViewData>() {{
                                        add(_topToken); // add top token
                                    }})

                    );
                }

                // wallet item view
                WalletCardViewData lstTokenViewData = mapTokenViewData.get(tokenName);
                lstTokenViewData.getLstWallet().add(
                        new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.Wallet)
                                .setSymbol(walletViewData.getTitle())
                                .setName(walletViewData.getAddress())
                                .setAmount(itemViewData.getAmount())
                                .setExchanged(itemViewData.getExchanged())
                                .setTxtAmount(itemViewData.getTxtAmount())
                                .setTxtExchanged(itemViewData.getTxtExchanged())
                );

                // accumulate top token
                if (topToken == null) {
                    topToken = lstTokenViewData.getLstWallet().get(0);
                    BigDecimal itemAmount = itemViewData.getAmount();
                    if (itemAmount != null) {
                        BigDecimal tokenAmount = topToken.getAmount();
                        if (tokenAmount != null) {
                            topToken.setAmount(itemAmount.add(tokenAmount));
                        } else {
                            topToken.setExchanged(itemAmount);
                        }
                    }
                    BigDecimal itemExchanged = itemViewData.getExchanged();
                    if (itemExchanged != null) {
                        BigDecimal tokenExchanged = topToken.getExchanged();
                        if (tokenExchanged != null) {
                            topToken.setExchanged(itemExchanged.add(tokenExchanged));
                        } else {
                            topToken.setExchanged(itemExchanged);
                        }
                    }

                }

            }
        }

        mTokenDataList = new ArrayList<WalletCardViewData>() {{
            addAll(mapTokenViewData.values());
        }};
        for (WalletCardViewData cardViewData : mTokenDataList) {
            WalletItemViewData itemViewData = cardViewData.getLstWallet().get(0);
            // update balance, exchange (double -> string)
            String txtAmount = itemViewData.getAmount() == null ? "-" : // decimal rounding 4
                    itemViewData.getAmount().setScale(4, BigDecimal.ROUND_FLOOR) + "";

            String txtExchanged = itemViewData.getExchanged() == null ? "-" :
                    itemViewData.getExchanged().setScale(exchangeRound, BigDecimal.ROUND_FLOOR)
                            + " " + currentExchangeUnit.name();

            itemViewData.setTxtAmount(txtAmount).setTxtExchanged(txtExchanged);
        }
    }

    private WalletCardViewData getCurrentWalletCardData() {
        int currentPosition = walletViewPager.getCurrentItem();
        WalletCardViewData viewData = mShownWalletDataList.get(currentPosition);
        return viewData;
    }

    @Override
    public void onStart() {
        super.onStart();
        ((AsyncRequester) getActivity()).notifyWalletDatachage();
    }

    // =========================== side menu listenenr

    private View.OnClickListener sideMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.menu_createWallet: {
                    ((SideMenu) getActivity()).createWallet();
                }
                break;
                case R.id.menu_loadWallet: {
                    ((SideMenu) getActivity()).loadWallet();
                }
                break;
                case R.id.menu_exportWalletBundle: {
                    ((SideMenu) getActivity()).exportWalletBundle();
                }
                break;
                case R.id.menu_screenLock: {
                    ((SideMenu) getActivity()).screenLock();
                }
                break;
                case R.id.menu_AppVer: {
                    ((SideMenu) getActivity()).appVer();
                }
                break;
                case R.id.menu_iconexDiscalimers: {
                    ((SideMenu) getActivity()).iconexDisclamers();
                }
                break;
            }
            drawer.closeDrawer(Gravity.LEFT);
        }
    };

    private void showInfo() {
        startActivity(new Intent(getContext(), AboutActivity.class)
                .putExtra(AboutActivity.PARAM_ABOUT_ITEM_LIST, new ArrayList<Parcelable>() {{

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo00)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo01)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo02)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo10)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo11)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo20)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo21)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo22)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo30)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo31)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo32)));

                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_HEAD,
                            getString(R.string.mainWalletInfo40)));
                    add(new AboutActivity.AboutItem(
                            AboutActivity.AboutItem.TYPE_PARAGRAPH,
                            getString(R.string.mainWalletInfo41)));
                }}));
    }
}