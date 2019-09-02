package foundation.icon.iconex.dev_mainWallet;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.component.ExpanableViewPager;
import foundation.icon.iconex.dev_mainWallet.component.RefreshLoadingView;
import foundation.icon.iconex.dev_mainWallet.component.TotalAssetInfoView;
import foundation.icon.iconex.dev_mainWallet.component.WalletCardView;
import foundation.icon.iconex.dev_mainWallet.component.WalletIndicator;
import foundation.icon.iconex.dev_mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.util.ScreenUnit;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class MainWalletFragment extends Fragment {

    private enum LoadState { loadedWalletData, loadedTokenData, unloaded }

    // UI field
    private DrawerLayout drawer;
    private CustomActionBar actionBar;
    private RefreshLayout refresh;
    private TotalAssetInfoView totalAssetInfoView;
    private ExpanableViewPager walletViewPager;
    private WalletIndicator walletIndicator;
    private ImageButton btnAction;

    // UI side field
    private PagerAdapter pagerAdapter = null;
    private LoadState mLoadState = LoadState.unloaded;
    private Runnable totalAssetInfoLooper = null;
    private int LOOPING_TIME_INTERVAL = 5000;

    // Data field
    private List<WalletCardViewData> walletDataList = new ArrayList<>();
    private TotalAssetsViewData totalAssetsData = new TotalAssetsViewData();

    // data loader
    public interface SyncRequester {
        TotalAssetsViewData onSyncRequestTotalAssetsData();
        List<WalletCardViewData> onSyncRequestWalletListData();
        List<WalletCardViewData> onSyncRequestTokenListData();
    }

    public static MainWalletFragment newInstance(){
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

        initUI(v);
        refreshAllData();

        return v;
    }

    private void initUI(View content) {
        // init drawer
        // noting.

        // init actiobar
        actionBar.setmOnActionClickListener(new CustomActionBar.OnActionClickListener() {
            @Override
            public void onClickAction(CustomActionBar.ClickAction action) {
                switch (action) {
                    case btnStart: {
                        drawer.openDrawer(Gravity.LEFT);
                    } break;
                    case btnEnd: {
                        // TODO: not implement
                        Toast.makeText(getContext(), "not implement", Toast.LENGTH_SHORT).show();
                    } break;
                    case btnToggle: {
                        toggleWalletDataLoad();
                        Toast.makeText(getContext(), "not implement", Toast.LENGTH_SHORT).show();
                    } break;
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
            public void onRefresh() { refreshAllData(); }
            @Override
            public void onLoadMore() { }
        });
        refresh.setRefreshEnable(true);

        // init totalAssetInfoView.
        totalAssetInfoLooper  = new Runnable() {
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

        // init btnAction
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });
        pagerAdapter = new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                WalletCardView walletCardView = onGenWalletCardView(container);
                container.addView(walletCardView);

                WalletCardViewData data = walletDataList.get(position);
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
                return walletDataList.size();
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                Log.d("go", "go");
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
        boolean collapsable = walletCardView.getIsScrollTop();
        walletViewPager.setIsCollapsable(collapsable);
    }

    private WalletCardView onGenWalletCardView (ViewGroup container) {
        WalletCardView walletCardView = new WalletCardView(container.getContext());

        // init UI
        walletCardView.setOnChagneIsScrollTopListener(new WalletCardView.OnChangeIsScrollTopListener() {
            @Override
            public void onChangeIsScrollTop(boolean isScrollTop) {
                updateCollapsable();
            }
        });

        return walletCardView;
    }

    private void updateWalletView() {
        pagerAdapter.notifyDataSetChanged();
        walletIndicator.setSize(walletDataList.size());
    }

    private void updateTotalAssetsView() {

    }

    private void refreshAllData() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                totalAssetsData = ((SyncRequester) getActivity()).onSyncRequestTotalAssetsData();
                switch (mLoadState) {
                    case unloaded:
                    case loadedWalletData: {
                        walletDataList = ((SyncRequester) getActivity()).onSyncRequestWalletListData();
                        mLoadState = LoadState.loadedWalletData;
                    } break;
                    case loadedTokenData: {
                        walletDataList = ((SyncRequester) getActivity()).onSyncRequestTokenListData();
                        mLoadState = LoadState.loadedTokenData;
                    } break;

                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        updateTotalAssetsView();
                        updateWalletView();
                        refresh.stopRefresh(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void toggleWalletDataLoad() {
        // inverse state
        switch (mLoadState) {
            case unloaded:
            case loadedTokenData: {
                loadWalletsData();
            } break;
            case loadedWalletData: {
                loadTokensData();
            } break;
        }
    }

    private void loadWalletsData () {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                walletDataList = ((SyncRequester) getActivity()).onSyncRequestWalletListData();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        mLoadState = LoadState.loadedWalletData;
                        updateWalletView();
                        // TODO: hardcoding
                        actionBar.setTitle("지갑");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void loadTokensData () {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                walletDataList = ((SyncRequester) getActivity()).onSyncRequestTokenListData();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        mLoadState = LoadState.loadedTokenData;
                        updateWalletView();
                        // TODO: hardcoding
                        actionBar.setTitle("코인&토큰");
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }
}
