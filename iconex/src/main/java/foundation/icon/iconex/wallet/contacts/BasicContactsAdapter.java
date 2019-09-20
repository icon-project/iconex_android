package foundation.icon.iconex.wallet.contacts;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.util.ConvertUtil;

/**
 * Created by js on 2018. 3. 19..
 */

public class BasicContactsAdapter extends RecyclerView.Adapter<BasicContactsAdapter.ViewHolder> {

    private static final String TAG = BasicContactsAdapter.class.getSimpleName();

    public static final String TYPE_RECENT = "RECENT";
    public static final String TYPE_WALLET = "WALLET";

    private Context mContext;
    private LayoutInflater mInflater;
    private List<?> mData;
    private String mAddress;
    private String mCoinType;
    private String mType;

    public BasicContactsAdapter(Context context, String address, List<?> data, String coinType, String type) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
        mCoinType = coinType;
        mAddress = address;
        mType = type;
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

    private String getDate(String timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = new Date(Long.parseLong(timestamp) / 1000);

        return sdf.format(date);
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onClick(String address);
    }
}
