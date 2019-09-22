package foundation.icon.iconex.view.ui.mainWallet.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletItemViewData;

public class ICXcoinWalletItem extends WalletItem{

    public ViewGroup layoutWalletItem;

    public ImageView imgSymbol;
    public TextView txtSymbol;
    public TextView txtName;
    public TextView txtAmount;
    public TextView txtExchanged;
    public TextView labelStaked;
    public TextView txtStaked;
    public TextView labelVotingPower;
    public TextView txtVotingPower;
    public TextView labelIScore;
    public TextView txtIScore;

    public ICXcoinWalletItem(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_icx_coin, this, false);

        layoutWalletItem = v.findViewById(R.id.wallet_item_layout);

        imgSymbol = v.findViewById(R.id.img_symbol);
        txtSymbol = v.findViewById(R.id.txt_symbol);
        txtName = v.findViewById(R.id.txt_name);
        txtAmount = v.findViewById(R.id.txt_amount);
        txtExchanged = v.findViewById(R.id.txt_exchanged);
        labelStaked = v.findViewById(R.id.lb_staked);
        txtStaked = v.findViewById(R.id.txt_staked);
        labelVotingPower = v.findViewById(R.id.lb_voting_power);
        txtVotingPower = v.findViewById(R.id.txt_voting_power);
        labelIScore = v.findViewById(R.id.lb_iscore);
        txtIScore = v.findViewById(R.id.txt_iscore);

        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void bind(WalletItemViewData data) {
        // It's OK, If you don't
        // imgSymbol.setImageResource(data.getDrawableSymbolresId());
        // txtSymbol.setText(data.getSymbol());
        // txtName.setText(data.getName());

        txtAmount.setText(data.getTxtAmount());
        txtExchanged.setText(data.getTxtExchanged());

        txtStaked.setText(data.getStacked());
        txtVotingPower.setText(data.getVotingPower());
        txtIScore.setText(data.getiScore());
    }

    @Override
    public void setOnClickWalletItem(OnClickListener listener) {
        layoutWalletItem.setOnClickListener(listener);
    }
}
