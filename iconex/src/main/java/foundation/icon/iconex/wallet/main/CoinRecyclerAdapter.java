package foundation.icon.iconex.wallet.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

import static foundation.icon.MyConstants.EXCHANGE_USD;
import static foundation.icon.MyConstants.NO_BALANCE;

/**
 * Created by js on 2018. 4. 21..
 */

public class CoinRecyclerAdapter extends RecyclerView.Adapter<CoinRecyclerAdapter.ViewHolder> {

    private static final String TAG = CoinRecyclerAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private CoinsViewItem mItem;
    private String mType;
    private List<Wallet> mData;
    private int mDec;
    private String mSymbol;
    private final String ercIcxAddr;

    public CoinRecyclerAdapter(Context context, CoinsViewItem item) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mItem = item;
        mType = item.getType();
        mData = item.getWallets();
        mSymbol = item.getSymbol();
        mDec = item.getDec();

        if (ICONexApp.network == MyConstants.NETWORK_MAIN)
            ercIcxAddr = MyConstants.M_ERC_ICX_ADDR;
        else
            ercIcxAddr = MyConstants.T_ERC_ICX_ADDR;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_wallet_contents, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wallet wallet = mData.get(position);
        String totalBalance = getTotalBalance(wallet);

        if (totalBalance != null) {
            holder.loadingBalance.setVisibility(View.GONE);
            if (totalBalance.equals(NO_BALANCE)) {
                holder.txtBalance.setText(NO_BALANCE);
                holder.txtTransBalance.setText(NO_BALANCE);
            } else {
                String value = ConvertUtil.getValue(new BigInteger(totalBalance), mDec);
                Double doubTotal = Double.parseDouble(value);
                holder.txtBalance.setText(String.format(Locale.getDefault(), "%s", Utils.formatFloating(value, 4)));

                String exchange = mSymbol.toLowerCase() + ((MainActivity) mContext).getExchangeUnit().toLowerCase();
                String strPrice;
                if (exchange.equals("etheth"))
                    strPrice = "1";
                else
                    strPrice = ICONexApp.EXCHANGE_TABLE.get(exchange);
                if (strPrice != null) {
                    if (strPrice.equals(MyConstants.NO_EXCHANGE)) {
                        holder.txtTransBalance.setText(MyConstants.NO_BALANCE);
                    } else {
                        Double price = Double.parseDouble(strPrice);

                        if (((MainActivity) mContext).getExchangeUnit().equals(EXCHANGE_USD)) {
                            holder.txtTransBalance.setText((String.format(Locale.getDefault(), "%,.2f", doubTotal * price)));
                        } else {
                            holder.txtTransBalance.setText((String.format(Locale.getDefault(), "%,.4f", doubTotal * price)));
                        }
                    }
                }
            }
        }

        holder.txtName.setText(wallet.getAlias());
        holder.txtUnit.setText(mSymbol);
        holder.txtTransUnit.setText(((MainActivity) mContext).getExchangeUnit());

        holder.txtLoadingUnit.setText(mSymbol);
        holder.txtLoadingTrUnit.setText(((MainActivity) mContext).getExchangeUnit());

        if (position == getItemCount() - 1)
            holder.line.setVisibility(View.INVISIBLE);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private String getTotalBalance(Wallet info) {
        BigInteger totalBalance = BigInteger.ZERO;
        int cntNoBalance = 0;
        int cntTarget = 0;
        boolean isDone = true;

        for (WalletEntry entry : info.getWalletEntries()) {
            if (entry.getType().equals(mType)
                    && entry.getSymbol().equals(mSymbol)) {
                String balance = entry.getBalance();
                if (!balance.isEmpty()) {
                    if (!balance.equals(NO_BALANCE)) {
                        BigInteger bBalance = new BigInteger(balance);
                        totalBalance = totalBalance.add(bBalance);
                    } else {
                        cntNoBalance++;
                    }

                    isDone = isDone && true;
                } else {
                    isDone = isDone && false;
                }

                cntTarget++;
            }
        }

        if (isDone) {
            if (cntNoBalance == cntTarget)
                return NO_BALANCE;
            else
                return totalBalance.toString();
        } else {
            return null;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtName;
        TextView txtBalance, txtUnit;
        TextView txtTransBalance, txtTransUnit;
        Button btnSwap;
        View line;
        ViewGroup loadingBalance;
        TextView txtLoadingUnit, txtLoadingTrUnit;

        ViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_name);
            txtBalance = itemView.findViewById(R.id.txt_total_balance);
            txtUnit = itemView.findViewById(R.id.txt_unit);
            txtTransBalance = itemView.findViewById(R.id.txt_trans_balance);
            txtTransUnit = itemView.findViewById(R.id.txt_trans_unit);

            line = itemView.findViewById(R.id.line_view);

            loadingBalance = itemView.findViewById(R.id.loading_balance);
            itemView.setOnClickListener(this);
            txtLoadingUnit = itemView.findViewById(R.id.txt_loading_unit);
            txtLoadingTrUnit = itemView.findViewById(R.id.txt_loading_trans_unit);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) {
                mClickListener.onWalletClick(mData.get(getAdapterPosition()), mSymbol);
            }
        }
    }

    private WalletClickListener mClickListener;

    public void setClickListener(WalletClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface WalletClickListener {
        void onWalletClick(Wallet wallet, String symbol);

        void onRequestSwap(Wallet wallet);
    }
}
