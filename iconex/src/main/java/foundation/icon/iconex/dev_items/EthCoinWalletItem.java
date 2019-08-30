package foundation.icon.iconex.dev_items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;

public class EthCoinWalletItem extends FrameLayout {
    public EthCoinWalletItem(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_eth_coin, this, false);
        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
}
