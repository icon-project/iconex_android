package foundation.icon.iconex.dev_mainWallet;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;

public class ExpanableViewPager extends ViewPager {

    private String TAG = ExpanableViewPager.class.getSimpleName();

    public enum State { Expaned, Collapsed }
    public interface OnStateChangeListener { void onChangeState(State state); }

    private GestureDetectorCompat gestureDetector;
    private int mExpandedHeight = 750;
    private int mCollapseHeight = 450;
    private boolean mIsExpanable = true;
    private boolean mIsCollapsable = true;
    private State mState = State.Collapsed;
    private OnStateChangeListener changeListener = null;

    public ExpanableViewPager(@NonNull Context context) {
        super(context);
        initView();
    }

    public ExpanableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView () {
        gestureDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceY) < Math.abs(distanceX)) return false;

                Log.d(TAG, "distanceY=" + distanceY);
                if (distanceY > 0.5f && mState == State.Collapsed && mIsExpanable) {
                    updateState(State.Expaned);
                    return true;
                }

                if (distanceY < -0.5f && mState == State.Expaned && mIsCollapsable) { // scrol down
                    updateState(State.Collapsed);
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return gestureDetector == null ? super.onInterceptTouchEvent(ev) :
                gestureDetector.onTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    public int getExpandedHeight () { return mExpandedHeight; }
    public int getCollapseHeight () { return mCollapseHeight; }

    public void setExpandedHeight (int expandedHeight) {
        mExpandedHeight = expandedHeight;
        updateHeight(mState);
    }

    public void setCollapseHeight (int collapseHeight) {
        mCollapseHeight = collapseHeight;
        updateHeight(mState);
    }

    public boolean getExpanable() {
        return mIsExpanable;
    }

    public boolean getCollapsable() {
        return mIsCollapsable;
    }

    public void setIsExpanable(boolean isExpanable) {
        mIsExpanable = isExpanable;
    }

    public void setIsCollapsable(boolean isCollapsable) {
        mIsCollapsable = isCollapsable;
    }

    public void setOnStateChangeListener(OnStateChangeListener listener) {
        changeListener = listener;
    }

    public OnStateChangeListener getOnStateChangeListener() {
        return changeListener;
    }

    private void updateState (State state) {
        updateHeight(state);
        if (changeListener != null && mState != state)
            changeListener.onChangeState(mState);
        mState = state;
    }

    private void updateHeight (State state) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        switch (state) {
            case Expaned:
                layoutParams.height = mExpandedHeight;
                break;
            case Collapsed:
                layoutParams.height = mCollapseHeight;
                break;
        }
        setLayoutParams(layoutParams);
    }
}
