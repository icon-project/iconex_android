package foundation.icon.iconex.view;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.ContactsDialog;
import foundation.icon.iconex.wallet.Wallet;

public class IScoreActivity extends AppCompatActivity {
    private static final String TAG = IScoreActivity.class.getSimpleName();

    private Wallet wallet;

    private TextView txtCurrent, txtReceive, txtLimitPrice, txtFee, txtFeeUsd;
    private Button btnClaim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_iscore);

        if (getIntent() != null)
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initView() {
        txtCurrent = findViewById(R.id.txt_current);
        txtReceive = findViewById(R.id.txt_receive);
        txtLimitPrice = findViewById(R.id.txt_limit_price);
        txtFee = findViewById(R.id.txt_fee);
        txtFeeUsd = findViewById(R.id.txt_fee_usd);
        btnClaim = findViewById(R.id.btn_claim);
        btnClaim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void getData() {

    }
}
