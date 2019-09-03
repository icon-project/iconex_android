package foundation.icon.iconex.dev_mainWallet.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;

public class ETHcoinWalletItem extends FrameLayout implements WalletItem {

    public ImageView imgSymbol;
    public TextView txtSymbol;
    public TextView txtName;
    public TextView txtAmount;
    public TextView txtExchanged;

    public ETHcoinWalletItem(@NonNull Context context) {
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

    @Override
    public void bind(WalletItemViewData data) {
        // It's OK, If you don't
        // imgSymbol.setImageResource(data.getDrawableSymbolresId());
        // txtSymbol.setText(data.getSymbol());
        // txtName.setText(data.getName());

        txtAmount.setText(data.getAmount());
        txtExchanged.setText(data.getExchanged());
    }
}
