package foundation.icon.iconex.dev_mainWallet;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;

public class ExpanableViewPager extends ViewPager {

    private String TAG = ExpanableViewPager.class.getSimpleName();

    public enum State { Expaned, Collapsed, Dragging, Expanding, Collapsing }
    public interface OnStateChangeListener { void onChangeState(State state); }

    private static final int ANIMATION_DURATION = 300;

    private int mExpandedHeight = 750;
    private int mCollapseHeight = 450;
    private boolean mIsExpanable = true;
    private boolean mIsCollapsable = true;
    private State mState = State.Collapsed;
    private OnStateChangeListener changeListener = null;

    private GestureDetectorCompat dragStartDetector;
    int mStartRawY = 0;
    int mStartHeight = 0;


    public ExpanableViewPager(@NonNull Context context) {
        super(context);
        initView();
    }

    public ExpanableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView () {
        dragStartDetector = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (Math.abs(distanceY) < Math.abs(distanceX)) return false;

                if (
                    (distanceY > 0.5f && mState == State.Collapsed && mIsExpanable)
                    ||
                    (distanceY < -0.5f && mState == State.Expaned && mIsCollapsable)
                ) {
                    ViewParent parent = getParent();
                    if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
                    updateState(State.Dragging);
                    mStartRawY = ((int) e1.getRawY());
                    mStartHeight = getHeight();
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (mState != State.Dragging) {
            return dragStartDetector.onTouchEvent(ev) || super.onInterceptTouchEvent(ev);
        }

        // dragging.
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mState == State.Dragging) {
            switch (action) {
                case MotionEvent.ACTION_UP: {
                    ViewParent parent = getParent();
                    if (parent != null) parent.requestDisallowInterceptTouchEvent(false);
                    int deltaY = ((int) (ev.getRawY() - mStartRawY));
                    int currentHeight = mStartHeight - deltaY;
                    boolean isExpan = (currentHeight - mCollapseHeight) > (mExpandedHeight - mCollapseHeight) / 2;
                    updateHeightSmooth(isExpan ? State.Expaned : State.Collapsed);
                    return true;
                }
                case MotionEvent.ACTION_MOVE: {
                    int deltaY = ((int) (ev.getRawY() - mStartRawY));
                    setHeight(mStartHeight - deltaY);
                    return true;
                }
            }
        }

        return super.onTouchEvent(ev);
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
        if (changeListener != null && mState != state)
            changeListener.onChangeState(state);
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

    private void setHeight(int height) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = height;
        setLayoutParams(layoutParams);
    }

    private void updateHeightSmooth (State state) {
        int toHeight;
        switch (state) {
            case Expaned:
                toHeight = mExpandedHeight;
                updateState(State.Expanding);
                break;
            case Collapsed:
                toHeight = mCollapseHeight;
                updateState(State.Collapsing);
                break;
            default: return;
        }

        ValueAnimator animator = ValueAnimator.ofInt(getHeight(), toHeight);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = ((Integer) animation.getAnimatedValue());
                setHeight(height);

                if (height == toHeight) {
                    if (mState == State.Collapsing) updateState(State.Collapsed);
                    if (mState == State.Expanding) updateState(State.Expaned);
                }
            }
        });
        animator.setDuration(ANIMATION_DURATION);
        animator.start();
    }
}
