package foundation.icon.iconex.wallet.menu.language;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 4. 22..
 */

public class LanguageRecyclerAdapter extends RecyclerView.Adapter<LanguageRecyclerAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<LanguageItem> mData;

    public LanguageRecyclerAdapter(Context context, List<LanguageItem> data) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_item_coin_list, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LanguageItem item = mData.get(position);

        holder.txtName.setText(item.getLanguage());
        holder.selected.setChecked(item.isSelected);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView txtName;
        RadioButton selected;

        public ViewHolder(View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txt_name);
            selected = itemView.findViewById(R.id.radio_selected);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (!mData.get(getAdapterPosition()).isSelected) {
                for (LanguageItem item : mData) {
                    item.setSelected(false);
                }
                mData.get(getAdapterPosition()).setSelected(true);

                mListener.onChanged(mData.get(getAdapterPosition()).getCode());
            }
        }
    }

    private OnLanguageChangeListener mListener = null;

    public void setLanguageChangeListener(OnLanguageChangeListener listener) {
        mListener = listener;
    }

    public interface OnLanguageChangeListener {
        void onChanged(String code);
    }
}
