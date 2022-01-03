package foundation.icon.iconex.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.dialogs.DataTypeDialog;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.TransactionSendDialog;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.DecimalFomatter;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.transfer.IconEnterDataFragment;
import foundation.icon.iconex.view.ui.transfer.TransferViewModel;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.contacts.ContactsActivity;
import foundation.icon.iconex.wallet.transfer.data.ICONTxInfo;
import foundation.icon.iconex.wallet.transfer.data.InputData;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.CustomToast;
import foundation.icon.iconex.widgets.TTextInputLayout;
import foundation.icon.icx.IconService;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.request.Transaction;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IconTransferActivity extends AppCompatActivity implements IconEnterDataFragment.OnEnterDataLisnter {

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

    // Input Data UI
    private ViewGroup layoutData;
    private TTextInputLayout editData;
    private Button btnViewData;
    private View btnData;

    // Fee UI
    private TextView labelStepLimit;
    private TextView labelEstimatedMaxFee;
    private TextView symbolStepLimit;
    private TextView symbolEstimatedMaxFee;
    private TextView txtStepLimit;
    private TextView txtEstimatedMaxFee;
    private TextView txtTransFee;

    // send button UI
    private Button btnSend;

    // activity result
    private static final int RC_CONTACTS = 9001;
    private static final int RC_BARCODE_CAPTURE = 9002;
    private static final int RC_DATA = 9003;

    // data field
    private TransferViewModel vm;
    private Wallet wallet;
    private WalletEntry entry;
    private String privateKey;

    private BigInteger balance;

    private LoopChainClient LCClient = null;
    private BigInteger stepPriceLoop = null;
    private BigInteger stepPriceICX = null;

    private final String CODE_EXCHANGE = "icxusd";
    private String fee;

    private NetworkService mService;
    private boolean mBound = false;

    private InputData data = null;

    private BigInteger defaultLimit = BigInteger.ZERO;
    private BigInteger minStep = BigInteger.ZERO;
    private BigInteger maxStep = BigInteger.ZERO;
    private BigInteger tokenStep = BigInteger.ZERO;
    private BigInteger inputPrice = BigInteger.ZERO;
    private BigInteger contractCall = BigInteger.ZERO;

    private String txtStepICX, txtStepGloop, txtStepTrans;

    private String strLimit = "";
    private String strFee = "";
    private String strTransFee = "";

    private String txtRemain;
    private String txtTransRemain;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_transfer_new);

        // activity field init
        if (getIntent() != null) {
            wallet = (Wallet) getIntent().getExtras().get("walletInfo");
            entry = (WalletEntry) getIntent().getExtras().get("walletEntry");
            privateKey = getIntent().getStringExtra("privateKey");

            vm = ViewModelProviders.of(this).get(TransferViewModel.class);
            vm.setWallet(wallet);
            vm.setEntry(entry);
            vm.setPrivateKey(privateKey);
        }

        if (savedInstanceState != null) {
            wallet = vm.getWallet().getValue();
            entry = vm.getEntry().getValue();
            privateKey = vm.getPrivateKey().getValue();

            try {
                balance = new BigInteger(entry.getBalance());
            } catch (Exception e) {
                balance = BigInteger.ZERO;
            }
        }

        loadView();
        initView();

        // set appbar title
        appbar.setTitle(wallet.getAlias());

        // set Symbol
        editSend.setAppendText(entry.getSymbol());
        labelSymbol.setText("(" + entry.getSymbol() + ")");
        String symbol = "(" + MyConstants.SYMBOL_ICON + ")";
        symbolStepLimit.setText(symbol);
        symbolEstimatedMaxFee.setText(symbol);

        // set show data layout
        boolean isToken = entry.getType().equals(MyConstants.TYPE_TOKEN);
        layoutData.setVisibility(isToken ? View.GONE : View.VISIBLE);

        // set LoopChainClient
        if (LCClient == null) {
            String url = null;
            switch (ICONexApp.NETWORK.getNid().intValue()) {
                case MyConstants.NETWORK_MAIN:
                    url = ServiceConstants.TRUSTED_HOST_MAIN;
                    break;

                case MyConstants.NETWORK_TEST:
                    url = ServiceConstants.TRUSTED_HOST_TEST;
                    break;

                case MyConstants.NETWORK_DEV:
                    url = ServiceConstants.DEV_HOST;
                    break;
            }

            try {
                LCClient = new LoopChainClient(url);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        getStepPrice();
        getStepLimit();

        editSend.setFocus(true);

        if (getIntent().getStringExtra("address") != null)
            editAddress.setText(getIntent().getStringExtra("address"));

        if (getIntent().getSerializableExtra("amount") != null) {
            BigInteger amount = (BigInteger) getIntent().getSerializableExtra("amount");
            requestAmount = ConvertUtil.getValue(amount, 18);
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setSendEnable();
            }
        }, 300);
    }

    String requestAmount = null;

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, NetworkService.class);
        mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            balance = new BigInteger(entry.getBalance());
        } catch (Exception e) {
            balance = BigInteger.ZERO;
        }

        setBalance(balance);
        if (requestAmount != null) {
            editSend.setText(requestAmount);
            validateSendAmount(requestAmount);
            requestAmount = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CONTACTS) {
            if (resultCode == ContactsActivity.CODE_RESULT) {
                String address = data.getStringExtra("address");
                editAddress.setText(address);

                setSendEnable();
            }
        } else if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    editAddress.setText(barcode.displayValue);
                    setSendEnable();
                }
            }
        } else if (requestCode == RC_DATA) {
            IconEnterDataActivity.activityResultHelper(resultCode, data, this);
        }
    }

    private void loadView() {
        // load appbar UI
        appbar = findViewById(R.id.appbar);

        // load available UI
        labelBalance = findViewById(R.id.lb_balance);
        labelSymbol = findViewById(R.id.lb_symbol);
        txtBalance = findViewById(R.id.txt_balance);
        txtTransBalance = findViewById(R.id.txt_trans_balance);

        editSend = findViewById(R.id.edit_send_amount);
        txtTransSend = findViewById(R.id.txt_trans_amount);
        btnPlus10 = findViewById(R.id.btn_plus_10);
        btnPlus100 = findViewById(R.id.btn_plus_100);
        btnPlus1000 = findViewById(R.id.btn_plus_1000);
        btnTheWhole = findViewById(R.id.btn_plus_all);

        // load Receiving Address UI
        editAddress = findViewById(R.id.edit_to_address);
        btnContact = findViewById(R.id.btn_contacts);
        btnQRcodeScan = findViewById(R.id.btn_qr_scan);

        // load Input Data UI
        layoutData = findViewById(R.id.layout_data);
        editData = findViewById(R.id.edit_data);
        btnViewData = findViewById(R.id.btn_view_data);
        btnData = findViewById(R.id.btnData);

        // load Fee UI
        labelStepLimit = findViewById(R.id.lb_step_limit);
        labelEstimatedMaxFee = findViewById(R.id.lb_estimated_max_fee);
        symbolStepLimit = findViewById(R.id.symbol_step_limit);
        symbolEstimatedMaxFee = findViewById(R.id.symbol_estimated_max_fee);
        txtStepLimit = findViewById(R.id.txt_step_limit);
        txtEstimatedMaxFee = findViewById(R.id.txt_estimated_max_fee);
        txtTransFee = findViewById(R.id.txt_trans_fee);

        // load send button UI
        btnSend = findViewById(R.id.btn_send);
    }

    private void initView() {

        TextViewCompat.setAutoSizeTextTypeWithDefaults(txtBalance, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(txtStepLimit, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        // init appbar
        appbar.setOnActionClickListener(new CustomActionBar.OnActionClickListener() {
            @Override
            public void onClickAction(CustomActionBar.ClickAction action) {
                switch (action) {
                    case btnStart:
                        finish();
                        break;
                    case btnEnd:
                        showInfo();
                        break;
                }
            }
        });

        // init plus buttons
        View.OnClickListener onClickPlusButtons = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_plus_10:
                        addPlus(10);
                        break;
                    case R.id.btn_plus_100:
                        addPlus(100);
                        break;
                    case R.id.btn_plus_1000:
                        addPlus(1000);
                        break;
                    case R.id.btn_plus_all:
                        if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                            if (balance.compareTo(ConvertUtil.valueToBigInteger(fee, 18)) < 0) {
                                editSend.setText("0");
                            } else {
                                BigInteger allIcx = balance.subtract(ConvertUtil.valueToBigInteger(fee, 18));
                                editSend.setText(ConvertUtil.getValue(allIcx, entry.getDefaultDec()));
                            }
                        } else {
                            editSend.setText(ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec()));
                        }
                        break;
                }

                setSendEnable();
                editSend.setSelection(editSend.getText().length());
            }
        };
        btnPlus10.setOnClickListener(onClickPlusButtons);
        btnPlus100.setOnClickListener(onClickPlusButtons);
        btnPlus1000.setOnClickListener(onClickPlusButtons);
        btnTheWhole.setOnClickListener(onClickPlusButtons);

        // init btn contact
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(IconTransferActivity.this, ContactsActivity.class)
                        .putExtra("coinType", wallet.getCoinType())
                        .putExtra("tokenType", entry.getSymbol())
                        .putExtra("address", wallet.getAddress()), RC_CONTACTS);
            }
        });

        btnQRcodeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IconTransferActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                intent.putExtra(BarcodeCaptureActivity.PARAM_SCANTYPE, BarcodeCaptureActivity.ScanType.ICX_Address.name());

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });
        editData.setInputEnabled(false);
        btnData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data == null) {
                    DataTypeDialog typeDialog = new DataTypeDialog(IconTransferActivity.this);
                    typeDialog.setOnTypeListener(new DataTypeDialog.OnTypeListener() {
                        @Override
                        public void onSelect(IconEnterDataFragment.DataType type) {
                            data = new InputData();
                            data.setAddress(wallet.getAddress());
                            data.setBalance(balance);
                            data.setStepPrice(stepPriceLoop);
                            if (editSend.getText().isEmpty())
                                data.setAmount(BigInteger.ZERO);
                            else
                                data.setAmount(ConvertUtil.valueToBigInteger(editSend.getText(), 18));
                            data.setDataType(type);

                            startActivityForResult(new Intent(
                                            IconTransferActivity.this, IconEnterDataActivity.class
                                    ).putExtra(IconEnterDataActivity.DATA, data),
                                    RC_DATA
                            );

                            typeDialog.dismiss();
                        }
                    });
                    typeDialog.show();
                }
            }
        });

        btnViewData.setVisibility(View.GONE);
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data != null) {
                    startActivityForResult(new Intent(
                                    IconTransferActivity.this, IconEnterDataActivity.class
                            ).putExtra(IconEnterDataActivity.DATA, data),
                            RC_DATA
                    );
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSend();
            }
        });

        // set common edit event
        TTextInputLayout.OnKeyPreIme onKeyPreIme = new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                setSendEnable();
            }
        };

        TTextInputLayout.OnEditorAction onEditorAction = new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                setSendEnable();
            }
        };

        // init editSend
        editSend.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editSend.setOnKeyPreImeListener(onKeyPreIme);
        editSend.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (editSend.getText().length() > 0) {
                    validateSendAmount(editSend.getText());
                }
            }
        });
        editSend.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    String amount;

                    if (s.toString().startsWith(".")) {
                        editSend.setText("");
                    } else {

                        if (s.toString().indexOf(".") < 0) {
                            if (s.length() > 10) {
                                editSend.setText(s.subSequence(0, 10).toString());
                                editSend.setSelection(10);
                            }
                        } else {
                            String[] values = s.toString().split("\\.");

                            if (values.length == 2) {
                                String decimal = values[0];
                                String below = values[1];

                                if (decimal.length() > 10) {
                                    decimal = decimal.substring(0, 10);
                                    editSend.setText(decimal + "." + below);
                                    editSend.setSelection(editSend.getText().length());
                                } else if (below.length() > 18) {
                                    below = below.substring(0, 18);
                                    editSend.setText(decimal + "." + below);
                                    editSend.setSelection(editSend.getText().length());
                                }
                            }
                        }

                        amount = editSend.getText();
                        String strPrice = ICONexApp.EXCHANGE_TABLE.get(entry.getSymbol().toLowerCase() + "usd");
                        if (strPrice != null) {
                            Double transUSD = Double.parseDouble(amount)
                                    * Double.parseDouble(strPrice);
                            String strTransUSD = String.format("%,.2f", transUSD);

                            txtTransSend.setText(String.format("$ %s", strTransUSD));
                        } else
                            txtTransSend.setText(String.format("$ %s", "-"));

                        setRemain(amount);
                    }
                } else {
                    txtTransSend.setText(String.format("$ %s", "-"));
                    btnSend.setEnabled(false);

                    editSend.setError(false, null);
                    setRemain(editSend.getText());
                }
            }
        });
        editSend.setOnEditorActionListener(onEditorAction);

        // init editAddress
        editAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editAddress.setOnKeyPreImeListener(onKeyPreIme);
        editAddress.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (editAddress.getText().length() > 0) {
                    validateAddress(editAddress.getText());
                }
            }
        });
        editAddress.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.toString().startsWith("hx")) {
                        String tmp = s.toString().substring(2);
                        if (tmp.length() != 40)
                            btnSend.setEnabled(false);
                        else
                            setSendEnable();
                    } else {
                        btnSend.setEnabled(false);
                    }
                } else {
                    btnSend.setEnabled(false);
                    editAddress.setError(false, null);
                }
            }
        });
        editAddress.setOnEditorActionListener(onEditorAction);
    }

    private void addPlus(int plus) {
        BigInteger value;
        String amount = editSend.getText();
        if (amount.isEmpty()) {
            editSend.setText(Integer.toString(plus));
        } else {
            if (amount.indexOf(".") < 0) {
                BigInteger oldValue = new BigInteger(amount);
                value = oldValue.add(BigInteger.valueOf(plus));
                if (value.toString().length() > 10)
                    editSend.setText(oldValue.toString());
                else
                    editSend.setText(value.toString());
            } else {
                String[] total = amount.split("\\.");
                BigInteger oldValue = new BigInteger(total[0]);
                value = oldValue.add(BigInteger.valueOf(plus));
                if (value.toString().length() > 10)
                    editSend.setText(oldValue.toString() + "." + total[1]);
                else
                    editSend.setText(value.toString() + "." + total[1]);
            }
        }
    }

    private boolean validateAddress(String address) {
        if (address.isEmpty())
            return false;

        if (address.equals(wallet.getAddress())) {
            editAddress.setError(true, getString(R.string.errSameAddress));
            return false;
        }


        if (!(address.startsWith("hx") || address.startsWith("cx"))) {
            editAddress.setError(true, getString(R.string.errIncorrectICXAddr));
            return false;
        }

        address = address.substring(2);
        if (address.length() != 40) {
            editAddress.setError(true, getString(R.string.errIncorrectICXAddr));
            return false;
        }

        if (address.contains(" ")) {
            editAddress.setError(true, getString(R.string.errIncorrectICXAddr));
            return false;
        }

        editAddress.setSelection(editAddress.getText().length());
        editAddress.setError(false, null);

        return true;
    }

    private void setLimitPrice(String limit, String price) {
        try {
            String fLimit = new DecimalFormat("#,##0").format(Integer.parseInt(limit));
            txtStepLimit.setText(fLimit + " / " + price);
        } catch (Exception e) {
            txtStepLimit.setText("- / -");
        }

    }

    private void setBalance(BigInteger balance) {
        this.balance = balance;

        if (balance == null) {
            txtBalance.setText(MyConstants.NO_BALANCE);
            entry.setBalance(MyConstants.NO_BALANCE);
        } else {
            BigDecimal decimalBalance = new BigDecimal(ConvertUtil.getValue(balance, entry.getDefaultDec()));
            txtBalance.setText(DecimalFomatter.format(decimalBalance, entry.getDefaultDec()));
            entry.setBalance(balance.toString());
        }

        String strPrice = ICONexApp.EXCHANGE_TABLE.get(entry.getSymbol().toLowerCase() + "usd");
        if (strPrice != null) {
            Double balanceUSD = Double.parseDouble(ConvertUtil.getValue(balance, entry.getUserDec()))
                    * Double.parseDouble(strPrice);

            String strBalanceUSD = String.format(Locale.getDefault(), "%,.2f", balanceUSD);
            ((TextView) findViewById(R.id.txt_trans_balance))
                    .setText("$ " + strBalanceUSD);

            setRemain(editSend.getText());
        }
    }

    private void setRemain(String value) {
        BigInteger fee;
        BigInteger remain = null;
        BigInteger send;

        String strPrice = ICONexApp.EXCHANGE_TABLE.get(entry.getSymbol().toLowerCase() + "usd");
        String feePrice = ICONexApp.EXCHANGE_TABLE.get("icxusd");

        if (stepPriceICX != null && !strLimit.isEmpty())
            fee = stepPriceICX.multiply(new BigInteger(strLimit));
        else
            fee = BigInteger.ZERO;
        strFee = ConvertUtil.getValue(fee, 18);
//        txtEstimatedMaxFee.setText(DecimalFomatter.format(new BigDecimal(strFee)));
        BigDecimal bigFee = new BigDecimal(fee);
        txtEstimatedMaxFee.setText(bigFee.stripTrailingZeros().scaleByPowerOfTen(-18).toString());

        this.fee = ConvertUtil.getValue(fee, 18);

        boolean isNegative = false;

        if (value.isEmpty()) {

            if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                if (balance.compareTo(fee) < 0) {
                    remain = fee.subtract(balance);
                    isNegative = true;
                } else {
                    remain = balance.subtract(fee);
                    isNegative = false;
                }
            } else {
                remain = balance;
                isNegative = false;
            }
        } else {
            send = ConvertUtil.valueToBigInteger(value, entry.getDefaultDec());
            switch (balance.compareTo(send)) {
                case -1:
                    if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                        remain = (send.add(fee)).subtract(balance);
                        isNegative = true;
                    } else {
                        remain = send.subtract(balance);
                        isNegative = true;
                    }
                    break;
                case 0:
                    if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                        remain = fee;
                        isNegative = true;
                    } else {
                        remain = balance.subtract(send);
                        isNegative = false;
                    }
                    break;
                case 1:
                    if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                        BigInteger realBigSend = send.add(fee);
                        if (balance.compareTo(realBigSend) < 0) {
                            remain = realBigSend.subtract(balance);
                            isNegative = true;
                        } else {
                            remain = balance.subtract(realBigSend);
                            isNegative = false;
                        }
                    } else {
                        remain = balance.subtract(send);
                        isNegative = false;
                    }
                    break;
            }
        }

        if (stepPriceICX != null) {
            if (feePrice != null) {
                strTransFee = String.format(Locale.getDefault(), "%,.2f",
                        Double.parseDouble(strFee) * Double.parseDouble(feePrice));

                txtTransFee.setText("$ " + strTransFee);
            }
        }

        if (strPrice != null) {
            Double remainUSD = Double.parseDouble(ConvertUtil.getValue(remain, entry.getDefaultDec()))
                    * Double.parseDouble(strPrice);
            String strRemainUSD = String.format(Locale.getDefault(), "%,.2f", remainUSD);

            if (isNegative) {
                txtRemain = String.format(getString(R.string.txWithdraw), ConvertUtil.getValue(remain, entry.getDefaultDec()));
                txtTransRemain = String.format(getString(R.string.exchange_usd), String.format(getString(R.string.txWithdraw), strRemainUSD));
            } else {
                txtRemain = ConvertUtil.getValue(remain, entry.getDefaultDec());
                txtTransRemain = String.format(getString(R.string.exchange_usd), strRemainUSD);
            }
        }
    }

    private boolean validateSendAmount(String value) {
        if (value.isEmpty()) {
            //editSend.setError(true, getString(R.string.errNoSendAmount));
            return false;
        }

        if (entry.getType().equals(MyConstants.TYPE_COIN)) {
            BigInteger sendAmount = ConvertUtil.valueToBigInteger(value, 18);

            if (balance.compareTo(sendAmount) < 0) {
                editSend.setError(true, getString(R.string.errNotEnough));

                return false;
            }
        } else {
            BigInteger sendAmount = ConvertUtil.valueToBigInteger(value, entry.getDefaultDec());
//            if (sendAmount.equals(BigInteger.ZERO)) {
//                editSend.setError(true, getString(R.string.errNonZero));
//                return false;
//            }

            if (balance.compareTo(sendAmount) < 0) {
                editSend.setError(true, getString(R.string.errNotEnough));
                return false;
            }
        }

        editSend.setSelection(editSend.getText().length());
        editSend.setError(false, null);

        return true;
    }

    private void setSendEnable() {
        boolean amount = validateSendAmount(editSend.getText());
        boolean address = validateAddress(editAddress.getText());

        btnSend.setEnabled(amount && address);
    }

    private void getStepPrice() {
        int id = new Random().nextInt(999999) + 100000;
        try {
            retrofit2.Call<LCResponse> responseCall = LCClient.getStepPrice(id, wallet.getAddress());
            responseCall.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(retrofit2.Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        String result = response.body().getResult().getAsString();
                        try {
                            stepPriceLoop = ConvertUtil.hexStringToBigInt(result, 18);
                        } catch (Exception e) {

                        }
                        String icx = ConvertUtil.getValue(stepPriceLoop, 18);
                        String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                        stepPriceICX = ConvertUtil.valueToBigInteger(icx, 18);
                        txtStepICX = mIcx;
                        setLimitPrice(strLimit, txtStepICX);
                        String gloop = ConvertUtil.getValue(stepPriceLoop, 9);
                        String mGloop = gloop.indexOf(".") < 0 ? gloop : gloop.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtStepGloop = String.format(Locale.getDefault(), "ICX (%s Gloop)", mGloop);

                        String value = ConvertUtil.getValue(stepPriceLoop, 18);
                        String strExc = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);
                        if (strExc == null)
                            txtStepTrans = String.format(Locale.getDefault(), "-");
                        else
                            txtStepTrans = String.format(Locale.getDefault(), "%.2f", Double.parseDouble(value) * Double.parseDouble(strExc));

                    } else {
                        txtStepICX = "- ";
                        txtStepTrans = "- ";
                        setLimitPrice(strLimit, txtStepICX);
                    }

                    setRemain(editSend.getText());
                }

                @Override
                public void onFailure(retrofit2.Call<LCResponse> call, Throwable t) {

                }
            });
        } catch (Exception e) {

        }
    }

    private void getStepLimit() {
        PreferenceUtil preferenceUtil = new PreferenceUtil(this);
        try {
            Call<LCResponse> getStepCost = LCClient.getStepCost(1234, wallet.getAddress());
            getStepCost.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        int defaultLimit = Integer.decode(response.body().getResult().getAsJsonObject().get("default").getAsString());
                        int input = Integer.decode(response.body().getResult().getAsJsonObject().get("input").getAsString());
                        int contract = Integer.decode(response.body().getResult().getAsJsonObject().get("contractCall").getAsString());

                        IconTransferActivity.this.defaultLimit = new BigInteger(Integer.toString(defaultLimit));
                        inputPrice = new BigInteger(Integer.toString(input));
                        contractCall = new BigInteger(Integer.toString(contract));

                        preferenceUtil.setDefaultLimit(Integer.toString(defaultLimit));
                        preferenceUtil.setInputPrice(Integer.toString(input));
                        preferenceUtil.setContractCall(Integer.toString(contract));
                    } else {
                        defaultLimit = new BigInteger(preferenceUtil.getDefaultLimit());
                        inputPrice = new BigInteger(preferenceUtil.getInputPrice());
                        contractCall = new BigInteger(preferenceUtil.getContractCall());
                    }

                    if (entry.getType().equals(MyConstants.TYPE_TOKEN))
                        minStep = defaultLimit.multiply(BigInteger.valueOf(2));
                    else
                        minStep = defaultLimit;

                    strLimit = minStep.toString();
                    setRemain(editSend.getText());
                    setLimitPrice(strLimit, txtStepICX);
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {
                    defaultLimit = new BigInteger(preferenceUtil.getDefaultLimit());
                    inputPrice = new BigInteger(preferenceUtil.getInputPrice());
                    contractCall = new BigInteger(preferenceUtil.getContractCall());

                    if (entry.getType().equals(MyConstants.TYPE_TOKEN))
                        minStep = defaultLimit.add(contractCall).multiply(BigInteger.valueOf(2));
                    else
                        minStep = defaultLimit;

                    strLimit = minStep.toString();
                    setRemain(editSend.getText());
                    setLimitPrice(strLimit, txtStepICX);
                }
            });

            Call<LCResponse> getMaxStep = LCClient.getStepMaxLimit(2345, wallet.getAddress());
            getMaxStep.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        try {
                            maxStep = new BigInteger(Utils.remove0x(response.body().getResult().getAsString()), 16);
                            preferenceUtil.setMaxStep(maxStep.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else
                        maxStep = new BigInteger(preferenceUtil.getMaxStep());
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {
                    maxStep = new BigInteger(preferenceUtil.getMaxStep());
                }
            });
        } catch (Exception e) {

        }
    }

    protected String getTimeStamp() {
        long time = System.currentTimeMillis() * 1000;
        return "0x" + Long.toHexString(time);
    }

    private void onClickSend() {

        if (entry.getType().equals(MyConstants.TYPE_COIN)) {
            BigInteger necessaryIcx = ConvertUtil.valueToBigInteger(editSend.getText(), 18)
                    .add(ConvertUtil.valueToBigInteger(fee, 18));

            if (balance.compareTo(necessaryIcx) < 0) {
                MessageDialog messageDialog = new MessageDialog(this);
                messageDialog.setMessage(getString(R.string.errICXFee));
                messageDialog.show();
                return;
            }
        } else {
            WalletEntry own = wallet.getWalletEntries().get(0);
            BigInteger ownBalance = new BigInteger(own.getBalance());
            BigInteger feeInt = ConvertUtil.valueToBigInteger(fee, 18);

            if (ownBalance.compareTo(feeInt) < 0) {
                MessageDialog messageDialog = new MessageDialog(this);
                messageDialog.setMessage(getString(R.string.errICXFee));
                messageDialog.show();

                return;
            }
        }

        final ICONTxInfo txInfo = new ICONTxInfo(editAddress.getText(), editSend.getText(),
                txtEstimatedMaxFee.getText().toString(), Integer.toHexString(Integer.parseInt(strLimit)), entry.getSymbol());

        txInfo.setLimitPrice(txtStepLimit.getText().toString());
        txInfo.setTransFee(txtTransFee.getText().toString());
        TransactionSendDialog dialog = new TransactionSendDialog(this, txInfo);
        dialog.setOnDialogListener(new TransactionSendDialog.OnDialogListener() {
            @Override
            public void onOk() {
                String timestamp = getTimeStamp();
                Transaction tx;
                String nid;
                switch (ICONexApp.NETWORK.getNid().intValue()) {
                    case MyConstants.NETWORK_MAIN:
                        nid = "0x1";
                        break;

                    case MyConstants.NETWORK_TEST:
                        nid = "0x53";
                        break;

                    default:
                    case MyConstants.NETWORK_DEV:
                        nid = "0x3";
                        break;
                }

                if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                    tx = new Transaction.Builder(entry.getId(), nid, privateKey)
                            .from(entry.getAddress())
                            .to(editAddress.getText())
                            .stepLimit(txInfo.getStepLimit())
                            .timestamp(timestamp)
                            .nonce("0x1")
                            .build();

                    if (editSend.getText().isEmpty())
                        tx = tx.getBuilder().value(ConvertUtil.valueToHexString("0", 18)).build();
                    else
                        tx = tx.getBuilder().value(ConvertUtil.valueToHexString(editSend.getText(), 18)).build();

                    if (data != null) {
                        tx = tx.getBuilder().dataType(Constants.DATA_MESSAGE)
                                .data(data.getData())
                                .build();
                    }
                } else {
                    JsonObject data = new JsonObject();
                    data.addProperty("method", "transfer");
                    JsonObject params = new JsonObject();
                    params.addProperty("_to", editAddress.getText());
                    params.addProperty("_value", ConvertUtil.valueToHexString(editSend.getText(), entry.getDefaultDec()));
                    data.add("params", params);
                    tx = new Transaction.Builder(entry.getId(), nid, privateKey)
                            .from(entry.getAddress())
                            .to(entry.getContractAddress())
                            .stepLimit(txInfo.getStepLimit())
                            .timestamp(timestamp)
                            .nonce("0x1")
                            .dataType(Constants.DATA_CALL)
                            .data(data.toString())
                            .dataTo(editAddress.getText())
                            .build();
                }

                mService.requestICXTransaction(tx);
            }
        });
        dialog.show();
    }

    private class IcxGetBalance extends AsyncTask<Void, BigInteger, BigInteger> {
        @Override
        protected BigInteger doInBackground(Void... voids) {
            String url;
            switch (ICONexApp.NETWORK.getNid().intValue()) {
                case MyConstants.NETWORK_TEST:
                    url = ServiceConstants.TRUSTED_HOST_TEST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;

                default:
                    url = ServiceConstants.TRUSTED_HOST_MAIN + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            IconService iconService = new IconService(new HttpProvider(httpClient, url));

            Address address = new Address(entry.getAddress());
            BigInteger result;

            try {
                result = iconService.getBalance(address).execute();
            } catch (IOException e) {
                return null;
            }
            return result;
        }

        @Override
        protected void onPostExecute(BigInteger result) {
            super.onPostExecute(result);

            setBalance(result);
        }
    }

    private class TokenGetBalance extends AsyncTask<Void, BigInteger, BigInteger> {
        @Override
        protected BigInteger doInBackground(Void... voids) {
            String url;
            switch (ICONexApp.NETWORK.getNid().intValue()) {
                case MyConstants.NETWORK_TEST:
                    url = ServiceConstants.TRUSTED_HOST_TEST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;

                default:
                    url = ServiceConstants.TRUSTED_HOST_MAIN + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            IconService iconService = new IconService(new HttpProvider(httpClient, url));

            Address owner = new Address(entry.getAddress());
            Address score = new Address(entry.getContractAddress());

            RpcObject params = new RpcObject.Builder()
                    .put("_owner", new RpcValue(owner))
                    .build();

            foundation.icon.icx.Call<RpcItem> call = new foundation.icon.icx.Call.Builder()
                    .from(owner)
                    .to(score)
                    .method("balanceOf")
                    .params(params)
                    .build();

            RpcItem result;
            try {
                result = iconService.call(call).execute();
            } catch (IOException e) {
                return null;
            }

            return result.asInteger();
        }

        @Override
        protected void onPostExecute(BigInteger result) {
            super.onPostExecute(result);

            setBalance(result);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerExchangeCallback(mExchangeCallback);
            mService.registerRemCallback(mTransferCallback);

            if (mBound) {
                mService.requestExchangeList(CODE_EXCHANGE);
            } else {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    private NetworkService.ExchangeCallback mExchangeCallback = new NetworkService.ExchangeCallback() {
        @Override
        public void onReceiveExchangeList() {

        }

        @Override
        public void onReceiveError(String resCode) {
        }

        @Override
        public void onReceiveException(Throwable t) {
        }
    };

    private NetworkService.TransferCallback mTransferCallback = new NetworkService.TransferCallback() {
        @Override
        public void onReceiveTransactionResult(String id, String txHash) {

            CustomToast.makeText(getApplicationContext(), getString(R.string.msgDoneRequestTransfer), Toast.LENGTH_SHORT).show();
            finish();
        }

        @Override
        public void onReceiveError(String address, int code) {
            Toast.makeText(getApplicationContext(), getString(R.string.errTransferFailed), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReceiveException(Throwable t) {
            Toast.makeText(getApplicationContext(), getString(R.string.errTransferFailed), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onSetData(InputData data) {
        this.data = data;

        if (data.getDataType() == IconEnterDataFragment.DataType.UTF)
            editData.setText(new String(Hex.decode(Utils.remove0x(data.getData()))));
        else
            editData.setText(data.getData());
        btnViewData.setVisibility(View.VISIBLE);
        minStep = new BigInteger(Integer.toString(this.data.getStepCost()));
        strLimit = minStep.toString();
        setRemain(editSend.getText());
        setLimitPrice(strLimit, txtStepICX);

        setSendEnable();
    }

    @Override
    public void onDataCancel(InputData data) {
        if (data.getData() == null) {
            this.data = null;
            editData.setText("");
            btnViewData.setVisibility(View.GONE);
        } else {
            btnViewData.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDataDelete() {
        this.data = null;
        editData.setText("");
        btnViewData.setVisibility(View.GONE);

        minStep = defaultLimit;
        strLimit = minStep.toString();
        setRemain(editSend.getText());
        setLimitPrice(strLimit, txtStepICX);
    }

    private void showInfo() {
        startActivity(new Intent(this, AboutActivity.class)
                .putExtra(AboutActivity.PARAM_ABOUT_TITLE, getString(R.string.guidance))
                .putExtra(AboutActivity.PARAM_ABOUT_ITEM_LIST, new ArrayList<Parcelable>() {{
                    add(getHeadText(R.string.data));
                    add(new AboutActivity.AboutItem(AboutActivity.AboutItem.TYPE_PARAGRAPH, getString(R.string.msgIcxData)));

                    add(getHeadText(R.string.icxStepLimit));
                    add(new AboutActivity.AboutItem(AboutActivity.AboutItem.TYPE_PARAGRAPH, getString(R.string.msgStepLimit)));

                    add(getHeadText(R.string.icxStepPrice));
                    add(new AboutActivity.AboutItem(AboutActivity.AboutItem.TYPE_PARAGRAPH, getString(R.string.msgStepPrice)));

                    add(getHeadText(R.string.estimateFee));
                    add(new AboutActivity.AboutItem(AboutActivity.AboutItem.TYPE_PARAGRAPH, getString(R.string.msgICXEstimateFee)));
                }}));
    }

    private AboutActivity.AboutItem getHeadText(@StringRes int resId) {
        String headText = getString(resId);
        return new AboutActivity.AboutItem(AboutActivity.AboutItem.TYPE_HEAD, headText);
    }
}
