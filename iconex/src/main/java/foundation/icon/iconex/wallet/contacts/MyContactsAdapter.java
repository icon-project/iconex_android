package foundation.icon.iconex.wallet.contacts;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.Contacts;

/**
 * Created by js on 2018. 3. 19..
 */

public class MyContactsAdapter extends RecyclerView.Adapter<MyContactsAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Contacts> mData;
    private boolean mEditable;

    public MyContactsAdapter(Context context, List<Contacts> data, boolean editable) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = data;
        mEditable = editable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.layout_contacts, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contacts contact = mData.get(position);

        if (mEditable) {
            holder.btnDelete.setVisibility(View.VISIBLE);
        } else {
            holder.btnDelete.setVisibility(View.GONE);
        }

        holder.txtName.setText(contact.getName());
        holder.txtAddress.setText(contact.getAddress());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        Button btnDelete;
        TextView txtName, txtAddress;

        public ViewHolder(View itemView) {
            super(itemView);

            btnDelete = itemView.findViewById(R.id.btn_delete);
            btnDelete.setOnClickListener(this);

            txtName = itemView.findViewById(R.id.txt_name);
            txtAddress = itemView.findViewById(R.id.txt_address);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_delete:
                    if (mListener != null)
                        mListener.onDelete(getAdapterPosition());
                    break;

                default:
                    if (!mEditable) {
                        if (mListener != null)
                            mListener.onSelect(mData.get(getAdapterPosition()).getAddress());
                    }
            }
        }
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
        notifyDataSetChanged();
    }

    private ContactsClickListener mListener = null;

    public void setContactsClickListener(ContactsClickListener listener) {
        mListener = listener;
    }

    public interface ContactsClickListener {
        void onDelete(int position);

        void onEdit(int position);

        void onSelect(String address);
    }
}
