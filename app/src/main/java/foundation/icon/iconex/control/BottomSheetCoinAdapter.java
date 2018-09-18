package foundation.icon.iconex.control;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.WalletEntry;

/**
 * Created by js on 2018. 3. 14..
 */

public class BottomSheetCoinAdapter extends RecyclerView.Adapter<BottomSheetCoinAdapter.ViewHolder> {

    private static final String TAG = BottomSheetCoinAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<WalletEntry> mData;

    public BottomSheetCoinAdapter(Context context, List<WalletEntry> list) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = mInflater.inflate(R.layout.layout_bottom_sheet_coin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WalletEntry data = mData.get(position);

        holder.txtName.setText(data.getUserName());
        holder.txtSymbol.setText(data.getSymbol());

        if (!data.getBalance().isEmpty()) {
            try {
                String value = ConvertUtil.getValue(new BigInteger(data.getBalance()), data.getDefaultDec());
                Double doubValue = Double.parseDouble(value);
                holder.txtBalance.setText(String.format(Locale.getDefault(), "%,.4f", doubValue));
            } catch (Exception e) {
                holder.txtBalance.setText(MyConstants.NO_BALANCE);
            }
        } else {
            holder.txtBalance.setText(MyConstants.NO_BALANCE);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtName, txtSymbol, txtBalance;

        public ViewHolder(View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txt_name);
            txtSymbol = itemView.findViewById(R.id.txt_symbol);
            txtBalance = itemView.findViewById(R.id.txt_balance);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onClick(getAdapterPosition());
        }
    }

    private CoinSelectListener mListener = null;

    public void setItemClickListener(CoinSelectListener listener) {
        this.mListener = listener;
    }

    public interface CoinSelectListener {
        void onClick(int position);
    }
}
