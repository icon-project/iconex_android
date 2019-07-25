package foundation.icon.iconex.control;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 3. 14..
 */

public class BottomSheetBasicAdapter extends RecyclerView.Adapter<BottomSheetBasicAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mData;

    public BottomSheetBasicAdapter(Context context, List<String> list) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_bottom_sheet_basic, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String item = mData.get(position);
        holder.txtItem.setText(item);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtItem;

        public ViewHolder(View itemView) {
            super(itemView);

            txtItem = itemView.findViewById(R.id.txt_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onClick(mData.get(getAdapterPosition()));
        }
    }

    private BasicItemClickListener mListener = null;

    public void setItemClickListener(BasicItemClickListener listener) {
        mListener = listener;
    }

    public interface BasicItemClickListener {
        void onClick(String item);
    }
}
