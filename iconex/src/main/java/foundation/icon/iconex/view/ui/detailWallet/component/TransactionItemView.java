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

public class TransactionItemView extends FrameLayout {

    private TextView txtName;
    private TextView txtDate;
    private TextView txtAddress;
    private TextView txtPrimaryAmount;
    private TextView txtDarkAmount;

    public TransactionItemView(@NonNull Context context) {
        super(context);
        initView();
    }

    public TransactionItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TransactionItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, this, true);

        txtName = findViewById(R.id.txt_name);
        txtDate = findViewById(R.id.txt_date);
        txtAddress = findViewById(R.id.txt_address);
        txtPrimaryAmount = findViewById(R.id.txt_primary_amount);
        txtDarkAmount = findViewById(R.id.txt_dark_amount);
    }

    public void setTextAmount(String amount, boolean isDark) {
        txtDarkAmount.setVisibility(isDark ? VISIBLE : GONE);
        txtDarkAmount.setText(amount);
        txtPrimaryAmount.setVisibility(isDark ? GONE : VISIBLE);
        txtPrimaryAmount.setText(amount);
    }

    public void setTextName(String name) {
        txtName.setText(name);
    }

    public void setTextDate(String date) {
        txtDate.setText(date);
    }

    public void setTextAddress(String address) {
        txtAddress.setText(address);
    }

    public void bind(TransactionItemViewData viewData) {
        setTextName(viewData.getTxtName());
        setTextAddress(viewData.getTxtAddress());
        setTextDate(viewData.getTxtDate());
        setTextAmount(viewData.getTxtAmount(), viewData.isDark());
    }
}
