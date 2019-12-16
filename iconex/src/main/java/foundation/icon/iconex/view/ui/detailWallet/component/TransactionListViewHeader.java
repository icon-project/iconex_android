package foundation.icon.iconex.view.ui.detailWallet.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;

public class TransactionListViewHeader extends FrameLayout {

    private ImageButton btnInfo;
    private TextView txtViewOption;

    public TransactionListViewHeader(@NonNull Context context) {
        super(context);
        initView();
    }

    public TransactionListViewHeader(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public TransactionListViewHeader(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.layout_detail_wallet_header, this, true);

        btnInfo = findViewById(R.id.btn_info);
        txtViewOption = findViewById(R.id.txt_view_option);
        txtViewOption.setText(getContext().getString(R.string.all) + " ▼");
    }

    public void setOnClickInfoButton(View.OnClickListener listener) {
        btnInfo.setOnClickListener(listener);
    }

    public void setOnClickViewOption(View.OnClickListener listener) {
        txtViewOption.setOnClickListener(listener);
    }

    public void setTextViewOption(String text) {
        txtViewOption.setText(text);
    }

    public void setInfoButtonVisible(boolean isVisible) {
        btnInfo.setVisibility(isVisible ? VISIBLE : GONE);
    }
}
