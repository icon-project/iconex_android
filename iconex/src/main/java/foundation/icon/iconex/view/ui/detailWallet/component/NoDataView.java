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
    private boolean mIsEtherscanVisible = false;

    public interface OnClickEtherScanListener {
        void onClickEtherScan();
    }
    private OnClickEtherScanListener mOnClickEtherScanListener = null;

    public interface OnUpdateHeightListener {
        int getHeight();
    }
    OnUpdateHeightListener updateHeightListener = null;

    public void setOnClickEtherScanListener (OnClickEtherScanListener listener){
        mOnClickEtherScanListener = listener;
    }

    public void setOnUpdateHeightListener(OnUpdateHeightListener listener) {
        updateHeightListener = listener;
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

    public void setTextNoTransaction(String text, boolean isVisibleEtherscan) {
        txtNoTransaction.setText(text);
        mIsEtherscanVisible = isVisibleEtherscan;
    }

    public void setLoading(boolean isLoading) {
        loading.setVisibility(isLoading ? VISIBLE : GONE);
        updateView();
    }

    private boolean isSize0 = false;
    public void setNodata(boolean isSize0) {
        this.isSize0 = isSize0;
        updateView();
    }

    private void updateView() {
        boolean loadingVisible = loading.getVisibility() == VISIBLE;
        txtNoTransaction.setVisibility(isSize0 && !loadingVisible? VISIBLE : GONE);
        lnkEtehrscan.setVisibility(isSize0 && mIsEtherscanVisible  && !loadingVisible ? VISIBLE : GONE);

        boolean isVisible = loading.getVisibility() == VISIBLE || txtNoTransaction.getVisibility() == VISIBLE;
        setVisibility(isVisible ? VISIBLE : GONE);

        if (isVisible && updateHeightListener != null) {
            int height = updateHeightListener.getHeight();
            if (getLayoutParams().height != height) {
                getLayoutParams().height = height;
                requestLayout();
            }
        }
    }
}
