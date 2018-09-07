package foundation.icon.iconex.wallet.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import loopchain.icon.wallet.core.Constants;

import static foundation.icon.iconex.ICONexApp.network;
import static foundation.icon.iconex.MyConstants.EXCHANGE_USD;

/**
 * Created by js on 2018. 3. 13..
 */

public class TransactionListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = TransactionListAdapter.class.getSimpleName();

    private final int VIEW_HEADER = 1;
    private final int VIEW_ITEM = 2;
    private final int VIEW_EMPTY = 3;
    private final int VIEW_ETHER = 4;
    private final int VIEW_LOADER = 5;
    private final int VIEW_LOADING = 9;

    private boolean isLoading = false;
    private boolean moreLoading = false;

    private Context mContext;
    private List<TxItem> mData;
    private MyConstants.TxState mState;
    private MyConstants.TxType mType;
    private String mCoinType;
    private final String ercIcxAddr;

    private LayoutInflater mInflater;
    private HeaderClickListener mHeaderClickListener;
    private ItemClickListener mItemClickListener;

    private WalletEntry entry;
    private String EXCHANGE = EXCHANGE_USD;

    public TransactionListAdapter(Context context, WalletEntry entry, String exchange, List<TxItem> list,
                                  MyConstants.TxState state, MyConstants.TxType type, String coinType) {
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = list;
        mState = state;
        mType = type;
        mCoinType = coinType;

        this.entry = entry;
        this.EXCHANGE = exchange;

        if (ICONexApp.network == MyConstants.NETWORK_MAIN)
            ercIcxAddr = MyConstants.M_ERC_ICX_ADDR;
        else
            ercIcxAddr = MyConstants.T_ERC_ICX_ADDR;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == VIEW_HEADER) {
            View v = mInflater.inflate(R.layout.layout_tx_header, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == VIEW_EMPTY) {
            View v = mInflater.inflate(R.layout.layout_tx_empty, parent, false);
            return new FooterViewHolder(v);
        } else if (viewType == VIEW_ETHER) {
            View v = mInflater.inflate(R.layout.layout_tx_eth, parent, false);
            return new ETHViewHolder(v);
        } else if (viewType == VIEW_LOADER) {
            View v = mInflater.inflate(R.layout.layout_recycler_loader, parent, false);
            return new LoaderViewHolder(v);
        } else if (viewType == VIEW_LOADING) {
            View v = mInflater.inflate(R.layout.layout_loading, parent, false);
            return new LoadingViewHolder(v);
        } else {
            View v = mInflater.inflate(R.layout.layout_tx_item, parent, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) holder;
            // Coin or Token Name
            if (!entry.getBalance().equals(MyConstants.NO_BALANCE)) {
                try {
                    String value = ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec());
                    Double doubBalance = Double.parseDouble(value);
                    headerViewHolder.txtAsset.setText(String.format(Locale.getDefault(), "%,.4f", doubBalance));

                    String exchangeCode = entry.getSymbol().toLowerCase() + EXCHANGE.toLowerCase();
                    String strPrice;
                    if (exchangeCode.equals("etheth"))
                        strPrice = "1";
                    else
                        strPrice = ICONexApp.EXCHANGE_TABLE.get(exchangeCode);
                    if (strPrice != null) {
                        if (strPrice.equals(MyConstants.NO_EXCHANGE)) {
                            headerViewHolder.txtTransAsset.setText(MyConstants.NO_BALANCE);
                        } else {
                            Double price = Double.parseDouble(strPrice);
                            if (EXCHANGE.equals(EXCHANGE_USD)) {
                                headerViewHolder.txtTransAsset.setText(String.format(Locale.getDefault(), "%,.2f", doubBalance * price));
                            } else {
                                headerViewHolder.txtTransAsset.setText(String.format(Locale.getDefault(), "%,.4f", doubBalance * price));
                            }
                        }
                    }
                } catch (Exception e) {
                    headerViewHolder.txtAsset.setText(MyConstants.NO_BALANCE);
                    headerViewHolder.txtTransAsset.setText(MyConstants.NO_BALANCE);
                }
            }

            if (entry.getType().equals(MyConstants.TYPE_TOKEN)) {
                if (entry.getContractAddress().equals(ercIcxAddr))
                    ((HeaderViewHolder) holder).btnSwap.setVisibility(View.VISIBLE);
                else
                    ((HeaderViewHolder) holder).btnSwap.setVisibility(View.GONE);
            }


            ((TextView) headerViewHolder.btnExchange.findViewById(R.id.txt_exchange)).setText(EXCHANGE);
            ((TextView) headerViewHolder.btnSelectCoin.findViewById(R.id.txt_selected_name)).setText(entry.getUserName());
            headerViewHolder.txtSymbol.setText(entry.getSymbol());
            setSearchState(headerViewHolder.txtState, mState);
            setSearchType(headerViewHolder.txtType, mType);
        } else if (holder instanceof FooterViewHolder) {
        } else if (holder instanceof ETHViewHolder) {
        } else if (holder instanceof LoaderViewHolder) {
        } else if (holder instanceof LoadingViewHolder) {
        } else {
            ViewHolder itemViewHolder = (ViewHolder) holder;
            TxItem item = mData.get(position - 1);
            itemViewHolder.txtTxHash.setText(item.getTxHash());

            String date = item.getDate().substring(0, item.getDate().indexOf("T"));
            String time = item.getDate().substring(date.length() + 1, item.getDate().indexOf("."));

            Timestamp timestamp = Timestamp.valueOf(date + " " + time + ".0000");

            Calendar calendar = Calendar.getInstance(Locale.KOREA);
            calendar.setTimeInMillis(timestamp.getTime());

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date tmpDate = sdf.parse(date + " " + time);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                itemViewHolder.txtDate.setText(format.format(tmpDate));
            } catch (Exception e) {
                itemViewHolder.txtDate.setText("- -");
            }


            if (entry.getAddress().equals(item.getFrom())) {
                itemViewHolder.txtDate.setTextColor(mContext.getResources().getColor(R.color.colorRemittance));
                itemViewHolder.txtValue.setTextColor(mContext.getResources().getColor(R.color.colorRemittance));
                itemViewHolder.txtSymbol.setTextColor(mContext.getResources().getColor(R.color.colorRemittance));
                itemViewHolder.txtValue.setText("- " + item.getAmount());
            } else {
                itemViewHolder.txtDate.setTextColor(mContext.getResources().getColor(R.color.colorDeposit));
                itemViewHolder.txtValue.setTextColor(mContext.getResources().getColor(R.color.colorDeposit));
                itemViewHolder.txtSymbol.setTextColor(mContext.getResources().getColor(R.color.colorDeposit));
                itemViewHolder.txtValue.setText("+ " + item.getAmount());
            }
            itemViewHolder.txtSymbol.setText(entry.getSymbol());
        }
    }

    @Override
    public int getItemCount() {
        if (mContext.equals(Constants.KS_COINTYPE_ETH)) {
            return 2;
        }

        if (mData.size() == 0) {
            return 2;
        } else if (mData.size() > 0 && moreLoading) {
            return mData.size() + 2;
        } else {
            return mData.size() + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_HEADER;

        if (mCoinType.equals(Constants.KS_COINTYPE_ETH))
            return VIEW_ETHER;

        if (isLoading)
            return VIEW_LOADING;

        if (mData.size() == 0)
            return VIEW_EMPTY;

        if (moreLoading && position != 0
                && position == getItemCount() - 1)
            return VIEW_LOADER;

        return VIEW_ITEM;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtTxHash;
        TextView txtDate;
        TextView txtValue;
        TextView txtSymbol;

        ViewHolder(View itemView) {
            super(itemView);

            txtTxHash = itemView.findViewById(R.id.txt_tx_hash);
            txtTxHash.setPaintFlags(txtTxHash.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtDate = itemView.findViewById(R.id.txt_date);
            txtValue = itemView.findViewById(R.id.txt_value);
            txtSymbol = itemView.findViewById(R.id.txt_symbol);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mItemClickListener != null)
                mItemClickListener
                        .onItemClick(((TextView) view.findViewById(R.id.txt_tx_hash)).getText().toString());
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ViewGroup btnSelectCoin;
        TextView txtSymbol;
        TextView txtAsset, txtTransAsset;
        ViewGroup btnExchange;
        ViewGroup btnSwap, btnWithdraw, btnDeposit;
        ViewGroup btnInfo;
        ViewGroup btnSearchCondition;

        ViewGroup layoutSearch;

        TextView txtState, txtType;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            txtSymbol = itemView.findViewById(R.id.txt_selected_symbol);
            txtAsset = itemView.findViewById(R.id.txt_asset);
            txtTransAsset = itemView.findViewById(R.id.txt_trans_asset);

            btnSelectCoin = itemView.findViewById(R.id.btn_select_coin);
            btnSelectCoin.setOnClickListener(this);
            btnExchange = itemView.findViewById(R.id.btn_select_exchange);
            btnExchange.setOnClickListener(this);
            btnSwap = itemView.findViewById(R.id.btn_swap);
            btnSwap.setOnClickListener(this);
            btnWithdraw = itemView.findViewById(R.id.btn_withdraw);
            btnWithdraw.setOnClickListener(this);
            btnDeposit = itemView.findViewById(R.id.btn_deposit);
            btnDeposit.setOnClickListener(this);
            btnInfo = itemView.findViewById(R.id.btn_info);
            btnInfo.setOnClickListener(this);
            btnSearchCondition = itemView.findViewById(R.id.btn_search_condition);
            btnSearchCondition.setOnClickListener(this);

            layoutSearch = itemView.findViewById(R.id.layout_tx_search);

            if (mCoinType.equals(Constants.KS_COINTYPE_ETH))
                layoutSearch.setVisibility(View.GONE);

            txtState = itemView.findViewById(R.id.txt_tx_state);
            txtType = itemView.findViewById(R.id.txt_tx_type);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_select_coin:
                    if (mHeaderClickListener != null)
                        mHeaderClickListener.onSelectCoin();
                    break;

                case R.id.btn_select_exchange:
                    if (mHeaderClickListener != null)
                        mHeaderClickListener.onSelectExchange();
                    break;

                case R.id.btn_swap:
                    if (mHeaderClickListener != null)
                        mHeaderClickListener.onSwap();
                    break;

                case R.id.btn_withdraw:
                    if (mHeaderClickListener != null)
                        mHeaderClickListener.onTransfer();
                    break;

                case R.id.btn_deposit:
                    if (mHeaderClickListener != null)
                        mHeaderClickListener.onDeposit();
                    break;

                case R.id.btn_info:
                    BasicDialog dialog = new BasicDialog(mContext);
                    dialog.setMessage(mContext.getString(R.string.infoTx));
                    dialog.show();
                    break;

                case R.id.btn_search_condition:
                    if (mHeaderClickListener != null)
                        mHeaderClickListener.onSearchCondition();
                    break;
            }
        }
    }

    public class ETHViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtEtherscan;

        public ETHViewHolder(View itemView) {
            super(itemView);

            txtEtherscan = itemView.findViewById(R.id.txt_etherscan);
            txtEtherscan.setPaintFlags(txtEtherscan.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            txtEtherscan.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.txt_etherscan:
                    String tracker;
                    if (network == MyConstants.NETWORK_MAIN)
                        tracker = ServiceConstants.URL_ETHERSCAN;
                    else
                        tracker = ServiceConstants.URL_ROPSTEN;

//                    if (entry.getType().equals(MyConstants.TYPE_COIN))
                    String url = tracker + "address/" + entry.getAddress();
//                    else
//                        url += "token/" + entry.getContractAddress();

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    mContext.startActivity(intent);
                    break;
            }
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        public FooterViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class LoaderViewHolder extends RecyclerView.ViewHolder {
        public LoaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(View itemView) {
            super(itemView);
        }
    }

    private void setSearchState(TextView txtView, MyConstants.TxState state) {
        if (state == MyConstants.TxState.DONE)
            txtView.setText(mContext.getString(R.string.complete));
        else
            txtView.setText(mContext.getString(R.string.pending));
    }

    private void setSearchType(TextView txtView, MyConstants.TxType type) {
        if (type == MyConstants.TxType.WHOLENESS)
            txtView.setText(mContext.getString(R.string.all));
        else if (type == MyConstants.TxType.REMITTANCE)
            txtView.setText(mContext.getString(R.string.withdraw));
        else
            txtView.setText(mContext.getString(R.string.deposit));
    }

    public void setWalletEntry(WalletEntry entry) {
        this.entry = entry;
        notifyDataSetChanged();
    }

    public void setExchange(String exchange) {
        EXCHANGE = exchange;
        notifyDataSetChanged();
    }

    public void moreLoading(boolean loading) {
        moreLoading = loading;
    }

    public void showLoading(boolean loading) {
        isLoading = loading;
        notifyDataSetChanged();
    }

    // allows clicks events to be caught
    public void setItemClickListener(TransactionListAdapter.ItemClickListener itemClickListener) {
        this.mItemClickListener = itemClickListener;
    }

    public void setHeaderClickListener(HeaderClickListener listener) {
        this.mHeaderClickListener = listener;
    }

    public boolean isMoreLoading() {
        return moreLoading;
    }

    public void setMoreData(List<TxItem> data) {
        mData = data;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(String txHash);
    }

    public interface HeaderClickListener {
        void onSelectCoin();

        void onSelectExchange();

        void onSwap();

        void onTransfer();

        void onDeposit();

        void onSearchCondition();
    }
}
