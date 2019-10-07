package foundation.icon.iconex.view.ui.mainWallet;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.fasterxml.jackson.databind.node.BigIntegerNode;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.DecimalFomatter;
import foundation.icon.iconex.view.AboutActivity;
import foundation.icon.iconex.view.ui.mainWallet.component.ExpanableViewPager;
import foundation.icon.iconex.view.ui.mainWallet.component.RefreshLoadingView;
import foundation.icon.iconex.view.ui.mainWallet.component.TotalAssetInfoView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletAddressCardView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletCardView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletFloatingMenu;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletIndicator;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletManageMenuDialog;
import foundation.icon.iconex.view.ui.mainWallet.items.ICXcoinWalletItem;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.util.ScreenUnit;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.transfer.ICONTransferActivity;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;

import static foundation.icon.iconex.view.MainWalletActivity.TAG;

public class MainWalletFragment extends Fragment {

    private static final String TAG  = MainWalletFragment.class.getSimpleName();

    private enum LoadState {loadedWalletData, loadedTokenData, unloaded}

    // UI field
    private DrawerLayout drawer;
    private CustomActionBar actionBar;
    private RefreshLayout refresh;
    private TotalAssetInfoView totalAssetInfoView;
    private ExpanableViewPager walletViewPager;
    private WalletIndicator walletIndicator;

    private WalletFloatingMenu floatingMenu;
    private WalletAddressCardView walletAddressCard;

    // side menu
    private ImageView imgLogo01;
    private ImageView imgLogo02;
    private Button btnCreateWallet;
    private Button btnLoadWallet;
    private Button btnExportWalletBundle;
    private View line;
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

        floatingMenu = v.findViewById(R.id.floating_menu);
        walletAddressCard = v.findViewById(R.id.wallet_address_card);

        imgLogo01 = v.findViewById(R.id.img_logo_01);
        imgLogo02 = v.findViewById(R.id.img_logo_02);
        btnCreateWallet = v.findViewById(R.id.menu_createWallet);
        btnLoadWallet = v.findViewById(R.id.menu_loadWallet);
        btnExportWalletBundle = v.findViewById(R.id.menu_exportWalletBundle);
        line = v.findViewById(R.id.line);
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

        imgLogo01.setVisibility(View.INVISIBLE);
        imgLogo02.setVisibility(View.INVISIBLE);
        btnCreateWallet.setVisibility(View.INVISIBLE);
        btnLoadWallet.setVisibility(View.INVISIBLE);
        btnExportWalletBundle.setVisibility(View.INVISIBLE);
        btnScreenLock.setVisibility(View.INVISIBLE);
        btnAppVer.setVisibility(View.INVISIBLE);
        btnICONexDisclaimers.setVisibility(View.INVISIBLE);
        line.setVisibility(View.INVISIBLE);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                startAnimation();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                cancelAnimation();
            }
        });

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

        floatingMenu.setOnCilckMenuItemListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WalletCardViewData viewData = getCurrentWalletCardData();

                switch (v.getId()) {
                    case R.id.btn_preps: {
                        ((MainWalletFragment.PRepsMenu) getActivity()).pReps(viewData);
                    }
                    break;
                    case R.id.btn_stake: {
                        ((MainWalletFragment.PRepsMenu) getActivity()).stake(viewData);
                    }
                    break;
                    case R.id.btn_vote: {
                        ((MainWalletFragment.PRepsMenu) getActivity()).vote(viewData);
                    }
                    break;
                    case R.id.btn_iscore: {
                        ((MainWalletFragment.PRepsMenu) getActivity()).iScore(viewData);
                    }
                    break;
                }
            }
        });

        walletAddressCard.setOnDismissListener(new WalletAddressCardView.OnDismissListener() {
            @Override
            public void onDismiss() {
                boolean isICX = getCurrentWalletCardData()
                        .getWalletType() == WalletCardViewData.WalletType.ICXwallet;
                floatingMenu.setEnableFloatingButton(isICX);

                Animator aniShow = AnimatorInflater.loadAnimator(getContext(), R.animator.wallet_card_flip_show);
                aniShow.setTarget(walletViewPager);
                walletViewPager.setVisibility(View.VISIBLE);
                aniShow.start();
            }
        });
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

                WalletCardViewData cardViewData = mShownWalletDataList.get(position);
                if (cardViewData.getWalletType() == WalletCardViewData.WalletType.ICXwallet) {
                    floatingMenu.setEnableFloatingButton(true);

                } else {
                    floatingMenu.setEnableFloatingButton(false);
                }
            }
        });
        walletViewPager.setOnStateChangeListener(new ExpanableViewPager.OnStateChangeListener() {
            @Override
            public void onChangeState(ExpanableViewPager.State state) {
                Log.d("onChangeState", state.name());
                switch (state) {
                    case Expaned: {
                        refresh.setRefreshEnable(false);
                        actionBar.setIsShowIcToggle(false);
                    }
                    break;
                    case Collapsed: {
                        refresh.setRefreshEnable(true);
                        actionBar.setIsShowIcToggle(true);
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
        float scale = getContext().getResources().getDisplayMetrics().density;
        walletViewPager.setCameraDistance(scale * 8000);

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
                WalletCardViewData viewData = getCurrentWalletCardData();
                Wallet wallet = findWalletByViewData(viewData);
                floatingMenu.setEnableFloatingButton(false);
                walletAddressCard.show(wallet);

                Animator aniDisappear = AnimatorInflater.loadAnimator(getContext(), R.animator.wallet_card_flip_disappear);
                aniDisappear.setTarget(walletViewPager);
                aniDisappear.start();
                aniDisappear.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        walletViewPager.setVisibility(View.GONE);
                    }
                });
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
                        walletViewPager.setCurrentItem(0);
                        ((AsyncRequester) getActivity()).notifyWalletDatachage();
                        ((AsyncRequester) getActivity()).asyncRequestRefreshData();
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
                boolean isICX = getCurrentWalletCardData().getWalletType() == WalletCardViewData.WalletType.ICXwallet;
                floatingMenu.setEnableFloatingButton(isICX);
            }
            break;
            case loadedTokenData: {
                mShownWalletDataList.addAll(mTokenDataList);
                actionBar.setTitle(getString(R.string.appbarSelectorCoinsNTokens));
                floatingMenu.setEnableFloatingButton(false);
            }
            break;
        }
        pagerAdapter.notifyDataSetChanged();
        walletIndicator.setSize(mShownWalletDataList.size());
    }

    private void updateTotalAssetsView(TotalAssetsViewData viewData) {
        int exchangeRound = currentExchangeUnit == MainWalletFragment.ExchangeUnit.USD ? 2 : 4;
        String txtTotalAsset = DecimalFomatter.format(viewData.getTotalAsset(), exchangeRound);

        String txtVotingPower = (viewData.getVotedPower() == null ? "-" :
                viewData.getVotedPower().setScale(1, BigDecimal.ROUND_HALF_UP)) + " %";
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
        walletViewPager.setCurrentItem(0);
        updateWalletView();
    }

    private void updateWalletData(List<WalletCardViewData> walletDataList) {
        int exchangeRound = currentExchangeUnit == MainWalletFragment.ExchangeUnit.USD ? 2 : 4;
        mWalletDataList = walletDataList;

        Map<String, WalletCardViewData> mapTokenViewData = new HashMap<>();
        mTokenDataList = new ArrayList<>();
        for (WalletCardViewData walletCard : mWalletDataList) { // wallet
            for (WalletItemViewData entryViewData : walletCard.getLstWallet()) { // entry
                // update balance, exchange (double -> string)
                String txtAmount = DecimalFomatter.format(entryViewData.getAmount());
                String txtExchanged = DecimalFomatter.format(entryViewData.getExchanged(), exchangeRound);
                txtExchanged += " " + currentExchangeUnit.name();

                entryViewData.setTxtAmount(txtAmount).setTxtExchanged(txtExchanged);

                if (entryViewData.getWalletItemType() == WalletItemViewData.WalletItemType.ICXcoin) {
                    if (entryViewData.getStacked() != null && entryViewData.getAmount() != null) {
                        BigDecimal staked = new BigDecimal(ConvertUtil.getValue(entryViewData.getStacked(), 18));
                        BigDecimal balance = staked.add(entryViewData.getAmount());

                        BigDecimal percent = balance.compareTo(BigDecimal.ZERO) == 0 ? null :
                                staked.multiply(new BigDecimal(100))
                                        .divide(balance, 1, BigDecimal.ROUND_HALF_UP);

                        entryViewData.setTxtStacked(DecimalFomatter.format(staked) + " (" + (percent == null ? " - " : percent) + "%)");
                    } else {
                        entryViewData.setTxtStacked("- ( - %)");
                    }

                    if (entryViewData.getiScore() != null) {
                        BigDecimal iscore = new BigDecimal(ConvertUtil.getValue(entryViewData.getiScore(), 18));
                        entryViewData.setTxtIScore(DecimalFomatter.format(iscore));
                    } else {
                        entryViewData.setTxtIScore("-");
                    }
                }

                String tokenName = entryViewData.getName();
                WalletItemViewData topToken = null;

                if (!mapTokenViewData.containsKey(tokenName)) {
                    topToken = new WalletItemViewData(entryViewData);
                    final WalletItemViewData _topToken = topToken;
                    // create wallet card
                    WalletCardViewData cardviewData = new WalletCardViewData()
                            .setWalletType(WalletCardViewData.WalletType.TokenList)
                            .setTitle(tokenName)
                            .setLstWallet(new ArrayList<WalletItemViewData>() {{
                                add(_topToken); // add top token
                            }});
                    mapTokenViewData.put(tokenName, cardviewData);
                    switch (entryViewData.getWalletItemType()) {
                        case ICXcoin: mTokenDataList.add(0, cardviewData); break;
                        case ETHcoin: mTokenDataList.add(mTokenDataList.size() > 0 ? 1 : 0, cardviewData); break;
                        default: mTokenDataList.add(cardviewData); break;
                    }
                }

                // wallet item view
                WalletCardViewData lstTokenViewData = mapTokenViewData.get(tokenName);
                lstTokenViewData.getLstWallet().add(
                        new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.Wallet)
                                .setEntryID(entryViewData.getEntryID())
                                .setSymbol(walletCard.getTitle())
                                .setName(walletCard.getAddress())
                                .setAmount(entryViewData.getAmount())
                                .setExchanged(entryViewData.getExchanged())
                                .setTxtAmount(entryViewData.getTxtAmount())
                                .setTxtExchanged(entryViewData.getTxtExchanged())
                );

                // accumulate top token
                if (topToken == null) {
                    topToken = lstTokenViewData.getLstWallet().get(0);
                    BigDecimal itemAmount = entryViewData.getAmount();
                    if (itemAmount != null) {
                        BigDecimal tokenAmount = topToken.getAmount();
                        if (tokenAmount != null) {
                            topToken.setAmount(itemAmount.add(tokenAmount));
                        } else {
                            topToken.setExchanged(itemAmount);
                        }
                    }
                    BigDecimal itemExchanged = entryViewData.getExchanged();
                    if (itemExchanged != null) {
                        BigDecimal tokenExchanged = topToken.getExchanged();
                        if (tokenExchanged != null) {
                            topToken.setExchanged(itemExchanged.add(tokenExchanged));
                        } else {
                            topToken.setExchanged(itemExchanged);
                        }
                    }

                    // if entry == icx coin
                    if (entryViewData.getWalletItemType() == WalletItemViewData.WalletItemType.ICXcoin) {

                        BigInteger walletStaked = walletCard.getStaked();
                        if (walletStaked != null) {
                            BigInteger topStacked = topToken.getStacked();
                            if (topStacked != null) {
                                topToken.setStacked(topStacked.add(walletStaked));
                            } else {
                                topToken.setStacked(walletStaked);
                            }
                        }

                        BigInteger walletIScore = walletCard.getiScore();
                        if (walletIScore != null) {
                            BigInteger topIScore = topToken.getiScore();
                            if (topIScore != null) {
                                topToken.setiScore(topIScore.add(walletIScore));
                            } else {
                                topToken.setiScore(walletIScore);
                            }
                        }
                    }
                }

            }
        }

        for (WalletCardViewData cardViewData : mTokenDataList) {
            WalletItemViewData entryViewData = cardViewData.getLstWallet().get(0);
            // update balance, exchange (double -> string)
            String txtAmount = DecimalFomatter.format(entryViewData.getAmount());
            String txtExchanged = DecimalFomatter.format(entryViewData.getExchanged(), exchangeRound);

            entryViewData.setTxtAmount(txtAmount).setTxtExchanged(txtExchanged);

            if (entryViewData.getWalletItemType() == WalletItemViewData.WalletItemType.ICXcoin) {
                if (entryViewData.getStacked() != null && entryViewData.getAmount() != null) {
                    BigDecimal staked = new BigDecimal(ConvertUtil.getValue(entryViewData.getStacked(), 18));
                    BigDecimal balance = staked.add(entryViewData.getAmount());

                    BigDecimal percent = balance.compareTo(BigDecimal.ZERO) == 0 ? null :
                            staked.multiply(new BigDecimal(100))
                                    .divide(balance, 1, BigDecimal.ROUND_UP);

                    entryViewData.setTxtStacked(staked.setScale(4, BigDecimal.ROUND_HALF_UP) + " (" + (percent == null ? " - " : percent) + "%)");
                } else {
                    entryViewData.setTxtStacked("- ( - %)");
                }

                if (entryViewData.getiScore() != null) {
                    BigDecimal iscore = new BigDecimal(ConvertUtil.getValue(entryViewData.getiScore(), 18));
                    entryViewData.setTxtIScore(iscore.setScale(4, BigDecimal.ROUND_HALF_UP) + "");
                } else {
                    entryViewData.setTxtIScore("-");
                }
            }
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

    private void startAnimation() {
        Animation aniLogo01 = AnimationUtils.loadAnimation(getContext(), R.anim.sidemenu_logo01);
        Animation aniLogo02 = AnimationUtils.loadAnimation(getContext(), R.anim.sidemenu_logo02);
        imgLogo01.startAnimation(aniLogo01);
        imgLogo02.startAnimation(aniLogo02);

        Animation aniMenuItem = AnimationUtils.loadAnimation(getContext(), R.anim.sidemenu_item_showup);

        btnCreateWallet.startAnimation(aniMenuItem);
        btnLoadWallet.startAnimation(aniMenuItem);
        btnExportWalletBundle.startAnimation(aniMenuItem);
        btnScreenLock.startAnimation(aniMenuItem);
        btnAppVer.startAnimation(aniMenuItem);
        btnICONexDisclaimers.startAnimation(aniMenuItem);

        Animation aniLineAlpha = new AlphaAnimation(0, 0.5f);
        aniLineAlpha.setFillAfter(true);
        aniLineAlpha.setFillBefore(true);
        aniLineAlpha.setStartOffset(100);
        aniLineAlpha.setDuration(300);

        line.startAnimation(aniLineAlpha);

        imgLogo01.setVisibility(View.VISIBLE);
        imgLogo02.setVisibility(View.VISIBLE);
        btnCreateWallet.setVisibility(View.VISIBLE);
        btnLoadWallet.setVisibility(View.VISIBLE);
        btnExportWalletBundle.setVisibility(View.VISIBLE);
        btnScreenLock.setVisibility(View.VISIBLE);
        btnAppVer.setVisibility(View.VISIBLE);
        btnICONexDisclaimers.setVisibility(View.VISIBLE);
        line.setVisibility(View.VISIBLE);
    }

    private void cancelAnimation() {
        imgLogo01.clearAnimation();
        imgLogo02.clearAnimation();

        btnCreateWallet.clearAnimation();
        btnLoadWallet.clearAnimation();
        btnExportWalletBundle.clearAnimation();
        btnScreenLock.clearAnimation();
        btnAppVer.clearAnimation();
        btnICONexDisclaimers.clearAnimation();

        line.clearAnimation();

        imgLogo01.setVisibility(View.INVISIBLE);
        imgLogo02.setVisibility(View.INVISIBLE);
        btnCreateWallet.setVisibility(View.INVISIBLE);
        btnLoadWallet.setVisibility(View.INVISIBLE);
        btnExportWalletBundle.setVisibility(View.INVISIBLE);
        btnScreenLock.setVisibility(View.INVISIBLE);
        btnAppVer.setVisibility(View.INVISIBLE);
        btnICONexDisclaimers.setVisibility(View.INVISIBLE);
        line.setVisibility(View.INVISIBLE);
    }
}