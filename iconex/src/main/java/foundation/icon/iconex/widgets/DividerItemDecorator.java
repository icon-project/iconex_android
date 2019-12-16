package foundation.icon.iconex.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import foundation.icon.iconex.R;

public class DividerItemDecorator extends RecyclerView.ItemDecoration {
    private final Context mContext;
    private final Drawable mDivider;

    public DividerItemDecorator(Context context, Drawable divider) {
        mContext = context;
        mDivider = divider;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int inset = mContext.getResources().getDimensionPixelSize(R.dimen.divider_padding);
        int dividerLeft = inset;
        int dividerRight = parent.getWidth() - inset;

        int childCount = parent.getChildCount();
        for (int i = 1; i <= childCount - 2; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            mDivider.draw(canvas);
        }
    }
}
