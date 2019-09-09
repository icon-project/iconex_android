package foundation.icon.iconex.dev2_detail.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;

public class WalletDetailInfoView extends FrameLayout implements View.OnClickListener {

    private TextView txtSymbol;
    private ImageButton btnSymbol;
    private TextView txtAmount;
    private TextView txtExchange;
    private TextView txtUnit;
    private ImageButton btnUnit;

    private List<String> mLstToken = new ArrayList<>();
    private List<String> mLstUnit = new ArrayList<>();

    private int mCursorLstToken = 0;
    private int mCursorLstUnit = 0;

    public interface OnTextChangeListener {
        void onSymbolTextChange(String text);
        void onUnitTextChange(String text);
    }

    private OnTextChangeListener mOnTextChangeListener = null;

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

        txtSymbol.setOnClickListener(this);
        btnSymbol.setOnClickListener(this);
        txtUnit.setOnClickListener(this);
        btnUnit.setOnClickListener(this);
    }

    public void setTokenList(List<String> lstToken) {
        mLstToken = lstToken;
        mCursorLstToken = 0;
        String strToken = mLstToken.get(mCursorLstToken);
        txtSymbol.setText(strToken);
    }

    public void setUnitList(List<String> lstUnit) {
        mLstUnit = lstUnit;
        mCursorLstUnit = 0;
        String strUnit = mLstUnit.get(mCursorLstUnit);
        txtUnit.setText(strUnit);
    }

    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mOnTextChangeListener = listener;
    }

    public void setAmount(BigDecimal amount) {
        String strAmount = amount.setScale(4, BigDecimal.ROUND_FLOOR) + "";
        txtAmount.setText(strAmount);
    }

    public void setExchange(BigDecimal exchange, int scale) {
        String strExchange = exchange.setScale(scale, BigDecimal.ROUND_FLOOR) + "";
        txtExchange.setText(strExchange);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_symbol:
            case R.id.btn_symbol: {
                if (++mCursorLstToken >= mLstToken.size())
                    mCursorLstToken = 0;

                String strToken = mLstToken.get(mCursorLstToken);
                txtSymbol.setText(strToken);

                if (mOnTextChangeListener != null)
                    mOnTextChangeListener.onSymbolTextChange(strToken);
            } break;
            case R.id.txt_unit:
            case R.id.btn_unit: {
                if (++mCursorLstUnit >= mLstUnit.size())
                    mCursorLstUnit = 0;

                String strUnit = mLstUnit.get(mCursorLstUnit);
                txtUnit.setText(strUnit);

                if (mOnTextChangeListener != null)
                    mOnTextChangeListener.onUnitTextChange(strUnit);
            } break;
            default: break;
        }
    }
}
