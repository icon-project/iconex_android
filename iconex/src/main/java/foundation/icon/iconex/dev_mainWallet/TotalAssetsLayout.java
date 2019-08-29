package foundation.icon.iconex.dev_mainWallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;

public class TotalAssetsLayout extends FrameLayout {

    public View v;
    public TextView txtLabel;
    public TextView txtUint;
    public ImageView btnToggle;
    public TextView txtAsset;

    public TotalAssetsLayout(@NonNull Context context) {
        super(context);
        v = LayoutInflater.from(context)
                .inflate(R.layout.layout_total_asset, this, false);

        txtLabel = v.findViewById(R.id.lb_total_assets);
        txtUint = v.findViewById(R.id.txt_unit);
        btnToggle = v.findViewById(R.id.btn_toggle);
        txtAsset = v.findViewById(R.id.txt_total_asset);

        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
}
