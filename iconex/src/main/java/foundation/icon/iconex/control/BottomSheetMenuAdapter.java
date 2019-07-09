package foundation.icon.iconex.control;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 3. 14..
 */

public class BottomSheetMenuAdapter extends RecyclerView.Adapter<BottomSheetMenuAdapter.ViewHolder> {

    private static final String TAG = BottomSheetMenuAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<BottomSheetMenu> mData;

    public BottomSheetMenuAdapter(Context context, List<BottomSheetMenu> list) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_bottom_sheet_menu, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BottomSheetMenu menu = mData.get(position);

        holder.imgIcon.setBackgroundResource(menu.getResource());
        holder.txtName.setText(menu.getName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imgIcon;
        TextView txtName;

        public ViewHolder(View itemView) {
            super(itemView);

            imgIcon = itemView.findViewById(R.id.ic_menu);
            txtName = itemView.findViewById(R.id.txt_menu);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onClick(mData.get(getAdapterPosition()).getTag());
        }
    }

    private MenuClickListener mListener = null;

    public void setMenuClickListener(MenuClickListener listener) {
        mListener = listener;
    }

    public interface MenuClickListener {
        void onClick(String tag);
    }
}
