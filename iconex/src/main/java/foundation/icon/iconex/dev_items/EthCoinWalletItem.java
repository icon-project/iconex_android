package foundation.icon.iconex.dev_items;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;

public class EthCoinWalletItem extends FrameLayout {

    public ImageView imgSymbol;
    public TextView txtSymbol;
    public TextView txtName;
    public TextView txtAmount;
    public TextView txtExchanged;

    public EthCoinWalletItem(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_eth_coin, this, false);

        imgSymbol = v.findViewById(R.id.img_symbol);
        txtSymbol = v.findViewById(R.id.txt_symbol);
        txtName = v.findViewById(R.id.txt_name);
        txtAmount = v.findViewById(R.id.txt_amount);
        txtExchanged = v.findViewById(R.id.txt_exchanged);

        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }
}
