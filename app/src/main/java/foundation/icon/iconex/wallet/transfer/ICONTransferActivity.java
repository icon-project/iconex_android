package foundation.icon.iconex.wallet.transfer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.DataTypeDialog;
import foundation.icon.iconex.dialogs.SendConfirmDialog;
import foundation.icon.iconex.service.NetworkService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.contacts.ContactsActivity;
import foundation.icon.iconex.wallet.transfer.data.ICONTxInfo;
import foundation.icon.iconex.wallet.transfer.data.InputData;
import foundation.icon.iconex.widgets.MyEditText;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.request.Transaction;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import loopchain.icon.wallet.service.crypto.PKIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ICONTransferActivity extends AppCompatActivity implements View.OnClickListener, EnterDataFragment.OnEnterDataLisnter {

    private static final String TAG = ICONTransferActivity.class.getSimpleName();

    private ScrollView scroll;
    private Button btnBack;
    private MyEditText editSend, editAddress, editLimit;
    private View lineSend, lineAddress, lineLimit;
    private TextView txtSendWarning, txtAddrWarning, txtLimitWarning;
    private Button btnDelAmount, btnDelAddr, btnDelLimit;
    private TextView txtTransSend;
    private Button btnPlus10, btnPlus100, btnPlus1000, btnTheWhole;
    private Button btnContacts, btnScan, btnInput;

    private TextView txtStepICX, txtStepGloop, txtStepTrans;

    private TextView txtFee, txtTransFee;
    private TextView txtRemain, txtTransRemain;

    private Button btnSend;

    private BigInteger balance;

    private final String CODE_EXCHANGE = "icxusd";
    private String EXCHANGE_PRICE = "";
    private String FEE;
    private String timestamp = null;
    private String txHash = null;

    private NetworkService mService;
    private boolean mBound = false;

    private static final int RC_CONTACTS = 9001;
    private static final int RC_BARCODE_CAPTURE = 9002;
    private static final int RC_DATA = 9003;

    private Wallet mWallet;
    private WalletEntry mWalletEntry;
    private String privKey;

    private LoopChainClient LCClient = null;
    private BigInteger stepPriceLoop = null;
    private BigInteger stepPriceICX = null;

    private BigInteger defaultLimit = BigInteger.ZERO;
    private BigInteger minStep = BigInteger.ZERO;
    private BigInteger maxStep = BigInteger.ZERO;
    private BigInteger tokenStep = BigInteger.ZERO;
    private BigInteger inputPrice = BigInteger.ZERO;
    private BigInteger contractCall = BigInteger.ZERO;

    private InputData data = null;
    private FragmentManager fragmentManager = null;

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

    private NetworkService.TransferCallback mTransferCallback = new NetworkService.TransferCallback() {
        @Override
        public void onReceiveTransactionResult(String id, String txHash) {

            Toast.makeText(getApplicationContext(), getString(R.string.msgDoneRequestTransfer), Toast.LENGTH_SHORT).show();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_icon_transfer);

        if (getIntent() != null) {
            mWallet = (Wallet) getIntent().getExtras().get("walletInfo");
            mWalletEntry = (WalletEntry) getIntent().getExtras().get("walletEntry");
            privKey = getIntent().getStringExtra("privateKey");
        }

        EXCHANGE_PRICE = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);

        scroll = findViewById(R.id.scroll);

        ((TextView) findViewById(R.id.txt_title)).setText(mWallet.getAlias());
        ((TextView) findViewById(R.id.txt_possession))
                .setText(String.format(getString(R.string.possessionAmount), mWalletEntry.getSymbol()));
        ((TextView) findViewById(R.id.txt_send_amount))
                .setText(String.format(getString(R.string.sendAmount), mWalletEntry.getSymbol()));
        ((TextView) findViewById(R.id.txt_send_fee))
                .setText(String.format(getString(R.string.estiFee), MyConstants.SYMBOL_ICON));
        ((TextView) findViewById(R.id.txt_remain_amount))
                .setText(String.format(getString(R.string.estiRemain), mWalletEntry.getSymbol()));
        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        if (mWalletEntry.getType().equals(MyConstants.TYPE_TOKEN))
            findViewById(R.id.layout_input_data).setVisibility(View.GONE);

        lineSend = findViewById(R.id.line_send_amount);
        lineAddress = findViewById(R.id.line_to_address);
        lineLimit = findViewById(R.id.line_step_limit);

        txtSendWarning = findViewById(R.id.txt_send_warning);
        txtAddrWarning = findViewById(R.id.txt_address_warning);
        txtLimitWarning = findViewById(R.id.txt_step_limit_warning);

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
                    scroll.smoothScrollTo(0, editSend.getScrollY());
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
                        String strPrice = ICONexApp.EXCHANGE_TABLE.get(mWalletEntry.getSymbol().toLowerCase() + "usd");
                        if (strPrice != null) {
                            Double transUSD = Double.parseDouble(amount)
                                    * Double.parseDouble(strPrice);
                            String strTransUSD = String.format("%,.2f", transUSD);

                            txtTransSend.setText(String.format("%s USD", strTransUSD));
                        }

//                        if (mWalletEntry.getType().equals(MyConstants.TYPE_TOKEN)) {
//                            getIrcStepLimit();
//                            editLimit.setText(minStep.toString());
//                        }
                        setRemain(amount);
                    }
                } else {
                    btnDelAmount.setVisibility(View.INVISIBLE);
                    txtTransSend.setText(String.format("%s USD", MyConstants.NO_BALANCE));
                    btnSend.setEnabled(false);

                    txtSendWarning.setVisibility(View.GONE);
                    if (editSend.isFocused())
                        lineSend.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineSend.setBackgroundColor(getResources().getColor(R.color.editNormal));

                    setRemain(editSend.getText().toString());
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
                    int[] location = new int[2];
                    btnPlus10.getLocationInWindow(location);
                    Log.d(TAG, "x=" + location[0] + ", y=" + location[1]);
                    Log.d(TAG, "appbar getBottom=" + findViewById(R.id.appbar).getBottom());
                    scroll.smoothScrollTo(0, location[1] - findViewById(R.id.appbar).getBottom());
                }
                return false;
            }
        });

        btnDelAmount = findViewById(R.id.del_amount);
        btnDelAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editSend.setText("");
            }
        });

        editAddress = findViewById(R.id.edit_to_address);
        editAddress.setOnKeyPreImeListener(onKeyPreImeListener);
        editAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
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
                if (s.length() > 0) {
                    btnDelAddr.setVisibility(View.VISIBLE);
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
                    btnDelAddr.setVisibility(View.INVISIBLE);
                    btnSend.setEnabled(false);

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
        editAddress.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
//                    setSendEnable();
                    editLimit.requestFocus();

                    int[] location = new int[2];
                    btnContacts.getLocationInWindow(location);
                    Log.d(TAG, "x=" + location[0] + ", y=" + location[1]);
                    Log.d(TAG, "appbar getBottom=" + findViewById(R.id.appbar).getBottom());
                    scroll.smoothScrollTo(0, scroll.getScrollY() + location[1] - findViewById(R.id.appbar).getBottom());
                }
                return false;
            }
        });

        btnDelAddr = findViewById(R.id.del_address);
        btnDelAddr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editAddress.setText("");
            }
        });

        editLimit = findViewById(R.id.edit_step_limit);
        editLimit.setOnKeyPreImeListener(onKeyPreImeListener);
        editLimit.setLongClickable(false);
        editLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        editLimit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    if (editLimit.getText().toString().length() > 0)
                        validateLimit(editLimit.getText().toString());
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
                } else {
                    btnDelLimit.setVisibility(View.INVISIBLE);
                    btnSend.setEnabled(false);

                    txtLimitWarning.setVisibility(View.GONE);
                    if (editLimit.isFocused())
                        lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));


                }

                setRemain(editSend.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editLimit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    setSendEnable();
                }
                return false;
            }
        });

        btnDelLimit = findViewById(R.id.del_step_limit);
        btnDelLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLimit.setText("");
            }
        });

        txtFee = findViewById(R.id.txt_fee);
        txtTransFee = findViewById(R.id.txt_trans_fee);

        txtRemain = findViewById(R.id.txt_remain);
        txtTransRemain = findViewById(R.id.txt_trans_remain);
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

        txtStepICX = findViewById(R.id.txt_step_icx);
        txtStepGloop = findViewById(R.id.txt_step_gloop);
        txtStepTrans = findViewById(R.id.txt_step_trans);

        btnInput = findViewById(R.id.btn_input);
        btnInput.setOnClickListener(this);

        findViewById(R.id.info_step_limit).setOnClickListener(this);
        findViewById(R.id.info_step_price).setOnClickListener(this);
        findViewById(R.id.info_data).setOnClickListener(this);
        findViewById(R.id.info_fee).setOnClickListener(this);

        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);

        if (LCClient == null) {
            String url = null;
            switch (ICONexApp.network) {
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

            }
        }

        getStepPrice();
        getStepLimit();
    }

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

        balance = new BigInteger(mWalletEntry.getBalance());

        ((TextView) findViewById(R.id.txt_balance)).setText(ConvertUtil.getValue(balance, mWalletEntry.getDefaultDec()));
        String strPrice = ICONexApp.EXCHANGE_TABLE.get(mWalletEntry.getSymbol().toLowerCase() + "usd");
        if (strPrice != null) {
            Double balanceUSD = Double.parseDouble(ConvertUtil.getValue(balance, mWalletEntry.getDefaultDec()))
                    * Double.parseDouble(strPrice);

            String strBalanceUSD = String.format(Locale.getDefault(), "%,.2f", balanceUSD);
            ((TextView) findViewById(R.id.txt_trans_balance))
                    .setText(String.format(getString(R.string.exchange_usd), strBalanceUSD));

            setRemain(editSend.getText().toString());
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        String message;
        BasicDialog info = new BasicDialog(this);

        switch (v.getId()) {
            case R.id.btn_back:
                finish();
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
                    if (balance.compareTo(ConvertUtil.valueToBigInteger(FEE, 18)) < 0) {
                        editSend.setText("");
                        lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                        txtSendWarning.setVisibility(View.VISIBLE);
                        txtSendWarning.setText(getString(R.string.errNeedFee));
                    } else {
                        BigInteger allIcx = balance.subtract(ConvertUtil.valueToBigInteger(FEE, 18));
                        editSend.setText(ConvertUtil.getValue(allIcx, mWalletEntry.getDefaultDec()));
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
                        .putExtra("coinType", mWallet.getCoinType())
                        .putExtra("address", mWallet.getAddress()), RC_CONTACTS);
                break;

            case R.id.btn_scan:
                intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);

                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                break;

            case R.id.btn_input:
                if (data == null) {
                    DataTypeDialog typeDialog = new DataTypeDialog(this);
                    typeDialog.setOnTypeListener(new DataTypeDialog.OnTypeListener() {
                        @Override
                        public void onSelect(EnterDataFragment.DataType type) {
                            data = new InputData();
                            data.setAddress(mWallet.getAddress());
                            data.setBalance(balance);
                            data.setStepPrice(stepPriceLoop);
                            if (editSend.getText().toString().isEmpty())
                                data.setAmount(BigInteger.ZERO);
                            else
                                data.setAmount(ConvertUtil.valueToBigInteger(editSend.getText().toString(), 18));
                            data.setDataType(type);

                            fragmentManager = getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.add(R.id.container, EnterDataFragment.newInstance(data));
                            transaction.addToBackStack("DATA");
                            transaction.commit();

                            typeDialog.dismiss();
                        }
                    });
                    typeDialog.show();
                } else {
                    fragmentManager = getSupportFragmentManager();
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.add(R.id.container, EnterDataFragment.newInstance(data));
                    transaction.addToBackStack("DATA");
                    transaction.commit();
                }
                break;

            case R.id.info_step_limit:
                info.setMessage(getString(R.string.msgStepLimit));
                info.show();
                break;

            case R.id.info_step_price:
                message = getString(R.string.msgStepPrice);
                info = new BasicDialog(this, BasicDialog.TYPE.SUPER, message.indexOf("-"), message.indexOf("-") + 3);
                info.setMessage(getString(R.string.msgStepPrice));
                info.show();
                break;

            case R.id.info_data:
                info.setMessage(getString(R.string.msgIcxData));
                info.show();
                break;

            case R.id.info_fee:
                info.setMessage(getString(R.string.msgICXEstimateFee));
                info.show();
                break;

            case R.id.btn_send:
                BigInteger value;
                if (editSend.getText().toString().isEmpty())
                    value = BigInteger.ZERO;
                else
                    value = ConvertUtil.valueToBigInteger(editSend.getText().toString(), mWalletEntry.getDefaultDec());
                final ICONTxInfo txInfo = new ICONTxInfo(editAddress.getText().toString(), ConvertUtil.getValue(value, mWalletEntry.getDefaultDec()),
                        txtFee.getText().toString(), Integer.toHexString(Integer.parseInt(editLimit.getText().toString())), mWalletEntry.getSymbol());

                SendConfirmDialog dialog = new SendConfirmDialog(this, txInfo);
                dialog.setOnDialogListener(new SendConfirmDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        timestamp = getTimeStamp();
                        Transaction tx;
                        String nid;
                        if (ICONexApp.network == MyConstants.NETWORK_MAIN)
                            nid = "0x1";
                        else if (ICONexApp.network == MyConstants.NETWORK_TEST)
                            nid = "0x2";
                        else
                            nid = "0x3";

                        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                            tx = new Transaction.Builder(mWalletEntry.getId(), nid, privKey)
                                    .from(mWalletEntry.getAddress())
                                    .to(editAddress.getText().toString())
                                    .stepLimit(txInfo.getStepLimit())
                                    .timestamp(timestamp)
                                    .nonce("0x1")
                                    .build();

                            if (editSend.getText().toString().isEmpty())
                                tx = tx.getBuilder().value(ConvertUtil.valueToHexString("0", 18)).build();
                            else
                                tx = tx.getBuilder().value(ConvertUtil.valueToHexString(editSend.getText().toString(), 18)).build();

                            if (data != null) {
                                tx = tx.getBuilder().dataType(Constants.DATA_MESSAGE)
                                        .data(data.getData())
                                        .build();
                            }
                        } else {
                            JsonObject data = new JsonObject();
                            data.addProperty("method", "transfer");
                            JsonObject params = new JsonObject();
                            params.addProperty("_to", editAddress.getText().toString());
                            params.addProperty("_value", ConvertUtil.valueToHexString(editSend.getText().toString(), mWalletEntry.getDefaultDec()));
                            data.add("params", params);
                            tx = new Transaction.Builder(mWalletEntry.getId(), nid, privKey)
                                    .from(mWalletEntry.getAddress())
                                    .to(mWalletEntry.getContractAddress())
                                    .stepLimit(txInfo.getStepLimit())
                                    .timestamp(timestamp)
                                    .nonce("0x1")
                                    .dataType(Constants.DATA_CALL)
                                    .data(data.toString())
                                    .dataTo(editAddress.getText().toString())
                                    .build();
                        }

//                        RecentSendInfo pending = new RecentSendInfo();
//                        pending.setAmount(editSend.getText().toString());
//                        pending.setDate(timestamp);
//                        pending.setSymbol(mWalletEntry.getSymbol());
//                        pending.

                        mService.requestICXTransaction(tx);
                    }
                });
                dialog.show();
                break;
        }
    }

    private void setRemain(String value) {
        BigInteger fee;
        BigInteger remain = null;
        BigInteger send;

        String strPrice = ICONexApp.EXCHANGE_TABLE.get(mWalletEntry.getSymbol().toLowerCase() + "usd");
        String feePrice = ICONexApp.EXCHANGE_TABLE.get("icxusd");

        if (stepPriceICX != null && !editLimit.getText().toString().isEmpty())
            fee = stepPriceICX.multiply(new BigInteger(editLimit.getText().toString()));
        else
            fee = BigInteger.ZERO;
        txtFee.setText(ConvertUtil.getValue(fee, 18));

        FEE = ConvertUtil.getValue(fee, 18);

        boolean isNegative = false;

        if (value.isEmpty()) {

            if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
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
            send = ConvertUtil.valueToBigInteger(value, mWalletEntry.getDefaultDec());
            switch (balance.compareTo(send)) {
                case -1:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        remain = (send.add(fee)).subtract(balance);
                        isNegative = true;
                    } else {
                        remain = send.subtract(balance);
                        isNegative = true;
                    }
                    break;
                case 0:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
                        remain = fee;
                        isNegative = true;
                    } else {
                        remain = balance.subtract(send);
                        isNegative = false;
                    }
                    break;
                case 1:
                    if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
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

        if (strPrice != null) {
            Double remainUSD = Double.parseDouble(ConvertUtil.getValue(remain, mWalletEntry.getDefaultDec()))
                    * Double.parseDouble(strPrice);
            String strRemainUSD = String.format(Locale.getDefault(), "%,.2f", remainUSD);

            if (stepPriceICX != null) {
                if (feePrice != null) {
                    txtTransFee.setText(String.format(Locale.getDefault(), "%,.2f USD",
                            Double.parseDouble(txtFee.getText().toString()) * Double.parseDouble(feePrice)));
                }
            }

            if (isNegative) {
                txtRemain.setText(String.format(getString(R.string.txWithdraw), ConvertUtil.getValue(remain, mWalletEntry.getDefaultDec())));
                txtTransRemain.setText(String.format(getString(R.string.exchange_usd), String.format(getString(R.string.txWithdraw), strRemainUSD)));
            } else {
                txtRemain.setText(ConvertUtil.getValue(remain, mWalletEntry.getDefaultDec()));
                txtTransRemain.setText(String.format(getString(R.string.exchange_usd), strRemainUSD));
            }
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

    private boolean validateSendAmount(String value) {
        if (value.isEmpty()) {
            lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtSendWarning.setVisibility(View.VISIBLE);
            txtSendWarning.setText(getString(R.string.errNoSendAmount));

            return false;
        }

        if (mWalletEntry.getType().equals(MyConstants.TYPE_COIN)) {
            BigInteger sendAmount = ConvertUtil.valueToBigInteger(value, 18);
            BigInteger canICX = balance.subtract(ConvertUtil.valueToBigInteger(FEE, 18));

            if (balance.compareTo(sendAmount) < 0) {
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
            WalletEntry own = mWallet.getWalletEntries().get(0);
            BigInteger ownBalance = new BigInteger(own.getBalance());

            BigInteger sendAmount = ConvertUtil.valueToBigInteger(value, mWalletEntry.getDefaultDec());
            BigInteger canICX = ownBalance.subtract(ConvertUtil.valueToBigInteger(FEE, 18));
            if (sendAmount.equals(BigInteger.ZERO)) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNonZero));

                return false;
            }

            if (balance.compareTo(sendAmount) < 0) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNotEnough));

                return false;
            } else if (ownBalance.compareTo(canICX) < 0) {
                lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                txtSendWarning.setVisibility(View.VISIBLE);
                txtSendWarning.setText(getString(R.string.errNeedFee));

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
        if (address.isEmpty())
            return false;

        if (address.equals(mWallet.getAddress())) {
            lineAddress.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(getString(R.string.errSameAddress));
            return false;
        }

        if (address.startsWith("hx")) {
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

    private boolean validateLimit(String limit) {
        if (limit.isEmpty())
            return false;

        BigInteger targetLimit = new BigInteger(limit);

        if (targetLimit.compareTo(minStep) < 0) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);
            txtLimitWarning.setText(String.format(Locale.getDefault(), getString(R.string.errMinStep), minStep.toString()));

            return false;
        } else if (targetLimit.compareTo(maxStep) > 0) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);
            txtLimitWarning.setText(String.format(Locale.getDefault(), getString(R.string.errMaxStep), maxStep.toString()));

            return false;
        }

        if (editLimit.hasFocus())
            lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));
        editLimit.setSelection(editLimit.getText().toString().length());
        txtLimitWarning.setVisibility(View.GONE);

        return true;
    }

    private void setSendEnable() {
        boolean amount = validateSendAmount(editSend.getText().toString());
        boolean address = validateAddress(editAddress.getText().toString());
        boolean limit = validateLimit(editLimit.getText().toString());

        if (amount && address && limit) {
            btnSend.setEnabled(true);
        } else {
            btnSend.setEnabled(false);
        }
    }

    public String getTxHash(byte[] _tbs) {
        try {
            byte[] hash = PKIUtils.hash(_tbs, PKIUtils.ALGORITHM_HASH);
            return PKIUtils.hexEncode(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected String getTimeStamp() {
        long time = System.currentTimeMillis() * 1000;
        return "0x" + Long.toHexString(time);
    }

    private void getStepPrice() {
        int id = new Random().nextInt(999999) + 100000;
        try {
            retrofit2.Call<LCResponse> responseCall = LCClient.getStepPrice(id, mWallet.getAddress());
            responseCall.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(retrofit2.Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        String result = response.body().getResult().getAsString();
                        stepPriceLoop = ConvertUtil.hexStringToBigInt(result, 18);
                        String icx = ConvertUtil.getValue(stepPriceLoop, 18);
                        String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                        stepPriceICX = ConvertUtil.valueToBigInteger(icx, 18);
                        txtStepICX.setText(mIcx);
                        String gloop = ConvertUtil.getValue(stepPriceLoop, 9);
                        String mGloop = gloop.indexOf(".") < 0 ? gloop : gloop.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtStepGloop.setText(String.format(Locale.getDefault(), "ICX (%s Gloop)", mGloop));

                        String value = ConvertUtil.getValue(stepPriceLoop, 18);
                        String strExc = ICONexApp.EXCHANGE_TABLE.get(CODE_EXCHANGE);
                        if (strExc == null)
                            txtStepTrans.setText(String.format(Locale.getDefault(), "-"));
                        else
                            txtStepTrans.setText(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(value) * Double.parseDouble(strExc)));

                    } else {
                        txtStepICX.setText("- ");
                        txtStepTrans.setText("- ");
                    }

                    setRemain(editSend.getText().toString());
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
            Call<LCResponse> getStepCost = LCClient.getStepCost(1234, mWallet.getAddress());
            getStepCost.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        int defaultLimit = Integer.decode(response.body().getResult().getAsJsonObject().get("default").getAsString());
                        int input = Integer.decode(response.body().getResult().getAsJsonObject().get("input").getAsString());
                        int contract = Integer.decode(response.body().getResult().getAsJsonObject().get("contractCall").getAsString());

                        ICONTransferActivity.this.defaultLimit = new BigInteger(Integer.toString(defaultLimit));
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

                    if (mWalletEntry.getType().equals(MyConstants.TYPE_TOKEN))
                        minStep = defaultLimit.multiply(BigInteger.valueOf(2));
                    else
                        minStep = defaultLimit;

                    editLimit.setText(minStep.toString());
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {
                    defaultLimit = new BigInteger(preferenceUtil.getDefaultLimit());
                    inputPrice = new BigInteger(preferenceUtil.getInputPrice());
                    contractCall = new BigInteger(preferenceUtil.getContractCall());

                    if (mWalletEntry.getType().equals(MyConstants.TYPE_TOKEN))
                        minStep = defaultLimit.add(contractCall).multiply(BigInteger.valueOf(2));
                    else
                        minStep = defaultLimit;

                    editLimit.setText(minStep.toString());
                }
            });

            Call<LCResponse> getMaxStep = LCClient.getStepMaxLimit(2345, mWallet.getAddress());
            getMaxStep.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        try {
                            Log.d(TAG, "result=" + response.body().getResult().getAsString());
                            maxStep = new BigInteger(Utils.remove0x(response.body().getResult().getAsString()), 16);
                            Log.d(TAG, "maxStep=" + maxStep.toString());
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

    @Override
    public void onSetData(InputData data) {
        this.data = data;
        minStep = new BigInteger(Integer.toString(this.data.getStepCost()));
        editLimit.setText(minStep.toString());

        btnInput.setText(getString(R.string.view));
        btnInput.setSelected(true);

        Log.d(TAG, "Hex string=" + this.data.getData());

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        fragmentManager.popBackStackImmediate();

        setSendEnable();
    }

    @Override
    public void onDataCancel(InputData data) {
        if (data.getData() == null)
            this.data = null;

        fragmentManager.popBackStackImmediate();
    }

    @Override
    public void onDataDelete() {
        this.data = null;

        btnInput.setText(R.string.input);
        btnInput.setSelected(false);

        minStep = defaultLimit;
        editLimit.setText(minStep.toString());

        fragmentManager.popBackStackImmediate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                } else {
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager != null && fragmentManager.getBackStackEntryCount() > 0) {
            Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
            dialog.setMessage(getString(R.string.cancelEnterData));
            dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                @Override
                public void onOk() {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                            | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                    fragmentManager.popBackStackImmediate();
                }

                @Override
                public void onCancel() {

                }
            });
            dialog.show();
        } else
            super.onBackPressed();
    }
}
