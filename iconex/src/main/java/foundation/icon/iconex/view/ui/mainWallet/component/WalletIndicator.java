package foundation.icon.iconex.view.ui.mainWallet.component;

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

public class WalletIndicator extends LinearLayout {

    private List<ImageView> mLstImg = new ArrayList<>();
    private int mSize = 1;
    private int mIndex = 0;

    public WalletIndicator(Context context) {
        super(context);
        initView();
    }

    public WalletIndicator(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setTypeArray(getContext().obtainStyledAttributes(attrs, R.styleable.WalletIndicator));
        initView();
    }

    public WalletIndicator(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeArray(getContext().obtainStyledAttributes(attrs,
                R.styleable.WalletIndicator, defStyleAttr, 0
        ));
        initView();
    }

    private void setTypeArray(TypedArray typedArray) {
        mSize = typedArray.hasValue(R.styleable.WalletIndicator_size) ?
                typedArray.getInteger(R.styleable.WalletIndicator_size, 5)
                :
                5;
        mIndex = typedArray.hasValue(R.styleable.WalletIndicator_index) ?
                typedArray.getInteger(R.styleable.WalletIndicator_index, 0)
                :
                0;
    }

    private void initView () {
        setOrientation(HORIZONTAL);
        setSize(5);
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

            img.setImageResource(R.drawable.wallet_indicator);

            mLstImg.add(img);
            addView(img, layoutParams);
        }

        setIndex(mIndex);
    }

    public int getIndex() {
        return mIndex;
    }

    public void setIndex(int index) {
        for (int i = 0; mSize > i; i++ ) {
            ImageView img = mLstImg.get(i);
            img.setAlpha(index == i ? 0.7f : 0.2f);
        }

        mIndex = index;
    }

    private int dp2px (int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }
}
