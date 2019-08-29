package foundation.icon.iconex.dev_mainWallet;

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

public class TotalAssetInfoView extends FrameLayout {

    private ViewPager mViewPager;
    private TotalAssetsIndicator mIndicator;

    private TotalAssetsLayout mTotalAsset;
    private TotalAssetsLayout mVotedPower;

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

    private void initView () {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.layout_total_assets_info, this, true);

        mViewPager = findViewById(R.id.viewpager);
        mIndicator = findViewById(R.id.indicator);

        // set viewpage Adapter
        mViewPager.setAdapter(new PagerAdapter() {
            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                switch (position) {
                    default:
                    case 0: {
                        // inflate Total Asset view ==================
                        mTotalAsset = new TotalAssetsLayout(container.getContext());
                        container.addView(mTotalAsset);
                        return mTotalAsset;
                    }
                    case 1: {
                        // inflate Voted Power view ==================
                        mVotedPower = new TotalAssetsLayout(container.getContext());
                        mVotedPower.txtUint.setVisibility(GONE);
                        mVotedPower.btnToggle.setVisibility(GONE);
                        mVotedPower.txtAsset.setText("90.8 %");
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
            public int getCount() { return 2; }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mIndicator.setIndex(position);
            }
        });
    }


}
