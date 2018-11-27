package foundation.icon.iconex.token.manage;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.WalletEntry;

/**
 * Created by js on 2018. 4. 7..
 */

public class TokenListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<WalletEntry> mData;
    private LayoutInflater mInflater;

    public TokenListAdapter(Context context, List<WalletEntry> data) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_token_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WalletEntry token = mData.get(position);

        if (holder instanceof TokenListAdapter.ViewHolder) {
            ViewHolder viewHolder = (ViewHolder) holder;

            viewHolder.txtName.setText(token.getUserName());
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtName;

        public ViewHolder(View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txt_name);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onItemClick(mData.get(getAdapterPosition()));
        }
    }

    private OnTokenClickListener mListener = null;

    public void setOnItemClickListener(OnTokenClickListener listener) {
        mListener = listener;
    }

    public interface OnTokenClickListener {
        void onItemClick(WalletEntry token);
    }
}
