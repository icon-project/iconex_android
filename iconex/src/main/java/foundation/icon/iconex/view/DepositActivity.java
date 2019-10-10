package foundation.icon.iconex.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.WalletAddressQrcodeView;

public class DepositActivity extends AppCompatActivity {

    public static final String PARAM_WALLET = "wallet";

    private Button btnClose;
    private TextView txtTitle;
    private WalletAddressQrcodeView walletAddressQrcodeView;

    private Wallet wallet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

        btnClose = findViewById(R.id.btn_close);
        txtTitle = findViewById(R.id.txt_title);
        walletAddressQrcodeView = findViewById(R.id.wallet_address_qrcode_view);

        wallet = ((Wallet) getIntent().getSerializableExtra(PARAM_WALLET));

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtTitle.setText(wallet.getAlias());
        walletAddressQrcodeView.bind(getString(R.string.walletAddress), wallet);
    }
}
