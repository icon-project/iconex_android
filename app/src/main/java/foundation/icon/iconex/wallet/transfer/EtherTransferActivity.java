package foundation.icon.iconex.wallet.transfer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.control.WalletEntry;
import foundation.icon.iconex.control.WalletInfo;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.SendConfirmDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.contacts.ContactsActivity;
import foundation.icon.iconex.wallet.transfer.data.ErcTxInfo;
import foundation.icon.iconex.wallet.transfer.data.EthTxInfo;
import foundation.icon.iconex.wallet.transfer.data.TxInfo;
import foundation.icon.iconex.widgets.MyEditText;

public class EtherTransferActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = ICONTransferActivity.class.getSimpleName();

    private Button btnBack;
    private MyEditText editSend, editAddress;
    private View lineSend, lineAddress;
    private TextView txtSendWarning, txtAddrWarning;
    private Button btnDelAmount, btnDelAddr;
    private TextView txtTransSend;
    private Button btnPlus10, btnPlus100, btnPlus1000, btnTheWhole;
    private Button btnContacts, btnScan;

    private TextView txtFee, txtTransFee;
    private TextView txtRemain, txtTransRemain;

    private ViewGroup infoLimit, infoPrice, infoData, infoFee;

    private MyEditText editLimit;
    private View lineLimit;
    private TextView txtLimitWarning;
    private Button btnDelLimit;

    private ViewGroup layoutDataInfo;
    private ImageView dataInput;
    private ViewGroup layoutData;
    private MyEditText editData;
    private View lineData;
    private TextView txtDataWarning;
    private Button btnDelData;

    private TextView txtPrice;
    private SeekBar seekPrice;
    private ViewGroup priceDown, priceUp;

    private Button btnSend;

    private String balance;
    private BigInteger eth;

    private String CODE_EXCHANGE = "ethusd";
    private String EXCHANGE_PRICE = "";

    private static final BigInteger ETH_MULTI = new BigInteger("1000000000");

    private String timestamp = null;
    private String txHash = null;

    private WalletInfo mWalletInfo;
    private WalletEntry mWalletEntry;
    private String privKey;

    private int decimal = 18;

    private NetworkService mService;
    private boolean mBound = false;

    private static final int RC_CONTACTS = 9001;
    private static final int RC_BARCODE_CAPTURE = 9002;

    private static final int DEFAULT_PRICE = 21;
    private static final int DEFAULT_COIN_LIMIT = 21000;
    private static final int DEFAULT_TOKEN_LIMIT = 55000;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NetworkService.NetworkServiceBinder binder = (NetworkService.NetworkServiceBinder) service;
            mService = binder.getService();
            mService.registerExchangeCallback(mExchangeCallback);
//            mService.registerRemCallback(mRemittanceCallback);

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

    private OnKeyPreImeListener onKeyPreImeListener = new OnKeyPreImeListener() {
        @Override
        public void onBackPressed() {
            setSendEnable();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ether_transfer);

        if (getIntent() != null) {
            mWalletInfo = (WalletInfo) getIntent().getExtras().get("walletInfo");
            mWalletEntry = (WalletEntry) getIntent().getExtras().get("walletEntry");
            privKey = getIntent().getStringExtra("privateKey");
        }

        EXCHANGE_PRICE = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);

        ((TextView) findViewById(R.id.txt_title)).setText(mWalletInfo.getAlias());
        ((TextView) findViewById(R.id.txt_possession))
                .setText(String.format(getString(R.string.possessionAmount), mWalletEntry.getSymbol()));
        ((TextView) findViewById(R.id.txt_send_amount))
                .setText(String.format(getString(R.string.sendAmount), mWalletEntry.getSymbol()));
        ((TextView) findViewById(R.id.txt_send_fee)).setText(getString(R.string.ethEstiFee));
        ((TextView) findViewById(R.id.txt_remain_amount))
                .setText(String.format(getString(R.string.ethEstiRemain), mWalletEntry.getSymbol()));
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        editSend = findViewById(R.id.edit_send_amount);
        editSend.setOnKeyPreImeListener(onKeyPreImeListener);
        editSend.setLongClickable(false);
        editSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        editSend.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineSend.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineSend.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    if (editSend.getText().toString().length() > 0) {
                        validateSendAmount(editSend.getText().toString());
                    }
                }
            }
        });
        editSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {

                    btnDelAmount.setVisibility(View.VISIBLE);

                    String amount;

                    if (s.toString().startsWith(".")) {
                        editSend.setText("");
                    } else {

                        if (s.toString().indexOf(".") < 0) {
                            if (s.length() > 10) {
                                editSend.setText(s.subSequence(0, 10));
                                editSend.setSelection(10);
                            }
                        } else {
                            if (mWalletEntry.getDefaultDec() == 0) {
                                editSend.setText(s.subSequence(0, s.toString().indexOf(".")));
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
                                txtTransSend.setText(String.format(getString(R.string.exchange_usd), MyConstants.NO_BALANCE));
                            } else {
                                Double transUSD = Double.parseDouble(amount)
                                        * Double.parseDouble(strPrice);
                                String strTransUSD = String.format(Locale.getDefault(), "%,.2f", transUSD);

                                txtTransSend.setText(String.format(getString(R.string.exchange_usd), strTransUSD));
                            }
                        }
                        setRemain(amount);
                    }
                } else {
                    btnDelAmount.setVisibility(View.INVISIBLE);
                    txtTransSend.setText(String.format(getString(R.string.exchange_usd), MyConstants.NO_BALANCE));

                    txtSendWarning.setVisibility(View.GONE);
                    if (editSend.isFocused())
                        lineSend.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineSend.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editSend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    editAddress.requestFocus();
                }
                return false;
            }
        });

        editAddress = findViewById(R.id.edit_to_address);
        editAddress.setOnKeyPreImeListener(onKeyPreImeListener);
        editAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineAddress.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineAddress.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    if (editAddress.getText().toString().length() > 0)
                        validateAddress(editAddress.getText().toString());
                }
            }
        });
        editAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    btnDelAddr.setVisibility(View.VISIBLE);
                else {
                    btnDelAddr.setVisibility(View.INVISIBLE);

                    txtAddrWarning.setVisibility(View.GONE);
                    if (editAddress.isFocused())
                        lineAddress.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineAddress.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editLimit = findViewById(R.id.edit_limit);
        editLimit.setLongClickable(false);
        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN))
            editLimit.setText(String.valueOf(DEFAULT_COIN_LIMIT));
        else
            editLimit.setText(String.valueOf(DEFAULT_TOKEN_LIMIT));
        editLimit.setOnKeyPreImeListener(onKeyPreImeListener);
        editLimit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    validateGasLimit();
                }
            }
        });
        editLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnDelLimit.setVisibility(View.VISIBLE);
                    if (s.toString().startsWith(".")) {
                        editSend.setText("");
                    } else {
                        ((TextView) findViewById(R.id.txt_fee)).setText(calculateFee());
                        setRemain(calculateFee());
                    }
                } else {
                    btnDelLimit.setVisibility(View.INVISIBLE);

                    txtLimitWarning.setVisibility(View.GONE);
                    if (editLimit.isFocused())
                        lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editLimit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    boolean result = validateSendAmount(editSend.getText().toString())
                            && validateAddress(editAddress.getText().toString())
                            && validateGasLimit()
                            && validateData();
                    if (result) {
                        btnSend.setEnabled(true);
                    } else {
                        btnSend.setEnabled(false);
                    }
                }
                return false;
            }
        });

        editData = findViewById(R.id.edit_data);
        editData.setOnKeyPreImeListener(onKeyPreImeListener);
        editData.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineData.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineData.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    validateData();
                }
            }
        });
        editData.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0)
                    btnDelData.setVisibility(View.VISIBLE);
                else {
                    btnDelData.setVisibility(View.INVISIBLE);
                    txtDataWarning.setVisibility(View.GONE);
                    if (editData.isFocused())
                        lineData.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineData.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editData.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    boolean result = validateSendAmount(editSend.getText().toString())
                            && validateAddress(editAddress.getText().toString())
                            && validateGasLimit()
                            && validateData();
                    if (result) {
                        btnSend.setEnabled(true);
                    } else {
                        btnSend.setEnabled(false);
                    }
                }
                return false;
            }
        });

        lineSend = findViewById(R.id.line_send_amount);
        lineAddress = findViewById(R.id.line_to_address);
        lineLimit = findViewById(R.id.line_limit);
        lineData = findViewById(R.id.line_data);

        seekPrice = findViewById(R.id.seek_price);
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

                ((TextView) findViewById(R.id.txt_fee)).setText(calculateFee());
                setRemain(calculateFee());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setSendEnable();
            }
        });

        infoLimit = findViewById(R.id.info_limit);
        infoLimit.setOnClickListener(this);
        infoPrice = findViewById(R.id.info_price);
        infoPrice.setOnClickListener(this);
        infoData = findViewById(R.id.info_data);
        infoData.setOnClickListener(this);
        infoFee = findViewById(R.id.info_fee);
        infoFee.setOnClickListener(this);

        priceDown = findViewById(R.id.price_down);
        priceDown.setOnClickListener(this);
        priceUp = findViewById(R.id.price_up);
        priceUp.setOnClickListener(this);

        txtSendWarning = findViewById(R.id.txt_send_warning);
        txtAddrWarning = findViewById(R.id.txt_address_warning);
        txtLimitWarning = findViewById(R.id.txt_limit_warning);
        txtDataWarning = findViewById(R.id.txt_data_warning);

        btnDelAmount = findViewById(R.id.del_amount);
        btnDelAmount.setOnClickListener(this);
        btnDelAddr = findViewById(R.id.del_address);
        btnDelAddr.setOnClickListener(this);
        btnDelLimit = findViewById(R.id.del_limit);
        btnDelLimit.setOnClickListener(this);
        btnDelData = findViewById(R.id.del_data);
        btnDelData.setOnClickListener(this);

        txtRemain = findViewById(R.id.txt_remain);
        txtTransRemain = findViewById(R.id.txt_trans_remain);
        txtPrice = findViewById(R.id.txt_price);
        txtPrice.setText(String.valueOf(seekPrice.getProgress()));

        txtTransSend = findViewById(R.id.txt_trans_send_amount);


        btnPlus10 = findViewById(R.id.btn_plus_10);
        btnPlus10.setOnClickListener(this);
        btnPlus100 = findViewById(R.id.btn_plus_100);
        btnPlus100.setOnClickListener(this);
        btnPlus1000 = findViewById(R.id.btn_plus_1000);
        btnPlus1000.setOnClickListener(this);
        btnTheWhole = findViewById(R.id.btn_plus_all);
        btnTheWhole.setOnClickListener(this);

        btnContacts = findViewById(R.id.btn_contacts);
        btnContacts.setOnClickListener(this);
        btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(this);

        layoutDataInfo = findViewById(R.id.layout_data_info);
        dataInput = findViewById(R.id.arrow);
        dataInput.setOnClickListener(this);
        dataInput.setSelected(false);
        layoutData = findViewById(R.id.layout_data);
        layoutData.setVisibility(View.GONE);

        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);

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

        eth = new BigInteger(mWalletEntry.getBalance());

        ((TextView) findViewById(R.id.txt_balance)).setText(ConvertUtil.getValue(eth, mWalletEntry.getDefaultDec()));
        ((TextView) findViewById(R.id.txt_fee)).setText(calculateFee());
        String strPrice = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);
        if (strPrice != null) {
            if (strPrice.equals(MyConstants.NO_EXCHANGE)) {
                ((TextView) findViewById(R.id.txt_trans_balance))
                        .setText(String.format(getString(R.string.exchange_usd), MyConstants.NO_BALANCE));

                txtTransSend.setText(String.format(getString(R.string.exchange_usd), MyConstants.NO_BALANCE));
            } else {
                Double balanceUSD = Double.parseDouble(ConvertUtil.getValue(eth, mWalletEntry.getDefaultDec()))
                        * Double.parseDouble(strPrice);

                String strBalanceUSD = String.format(Locale.getDefault(), "%,.2f", balanceUSD);
                ((TextView) findViewById(R.id.txt_trans_balance))
                        .setText(String.format(getString(R.string.exchange_usd), strBalanceUSD));
            }

            Double feeUSD = Double.parseDouble(calculateFee())
                    * Double.parseDouble(strPrice);
            String strFeeUSD = String.format(Locale.getDefault(), "%,.2f", feeUSD);
            ((TextView) findViewById(R.id.txt_trans_fee))
                    .setText(String.format(getString(R.string.exchange_usd), strFeeUSD));

            setRemain(editSend.getText().toString());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;

            case R.id.del_amount:
                editSend.setText("");
                btnSend.setEnabled(false);
                break;

            case R.id.del_address:
                editAddress.setText("");
                btnSend.setEnabled(false);
                break;

            case R.id.del_limit:
                editLimit.setText("");
                btnSend.setEnabled(false);
                break;

            case R.id.del_data:
                editData.setText("");
                break;

            case R.id.btn_plus_10:
                addPlus(10);
                setSendEnable();
                editSend.setSelection(editSend.getText().toString().length());
                break;

            case R.id.btn_plus_100:
                addPlus(100);
                setSendEnable();
                editSend.setSelection(editSend.getText().toString().length());
                break;

            case R.id.btn_plus_1000:
                addPlus(1000);
                setSendEnable();
                editSend.setSelection(editSend.getText().toString().length());
                break;

            case R.id.btn_plus_all:
                if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                    BigInteger balance = new BigInteger(mWalletEntry.getBalance());
                    BigInteger bigFee = ConvertUtil.valueToBigInteger(calculateFee(), 18);

                    if (balance.compareTo(bigFee) == -1) {
                        editSend.setText("");
                        lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                        txtSendWarning.setVisibility(View.VISIBLE);
                        txtSendWarning.setText(getString(R.string.errNeedFee));
                    } else {
                        editSend.setText(ConvertUtil.getValue(balance.subtract(bigFee), mWalletEntry.getDefaultDec()));
                        setSendEnable();
                    }

                } else {
                    editSend.setText(ConvertUtil.getValue(new BigInteger(mWalletEntry.getBalance()), mWalletEntry.getDefaultDec()));
                    setSendEnable();
                }

                editSend.setSelection(editSend.getText().toString().length());

                break;

            case R.id.btn_contacts:
                startActivityForResult(new Intent(this, ContactsActivity.class)
                        .putExtra("coinType", mWalletInfo.getCoinType())
                        .putExtra("address", mWalletInfo.getAddress()), RC_CONTACTS);
                break;

            case R.id.btn_scan:
                Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                break;

            case R.id.price_down:
                if (seekPrice.getProgress() != 0)
                    seekPrice.setProgress(seekPrice.getProgress() - 1);

                setSendEnable();
                break;

            case R.id.price_up:
                if (seekPrice.getProgress() < seekPrice.getMax())
                    seekPrice.setProgress(seekPrice.getProgress() + 1);

                setSendEnable();
                break;

            case R.id.arrow:
                if (dataInput.isSelected()) {
                    dataInput.setSelected(false);
                    dataInput.setBackgroundResource(R.drawable.ic_arrow_down);
                    layoutData.setVisibility(View.GONE);
                } else {
                    dataInput.setSelected(true);
                    dataInput.setBackgroundResource(R.drawable.ic_arrow_up);
                    layoutData.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.btn_send:
                SendConfirmDialog dialog = new SendConfirmDialog(this, makeTxInfo());
                dialog.setOnDialogListener(new SendConfirmDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                            mService.requestETHTransaction(Integer.toString(mWalletEntry.getId()), txtPrice.getText().toString(),
                                    editLimit.getText().toString(), editAddress.getText().toString(), editData.getText().toString(),
                                    editSend.getText().toString(), privKey);
                        } else {
                            mService.requestTokenTransfer(Integer.toString(mWalletEntry.getId()), txtPrice.getText().toString(),
                                    editLimit.getText().toString(), mWalletEntry.getContractAddress(),
                                    editAddress.getText().toString(), editSend.getText().toString(),
                                    Integer.toString(mWalletEntry.getDefaultDec()), privKey);
                        }

                        timestamp = getTimeStamp();
                        saveRecentSent();

                        Toast.makeText(getApplicationContext(), getString(R.string.msgDoneRequestTransfer), Toast.LENGTH_SHORT).show();

                        finish();
                    }
                });
                dialog.show();
                break;

            case R.id.info_limit:
                BasicDialog infoLimit = new BasicDialog(this);
                infoLimit.setMessage(getString(R.string.msgEthGasLimit));
                infoLimit.show();
                break;

            case R.id.info_price:
                BasicDialog infoPrice = new BasicDialog(this);
                infoPrice.setMessage(getString(R.string.msgEthGasPrice));
                infoPrice.show();
                break;

            case R.id.info_data:
                BasicDialog infoData = new BasicDialog(this);
                infoData.setMessage(getString(R.string.msgEthData));
                infoData.show();
                break;

            case R.id.info_fee:
                BasicDialog infoFee = new BasicDialog(this);
                infoFee.setMessage(getString(R.string.msgEthEstimateFee));
                infoFee.show();
                break;
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
                if (eth.compareTo(bigFee) < 0) {
                    bigRemain = bigFee.subtract(eth);
                    isNegative = true;
                } else {
                    bigRemain = eth.subtract(bigFee);
                    isNegative = false;
                }
            } else {
                bigRemain = eth;
                isNegative = false;
            }
        } else {
            bigSend = ConvertUtil.valueToBigInteger(value, mWalletEntry.getDefaultDec());
            switch (eth.compareTo(bigSend)) {
                case -1:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        bigRemain = (bigSend.add(bigFee)).subtract(eth);
                        isNegative = true;
                    } else {
                        bigRemain = bigSend.subtract(eth);
                        isNegative = true;
                    }
                    break;
                case 0:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        bigRemain = bigFee;
                        isNegative = true;
                    } else {
                        bigRemain = eth.subtract(bigSend);
                        isNegative = false;
                    }
                    break;
                case 1:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        BigInteger realBigSend = bigSend.add(bigFee);
                        if (eth.compareTo(realBigSend) < 0) {
                            bigRemain = realBigSend.subtract(eth);
                            isNegative = true;
                        } else {
                            bigRemain = eth.subtract(realBigSend);
                            isNegative = false;
                        }
                    } else {
                        bigRemain = eth.subtract(bigSend);
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
                    txtRemain.setText(String.format(Locale.getDefault(), "- %s", remainValue));
                else
                    txtRemain.setText(remainValue);

                txtTransRemain.setText(String.format(getString(R.string.exchange_usd), MyConstants.NO_BALANCE));

            } else {
                String strRemainUSD = String.format(Locale.getDefault(), "%,.2f", remainUSD);

                if (isNegative) {
                    txtRemain.setText(String.format(Locale.getDefault(), "- %s", remainValue));
                    txtTransRemain.setText(String.format(Locale.getDefault(), "- %s",
                            getString(R.string.exchange_usd, strRemainUSD)));
                } else {
                    txtRemain.setText(remainValue);
                    txtTransRemain.setText(String.format(getString(R.string.exchange_usd), strRemainUSD));
                }
            }


        } else {
            if (isNegative)
                txtRemain.setText(String.format(Locale.getDefault(), "- %s", remainValue));
            else
                txtRemain.setText(remainValue);

            txtTransRemain.setText(String.format(getString(R.string.exchange_usd), MyConstants.NO_BALANCE));
        }
    }

    private void addPlus(int plus) {
        String value;
        if (editSend.getText().toString().isEmpty()) {
            editSend.setText(Integer.toString(plus));
        } else {
            value = editSend.getText().toString();
            if (value.indexOf(".") < 0) {
                value = Integer.toString(Integer.parseInt(value) + plus);
                editSend.setText(value);
            } else {
                String[] total = value.split("\\.");
                total[0] = Integer.toString(Integer.parseInt(total[0]) + plus);
                editSend.setText(total[0] + "." + total[1]);
            }
        }
    }

    private boolean validateSendAmount(String value) {
        if (value.isEmpty()) {
            txtSendWarning.setVisibility(View.GONE);
            return false;
        }

        BigInteger sendAmount = ConvertUtil.valueToBigInteger(value, mWalletEntry.getDefaultDec());
        BigInteger fee = ConvertUtil.valueToBigInteger(calculateFee(), 18);

        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
            BigInteger canICX = eth.subtract(fee);
            if (sendAmount.equals(BigInteger.ZERO)) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNonZero));

                return false;
            } else if (eth.compareTo(sendAmount) < 0) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNotEnough));

                return false;
            } else if (canICX.compareTo(sendAmount) < 0) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNeedFee));

                return false;
            }
        } else {
            WalletEntry own = mWalletInfo.getWalletEntries().get(0);
            BigInteger ownBalance = new BigInteger(own.getBalance());

            if (sendAmount.equals(BigInteger.ZERO)) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNonZero));

                return false;
            } else if (eth.compareTo(sendAmount) < 0) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNotEnough));

                return false;
            } else if (ownBalance.compareTo(fee) < 0) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errOwnNotEnough));

                return false;
            }
        }

        if (editSend.hasFocus())
            lineSend.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            lineSend.setBackgroundColor(getResources().getColor(R.color.editNormal));
        editSend.setSelection(editSend.getText().toString().length());
        txtSendWarning.setVisibility(View.GONE);
        return true;
    }

    private boolean validateAddress(String address) {
        if (address.isEmpty()) {
            return false;
        }

        if (address.equals(mWalletInfo.getAddress())) {
            lineAddress.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(getString(R.string.errSameAddress));
            return false;
        }

        if (address.startsWith("0x")) {
            address = address.substring(2);
            if (address.length() != 40) {
                lineAddress.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtAddrWarning.setVisibility(View.VISIBLE);
                txtAddrWarning.setText(getString(R.string.errCheckAddress));

                return false;
            }
        } else if (address.contains(" ")) {
            lineAddress.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(getString(R.string.errCheckAddress));

            return false;
        } else {
            lineAddress.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(getString(R.string.errCheckAddress));

            return false;
        }

        if (editAddress.hasFocus())
            lineAddress.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            lineAddress.setBackgroundColor(getResources().getColor(R.color.editNormal));
        editAddress.setSelection(editAddress.getText().toString().length());
        txtAddrWarning.setVisibility(View.GONE);
        return true;
    }

    private boolean validateGasLimit() {
        if (editLimit.getText().toString().isEmpty()) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);

            return false;
        } else {
            if (editLimit.hasFocus())
                lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
            else
                lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));
            txtLimitWarning.setVisibility(View.GONE);

            return true;
        }
    }

    private boolean validateData() {
        if (layoutData.getVisibility() == View.GONE)
            return true;

        if (editData.getText().toString().isEmpty())
            return true;
        else {
            if (!editData.getText().toString().startsWith("0x")) {
                txtDataWarning.setVisibility(View.VISIBLE);
                lineData.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                return false;
            } else {
                try {
                    String data = editData.getText().toString().substring(2);
                    byte[] temp = Hex.decode(data);
                } catch (Exception e) {
                    txtDataWarning.setVisibility(View.VISIBLE);
                    lineData.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                    return false;
                }

                txtDataWarning.setVisibility(View.GONE);
                if (editData.isFocused())
                    lineData.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    lineData.setBackgroundColor(getResources().getColor(R.color.editNormal));

                return true;
            }
        }
    }

    private String calculateFee() {
        BigInteger price = new BigInteger(txtPrice.getText().toString());
        BigInteger limit;
        if (editLimit.getText().toString().isEmpty()
                || editLimit.getText().toString().trim().length() == 0)
            limit = BigInteger.ZERO;
        else
            limit = new BigDecimal(editLimit.getText().toString()).toBigInteger();

        return Convert.fromWei(limit.multiply(price).multiply(ETH_MULTI).toString(), Convert.Unit.ETHER).toPlainString();
    }

    private void setSendEnable() {
        if (validateSendAmount(editSend.getText().toString())
                && validateAddress(editAddress.getText().toString())
                && validateGasLimit()
                && validateData()) {
            btnSend.setEnabled(true);
        } else {
            btnSend.setEnabled(false);
        }
    }

    private TxInfo makeTxInfo() {

        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
            BigInteger value = ConvertUtil.valueToBigInteger(editSend.getText().toString(), mWalletEntry.getDefaultDec());
            EthTxInfo txInfo = new EthTxInfo(ConvertUtil.getValue(value, 18), calculateFee(), editAddress.getText().toString());
            txInfo.setFromAddress(mWalletInfo.getAddress());
            txInfo.setPrice(txtPrice.getText().toString());
            txInfo.setLimit(editLimit.getText().toString());
            txInfo.setData(editData.getText().toString());

            return txInfo;
        } else {
            BigInteger value = ConvertUtil.valueToBigInteger(editSend.getText().toString(), mWalletEntry.getDefaultDec());
            ErcTxInfo txInfo = new ErcTxInfo(ConvertUtil.getValue(value, 18), calculateFee(), editAddress.getText().toString());
            txInfo.setFromAddress(mWalletInfo.getAddress());
            txInfo.setPrice(txtPrice.getText().toString());
            txInfo.setLimit(editLimit.getText().toString());

            ECKeyPair keyPair = ECKeyPair.create(Hex.decode(privKey));
            org.web3j.crypto.Credentials credentials = org.web3j.crypto.Credentials.create(keyPair);
            txInfo.setCredentials(credentials);
            txInfo.setContract(mWalletEntry.getContractAddress());
            txInfo.setDecimals(mWalletEntry.getDefaultDec());
            txInfo.setSymbol(mWalletEntry.getUserSymbol());

            return txInfo;
        }
    }

    protected String getTimeStamp() {
        long time = System.currentTimeMillis() * 1000;
        return Long.toString(time);
    }

    @Nullable
    private String findContactName(String address) {
        for (int i = 0; i < ICONexApp.mWallets.size(); i++) {
            if (address.equals(MyConstants.PREFIX_ETH + ICONexApp.mWallets.get(i).getAddress()))
                return ICONexApp.mWallets.get(i).getAlias();
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

    private void saveRecentSent() {
        String contactName = findContactName(editAddress.getText().toString());
        if (contactName == null)
            contactName = "";

        RealmUtil.addRecentSend(RealmUtil.COIN_TYPE.ETH, "", contactName,
                editAddress.getText().toString(), timestamp, editSend.getText().toString(), mWalletEntry.getSymbol());
        RealmUtil.loadRecents();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_CONTACTS) {
            if (resultCode == ContactsActivity.CODE_RESULT) {
                String address = data.getStringExtra("address");
                editAddress.setText(checkAddress(address));

                boolean result = validateSendAmount(editSend.getText().toString())
                        && validateAddress(editAddress.getText().toString());
                btnSend.setEnabled(result);
            }
        } else if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    editAddress.setText(checkAddress(barcode.displayValue));
                    boolean result = validateSendAmount(editSend.getText().toString())
                            && validateAddress(editAddress.getText().toString());
                    btnSend.setEnabled(result);
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {

            }
        }
    }
}
