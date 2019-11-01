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

public class ICXcoinWalletItem extends WalletItem{

    public ViewGroup layoutWalletItem;

    public ImageView imgSymbol;
    public TextView txtSymbol;
    public TextView txtName;
    public TextView txtAmount;
    public TextView txtExchanged;
    public TextView labelStaked;
    public TextView txtStaked;
    public TextView labelIScore;
    public TextView txtIScore;

    public ProgressBar loading0;
    public ProgressBar loading1;
    public ProgressBar loading2;
    public ProgressBar loading3;

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
        labelIScore = v.findViewById(R.id.lb_iscore);
        txtIScore = v.findViewById(R.id.txt_iscore);

        loading0 = v.findViewById(R.id.loading0);
        loading1 = v.findViewById(R.id.loading1);
        loading2 = v.findViewById(R.id.loading2);
        loading3 = v.findViewById(R.id.loading3);

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

        txtStaked.setText(data.getTxtStacked());
        txtIScore.setText(data.getTxtIScore());

        txtAmount.setVisibility(data.amountLoading ? INVISIBLE : VISIBLE);
        loading0.setVisibility(data.amountLoading ? VISIBLE : GONE);
        txtExchanged.setVisibility(data.exchageLoading ? INVISIBLE : VISIBLE);
        loading1.setVisibility(data.exchageLoading ? VISIBLE : GONE);

        txtStaked.setVisibility(data.prepsLoading ? INVISIBLE : VISIBLE);
        loading2.setVisibility(data.prepsLoading ? VISIBLE : GONE);
        txtIScore.setVisibility(data.iscoreLoading ? INVISIBLE : VISIBLE);
        loading3.setVisibility(data.iscoreLoading ? VISIBLE : GONE);
    }

    public void setTextStaked(String textStaked) {
        txtStaked.setText(textStaked);
    }

    public void setTextIScore(String textIScore) {
        txtIScore.setText(textIScore);
    }

    @Override
    public void setOnClickWalletItem(OnClickListener listener) {
        layoutWalletItem.setOnClickListener(listener);
    }
}
