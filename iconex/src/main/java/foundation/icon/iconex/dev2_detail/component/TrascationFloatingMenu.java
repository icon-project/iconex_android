package foundation.icon.iconex.dev2_detail.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;

public class TrascationFloatingMenu extends FrameLayout {
    public TrascationFloatingMenu(@NonNull Context context) {
        super(context);
        viewInit();
    }

    public TrascationFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewInit();
    }

    public TrascationFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewInit();
    }

    private void viewInit() {
        setClickable(false);

        LayoutInflater.from(getContext()).inflate(R.layout.layout_floating_menu, this, true);
    }
}
