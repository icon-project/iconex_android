package foundation.icon.iconex.view.ui.mainWallet.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.EntryViewData;

public class ETHcoinWalletItem extends WalletItem {

    public ViewGroup layoutWalletItem;

    public ImageView imgSymbol;
    public TextView txtSymbol;
    public TextView txtName;
    public TextView txtAmount;
    public TextView txtExchanged;

    public ProgressBar loading0;
    public ProgressBar loading1;

    public ETHcoinWalletItem(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_eth_coin, this, false);

        layoutWalletItem = v.findViewById(R.id.wallet_item_layout);

        imgSymbol = v.findViewById(R.id.img_symbol);
        txtSymbol = v.findViewById(R.id.txt_symbol);
        txtName = v.findViewById(R.id.txt_name);
        txtAmount = v.findViewById(R.id.txt_amount);
        txtExchanged = v.findViewById(R.id.txt_exchanged);

        loading0 = v.findViewById(R.id.loading0);
        loading1 = v.findViewById(R.id.loading1);

        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void bind(EntryViewData data) {
        // It's OK, If you don't
        // imgSymbol.setImageResource(data.getDrawableSymbolresId());
        // txtSymbol.setText(data.getSymbol());
        txtName.setText(data.getName());

        txtAmount.setText(data.getTxtAmount());
        txtExchanged.setText(data.getTxtExchanged());

        txtAmount.setVisibility(data.amountLoading ? INVISIBLE : VISIBLE);
        loading0.setVisibility(data.amountLoading ? VISIBLE : GONE);
        txtExchanged.setVisibility(data.exchageLoading ? INVISIBLE : VISIBLE);
        loading1.setVisibility(data.exchageLoading ? VISIBLE : GONE);
    }

    @Override
    public void setOnClickWalletItem(OnClickListener listener) {
        layoutWalletItem.setOnClickListener(listener);
    }
}
