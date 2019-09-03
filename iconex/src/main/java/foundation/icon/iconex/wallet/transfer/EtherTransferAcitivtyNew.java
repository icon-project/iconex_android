package foundation.icon.iconex.wallet.transfer;

import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import foundation.icon.iconex.R;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.TTextInputLayout;

public class EtherTransferAcitivtyNew extends AppCompatActivity {

    // appbar UI
    private CustomActionBar appbar;

    // available UI
    private TextView labelBalance;
    private TextView labelSymbol;
    private TextView txtBalance;
    private TextView txtTransBalance;

    // Send Amount UI
    private TTextInputLayout editSend;
    private TextView txtTransSend;
    private Button btnPlus10, btnPlus100, btnPlus1000, btnTheWhole;

    // Receving Address UI
    private TTextInputLayout editAddress;
    private Button btnContact;
    private Button btnQRcodeScan;

    // Gas Limit UI
    private TTextInputLayout editLimit;

    // Gas Contorl UI
    private TextView labelPrice;
    private TextView txtPrice;
    private TextView labelSlow;
    private TextView labelFast;
    private CustomSeekbar seekPrice;

    // Input Data UI
    private TTextInputLayout editData;
    private Button btnViewData;

    // Fee UI
    private TextView lbEstimatedMaxFee;
    private TextView txtFee;
    private TextView txtTransFee;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ether_transfer_new);

        initView();
    }

    private void initView() {
        // load appbar UI
        appbar = findViewById(R.id.appbar);

        // load available UI
        labelBalance = findViewById(R.id.lb_balance);
        labelSymbol = findViewById(R.id.lb_symbol);
        txtBalance = findViewById(R.id.txt_balance);
        txtTransBalance = findViewById(R.id.txt_trans_balance);

        btnPlus10 = findViewById(R.id.btn_plus_10);
        btnPlus100 = findViewById(R.id.btn_plus_100);
        btnPlus1000 = findViewById(R.id.btn_plus_1000);
        btnTheWhole = findViewById(R.id.btn_plus_all);

        // load Receiving Address UI
        editAddress = findViewById(R.id.edit_to_address);
        btnContact = findViewById(R.id.btn_contacts);
        btnQRcodeScan = findViewById(R.id.btn_qr_scan);

        // load Gas Control UI
        labelPrice = findViewById(R.id.lb_price);
        txtPrice = findViewById(R.id.txt_price);
        labelSlow = findViewById(R.id.lb_slow);
        labelFast = findViewById(R.id.lb_fast);
        seekPrice = findViewById(R.id.seek_price);

        // init
        txtPrice.setText(53 + " Gwei");
        seekPrice.setMax(98);
        seekPrice.setProgress(53);
        seekPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtPrice.setText((progress + 1) + " Gwei");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // load Input Data UI
        editData = findViewById(R.id.edit_data);
        btnViewData = findViewById(R.id.btn_view_data);

        // load Fee UI
        lbEstimatedMaxFee = findViewById(R.id.lb_estimated_max_fee);
        txtFee = findViewById(R.id.txt_fee);
        txtTransFee = findViewById(R.id.txt_trans_fee);
    }
}
