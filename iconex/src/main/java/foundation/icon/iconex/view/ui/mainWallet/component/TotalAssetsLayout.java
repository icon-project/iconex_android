package foundation.icon.iconex.view.ui.mainWallet.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;

public class TotalAssetsLayout extends FrameLayout implements View.OnClickListener {

    public View v;
    public TextView txtLabel;
    public TextView txtUint;
    public ImageView btnToggle;
    public TextView txtAsset;

    public View.OnClickListener mOnClickExchangeUnitButtonListener = null;

    public TotalAssetsLayout(@NonNull Context context) {
        super(context);
        v = LayoutInflater.from(context)
                .inflate(R.layout.layout_total_asset, this, false);

        txtLabel = v.findViewById(R.id.lb_total_assets);
        txtUint = v.findViewById(R.id.txt_unit);
        btnToggle = v.findViewById(R.id.btn_toggle);
        txtAsset = v.findViewById(R.id.txt_total_asset);

        txtUint.setOnClickListener(this);
        btnToggle.setOnClickListener(this);

        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setOnClickExchangeUnitButton(View.OnClickListener listener) {
        mOnClickExchangeUnitButtonListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_unit:
            case R.id.btn_toggle: {
                if (mOnClickExchangeUnitButtonListener != null) {
                    mOnClickExchangeUnitButtonListener.onClick(v);
                }
            } break;
        }
    }
}
