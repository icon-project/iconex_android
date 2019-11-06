package foundation.icon.iconex.wallet.contacts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

/**
 * Created by js on 2018. 3. 19..
 */

public class BasicContactsAdapter extends RecyclerView.Adapter<BasicContactsAdapter.ViewHolder> {

    private static final String TAG = BasicContactsAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<?> mData;
    private String mAddress;
    private String mCoinType;

    public BasicContactsAdapter(Context context, String address, List<?> data, String coinType) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
        mCoinType = coinType;
        try {
            ArrayList<Wallet> wallets = new ArrayList<>();
            for (int i = 0; data.size() > i; i++) {
                Wallet wallet = ((Wallet) data.get(i));
                wallets.add(wallet);
            }

            Collections.sort(wallets, new Comparator<Wallet>() {
                @Override
                public int compare(Wallet o1, Wallet o2) {
                    String s1 = o1.getAlias();
                    String s2 = o2.getAlias();

                    int compare = getPriority(s1).compareTo(getPriority(s2));
                    if (compare == 0) compare = s1.compareTo(s2);

                    return compare;
                }

                private Integer getPriority(String s) {
                    char c = s.charAt(0);
                    if ('0' <= c && c <= '9') {
                        return 2;
                    } else if ('A' <= c && c <= 'z') {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });

            mData = wallets;
        } catch (Exception e) { }
        mAddress = address;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_basic_contacts, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mData.get(position) instanceof RecentSendInfo) {
            RecentSendInfo data = (RecentSendInfo) mData.get(position);
            if (data.getAddress().equals(mAddress)) {
                return;
            }

            if (data.getName().isEmpty())
                holder.txtName.setText(mContext.getString(R.string.noName));
            else
                holder.txtName.setText(data.getName());

            holder.txtAddr.setText(data.getAddress());
            holder.txtAmount.setText(String.format(mContext.getString(R.string.txWithdraw), data.getAmount()));
            holder.txtSymbol.setText(data.getSymbol());
        } else {
            Wallet data = (Wallet) mData.get(position);
            if (data.getAddress().equals(mAddress)) {
                return;
            }
            holder.txtName.setText(data.getAlias());
            holder.txtAddr.setText(data.getAddress());
            holder.txtAmount.setTextColor(mContext.getResources().getColor(R.color.colorText));
            holder.txtAmount.setText(getAsset(data));
            holder.txtSymbol.setTextColor(mContext.getResources().getColor(R.color.colorText));
            holder.txtSymbol.setText(mCoinType);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtName, txtAddr, txtAmount, txtSymbol;

        public ViewHolder(View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txt_name);
            txtAddr = itemView.findViewById(R.id.txt_address);
            txtAmount = itemView.findViewById(R.id.txt_amount);
            txtSymbol = itemView.findViewById(R.id.txt_symbol);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                if (mData.get(getAdapterPosition()) instanceof RecentSendInfo) {
                    mListener.onClick(((RecentSendInfo) mData.get(getAdapterPosition())).getAddress());
                } else {
                    mListener.onClick(((Wallet) mData.get(getAdapterPosition())).getAddress());
                }
            }

        }
    }

    private String getAsset(Wallet wallet) {
        BigInteger asset = new BigInteger("0");
        for (WalletEntry entry : wallet.getWalletEntries()) {
            if (entry.getSymbol().equals(mCoinType)) {
                try {
                    BigInteger balance = new BigInteger(entry.getBalance());
                    asset = asset.add(balance);
                } catch (Exception e) {
                    // Do nothing.
                }
            }
        }

        return ConvertUtil.getValue(asset, 18);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(String address);
    }
}
