package foundation.icon.iconex.wallet.transfer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.TTextInputLayout;

public class ICONTransferActivityNew extends AppCompatActivity {

    // appbar UI
    private CustomActionBar appbar;

    // Select Network UI
    private ViewGroup layoutNetwork;
    private ViewGroup btnNetwork;
    private TextView txtNetwork;

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

    // Input Data UI
    private TTextInputLayout editData;
    private Button btnViewData;

    // Fee UI
    private TextView labelStepLimit;
    private TextView labelEstimatedMaxFee;
    private TextView txtStepLimit;
    private TextView txtEstimatedMaxFee;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_transfer_new);

        initView();
    }

    private void initView() {
        // load appbar UI
        appbar = findViewById(R.id.appbar);

        // load select network UI
        layoutNetwork = findViewById(R.id.layout_network);
        btnNetwork = findViewById(R.id.btn_network);
        txtNetwork = findViewById(R.id.txt_network);

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

        // load Input Data UI
        editData = findViewById(R.id.edit_data);
        btnViewData = findViewById(R.id.btn_view_data);

        // load Fee UI
        labelStepLimit = findViewById(R.id.lb_step_limit);
        labelEstimatedMaxFee = findViewById(R.id.lb_estimated_max_fee);
        txtStepLimit = findViewById(R.id.txt_step_limit);
        txtEstimatedMaxFee = findViewById(R.id.txt_trans_fee);
    }


}
