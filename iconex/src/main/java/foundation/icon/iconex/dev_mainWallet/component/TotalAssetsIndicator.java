package foundation.icon.iconex.dev_mainWallet.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;

public class TotalAssetsIndicator extends LinearLayout {

    private List<ImageView> mLstImg = new ArrayList<>(2);
    private int mSize = 2;
    private int mIndex = 0;

    public TotalAssetsIndicator(Context context) {
        super(context);
        initView();
    }

    public TotalAssetsIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTypArray(getContext().obtainStyledAttributes(attrs, R.styleable.TotalAssetsIndicator));
        initView();
    }

    public TotalAssetsIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypArray(getContext().obtainStyledAttributes(
                attrs, R.styleable.TotalAssetsIndicator, defStyleAttr, 0
        ));
        initView();
    }

    private void setTypArray(TypedArray typArray) {
        mSize = typArray.hasValue(R.styleable.TotalAssetsIndicator_size) ?
                typArray.getInteger(R.styleable.TotalAssetsIndicator_size, 2)
                :
                2;
        mIndex = typArray.hasValue(R.styleable.TotalAssetsIndicator_index) ?
                typArray.getInteger(R.styleable.TotalAssetsIndicator_index, 0)
                :
                0;
    }

    private void initView () {
        setOrientation(HORIZONTAL);
        setSize(mSize);
    }

    public int getSize() {
        return mSize;
    }

    public void setSize(int size) {
        mSize = size;

        removeAllViews();
        mLstImg.clear();

        int dp4 = dp2px(4);
        for (int i = 0; mSize > i; i++) {
            ImageView img = new ImageView(getContext());

            LayoutParams layoutParams = new LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            );

            if (i != mSize -1) layoutParams.setMarginEnd(dp4);

            mLstImg.add(img);
            addView(img, layoutParams);
        }

        setIndex(mIndex);
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        mIndex = index;

        for (int i = 0; mSize > i; i++) {
            ImageView img = mLstImg.get(i);
            img.setImageResource(
                    index == i ?
                            R.drawable.page_indicator_total_assets_selected
                            :
                            R.drawable.page_indicator_total_assets_unselected
            );
        }
    }

    private int dp2px (int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }
}
