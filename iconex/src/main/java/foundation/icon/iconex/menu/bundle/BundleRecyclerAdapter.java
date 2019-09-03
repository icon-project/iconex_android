package foundation.icon.iconex.menu.bundle;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;

/**
 * Created by js on 2018. 3. 23..
 */

public class BundleRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = BundleRecyclerAdapter.class.getSimpleName();

    private final int TYPE_HEADER = 101;
    private final int TYPE_ITEM = 102;

    private Context mContext;
    private LayoutInflater mInflater;
    private List<BundleItem> mData;

    public BundleRecyclerAdapter(Context context, List<BundleItem> data) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (viewType == TYPE_HEADER) {
            v = mInflater.inflate(R.layout.layout_header_make_bundle, parent, false);
            return new HeaderViewHolder(v);
        } else {
            v = mInflater.inflate(R.layout.layout_bundle_item, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {

            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.txtCount.setText(String.format(mContext.getString(R.string.countSelectedWallet), getCount()));

        } else {

            ItemViewHolder itemHolder = (ItemViewHolder) holder;
            BundleItem item = mData.get(position - 1);
            itemHolder.selected.setChecked(item.isSelected());
            itemHolder.txtAlias.setText(item.getAlias());
            try {
                itemHolder.txtBalance.setText(String.format(Locale.getDefault(), "%,.4f", Double.parseDouble(item.getBalance())));
            } catch (Exception e) {
                itemHolder.txtBalance.setText(MyConstants.NO_BALANCE);
            }

            itemHolder.txtSymbol.setText(item.getSymbol());
        }
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;
        else
            return TYPE_ITEM;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView txtCount;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            txtCount = itemView.findViewById(R.id.txt_count);
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CheckBox selected;
        TextView txtAlias, txtBalance, txtSymbol;

        public ItemViewHolder(View itemView) {
            super(itemView);

            selected = itemView.findViewById(R.id.selected);
            txtAlias = itemView.findViewById(R.id.txt_alias);
            txtBalance = itemView.findViewById(R.id.txt_balance);
            txtSymbol = itemView.findViewById(R.id.txt_symbol);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition() - 1;
                mListener.onWalletSelected(position, mData.get(position));
            }

        }
    }

    public void setSelected(int position, boolean selected) {
        mData.get(position).setSelected(selected);
        notifyDataSetChanged();
    }

    public List<Wallet> getBundle() {
        List<Wallet> bundle = new ArrayList<>();

        for (BundleItem item : mData) {
            if (item.isSelected()) {
                for (Wallet info : ICONexApp.wallets) {
                    if (info.getAlias().equals(item.getAlias())) {
                        bundle.add(info);
                        break;
                    }
                }
            }
        }

        return bundle;
    }

    private int getCount() {
        int count = 0;

        for (BundleItem item : mData) {
            if (item.isSelected())
                count++;
        }

        return count;
    }

    private OnWalletClickListener mListener = null;

    public void setOnWalletClickListener(OnWalletClickListener listener) {
        mListener = listener;
    }

    public interface OnWalletClickListener {
        void onWalletSelected(int position, BundleItem wallet);
    }
}
