package foundation.icon.iconex.dev_items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.util.zip.Inflater;

import foundation.icon.iconex.R;

public class TokenWalletItem extends FrameLayout {
    public TokenWalletItem(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_token, this, false);
        addView(v);
    }
}
