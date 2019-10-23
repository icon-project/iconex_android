package foundation.icon.iconex.view.ui.mainWallet.component;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager.widget.ViewPager;

public class ExpandableViewPager extends ViewPager {

    // Declaration
    private String TAG = ExpandableViewPager.class.getSimpleName();
    public enum State { Expaned, Collapsed, Dragging, Expanding, Collapsing }
    public interface OnStateChangeListener { void onChangeState(State state); }
    private static final int ANIMATION_DURATION = 200;

    // expand & collapse state value
    private int mExpandedHeight = 750;
    private int mCollapseHeight = 450;
    private boolean mIsExpanable = true;
    private boolean mIsCollapsable = true;
    private State mState = State.Collapsed;
    private OnStateChangeListener changeListener = null;

    // gesture detect value
    private GestureDetectorCompat interceptDragStarter;
    int mStartRawY = 0;
    int mStartHeight = 0;
    private boolean mLastActionDown = false;
    private float mLastActionDownX = 0;
    private float mLastActionDownY = 0;

    // =============== init methods
    public ExpandableViewPager(@NonNull Context context) {
        super(context);
        initGesture();
    }

    public ExpandableViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initGesture();
    }

    // ============= gesture detect methods
    private void initGesture() {
        // simple intercept drag start gesture detector
        interceptDragStarter = new GestureDetectorCompat(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return checkDragStartPattern(e1, distanceX, distanceY);
            }
        });
    }

    @Override // intercept gesture
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mState != State.Dragging) {
            return interceptDragStarter.onTouchEvent(ev) || super.onInterceptTouchEvent(ev);
        }
        return true;
    }

    @Override // gesture process
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;

        if (mState == State.Dragging) {
            switch (action) {
                case MotionEvent.ACTION_CANCEL:
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
                    int height = mStartHeight - deltaY;
                    if (height < mCollapseHeight)
                        height = mCollapseHeight - ((mCollapseHeight - height) / 2);
                    setHeight(height);
                    return true;
                }
            }
        } else {
            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    mLastActionDown = true;
                    mLastActionDownX = ev.getRawX();
                    mLastActionDownY = ev.getRawY();
                } break;
                case MotionEvent.ACTION_MOVE: {
                    if (mLastActionDown) {
                        float distanceY = mLastActionDownY - ev.getRawY();
                        float distanceX = mLastActionDownX - ev.getRawX();
                        if (checkDragStartPattern(ev, distanceX, distanceY)) {
                            mLastActionDown = false;
                            return true;
                        }
                    }
                } break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    mLastActionDown = false;
                } break;
            }
        }

        return super.onTouchEvent(ev);
    }

    private boolean checkDragStartPattern(MotionEvent e, float distanceX, float distanceY) {
        if (Math.abs(distanceY) < Math.abs(distanceX)) return false;
        boolean isExpandDragStart = distanceY > 0.5f && mState == State.Collapsed && mIsExpanable;
        boolean isCollapsDragStart = distanceY < -0.5f && mState == State.Expaned && mIsCollapsable;

        if (isExpandDragStart || isCollapsDragStart) {
            ViewParent parent = getParent();
            if (parent != null) parent.requestDisallowInterceptTouchEvent(true);
            updateState(State.Dragging);
            mStartRawY = ((int) e.getRawY());
            mStartHeight = getHeight();
            return true;
        }

        return false;
    }

    // ============== get/set methods
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

    // ================== height calc method
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
        getLayoutParams().height = height;
        requestLayout();
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
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(ANIMATION_DURATION);
        animator.start();
    }
}
