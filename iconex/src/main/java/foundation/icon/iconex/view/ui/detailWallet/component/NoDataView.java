package foundation.icon.iconex.view.ui.detailWallet.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;

public class NoDataView extends FrameLayout implements View.OnClickListener {


    private TextView txtNoTransaction;
    private TextView lnkEtehrscan;
    private ViewGroup loading;
    private ViewGroup container;
    private boolean mIsEtherscanVisible = false;

    private int expandedHeight;
    private int collapsedHeight;

    public interface OnClickEtherScanListener {
        void onClickEtherScan();
    }
    private OnClickEtherScanListener mOnClickEtherScanListener = null;

    public void setOnClickEtherScanListener (OnClickEtherScanListener listener){
        mOnClickEtherScanListener = listener;
    }

    public NoDataView(@NonNull Context context) {
        super(context);
        initView();
    }

    public NoDataView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public NoDataView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_detail_transaction_list, this, true);

        loading = findViewById(R.id.loading);

        txtNoTransaction = findViewById(R.id.txt_no_transaction);
        lnkEtehrscan = findViewById(R.id.link_etherscan);
        container = findViewById(R.id.container);

        txtNoTransaction.setOnClickListener(this);
        lnkEtehrscan.setOnClickListener(this);
        txtNoTransaction.setVisibility(GONE);
        lnkEtehrscan.setVisibility(GONE);
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

    public void setContainerHeight(int expandedHeight, int collapsedHeight) {
        this.expandedHeight = expandedHeight;
        this.collapsedHeight = collapsedHeight;
    }

    public void setTextNoTransaction(String text, boolean isVisibleEtherscan) {
        txtNoTransaction.setText(text);
        mIsEtherscanVisible = isVisibleEtherscan;
    }

    public void setExpaned(boolean isExpanded) {
        getLayoutParams().height = isExpanded ? expandedHeight : collapsedHeight;
        requestLayout();
    }

    public void setLoading(boolean isLoading) {
        loading.setVisibility(isLoading ? VISIBLE : GONE);

        boolean isVisible = loading.getVisibility() == VISIBLE || txtNoTransaction.getVisibility() == VISIBLE;
        setVisibility(isVisible ? VISIBLE : GONE);
    }

    public void setNodata(boolean isSize0) {
        txtNoTransaction.setVisibility(isSize0 ? VISIBLE : GONE);
        lnkEtehrscan.setVisibility(isSize0 && mIsEtherscanVisible ? VISIBLE : GONE);

        boolean isVisible = loading.getVisibility() == VISIBLE || txtNoTransaction.getVisibility() == VISIBLE;
        setVisibility(isVisible ? VISIBLE : GONE);
    }
}
