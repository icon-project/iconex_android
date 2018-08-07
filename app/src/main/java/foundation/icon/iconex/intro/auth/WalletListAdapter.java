package foundation.icon.iconex.intro.auth;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.WalletInfo;

/**
 * Created by js on 2018. 4. 24..
 */

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;

    public WalletListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_verification_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        WalletInfo wallet = ICONexApp.mWallets.get(position);
        holder.txtAlias.setText(wallet.getAlias());
    }

    @Override
    public int getItemCount() {
        return ICONexApp.mWallets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtAlias;

        public ViewHolder(View itemView) {
            super(itemView);

            txtAlias = itemView.findViewById(R.id.txt_alias);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onWalletClick(ICONexApp.mWallets.get(getAdapterPosition()));
        }
    }

    private OnWalletClickListener mListener = null;

    public void setOnWalletClickListener(OnWalletClickListener listener) {
        mListener = listener;
    }

    public interface OnWalletClickListener {
        void onWalletClick(WalletInfo wallet);
    }
}
