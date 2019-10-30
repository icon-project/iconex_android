package foundation.icon.iconex.token.manage;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.token.IrcToken;
import loopchain.icon.wallet.core.Constants;

public class IrcListAdapter extends RecyclerView.Adapter {
    private static final String TAG = IrcListAdapter.class.getSimpleName();

    private final Context mContext;
    private final String address;
    private List<IrcToken> list;
    private LayoutInflater inflater;

    private final int VIEW_ITEM = 0;
    private final int VIEW_FOOTER = 1;

    private List<String> ownTokens = new ArrayList<>();
    private List<IrcToken> checkedList = new ArrayList<>();

    public IrcListAdapter(Context context, String address, List<IrcToken> list) {
        mContext = context;
        this.address = address;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;

        makeTokenList();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            View v = inflater.inflate(R.layout.layout_irc_list_item, parent, false);
            return new ViewItemHolder(v);
        } else {
            View v = inflater.inflate(R.layout.layout_irc_list_footer, parent, false);
            return new ViewFooterHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ViewItemHolder) {
            IrcToken token = list.get(position);

            ((ViewItemHolder) holder).txtIrc.setText(list.get(position).getName());
            ((ViewItemHolder) holder).txtAddress.setText(list.get(position).getContractAddress());

            if (token.isOpened())
                ((ViewItemHolder) holder).viewAddress.setVisibility(View.VISIBLE);
            else
                ((ViewItemHolder) holder).viewAddress.setVisibility(View.GONE);

            if (ownTokens.contains(token.getContractAddress())) {
                ((ViewItemHolder) holder).checkBox.setEnabled(false);
                ((ViewItemHolder) holder).checkBox.setButtonDrawable(R.drawable.btn_check_disabled);
                ((ViewItemHolder) holder).txtIrc.setTextColor(mContext.getResources().getColor(R.color.colorText30));
            } else {
                ((ViewItemHolder) holder).checkBox.setEnabled(true);
                ((ViewItemHolder) holder).checkBox.setChecked(token.isChecked());
                ((ViewItemHolder) holder).checkBox.setButtonDrawable(R.drawable.bg_checkbox);
                ((ViewItemHolder) holder).txtIrc.setTextColor(mContext.getResources().getColor(R.color.colorText));
            }
        }
    }

    @Override
    public int getItemCount() {
        return list.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {

        if (position < list.size())
            return VIEW_ITEM;
        else
            return VIEW_FOOTER;
    }

    private void makeTokenList() {
        ownTokens = new ArrayList<>();

        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                if (wallet.getAddress().equals(address)) {
                    for (WalletEntry entry : wallet.getWalletEntries()) {
                        if (entry.getType().equals(MyConstants.TYPE_TOKEN)) {
                            ownTokens.add(entry.getContractAddress());
                        }
                    }

                    return;
                }
            }
        }
    }

    public List<IrcToken> getCheckedList() {
        List<IrcToken> checkedList = new ArrayList<>();

        for (IrcToken token : list) {
            if (token.isChecked())
                checkedList.add(token);
        }

        return checkedList;
    }

    public class ViewItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CheckBox checkBox;
        private TextView txtIrc;
        private ViewGroup btnOpen;
        private ImageView icArrow;
        private ViewGroup viewAddress;
        private TextView txtAddress;

        public ViewItemHolder(View itemView) {
            super(itemView);

            checkBox = itemView.findViewById(R.id.check_box);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    list.get(getAdapterPosition()).setChecked(isChecked);
                    if (mListener != null) {
                        mListener.onChangeCheckList();
                    }
                }
            });
            txtIrc = itemView.findViewById(R.id.txt_irc);
            btnOpen = itemView.findViewById(R.id.btn_open);
            btnOpen.setOnClickListener(this);
            icArrow = itemView.findViewById(R.id.ic_arrow);
            viewAddress = itemView.findViewById(R.id.view_address);
            txtAddress = itemView.findViewById(R.id.txt_address);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_open:
                    if (viewAddress.getVisibility() == View.GONE) {
                        icArrow.setBackgroundResource(R.drawable.ic_arrow_up);
                        viewAddress.setVisibility(View.VISIBLE);
                        list.get(getAdapterPosition()).setOpened(true);
                    } else {
                        icArrow.setBackgroundResource(R.drawable.ic_arrow_down);
                        viewAddress.setVisibility(View.GONE);
                        list.get(getAdapterPosition()).setOpened(false);
                    }
                    break;
            }
        }
    }

    public class ViewFooterHolder extends RecyclerView.ViewHolder {

        public ViewFooterHolder(View itemView) {
            super(itemView);

            itemView.findViewById(R.id.btn_enter_info).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onClick();
                }
            });
        }
    }

    private OnClickListener mListener;

    public void setOnClickListener (OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        void onClick();
        void onChangeCheckList();
    }
}
