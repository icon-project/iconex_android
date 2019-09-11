package foundation.icon.iconex.dev2_detail.component;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;

import static foundation.icon.ICONexApp.network;

public class TransactionListView extends FrameLayout implements View.OnClickListener {

    private RecyclerView lstTransaction;
    private TextView txtNoTransaction;
    private TextView lnkEtehrscan;

    private boolean mIsEtherscanVisible = false;
    private RecyclerView.Adapter adapter = null;
    private List<TransactionItemViewData> lstViewData = new ArrayList<>();

    public interface OnClickEtherScanListener {
        void onClickEtherScan();
    }
    private OnClickEtherScanListener mOnClickEtherScanListener = null;

    public void setOnClickEtherScanListener (OnClickEtherScanListener listener){
        mOnClickEtherScanListener = listener;
    }

    public interface OnScrollBottomListener {
        void onScrollBottom();
    }
    private OnScrollBottomListener mOnScrollBottomListener = null;

    public TransactionListView(@NonNull Context context) {
        super(context);
        initView();
    }

    public TransactionListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TransactionListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_detail_transaction_list, this, true);

        lstTransaction = findViewById(R.id.lstTransaction);
        txtNoTransaction = findViewById(R.id.txt_no_transaction);
        lnkEtehrscan = findViewById(R.id.link_etherscan);

        txtNoTransaction.setOnClickListener(this);
        lnkEtehrscan.setOnClickListener(this);
        txtNoTransaction.setVisibility(GONE);
        lnkEtehrscan.setVisibility(GONE);

        lstTransaction.setLayoutManager(new LinearLayoutManager(getContext()));
        lstTransaction.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(!recyclerView.canScrollVertically(1)) {
                    if (mOnScrollBottomListener != null)
                        mOnScrollBottomListener.onScrollBottom();
                }
            }
        });
        adapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                TransactionItemView v = new TransactionItemView(parent.getContext());

                v.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                ));

                RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(v) { };

                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                TransactionItemViewData viewData = lstViewData.get(position);
                ((TransactionItemView) holder.itemView).bind(viewData);
            }

            @Override
            public int getItemCount() {
                return lstViewData.size();
            }
        };
        lstTransaction.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_no_transaction:
            case R.id.link_etherscan: {
                if (mIsEtherscanVisible && mOnClickEtherScanListener != null)
                    mOnClickEtherScanListener.onClickEtherScan();
            } break;
        }
    }

    public void setOnScrollBottomListener(OnScrollBottomListener listener) {
        mOnScrollBottomListener = listener;
    }

    public void setTextNoTransaction(String text, boolean isVisibleEtherscan) {
        txtNoTransaction.setText(text);
        mIsEtherscanVisible = isVisibleEtherscan;
    }

    public void setViewDataList(List<TransactionItemViewData> viewDataList) {
        lstViewData = viewDataList;
        adapter.notifyDataSetChanged();

        boolean isSize0 = lstViewData.size() == 0;
        txtNoTransaction.setVisibility(isSize0 ? VISIBLE : GONE);
        lnkEtehrscan.setVisibility(isSize0 && mIsEtherscanVisible ? VISIBLE : GONE);
    }
}
