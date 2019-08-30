package foundation.icon.iconex.dev_items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;

public class IcxCoinWalletItem extends FrameLayout {
    public IcxCoinWalletItem(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_icx_coin, this, false);
        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
}
