package foundation.icon.iconex.menu.bundle;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import foundation.icon.iconex.R;

public class BundleStepView extends FrameLayout {

    private ImageView imgStep01, imgStep02;
    private TextView txtStep01, txtStep02;
    private View lineStep01;

    public BundleStepView(@NonNull Context context) {
        super(context);
        initView();
    }

    public BundleStepView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BundleStepView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_bundle_step, this, true);
        imgStep01 = findViewById(R.id.img_step_01);
        imgStep02 = findViewById(R.id.img_step_02);
        txtStep01 = findViewById(R.id.txt_step_01);
        txtStep02 = findViewById(R.id.txt_step_02);
        lineStep01 = findViewById(R.id.line_step_01);
    }

    public void setStep(int step) {
        switch (step) {
            case 0: {
                imgStep01.setImageResource(R.drawable.ic_step_01_on);
                int colorE6 = ContextCompat.getColor(getContext(), R.color.darkE6);
                lineStep01.setBackground(new ColorDrawable(colorE6));
                imgStep02.setImageResource(R.drawable.ic_step_02_off);
                int color4D = ContextCompat.getColor(getContext(), R.color.dark4D);
                txtStep02.setTextColor(color4D);
            } break;
            case 1: {
                imgStep01.setImageResource(R.drawable.ic_step_check);
                int color = ContextCompat.getColor(getContext(), R.color.primary);
                lineStep01.setBackground(new ColorDrawable(color));
                imgStep02.setImageResource(R.drawable.ic_step_02_on);
                txtStep02.setTextColor(color);
            } break;
        }
    }
}
