package foundation.icon.iconex.dev_mainWallet;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import foundation.icon.iconex.R;

import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;

public class MainWalletActivity extends AppCompatActivity implements WalletCardView.OnChangeIsScrollTopListener {

    private CustomActionBar actionBar;
    private TotalAssetInfoView totalAssetInfoView;
    private RefreshLayout refresh;
    private ExpanableViewPager walletViewPager;
    private WalletIndicator walletIndicator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_wallet_frgment);

        totalAssetInfoView = findViewById(R.id.info_total_asset);

        // refresh layout
        refresh = findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh.stopRefresh(true);
            }

            @Override
            public void onLoadMore() {

            }
        });
        refresh.setRefreshEnable(true);
        refresh.addHeader(new RefreshLoadingView(this) {
            @Override
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



        // wallet view pager
        walletViewPager = findViewById(R.id.wallet_viewpager);
        walletViewPager.setClipToPadding(false);
        walletViewPager.setPadding(dp2px(10), 0, dp2px(10), 0);
        walletViewPager.setPageMargin(dp2px(10));
        walletViewPager.setOffscreenPageLimit(5);
        walletViewPager.setAdapter(new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                WalletCardView walletCardView = new WalletCardView(container.getContext());
                walletCardView.setOnChagneIsScrollTopListener(new WalletCardView.OnChangeIsScrollTopListener() {
                    @Override
                    public void onChangeIsScrollTop(boolean isScrollTop) {
                        updateCollapsable();
                    }
                });
                container.addView(walletCardView);
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
                return 5;
            }
        });
        walletViewPager.setOnStateChangeListener(new ExpanableViewPager.OnStateChangeListener() {
            @Override
            public void onChangeState(ExpanableViewPager.State state) {
                Log.d("hello", "state=" + state);
            }
        });

        // set wallet page indicatore
        walletIndicator = findViewById(R.id.wallet_indicator);
        walletViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                updateCollapsable();
                walletIndicator.setIndex(position);
            }
        });

        View content = findViewById(android.R.id.content);
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

    @Override // WalletCardView's event listener
    public void onChangeIsScrollTop(boolean isScrollTop) {
        updateCollapsable();
    }

    private void updateCollapsable() {
        int position = walletViewPager.getCurrentItem();
        WalletCardView walletCardView = ((WalletCardView) walletViewPager.getChildAt(position));
        boolean collapsable = walletCardView.getIsScrollTop();
        walletViewPager.setIsCollapsable(collapsable);
    }

    private int dp2px (int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
