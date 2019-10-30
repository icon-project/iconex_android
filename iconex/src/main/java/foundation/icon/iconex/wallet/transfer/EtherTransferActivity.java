package foundation.icon.iconex.wallet.transfer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewCompat;

import com.fasterxml.jackson.databind.node.BigIntegerNode;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.jetbrains.annotations.NotNull;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.TransactionSendDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.DecimalFomatter;
import foundation.icon.iconex.view.AboutActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.contacts.ContactsActivity;
import foundation.icon.iconex.wallet.transfer.data.ErcTxInfo;
import foundation.icon.iconex.wallet.transfer.data.EthTxInfo;
import foundation.icon.iconex.wallet.transfer.data.TxInfo;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.TTextInputLayout;
import loopchain.icon.wallet.core.Constants;

public class EtherTransferActivity extends AppCompatActivity implements EtherDataEnterFragment.OnEnterDataLisnter{

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
    private ViewGroup layoutDataInfo;
    private TTextInputLayout editData;
    private Button btnViewData;

    // Fee UI
    private TextView lbEstimatedMaxFee;
    private TextView symbolEstimatedMaxFee;
    private TextView txtFee;
    private TextView txtTransFee;

    // Send Button UI
    private Button btnSend;

    private BigInteger balance;

    private String CODE_EXCHANGE = "ethusd";
    private String EXCHANGE_PRICE = "";

    private static final BigInteger ETH_MULTI = new BigInteger("1000000000");

    private String timestamp = null;
    private String txHash = null;

    private Wallet mWallet;
    private WalletEntry mWalletEntry;
    private String privKey;

    private int decimal = 18;

    private NetworkService mService;
    private boolean mBound = false;

    private static final int RC_CONTACTS = 9001;
    private static final int RC_BARCODE_CAPTURE = 9002;
    private static final int RC_DATA = 9003;

    private static final int DEFAULT_PRICE = 21;
    private static final int DEFAULT_GAS_LIMIT = 21000;
    private static final int CONTRACT_GAS_LIMIT = 55000;

    private String txtRemain;
    private String txtTransRemain;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerExchangeCallback(mExchangeCallback);
            mService.registerRemCallback(mRemittanceCallback);

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
            for (Map.Entry<String, String> entry : ICONexApp.EXCHANGE_TABLE.entrySet()) {
                if (entry.getKey().equals(CODE_EXCHANGE))
                    EXCHANGE_PRICE = entry.getValue();
            }
        }

        @Override
        public void onReceiveError(String resCode) {
        }

        @Override
        public void onReceiveException(Throwable t) {
        }
    };

    private NetworkService.TransferCallback mRemittanceCallback = new NetworkService.TransferCallback() {
        @Override
        public void onReceiveTransactionResult(String id, String txHash) {
            saveRecentSent(txHash);
        }

        @Override
        public void onReceiveError(String address, int code) {

        }

        @Override
        public void onReceiveException(Throwable t) {

        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ether_transfer_new);

        if (getIntent() != null) {
            mWallet = (Wallet) getIntent().getExtras().get("walletInfo");
            mWalletEntry = (WalletEntry) getIntent().getExtras().get("walletEntry");
            privKey = getIntent().getStringExtra("privateKey");
        }

        EXCHANGE_PRICE = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);

        // load appbar UI
        appbar = findViewById(R.id.appbar);

        // load available UI
        labelBalance = findViewById(R.id.txt_sub_balance);
        labelSymbol = findViewById(R.id.txt_sub_balance_symbol);
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

        // load Gas Control UI
        editLimit = findViewById(R.id.edit_limit);
        labelPrice = findViewById(R.id.lb_price);
        txtPrice = findViewById(R.id.txt_price);
        labelSlow = findViewById(R.id.lb_slow);
        labelFast = findViewById(R.id.lb_fast);
        seekPrice = findViewById(R.id.seek_price);

        // load Input Data UI
        layoutDataInfo = findViewById(R.id.layout_data_info);
        editData = findViewById(R.id.edit_data);
        btnViewData = findViewById(R.id.btn_view_data);

        // load Fee UI
        lbEstimatedMaxFee = findViewById(R.id.lb_estimated_max_fee);
        symbolEstimatedMaxFee = findViewById(R.id.symbol_estimated_max_fee);
        txtFee = findViewById(R.id.txt_fee);
        txtTransFee = findViewById(R.id.txt_trans_fee);

        // load Send Button UI
        btnSend = findViewById(R.id.btn_send);

        initView();
    }

    private void initView() {
        // set symbol
        appbar.setTitle(mWallet.getAlias());
        symbolEstimatedMaxFee.setText("(" + Constants.KS_COINTYPE_ETH + ")");
        String symbol = "(" + mWalletEntry.getSymbol() + ")";
        labelSymbol.setText(symbol);
        editSend.setAppendText(symbol.substring(1, symbol.length() -1));

        // init appbar
        appbar.setOnActionClickListener(new CustomActionBar.OnActionClickListener() {
            @Override
            public void onClickAction(CustomActionBar.ClickAction action) {
                switch (action) {
                    case btnStart: finish(); break;
                    case btnEnd: showInfo(); break;
                }
            }
        });

        // init send
        editSend.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editSend.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateSendAmount(editSend.getText(), true);
                setSendEnable(false);
            }
        });
        editSend.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (editSend.getText().length() > 0) {
                    validateSendAmount(editSend.getText(), true);
                }
                setSendEnable(false);
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
                            if (mWalletEntry.getDefaultDec() == 0) {
                                editSend.setText(s.subSequence(0, s.toString().indexOf(".")).toString());
                                editSend.setSelection(editSend.getText().toString().length());
//                                return;
                            }

                            String[] values = s.toString().split("\\.");

                            if (values.length == 2) {
                                String decimal = values[0];
                                String below = values[1];

                                if (decimal.length() > 10) {
                                    decimal = decimal.substring(0, 10);
                                    editSend.setText(decimal + "." + below);
                                    editSend.setSelection(editSend.getText().toString().length());
                                } else if (below.length() > mWalletEntry.getDefaultDec()) {
                                    below = below.substring(0, mWalletEntry.getDefaultDec());
                                    editSend.setText(decimal + "." + below);
                                    editSend.setSelection(editSend.getText().toString().length());
                                }
                            }
                        }

                        amount = editSend.getText().toString();
                        String strPrice = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);
                        if (strPrice != null) {
                            if (strPrice.equals(MyConstants.NO_EXCHANGE)) {
                                txtTransSend.setText("$ 0.00");
                            } else {
                                Double transUSD = Double.parseDouble(amount)
                                        * Double.parseDouble(strPrice);
                                String strTransUSD = String.format(Locale.getDefault(), "%,.2f", transUSD);

                                txtTransSend.setText("$ " + strTransUSD);
                            }
                        }
                        setRemain(amount);
                    }
                } else {
                    txtTransSend.setText("$ 0.00");
                    editSend.setError(false, null);
                }

                setRemain(editSend.getText());
            }
        });

        // edit address
        editAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editAddress.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateAddress(editAddress.getText(), true);
                setSendEnable(false);
            }
        });
        editAddress.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (editAddress.getText().length() > 0) {
                    validateAddress(editAddress.getText(), true);
                }
                setSendEnable(false);
            }
        });
        editAddress.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() <= 0 )
                    editAddress.setError(false, null);
            }
        });

        // edit limit
        editLimit.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN))
            editLimit.setText(String.valueOf(DEFAULT_GAS_LIMIT));
        else
            editLimit.setText(String.valueOf(CONTRACT_GAS_LIMIT));
        editLimit.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateGasLimit(true);
                setSendEnable(false);
            }
        });
        editLimit.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (editAddress.getText().length() > 0) {
                    validateGasLimit(true);
                }
                setSendEnable(false);
            }
        });
        editLimit.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.toString().startsWith(".")) {
                        editSend.setText("");
                    } else {
                        ((TextView) findViewById(R.id.txt_fee)).setText(DecimalFomatter.format(new BigDecimal(calculateFee())));
                        setRemain(calculateFee());
                    }
                } else {
                    editLimit.setError(false, null);
                    txtFee.setText(DecimalFomatter.format(new BigDecimal(calculateFee())));
                    setRemain(calculateFee());
                    setSendEnable(false);
                }
            }
        });
        editLimit.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                validateGasLimit(true);
                setSendEnable(false);
            }
        });

        // edit Data
        editData.setInputEnabled(false);
        btnViewData.setVisibility(View.GONE);
        editData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editData.getText().length() <= 0) {
                    startActivityForResult(new Intent(
                                    EtherTransferActivity.this, EtherEnterDataActivity.class
                            ).putExtra(EtherEnterDataActivity.DATA, editData.getText()),
                            RC_DATA
                    );
                }
            }
        });
        btnViewData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editData.getText().length() > 0) {
                    startActivityForResult(new Intent(
                                    EtherTransferActivity.this, EtherEnterDataActivity.class
                            ).putExtra(EtherEnterDataActivity.DATA, editData.getText()),
                            RC_DATA
                    );
                }
            }
        });
        editData.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateData(true);
                setSendEnable(false);
            }
        });
        editData.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                validateData(true);
                setSendEnable(false);
            }
        });
        editData.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    editLimit.setText(String.valueOf(CONTRACT_GAS_LIMIT));
                    setRemain(editSend.getText());
                } else {
                    editData.setError(false, null);
                    editLimit.setText(String.valueOf(DEFAULT_GAS_LIMIT));
                    setRemain(editSend.getText());
                }
                editLimit.setError(false, null);
            }
        });
        editData.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                validateData(true);
                setSendEnable(false);
            }
        });

        // set seek bar
        labelSlow.setText(1 + " (" +getString(R.string.slow) +")");
        labelFast.setText("(" +getString(R.string.fast) +") " + 99);
        seekPrice.setProgress(DEFAULT_PRICE);
        seekPrice.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if ((progress % 10) < 3) {
                        progress = (progress / 10) * 10;
                    } else {
                        if ((10 - (progress % 10)) < 3) {
                            progress = ((progress / 10) + 1) * 10;
                        }
                    }
                }

                if (progress == 0)
                    progress = 1;

                if (progress == 100)
                    progress = 99;

                seekPrice.setProgress(progress);
                txtPrice.setText(String.valueOf(progress));

                txtFee.setText(DecimalFomatter.format(new BigDecimal(calculateFee())));
                setRemain(calculateFee());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        txtPrice.setText(String.valueOf(seekPrice.getProgress()));

        // set plus button
        View.OnClickListener onClickPlusButtons = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_plus_10: addPlus(10); break;
                    case R.id.btn_plus_100: addPlus(100); break;
                    case R.id.btn_plus_1000: addPlus(1000); break;
                    case R.id.btn_plus_all:
                        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                            BigInteger balance = new BigInteger(mWalletEntry.getBalance());
                            BigInteger bigFee = ConvertUtil.valueToBigInteger(calculateFee(), 18);

                            if (balance.compareTo(bigFee) == -1) {
                                editSend.setText("0");
                            } else {
                                editSend.setText(ConvertUtil.getValue(balance.subtract(bigFee), mWalletEntry.getDefaultDec()));
                            }
                        } else {
                            editSend.setText(ConvertUtil.getValue(new BigInteger(mWalletEntry.getBalance()), mWalletEntry.getDefaultDec()));

                        }
                        break;
                }
                validateSendAmount(editSend.getText(), true);
                setSendEnable(false);
                editSend.setSelection(editSend.getText().length());
            }
        };
        btnPlus10.setOnClickListener(onClickPlusButtons);
        btnPlus100.setOnClickListener(onClickPlusButtons);
        btnPlus1000.setOnClickListener(onClickPlusButtons);
        btnTheWhole.setOnClickListener(onClickPlusButtons);

        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(EtherTransferActivity.this, ContactsActivity.class)
                        .putExtra("coinType", mWallet.getCoinType())
                        .putExtra("tokenType", mWalletEntry.getSymbol())
                        .putExtra("address", mWallet.getAddress()), RC_CONTACTS);
            }
        });

        btnQRcodeScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EtherTransferActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                intent.putExtra(BarcodeCaptureActivity.PARAM_SCANTYPE, BarcodeCaptureActivity.ScanType.ETH_Address.name());

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                BigInteger fee = ConvertUtil.valueToBigInteger(calculateFee(), 18);
                boolean feeError = false;

                if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                    BigInteger canICX = balance.subtract(fee);
                    if (canICX.compareTo(BigInteger.ZERO) < 0) {
                        feeError = true;
                    }
                } else {
                    WalletEntry own = mWallet.getWalletEntries().get(0);
                    BigInteger ownBalance = new BigInteger(own.getBalance());
                    if (ownBalance.compareTo(fee) < 0) {
                        feeError = true;
                    }
                }

                if (feeError) {
                    MessageDialog messageDialog = new MessageDialog(EtherTransferActivity.this);
                    messageDialog.setTitleText(getString(R.string.errETHFee));
                    messageDialog.show();
                    return;
                }

                TransactionSendDialog dialog = new TransactionSendDialog(EtherTransferActivity.this, makeTxInfo());
                dialog.setOnDialogListener(new TransactionSendDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                            mService.requestETHTransaction(Integer.toString(mWalletEntry.getId()), txtPrice.getText().toString(),
                                    editLimit.getText(), editAddress.getText(), editData.getText(),
                                    editSend.getText(), privKey);
                        } else {
                            mService.requestTokenTransfer(Integer.toString(mWalletEntry.getId()), txtPrice.getText().toString(),
                                    editLimit.getText(), mWalletEntry.getContractAddress(),
                                    editAddress.getText(), editSend.getText(),
                                    Integer.toString(mWalletEntry.getDefaultDec()), privKey);
                        }
                        timestamp = getTimeStamp();
                        Toast.makeText(getApplicationContext(), getString(R.string.msgDoneRequestTransfer), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                dialog.show();
            }
        });

        if (mWalletEntry.getType().equals(MyConstants.TYPE_TOKEN)) {
            layoutDataInfo.setVisibility(View.GONE);
            decimal = mWalletEntry.getDefaultDec();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        CODE_EXCHANGE = mWalletEntry.getSymbol().toLowerCase() + MyConstants.EXCHANGE_USD.toLowerCase();

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
            balance = new BigInteger(mWalletEntry.getBalance());
        } catch (Exception e) {
            balance = BigInteger.ZERO;
        }
        BigDecimal decimalBalance = new BigDecimal(ConvertUtil.getValue(balance, mWalletEntry.getDefaultDec()));

        ((TextView) findViewById(R.id.txt_balance)).setText(DecimalFomatter.format(decimalBalance, mWalletEntry.getDefaultDec()));
        TextViewCompat.setAutoSizeTextTypeWithDefaults(findViewById(R.id.txt_balance), TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);
        ((TextView) findViewById(R.id.txt_fee)).setText(DecimalFomatter.format(new BigDecimal(calculateFee())));
        String strPrice = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);
        if (strPrice != null) {
            if (strPrice.equals(MyConstants.NO_EXCHANGE)) {
                ((TextView) findViewById(R.id.txt_trans_balance))
                        .setText("$ -");

                txtTransSend.setText("$ 0.00");
            } else {
                Double balanceUSD = Double.parseDouble(ConvertUtil.getValue(balance, mWalletEntry.getDefaultDec()))
                        * Double.parseDouble(strPrice);

                String strBalanceUSD = String.format(Locale.getDefault(), "%,.2f", balanceUSD);
                ((TextView) findViewById(R.id.txt_trans_balance))
                        .setText("$ " + strBalanceUSD);
            }

            setRemain(editSend.getText().toString());
        }
    }

    private void setRemain(String value) {
        BigInteger bigFee = ConvertUtil.valueToBigInteger(calculateFee(), 18);
        BigInteger bigRemain = null;
        BigInteger bigSend;

        String strPrice = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);

        boolean isNegative = false;

        if (editSend.getText().toString().isEmpty()) {
            if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                if (balance.compareTo(bigFee) < 0) {
                    bigRemain = bigFee.subtract(balance);
                    isNegative = true;
                } else {
                    bigRemain = balance.subtract(bigFee);
                    isNegative = false;
                }
            } else {
                bigRemain = balance;
                isNegative = false;
            }
        } else {
            bigSend = ConvertUtil.valueToBigInteger(value, mWalletEntry.getDefaultDec());
            switch (balance.compareTo(bigSend)) {
                case -1:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        bigRemain = (bigSend.add(bigFee)).subtract(balance);
                        isNegative = true;
                    } else {
                        bigRemain = bigSend.subtract(balance);
                        isNegative = true;
                    }
                    break;
                case 0:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        bigRemain = bigFee;
                        isNegative = true;
                    } else {
                        bigRemain = balance.subtract(bigSend);
                        isNegative = false;
                    }
                    break;
                case 1:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        BigInteger realBigSend = bigSend.add(bigFee);
                        if (balance.compareTo(realBigSend) < 0) {
                            bigRemain = realBigSend.subtract(balance);
                            isNegative = true;
                        } else {
                            bigRemain = balance.subtract(realBigSend);
                            isNegative = false;
                        }
                    } else {
                        bigRemain = balance.subtract(bigSend);
                        isNegative = false;
                    }
                    break;
            }
        }

        String remainValue = ConvertUtil.getValue(bigRemain, mWalletEntry.getDefaultDec());
        Double remainUSD = Double.parseDouble(remainValue) * Double.parseDouble(strPrice);

        if (strPrice != null) {
            if (strPrice.equals(MyConstants.NO_EXCHANGE)) {
                if (isNegative)
                    txtRemain = String.format(Locale.getDefault(), "- %s", remainValue);
                else
                    txtRemain = remainValue;

                txtTransRemain= String.format("$ %s", MyConstants.NO_BALANCE) ;

            } else {
                String strRemainUSD = String.format(Locale.getDefault(), "%,.2f", remainUSD);

                if (isNegative) {
                    txtRemain = String.format(Locale.getDefault(), "- %s", remainValue);
                    txtTransRemain = String.format(Locale.getDefault(), "- %s",
                            getString(R.string.exchange_usd, strRemainUSD));
                } else {
                    txtRemain = remainValue;
                    txtTransRemain = String.format("$ %s", strRemainUSD);
                }

                Double feeUSD = Double.parseDouble(calculateFee())
                        * Double.parseDouble(strPrice);
                String strFeeUSD = String.format(Locale.getDefault(), "%,.2f", feeUSD);
                ((TextView) findViewById(R.id.txt_trans_fee))
                        .setText(String.format("$ %s", strFeeUSD));
            }
        } else {
            if (isNegative)
                txtRemain = String.format(Locale.getDefault(), "- %s", remainValue);
            else
                txtRemain = remainValue;

            txtTransRemain = String.format("$ %s", MyConstants.NO_BALANCE);
        }
    }

    private void addPlus(int plus) {
        BigInteger value;
        String amount = editSend.getText().toString();
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

    private boolean validateSendAmount(String value, boolean errShow) {
        if (value.isEmpty()) {
            editSend.setError(false, null);
            return false;
        }

        BigInteger sendAmount = ConvertUtil.valueToBigInteger(value, mWalletEntry.getDefaultDec());
        BigInteger fee = ConvertUtil.valueToBigInteger(calculateFee(), 18);

        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
            if (balance.compareTo(sendAmount) < 0) {
                if (errShow) editSend.setError(true, getString(R.string.errNotEnough));
                return false;
            }
        } else {

            if (balance.compareTo(sendAmount) < 0) {
                if (errShow) editSend.setError(true, getString(R.string.errNotEnough));

                return false;
            }
        }

        editSend.setError(false, null);
        return true;
    }

    private boolean validateAddress(String address, boolean errShow) {
        if (address.isEmpty()) {
            return false;
        }

        if (address.equals(mWallet.getAddress())) {
            if (errShow) editAddress.setError(true, getString(R.string.errSameAddress));
            return false;
        }

        if (address.startsWith("0x")) {
            address = address.substring(2);
            if (address.length() != 40) {
                if (errShow) editAddress.setError(true, getString(R.string.errIncorrectETHAddr));

                return false;
            }
        } else if (address.contains(" ")) {
            if (errShow) editAddress.setError(true, getString(R.string.errIncorrectETHAddr));

            return false;
        } else {
            if (errShow) editAddress.setError(true, getString(R.string.errIncorrectETHAddr));

            return false;
        }

        editAddress.setError(false, null);
        editAddress.setSelection(editAddress.getText().toString().length());
        return true;
    }

    private boolean validateGasLimit(boolean errShow) {
        if (editLimit.getText().isEmpty()) {
            if (errShow) editLimit.setError(true, getString(R.string.errGasLimitEmpty));
            return false;
        } if (Integer.parseInt(editLimit.getText()) < 21000) {
            if (errShow) editLimit.setError(true, getString(R.string.errEtherGasLimit));
            return false;
        } else {
            editLimit.setError(false, null);
            return true;
        }
    }

    private boolean validateData(boolean errShow) {
        if (layoutDataInfo.getVisibility() == View.GONE)
            return true;

        if (editData.getText().isEmpty())
            return true;
        else {
            if (!editData.getText().startsWith("0x")) {
                if (errShow) editData.setError(true, getString(R.string.errInvalidData));
                return false;
            } else {
                try {
                    String data = editData.getText().toString().substring(2);
                    byte[] temp = Hex.decode(data);
                } catch (Exception e) {
                    if (errShow) editData.setError(true, getString(R.string.errInvalidData));

                    return false;
                }

                editData.setError(false, null);
                return true;
            }
        }
    }

    private String calculateFee() {
        BigInteger price = new BigInteger(txtPrice.getText().toString());
        BigInteger limit;
        if (editLimit.getText().isEmpty()
                || editLimit.getText().trim().length() == 0)
            limit = BigInteger.ZERO;
        else
            limit = new BigDecimal(editLimit.getText()).toBigInteger();

        return Convert.fromWei(limit.multiply(price).multiply(ETH_MULTI).toString(), Convert.Unit.ETHER).toPlainString();
    }

    private void setSendEnable(boolean errShow) {
        if (validateSendAmount(editSend.getText(), errShow)
                && validateAddress(editAddress.getText(), errShow)
                && validateGasLimit(errShow)
                && validateData(errShow)) {
            btnSend.setEnabled(true);
        } else {
            btnSend.setEnabled(false);
        }
    }

    private TxInfo makeTxInfo() {

        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
            BigInteger value = ConvertUtil.valueToBigInteger(editSend.getText().toString(), mWalletEntry.getDefaultDec());
            EthTxInfo txInfo = new EthTxInfo(editAddress.getText().toString(), ConvertUtil.getValue(value, 18), calculateFee());
            txInfo.setFromAddress(mWallet.getAddress());
            txInfo.setPrice(txtPrice.getText().toString());
            txInfo.setLimit(editLimit.getText().toString());
            txInfo.setData(editData.getText().toString());
            txInfo.setTransFee(txtTransFee.getText().toString());

            return txInfo;
        } else {
            BigInteger value = ConvertUtil.valueToBigInteger(editSend.getText().toString(), mWalletEntry.getDefaultDec());
            ErcTxInfo txInfo = new ErcTxInfo(editAddress.getText().toString(), ConvertUtil.getValue(value, 18), calculateFee());
            txInfo.setFromAddress(mWallet.getAddress());
            txInfo.setPrice(txtPrice.getText().toString());
            txInfo.setLimit(editLimit.getText().toString());

            ECKeyPair keyPair = ECKeyPair.create(Hex.decode(privKey));
            org.web3j.crypto.Credentials credentials = org.web3j.crypto.Credentials.create(keyPair);
            txInfo.setCredentials(credentials);
            txInfo.setContract(mWalletEntry.getContractAddress());
            txInfo.setDecimals(mWalletEntry.getDefaultDec());
            txInfo.setSymbol(mWalletEntry.getUserSymbol());
            txInfo.setTransFee(txtTransFee.getText().toString());

            return txInfo;
        }
    }

    protected String getTimeStamp() {
        long time = System.currentTimeMillis() * 1000;
        return Long.toString(time);
    }

    @Nullable
    private String findContactName(String address) {
        for (int i = 0; i < ICONexApp.wallets.size(); i++) {
            if (address.equals(MyConstants.PREFIX_HEX + ICONexApp.wallets.get(i).getAddress()))
                return ICONexApp.wallets.get(i).getAlias();
        }

        for (int j = 0; j < ICONexApp.ETHContacts.size(); j++) {
            if (address.equals(ICONexApp.ETHContacts.get(j).getAddress()))
                return ICONexApp.ETHContacts.get(j).getName();
        }

        return null;
    }

    private String checkAddress(String address) {
        if (!address.startsWith("0x"))
            return "0x" + address;
        else
            return address;
    }

    private void saveRecentSent(String txHash) {
        String contactName = findContactName(editAddress.getText().toString());
        if (contactName == null)
            contactName = "";

        RealmUtil.addRecentSend(MyConstants.Coin.ETH, txHash, contactName,
                mWalletEntry.getAddress(), timestamp, editSend.getText(), mWalletEntry.getSymbol());
        RealmUtil.loadRecents();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_CONTACTS) {
            if (resultCode == ContactsActivity.CODE_RESULT) {
                String address = data.getStringExtra("address");
                editAddress.setText(checkAddress(address));

                validateAddress(editAddress.getText(), true);
                setSendEnable(false);
            }
        } else if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    editAddress.setText(checkAddress(barcode.displayValue));
                    validateAddress(editAddress.getText(), true);
                    btnSend.setEnabled(false);
                } else {
                }
            }
        } else if (requestCode == RC_DATA) {
            EtherEnterDataActivity.activityResultHelper(resultCode, data, this);
        }
    }

    @Override
    public void onSetData(String data) {
        editData.setText(data);
        btnViewData.setVisibility(View.VISIBLE);
        validateData(true);
        setSendEnable(false);
    }

    @Override
    public void onDataCancel() {
        // nothing
    }

    @Override
    public void onDataDelete() {
        editData.setText("");
        btnViewData.setVisibility(View.GONE);
    }

    private void showInfo() {
        startActivity(new Intent(this, AboutActivity.class)
                .putExtra(AboutActivity.PARAM_ABOUT_TITLE, getString(R.string.guidance))
                .putExtra(AboutActivity.PARAM_ABOUT_ITEM_LIST, new ArrayList<Parcelable>() {{
                    add(getHeadText(R.string.data));
                    addAll(getParagraph(R.string.msgEthData));

                    add(getHeadText(R.string.gasLimit));
                    addAll(getParagraph(R.string.msgEthGasLimit));

                    add(getHeadText(R.string.gasPrice));
                    addAll(getParagraph(R.string.msgEthGasPrice));

                    add(getHeadText(R.string.estimateFee));
                    addAll(getParagraph(R.string.msgEthEstimateFee));
                }}));
    }

    private AboutActivity.AboutItem getHeadText(@StringRes int resId) {
        String headText = getString(resId);
        return new AboutActivity.AboutItem(AboutActivity.AboutItem.TYPE_HEAD, headText);
    }

    private List<AboutActivity.AboutItem> getParagraph(@StringRes int resId) {
        String paragraphText = getString(resId);
        String[] split = paragraphText.replace("\n", "").split("다\\.");
        return new ArrayList<AboutActivity.AboutItem>() {{
            for(String str : split) {
                add(new AboutActivity.AboutItem(AboutActivity.AboutItem.TYPE_PARAGRAPH, str + "다."));
            }
        }};
    }
}
