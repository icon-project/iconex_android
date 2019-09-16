package foundation.icon.iconex.view;

import android.animation.LayoutTransition;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import foundation.icon.iconex.R;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class ToolTip extends Dialog implements View.OnClickListener {

    private ViewGroup container;

    private View mBotLeft;
    private View mBotRight;
    private View mTopLeft;
    private View mTopRight;

    private TextView mText;
    private ViewGroup mBtnClose;

    @Override
    public void onClick(View v) {
        dismiss();
    }

    public enum TailDirection {
        BottomLeft,
        BottomRight,
        TopLeft,
        TopRight
    }

    public ToolTip(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        setContentView(R.layout.layout_tooltip);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setGravity(Gravity.TOP | Gravity.LEFT);

        container = findViewById(R.id.container);

        mBotLeft = findViewById(R.id.bottom_left);
        mBotRight = findViewById(R.id.bottom_right);
        mTopLeft = findViewById(R.id.top_left);
        mTopRight = findViewById(R.id.top_right);

        mText = findViewById(R.id.text);
        mBtnClose = findViewById(R.id.btn_close);

        mBtnClose.setOnClickListener(this);
    }

    private void setTailDirection(TailDirection direction) {

        mBotLeft.setVisibility(GONE);
        mBotRight.setVisibility(GONE);
        mTopLeft.setVisibility(GONE);
        mTopRight.setVisibility(GONE);

        switch (direction) {
            case BottomLeft: mBotLeft.setVisibility(VISIBLE); break;
            case BottomRight: mBotRight.setVisibility(VISIBLE); break;
            case TopLeft: mTopLeft.setVisibility(VISIBLE); break;
            case TopRight: mTopRight.setVisibility(VISIBLE); break;
        }
    }

    public ToolTip setText(String text) {
        mText.setText(text);
        return this;
    }

    public ToolTip setPosition(Activity a, View target) {
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                Display display = a.getWindowManager().getDefaultDisplay();
                Point windowSize = new Point();
                display.getSize(windowSize);
                int winHalfHeight = windowSize.y / 2;
                int winHalfWidth = windowSize.x / 2;

                Rect rect = new Rect();
                target.getGlobalVisibleRect(rect);

                Rect root = new Rect();
                a.findViewById(android.R.id.content).getGlobalVisibleRect(root);

                rect.left -= root.left;
                rect.right -= root.left;
                rect.top -= root.top;
                rect.bottom -= root.top;

                int height = container.getHeight();
                int width = container.getWidth();

                WindowManager.LayoutParams layoutParams = getWindow().getAttributes();

                if (rect.top < winHalfHeight) {
                    if (rect.left < winHalfWidth) { // target top - left
                        layoutParams.y = rect.bottom;
                        layoutParams.x = rect.left;
                        setTailDirection(TailDirection.TopLeft);
                    } else { // target top - right
                        layoutParams.y = rect.bottom;
                        layoutParams.x = rect.right - width;
                        setTailDirection(TailDirection.TopRight);
                    }
                } else {
                    if (rect.left < winHalfWidth) { // target bottom - left
                        layoutParams.y = rect.top - height;
                        layoutParams.x = rect.left;
                        setTailDirection(TailDirection.BottomLeft);
                    } else { // target bottom - right
                        layoutParams.y = rect.top - height;
                        layoutParams.x = rect.right - width;
                        setTailDirection(TailDirection.BottomRight);
                    }
                }

                getWindow().setAttributes(layoutParams);
            }
        });
        return this;
    }
}
