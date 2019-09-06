package foundation.icon.iconex.dev_mainWallet.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;

public class WalletWalletItem extends FrameLayout implements WalletItem{

    public TextView txtSymbol;
    public TextView txtName;
    public TextView txtAmount;
    public TextView txtExchanged;

    public WalletWalletItem(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_wallet, this, false);

        txtSymbol = v.findViewById(R.id.txt_symbol);
        txtName = v.findViewById(R.id.txt_name);
        txtAmount = v.findViewById(R.id.txt_amount);
        txtExchanged = v.findViewById(R.id.txt_exchanged);

        addView(v);
    }

    @Override
    public void bind(WalletItemViewData data) {
        txtSymbol.setText(data.getSymbol());
        txtName.setText(data.getName());
        txtAmount.setText(data.getTxtAmount());
        txtExchanged.setText(data.getTxtExchanged());
    }
}
