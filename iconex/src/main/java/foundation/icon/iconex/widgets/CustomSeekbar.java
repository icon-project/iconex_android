package foundation.icon.iconex.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;

import foundation.icon.iconex.R;

public class CustomSeekbar extends FrameLayout {

    private ImageView seekActive;
    private SeekBar seekBar;
    private int mProgress = 50;
    private int mMax = 100;
    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = null;

    public CustomSeekbar(Context context) {
        super(context);
        initView();
    }

    public CustomSeekbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeArray(getContext().obtainStyledAttributes(attrs, R.styleable.CustomSeekbar));
        initView();
    }

    public CustomSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypeArray(getContext().obtainStyledAttributes(attrs, R.styleable.CustomSeekbar, defStyleAttr, 0));
        initView();
    }

    private void setTypeArray(TypedArray typedArray) {
        if (typedArray.hasValue(R.styleable.CustomSeekbar_progress)) {
            mProgress = typedArray.getInteger(R.styleable.CustomSeekbar_progress, 0);
        }

        if (typedArray.hasValue(R.styleable.CustomSeekbar_max)) {
            mMax = typedArray.getInteger(R.styleable.CustomSeekbar_max, 100);
        }
    }

    private void initView () {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_custom_seekbar,this, true);

        seekActive = findViewById(R.id.seek_active);
        seekBar = findViewById(R.id.seekBar);
        seekBar.setMax(mMax);
        seekBar.setProgress(mProgress);
        seekActive.setVisibility(mProgress == 0 ? INVISIBLE : VISIBLE);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mProgress = progress;
                updateProgressbarView();
                seekActive.setVisibility(progress == 0 ? INVISIBLE : VISIBLE);
                if (onSeekBarChangeListener != null) onSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null) onSeekBarChangeListener.onStartTrackingTouch(seekBar);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (onSeekBarChangeListener != null) onSeekBarChangeListener.onStopTrackingTouch(seekBar);
            }
        });
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
                updateProgressbarView();
            }
        });
    }

    private void updateProgressbarView() {
        int width = getWidth();
        int progressWidth = width - (2 * dp2px(15));
        double percentPosition = (double)mProgress / (double)mMax;
        int position = (int)(progressWidth * percentPosition);
        position -= dp2px(1);

        ViewGroup.LayoutParams seekActiveLayoutParams = seekActive.getLayoutParams();
        seekActiveLayoutParams.width = position;
        seekActive.setLayoutParams(seekActiveLayoutParams);
    }

    private int dp2px (int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }

    public void setMax(int max) {
        mMax = max;
        this.seekBar.setMax(max);
    }

    public void setProgress(int progress) {
        mProgress = progress;
        this.seekBar.setProgress(progress);
    }

    public int getMax() {
        return mMax;
    }

    public int getProgress() {
        return mProgress;
    }

    public SeekBar getSeekBar() {
        return this.seekBar;
    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener listener) {
        onSeekBarChangeListener = listener;
    }
}
