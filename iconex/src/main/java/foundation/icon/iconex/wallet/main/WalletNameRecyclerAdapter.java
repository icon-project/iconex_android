package foundation.icon.iconex.wallet.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 3. 6..
 */

public class WalletNameRecyclerAdapter extends RecyclerView.Adapter<WalletNameRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private int selectedPosition = 0;

    // data is passed into the constructor
    WalletNameRecyclerAdapter(Context context, List<String> data) {
        mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_recycler_wallet_name, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String name = mData.get(position);
        holder.myTextView.setText(name);

        if (selectedPosition == position) {
            holder.myTextView.setSelected(true);
        } else {
            holder.myTextView.setSelected(false);
        }

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.myTextView.getLayoutParams();
        if (position == 0) {
            layoutParams.setMargins((int) mContext.getResources().getDimension(R.dimen.dp16), 0, 0, 0);
        } else if (position == mData.size() - 1) {
            layoutParams.setMargins(0, 0, (int) mContext.getResources().getDimension(R.dimen.dp16), 0);
        } else {
            layoutParams.setMargins((int) mContext.getResources().getDimension(R.dimen.dp6), 0, 0, 0);
        }
        holder.myTextView.setLayoutParams(layoutParams);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.txt_name);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }

    public void setNameList(List<String> data) {
        mData = data;
        notifyDataSetChanged();
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
