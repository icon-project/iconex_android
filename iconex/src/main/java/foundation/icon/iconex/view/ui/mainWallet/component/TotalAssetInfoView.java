package foundation.icon.iconex.view.ui.mainWallet.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.TotalAssetsViewData;

public class TotalAssetInfoView extends FrameLayout {

    private ViewPager mViewPager;
    private TotalAssetsIndicator mIndicator;

    private TotalAssetsLayout mTotalAsset;
    private TotalAssetsLayout mVotedPower;
    private PagerAdapter adapter;

    private int LOOPING_TIME_INTERVAL = 5000;
    private Runnable totalAssetInfoLooper = new Runnable() {
        @Override
        public void run() {
            setIndex(1 - getIndex());
        }
    };

    public TotalAssetInfoView(@NonNull Context context) {
        super(context);
        initView();
    }

    public TotalAssetInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TotalAssetInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public boolean isVotedPowerShow = false;
    public void setVotedPowerVisible(boolean isShow) {
        isVotedPowerShow = isShow;
        adapter.notifyDataSetChanged();
        if (isShow) {
            mIndicator.setVisibility(VISIBLE);
            postDelayed(totalAssetInfoLooper, LOOPING_TIME_INTERVAL);
        } else {
            removeCallbacks(totalAssetInfoLooper);
            mViewPager.setCurrentItem(0);
            mIndicator.setIndex(0);
            mIndicator.setVisibility(INVISIBLE);
        }
    }

    private void initView () {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_total_assets_info, this, true);

        mViewPager = findViewById(R.id.viewpager);
        mIndicator = findViewById(R.id.indicator);

        // inflate Total Asset view ==================
        mTotalAsset = new TotalAssetsLayout(getContext());
        mTotalAsset.txtLabel.setText(getContext().getString(R.string.totalAssets));

        // inflate Voted Power view ==================
        mVotedPower = new TotalAssetsLayout(getContext());
        mVotedPower.txtLabel.setText(getContext().getString(R.string.totalVotedPower));
        mVotedPower.txtUint.setVisibility(GONE);
        mVotedPower.btnToggle.setVisibility(GONE);

        // set view page Adapter
        adapter = new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                switch (position) {
                    default:
                    case 0: {
                        container.addView(mTotalAsset);
                        return mTotalAsset;
                    }
                    case 1: {
                        container.addView(mVotedPower);
                        return mVotedPower;
                    }
                }
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((TotalAssetsLayout) object);
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return (view ==  object);
            }

            @Override
            public int getCount() { return isVotedPowerShow ? 2 : 1; }
        };
        mViewPager.setAdapter(adapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mIndicator.setIndex(position);
                removeCallbacks(totalAssetInfoLooper);
                postDelayed(totalAssetInfoLooper, LOOPING_TIME_INTERVAL);
            }
        });
        setVotedPowerVisible(false);
    }

    public void setIndex(int idx) {
        mViewPager.setCurrentItem(idx, true);
    }

    public int getIndex() { // maybe will return only 0 or 1
        return mIndicator.getIndex();
    }

    public void bind(TotalAssetsViewData data) {
        setVotedPowerVisible(data.existVotingPower);
        mTotalAsset.txtUint.setText(data.getTxtExchangeUnit());
        mTotalAsset.txtAsset.setText(data.getTxtTotalAsset());
        mVotedPower.txtAsset.setText(data.getTxtVotedPower());

        mTotalAsset.txtAsset.setVisibility(data.loadingTotalAssets ? INVISIBLE : VISIBLE);
        mTotalAsset.loading.setVisibility(data.loadingTotalAssets ? VISIBLE : GONE);

        mVotedPower.txtAsset.setVisibility(data.loadingVotedpower ? INVISIBLE : VISIBLE);
        mVotedPower.loading.setVisibility(data.loadingVotedpower ? VISIBLE : GONE);
    }

    public void setOnClickExchangeUnitButton(View.OnClickListener listener) {
        mTotalAsset.setOnClickExchangeUnitButton(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onClick(view);
                }

                if (isVotedPowerShow) {
                    removeCallbacks(totalAssetInfoLooper);
                    postDelayed(totalAssetInfoLooper, LOOPING_TIME_INTERVAL);
                }
            }
        });
    }

    public void setOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        mViewPager.addOnPageChangeListener(listener);
    }
}
