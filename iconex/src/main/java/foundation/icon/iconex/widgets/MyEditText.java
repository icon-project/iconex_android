package foundation.icon.iconex.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import foundation.icon.iconex.control.OnKeyPreImeListener;

/**
 * Created by js on 2018. 2. 26..
 */

public class MyEditText extends AppCompatEditText {

    private final Context mContext;
    private OnKeyPreImeListener mKeyPreImeListener = null;

    public MyEditText(Context context) {
        super(context);
        mContext = context;
    }

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public MyEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setOnKeyPreImeListener(OnKeyPreImeListener listener) {
        mKeyPreImeListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            if (mKeyPreImeListener != null) {
                mKeyPreImeListener.onBackPressed();

//                InputMethodManager inputMethodManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//                inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

                return false;
            } else {
                return super.onKeyPreIme(keycode, event);
            }
        } else {
            return super.onKeyPreIme(keycode, event);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchListener != null)
            mTouchListener.onTouch();

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    private OnEditTouchListener mTouchListener = null;

    public void setOnEditTouchListener(OnEditTouchListener listener) {
        mTouchListener = listener;
    }

    public interface OnEditTouchListener {
        void onTouch();
    }
}
