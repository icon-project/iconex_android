package foundation.icon.iconex.dev_mainWallet;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.component.ExpanableViewPager;
import foundation.icon.iconex.dev_mainWallet.component.RefreshLoadingView;
import foundation.icon.iconex.dev_mainWallet.component.TotalAssetInfoView;
import foundation.icon.iconex.dev_mainWallet.component.WalletCardView;
import foundation.icon.iconex.dev_mainWallet.component.WalletIndicator;
import foundation.icon.iconex.dev_mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.util.ScreenUnit;
import foundation.icon.iconex.view.PRepIScoreActivity;
import foundation.icon.iconex.view.PRepListActivity;
import foundation.icon.iconex.view.PRepStakeActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;

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

    // UI side field
    private PagerAdapter pagerAdapter = null;
    private LoadState mLoadState = LoadState.unloaded;
    private Runnable totalAssetInfoLooper = null;
    private int LOOPING_TIME_INTERVAL = 5000;
    private Handler asyncUpdater = new Handler();

    // Data field
    private List<WalletCardViewData> mShownWalletDataList = new ArrayList<>();
    private TotalAssetsViewData mTotalAssetsData = new TotalAssetsViewData();

    // cached data
    private List<WalletCardViewData> mWalletDataList = new ArrayList<>();
    private List<WalletCardViewData> mTokenDataList = new ArrayList<>();

    // data loader
    public interface AsyncRequester {
        void requestInitData();

        void requestRefreshData();
    }

    public static MainWalletFragment newInstance() {
        MainWalletFragment fragment = new MainWalletFragment();
        return fragment;
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

        initUI(v);
        ((AsyncRequester) getActivity()).requestInitData();

        return v;
    }

    private void initUI(View content) {
        // init drawer
        // noting.

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
                        // TODO: not implement
                        Toast.makeText(getContext(), "not implement", Toast.LENGTH_SHORT).show();
                    }
                    break;
                    case btnToggle: {
                        toggleWalletDataLoad();
                        Toast.makeText(getContext(), "not implement", Toast.LENGTH_SHORT).show();
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
                totalAssetInfoView.postDelayed(this, LOOPING_TIME_INTERVAL);
                int inversedIdx = 1 - totalAssetInfoView.getIndex();
                totalAssetInfoView.setIndex(inversedIdx);
            }
        };
        totalAssetInfoView.postDelayed(totalAssetInfoLooper, LOOPING_TIME_INTERVAL);

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
                switch (v.getId()) {
                    case R.id.btn_preps: {
                        startActivity(new Intent(getContext(), PRepListActivity.class));
                        setBubbleMenuShow(false);
                    }
                    break;
                    case R.id.btn_stake: {
                        int position = walletViewPager.getCurrentItem();
                        Wallet wallet = ICONexApp.wallets.get(position);
                        startActivity(new Intent(getContext(), PRepStakeActivity.class)
                                .putExtra("wallet", (Serializable) wallet));
                        setBubbleMenuShow(false);
                    }
                    break;
                    case R.id.btn_vote: {
                        Toast.makeText(getContext(), "not implement btn_vote", Toast.LENGTH_SHORT).show();
                        setBubbleMenuShow(false);
                    }
                    break;
                    case R.id.btn_iscore: {
                        int position = walletViewPager.getCurrentItem();
                        Wallet wallet = ICONexApp.wallets.get(position);
                        startActivity(new Intent(getContext(), PRepIScoreActivity.class)
                                .putExtra("wallet", (Serializable) wallet));
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
                Log.d("get Item Pos", object.getClass().getSimpleName());
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

        return walletCardView;
    }

    private void updateWalletView() {
        mShownWalletDataList.clear();
        switch (mLoadState) {
            case unloaded:
            case loadedWalletData: {
                mShownWalletDataList.addAll(mWalletDataList);
                // TODO: hardcoding
                actionBar.setTitle("지갑");
            }
            break;
            case loadedTokenData: {
                mShownWalletDataList.addAll(mTokenDataList);
                // TODO: hardcoding
                actionBar.setTitle("코인&토큰");
            }
            break;
        }
        pagerAdapter.notifyDataSetChanged();
        walletIndicator.setSize(mShownWalletDataList.size());
    }

    private void updateTotalAssetsView() {
        totalAssetInfoView.bind(mTotalAssetsData);
    }

    private void refreshViewData() {
        ((AsyncRequester) getActivity()).requestRefreshData();
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
        mWalletDataList = walletDataList;
        Map<String, WalletCardViewData> mapTokenViewData = new HashMap<>();

        for (WalletCardViewData walletViewData : mWalletDataList) {
            for (WalletItemViewData itemViewData : walletViewData.getLstWallet()) {
                String tokenName = itemViewData.getName();

                if (!mapTokenViewData.containsKey(tokenName)) {
                    mapTokenViewData.put(tokenName,
                            new WalletCardViewData()
                                    .setWalletType(WalletCardViewData.WalletType.TokenList)
                                    .setTitle(tokenName)
                                    .setLstWallet(new ArrayList<WalletItemViewData>() {{
                                        add(itemViewData);
                                    }})
                    );
                }

                WalletCardViewData lstTokenViewData = mapTokenViewData.get(tokenName);
                lstTokenViewData.getLstWallet().add(
                        new WalletItemViewData()
                                .setWalletItemType(WalletItemViewData.WalletItemType.Wallet)
                                .setSymbol(walletViewData.getTitle())
                                //.setName() TODO: 앗 주소 빠졌다.
                                .setAmount("0.00")
                                .setExchanged("0.00 USD")
                );
            }
        }
        mTokenDataList = new ArrayList<WalletCardViewData>() {{
            addAll(mapTokenViewData.values());
        }};
    }

    public void asyncResponseInit(List<WalletCardViewData> walletDataList, TotalAssetsViewData totalAssetsViewData) {
        asyncUpdater.post(new Runnable() {
            @Override
            public void run() {
                updateWalletData(walletDataList);
                updateWalletView();
                mTotalAssetsData = totalAssetsViewData;
                updateTotalAssetsView();
            }
        });
    }

    public void asyncResponseRefreash(
            List<WalletCardViewData> walletDataList, TotalAssetsViewData totalAssetsViewData) {
        asyncUpdater.post(new Runnable() {
            @Override
            public void run() {
                updateWalletData(walletDataList);
                updateWalletView();
                mTotalAssetsData = totalAssetsViewData;
                updateTotalAssetsView();
                refresh.stopRefresh(true);
            }
        });
    }
}
