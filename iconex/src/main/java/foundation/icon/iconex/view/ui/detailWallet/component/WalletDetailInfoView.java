package foundation.icon.iconex.view.ui.detailWallet.component;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.TextViewCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.DecimalFomatter;

public class WalletDetailInfoView extends FrameLayout implements View.OnClickListener {

    private TextView txtSymbol;
    private ImageButton btnSymbol;
    private TextView txtAmount;
    private TextView txtExchange;
    private TextView txtUnit;
    private ImageButton btnUnit;
    private ProgressBar loading;


    private ViewGroup layoutStake;
    private TextView txtBalance;
    private TextView txtLiquid;
    private TextView txtStake;
    private ProgressBar loadingStake;

    private List<String> mLstUnit = new ArrayList<>();

    private int mCursorLstUnit = 0;

    public interface OnClickListener {
        void onSymbolClick();
        void onUnitTextChange(String text);
    }

    private OnClickListener mOnClickListener = null;

    public WalletDetailInfoView(@NonNull Context context) {
        super(context);
        initView();
    }

    public WalletDetailInfoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public WalletDetailInfoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_info_view, this, true);

        txtSymbol = v.findViewById(R.id.txt_symbol);
        btnSymbol = v.findViewById(R.id.btn_symbol);
        txtAmount = v.findViewById(R.id.txt_amount);
        txtExchange = v.findViewById(R.id.txt_exchange);
        txtUnit = v.findViewById(R.id.txt_unit);
        btnUnit = v.findViewById(R.id.btn_unit);
        loading = v.findViewById(R.id.loading);

        layoutStake = v.findViewById(R.id.layout_sataked);
        txtBalance = v.findViewById(R.id.txtICXBalance);
        txtLiquid = v.findViewById(R.id.txtLiquidICX);
        txtStake = v.findViewById(R.id.txtStakedICX);
        loadingStake = v.findViewById(R.id.loading_stake);

        txtSymbol.setOnClickListener(this);
        btnSymbol.setOnClickListener(this);
        txtUnit.setOnClickListener(this);
        btnUnit.setOnClickListener(this);

        TextViewCompat.setAutoSizeTextTypeWithDefaults(txtAmount, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
    }

    public void setTextSymbol(String symbol) {
        txtSymbol.setText(symbol);
    }

    public void setBtnSymbolVisible(boolean visible) {
        btnSymbol.setVisibility(visible ? VISIBLE : GONE);
    }

    public void setUnitList(List<String> lstUnit) {
        mLstUnit = lstUnit;
        mCursorLstUnit = 0;
        String strUnit = mLstUnit.get(mCursorLstUnit);
        txtUnit.setText(strUnit);
    }

    public void setOnTextChangeListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setAmount(BigDecimal amount) {
        txtAmount.setText(DecimalFomatter.format(amount));
    }

    public void setExchange(BigDecimal exchange, int scale) {
        txtExchange.setText(DecimalFomatter.format(exchange, scale));
    }

    public void setLoadingStake(boolean loading) {
        loadingStake.setVisibility(loading ? VISIBLE : GONE);
    }

    public void setStakeData(BigDecimal[] data) {
        layoutStake.setVisibility(data == null ? GONE : VISIBLE);
        if (data != null) {
            BigDecimal balance = data[0];
            BigDecimal liquid = data[1];
            BigDecimal stake = data[2];
            txtBalance.setText(DecimalFomatter.format(balance, 8));
            txtLiquid.setText(DecimalFomatter.format(liquid, 8));
            txtStake.setText(DecimalFomatter.format(stake, 8));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_symbol:
            case R.id.btn_symbol: {
                if (mOnClickListener != null && btnSymbol.getVisibility() == VISIBLE)
                    mOnClickListener.onSymbolClick();
            } break;
            case R.id.txt_unit:
            case R.id.btn_unit: {
                if (++mCursorLstUnit >= mLstUnit.size())
                    mCursorLstUnit = 0;

                String strUnit = mLstUnit.get(mCursorLstUnit);
                txtUnit.setText(strUnit);

                if (mOnClickListener != null)
                    mOnClickListener.onUnitTextChange(strUnit);
            } break;
            default: break;
        }
    }

    public void setLoading(boolean loading) {
        this.loading.setVisibility(loading ? VISIBLE : GONE);
        txtAmount.setVisibility(loading ? INVISIBLE : VISIBLE);
        txtExchange.setVisibility(loading ? INVISIBLE : VISIBLE);
    }
}
