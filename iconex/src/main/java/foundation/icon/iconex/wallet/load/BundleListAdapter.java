package foundation.icon.iconex.wallet.load;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.util.List;
import java.util.Locale;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;

/**
 * Created by js on 2018. 5. 5..
 */

public class BundleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = BundleListAdapter.class.getSimpleName();

    private Context mContext;
    private LayoutInflater mInflater;
    private List<BundleItem> mData;

    private final int TYPE_HEADER = 1;
    private final int TYPE_ITEM = 2;

    public BundleListAdapter(Context context, List<BundleItem> data) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mData = data;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = mInflater.inflate(R.layout.layout_load_bundle_header, parent, false);
            return new HeaderHolder(v);
        } else {
            View v = mInflater.inflate(R.layout.layout_bundle_list_item, parent, false);
            return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            HeaderHolder headerHolder = (HeaderHolder) holder;
            setWalletCount(headerHolder.txtCount);
        } else {
            ItemHolder itemHolder = (ItemHolder) holder;
            BundleItem item = mData.get(position - 1);
            itemHolder.txtAlias.setText(item.getAlias());

            if (!item.getBalance().isEmpty()) {
                itemHolder.layoutLoading.setVisibility(View.GONE);
                if (item.getBalance().equals(MyConstants.NO_BALANCE)) {
                    itemHolder.txtBalance.setText(MyConstants.NO_BALANCE);
                } else {
                    try {
                        String value = ConvertUtil.getValue(new BigInteger(item.getBalance()), 18);
                        Double doubValue = Double.parseDouble(value);
                        itemHolder.txtBalance.setText(String.format(Locale.getDefault(), "%,.4f", doubValue));
                    } catch (Exception e) {
                        // Do nothing.
                    }
                }
            }

            itemHolder.txtSymbol.setText(item.getSymbol());

            if (item.isRegistered()) {
                itemHolder.txtAlias.setTextColor(mContext.getResources().getColor(R.color.colorText30));
                itemHolder.txtBalance.setTextColor(mContext.getResources().getColor(R.color.colorText30));
                itemHolder.txtSymbol.setTextColor(mContext.getResources().getColor(R.color.colorText30));
                itemHolder.txtRegistered.setVisibility(View.VISIBLE);
            } else {
                itemHolder.txtAlias.setTextColor(mContext.getResources().getColor(R.color.colorText));
                itemHolder.txtBalance.setTextColor(mContext.getResources().getColor(R.color.colorText));
                itemHolder.txtSymbol.setTextColor(mContext.getResources().getColor(R.color.colorText));
                itemHolder.txtRegistered.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return mData.size() + 1;
    }

    public class HeaderHolder extends RecyclerView.ViewHolder {
        TextView txtCount;

        public HeaderHolder(View itemView) {
            super(itemView);

            txtCount = itemView.findViewById(R.id.txt_wallet_count);
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        TextView txtAlias;
        TextView txtRegistered;
        TextView txtBalance;
        TextView txtSymbol;
        ViewGroup layoutLoading;

        public ItemHolder(View itemView) {
            super(itemView);

            txtAlias = itemView.findViewById(R.id.txt_alias);
            txtRegistered = itemView.findViewById(R.id.txt_registered);
            txtBalance = itemView.findViewById(R.id.txt_balance);
            txtSymbol = itemView.findViewById(R.id.txt_symbol);
            layoutLoading = itemView.findViewById(R.id.layout_loading);
        }
    }

    private String getColoredSpanned(String text, String color) {
        String input = "<font color=" + color + ">" + text + "</font>";
        return input;
    }

    private void setWalletCount(TextView view) {
        int total = mData.size();
        int registered = 0;

        for (BundleItem item : mData) {
            if (item.isRegistered())
                registered++;
        }

        String strTotal = getColoredSpanned(mContext.getString(R.string.loadBundleTotal, total), "#262626");
        String strRegisterd = getColoredSpanned(mContext.getString(R.string.loadBundleRegistered, registered), "#1aaaba");

        if (Locale.getDefault().getLanguage().equals(MyConstants.LOCALE_KO))
            view.setText(Html.fromHtml(strTotal + " " + strRegisterd));
        else
            view.setText(Html.fromHtml(strRegisterd + " " + strTotal));

    }

    public void setData(List<BundleItem> data) {
        mData = data;
    }
}
