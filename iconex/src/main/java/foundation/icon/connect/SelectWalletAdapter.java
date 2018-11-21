package foundation.icon.connect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;

public class SelectWalletAdapter extends RecyclerView.Adapter<SelectWalletAdapter.ViewHolder> {
    private static final String TAG = SelectWalletAdapter.class.getSimpleName();

    private final Context mContext;
    private LayoutInflater mInflater;
    private List<Wallet> mList;

    private int selectedWallet = -1;

    public SelectWalletAdapter(Context context, List<Wallet> list, OnWalletSelectListener listener) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mList = list;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.item_selet_wallet, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wallet wallet = mList.get(position);

        holder.txtAlias.setText(wallet.getAlias());
        holder.txtAddr.setText(wallet.getAddress());

        if (!wallet.getWalletEntries().get(0).getBalance().isEmpty()) {
            if (wallet.getWalletEntries().get(0).getBalance().equals(MyConstants.NO_BALANCE))
                holder.txtBalance.setText(MyConstants.NO_BALANCE);
            else {
                BigInteger value = new BigInteger(wallet.getWalletEntries().get(0).getBalance());
                String balance = ConvertUtil.getValue(value, wallet.getWalletEntries().get(0).getDefaultDec());
                holder.txtBalance.setText(String.format(Locale.getDefault(), "%s", Utils.formatFloating(balance, 4)));
            }
        } else {
            holder.txtBalance.setText(MyConstants.NO_BALANCE);
        }

        holder.progress.setVisibility(View.GONE);
        holder.radioSelect.setChecked(selectedWallet == position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        RadioButton radioSelect;
        TextView txtAlias, txtAddr, txtBalance;

        ProgressBar progress;

        public ViewHolder(View itemView) {
            super(itemView);

            radioSelect = itemView.findViewById(R.id.radio_select);
            radioSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedWallet = getAdapterPosition();
                    mListener.onSelect();
                    notifyDataSetChanged();
                }
            });
            txtAlias = itemView.findViewById(R.id.txt_alias);
            txtAddr = itemView.findViewById(R.id.txt_address);
            txtBalance = itemView.findViewById(R.id.txt_balance);
            progress = itemView.findViewById(R.id.progress);
        }
    }

    public void setBalance(List<Wallet> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public String getSelected() {
        if (selectedWallet >= 0) {
            Log.d(TAG, "getSelectedWallet=" + selectedWallet);
            return mList.get(selectedWallet).getAddress();
        }

        return null;
    }

    private OnWalletSelectListener mListener;

    public interface OnWalletSelectListener {
        void onSelect();
    }
}
