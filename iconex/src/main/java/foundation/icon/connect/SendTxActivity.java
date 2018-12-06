package foundation.icon.connect;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.dialogs.SendConfirmDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.transfer.data.ICONTxInfo;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.view.View.GONE;
import static foundation.icon.ICONexApp.network;

public class SendTxActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = SendTxActivity.class.getSimpleName();

    private RequestParser parser;
    private RequestData requestData;
    private int id;
    private Constants.Method method;
    private JSONObject params;
    private Wallet wallet;
    private String contractAddress;
    private String from;
    private TransactionData txData;
    private String strPrivateKey;

    private BigInteger minLimit;
    private BigInteger maxLimit;

    private ScrollView scroll;
    private ViewGroup layoutNetwork, btnNetwork;
    private TextView txtNetwork;

    private ViewGroup infoLimit, infoPrice, infoFee;

    private TextView txtSend, txtAmount, txtTransAmount, txtTo, txtFee, txtTransFee, txtRemainAmount, txtRemain, txtTransRemain;
    private TextView txtStepICX, txtStepGloop, txtStepTrans;
    private MyEditText editLimit;
    private TextView txtLimitWarning;
    private Button btnDelLimit;
    private View lineLimit;

    private ViewGroup layoutTxData;
    private TextView txtOpenState;
    private ImageView imgArrow;
    private TextView txtTxData;

    private Button btnSend;

    private String trPrice;
    private String tokenPrice;

    private String dataType;
    private String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_tx);

        if (getIntent() != null) {
            id = getIntent().getIntExtra("id", -1);
            method = (Constants.Method) getIntent().getExtras().get("method");
            requestData = (RequestData) getIntent().getExtras().get("request");
            txData = (TransactionData) getIntent().getExtras().get("txData");

            if (method == Constants.Method.SendICX)
                minLimit = txData.getDefaultLimit();
            else
                minLimit = txData.getDefaultLimit().multiply(BigInteger.valueOf(2));

            maxLimit = txData.getMaxLimit();
            strPrivateKey = getIntent().getStringExtra("privateKey");
        }

        scroll = findViewById(R.id.scroll);

        ((TextView) findViewById(R.id.txt_title)).setText(txData.getAlias());
        findViewById(R.id.btn_close).setOnClickListener(this);
        ((TextView) findViewById(R.id.txt_send_amount)).setText(String.format(Locale.getDefault(), getString(R.string.sendAmount), txData.getSymbol()));
        ((TextView) findViewById(R.id.txt_send_fee)).setText(String.format(Locale.getDefault(), getString(R.string.estiFee), "ICX"));
        ((TextView) findViewById(R.id.txt_remain_amount)).setText(String.format(Locale.getDefault(), getString(R.string.estiRemain), txData.getSymbol()));

        layoutNetwork = findViewById(R.id.layout_network);
        btnNetwork = findViewById(R.id.btn_network);
        btnNetwork.setOnClickListener(this);
        txtNetwork = findViewById(R.id.txt_network);

        txtSend = findViewById(R.id.txt_send_amount);
        txtAmount = findViewById(R.id.txt_amount);
        txtTo = findViewById(R.id.txt_to);

        editLimit = findViewById(R.id.edit_step_limit);
        editLimit.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                validateLimit();
                setRemain();
            }
        });
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
                    validateLimit();
                    setRemain();
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
                    editLimit.setSelection(s.length());
                } else {
                    btnDelLimit.setVisibility(View.INVISIBLE);
                    btnSend.setEnabled(false);

                    txtLimitWarning.setVisibility(GONE);
                    if (editLimit.isFocused())
                        lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));


                }

                setRemain();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editLimit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    validateLimit();
                    setRemain();
                }
                return false;
            }
        });

        btnDelLimit = findViewById(R.id.del_step_limit);
        btnDelLimit.setOnClickListener(this);

        txtLimitWarning = findViewById(R.id.txt_step_limit_warning);
        lineLimit = findViewById(R.id.line_step_limit);

        txtFee = findViewById(R.id.txt_fee);
        txtTransFee = findViewById(R.id.txt_trans_fee);

        txtRemainAmount = findViewById(R.id.txt_remain_amount);
        txtRemain = findViewById(R.id.txt_remain);
        txtTransRemain = findViewById(R.id.txt_trans_remain);
        txtTransAmount = findViewById(R.id.txt_trans_amount);

        txtStepICX = findViewById(R.id.txt_step_icx);
        txtStepGloop = findViewById(R.id.txt_step_gloop);
        txtStepTrans = findViewById(R.id.txt_step_trans);

        infoLimit = findViewById(R.id.info_step_limit);
        infoLimit.setOnClickListener(this);
        infoPrice = findViewById(R.id.info_step_price);
        infoPrice.setOnClickListener(this);
        infoFee = findViewById(R.id.info_fee);
        infoFee.setOnClickListener(this);

        layoutTxData = findViewById(R.id.layout_tx_data);
        findViewById(R.id.btn_open).setOnClickListener(this);
        txtOpenState = findViewById(R.id.txt_open_state);
        imgArrow = findViewById(R.id.img_arrow);
        txtTxData = findViewById(R.id.txt_tx_data);

        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(this);

        getExchangeTable();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ICONexApp.isDeveloper) {
            layoutNetwork.setVisibility(View.VISIBLE);
            switch (ICONexApp.network) {
                case MyConstants.NETWORK_MAIN:
                    txtNetwork.setText("Main");
                    break;

                case MyConstants.NETWORK_TEST:
                    txtNetwork.setText("Test");
                    break;
            }
        }

        parser = RequestParser.newInstance(this);
        try {
            params = parser.getParams(parser.getData(requestData.getData()));
            from = params.getString("from");
            txtAmount.setText(ConvertUtil.getValue(ConvertUtil.hexStringToBigInt(params.getString("value"), txData.getDecimals()), txData.getDecimals()));

            txtTo.setText(params.getString("to"));

            String icx = ConvertUtil.getValue(txData.getStepPrice(), 18);
            String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
            txtStepICX.setText(mIcx);

            String gloop = ConvertUtil.getValue(txData.getStepPrice(), 9);
            String mGloop = gloop.indexOf(".") < 0 ? gloop : gloop.replaceAll("0*$", "").replaceAll("\\.$", "");
            txtStepGloop.setText(String.format(Locale.getDefault(), "ICX (%s Gloop)", mGloop));

            String value = ConvertUtil.getValue(txData.getStepPrice(), 18);
            if (trPrice == null)
                txtStepTrans.setText(String.format(Locale.getDefault(), "-"));
            else
                txtStepTrans.setText(String.format(Locale.getDefault(), "%.2f", Double.parseDouble(value) * Double.parseDouble(trPrice)));

            if (method == Constants.Method.SendToken)
                contractAddress = params.getString("contractAddress");

            dataType = params.getString("dataType");
            data = params.getString("data");
            if (dataType.isEmpty()) {
                layoutTxData.setVisibility(GONE);
                txtTxData.setVisibility(GONE);

                dataType = null;
                data = null;
            } else {
                layoutTxData.setVisibility(View.VISIBLE);
                txtTxData.setVisibility(View.VISIBLE);

                txtTxData.setText(parser.dataToString(parser.getData(requestData.getData())));
                minLimit = minLimit.multiply(BigInteger.valueOf(2));
            }

            editLimit.setText(minLimit.toString());

            setRemain();
        } catch (Exception e) {
            IconexConnect.sendError(SendTxActivity.this, requestData, new ErrorCodes.Error(ErrorCodes.ERR_INVALID_PARAMETER, "value"));
        }
    }

    @Override
    public void onClick(View v) {
        BasicDialog info = new BasicDialog(this);
        String message;
        switch (v.getId()) {
            case R.id.btn_close:
                Basic2ButtonDialog cancleDialog = new Basic2ButtonDialog(this);
                cancleDialog.setMessage(getString(R.string.msgSendCancel));
                cancleDialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        IconexConnect.sendError(SendTxActivity.this, requestData,
                                new ErrorCodes.Error(ErrorCodes.ERR_USER_CANCEL, ErrorCodes.MSG_USER_CANCEL));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                cancleDialog.show();
                break;

            case R.id.btn_network:
                BottomSheetMenuDialog menuDialog = new BottomSheetMenuDialog(this, getString(R.string.selectNetwork),
                        BottomSheetMenuDialog.SHEET_TYPE.BASIC);
                List<String> networks = new ArrayList<>();
                networks.add(getString(R.string.networkMain));
                networks.add(getString(R.string.networkTest));

                menuDialog.setBasicData(networks);
                menuDialog.setOnItemClickListener(mItemListener);

                menuDialog.show();
                break;

            case R.id.del_step_limit:
                editLimit.setText("");
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
                BigInteger value = ConvertUtil.valueToBigInteger(txtAmount.getText().toString(), txData.getDecimals());

                final ICONTxInfo txInfo = new ICONTxInfo(txtTo.getText().toString(), ConvertUtil.getValue(value, txData.getDecimals()),
                        txtFee.getText().toString(), Integer.toHexString(Integer.parseInt(editLimit.getText().toString())), txData.getSymbol());

                SendConfirmDialog dialog = new SendConfirmDialog(this, txInfo);
                dialog.setOnDialogListener(new SendConfirmDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        if (method == Constants.Method.SendICX) {
                            SendIcx sendIcx = new SendIcx();
                            sendIcx.execute();
                        } else {
                            SendToken sendToken = new SendToken();
                            sendToken.execute();
                        }
                    }
                });
                dialog.show();
                break;

            case R.id.btn_open:
                if (txtTxData.getVisibility() == View.VISIBLE) {
                    txtTxData.setVisibility(GONE);
                    txtOpenState.setText(getString(R.string.view));
                    imgArrow.setBackgroundResource(R.drawable.ic_arrow_down);
                } else {
                    txtTxData.setVisibility(View.VISIBLE);
                    txtOpenState.setText(getString(R.string.fold));
                    imgArrow.setBackgroundResource(R.drawable.ic_arrow_up);
                    txtTxData.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
                break;
        }
    }

    private boolean setRemain() {
        BigInteger balance;
        BigInteger remain = null;
        BigInteger send;

        BigInteger stepLimit;
        if (editLimit.getText().toString().isEmpty())
            stepLimit = BigInteger.ZERO;
        else
            stepLimit = new BigInteger(editLimit.getText().toString());

        BigInteger fee = txData.getStepPrice().multiply(stepLimit);

        txtFee.setText(ConvertUtil.getValue(fee, 18));

        boolean isNegative = false;

        if (method == Constants.Method.SendICX)
            balance = txData.getBalance();
        else
            balance = txData.getTokenBalance();

        if (txtAmount.getText().toString().isEmpty()) {

            if (method == Constants.Method.SendICX) {
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
            send = ConvertUtil.valueToBigInteger(txtAmount.getText().toString(), txData.getDecimals());
            switch (balance.compareTo(send)) {
                case -1:
                    if (method == Constants.Method.SendICX) {
                        remain = (send.add(fee)).subtract(balance);
                        isNegative = true;
                    } else {
                        remain = send.subtract(balance);
                        isNegative = true;
                    }
                    break;
                case 0:
                    if (method == Constants.Method.SendICX) {
                        remain = fee;
                        isNegative = true;
                    } else {
                        remain = balance.subtract(send);
                        isNegative = false;
                    }
                    break;
                case 1:
                    if (method == Constants.Method.SendICX) {
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

        if (trPrice != null) {
            Double remainUSD = Double.parseDouble(ConvertUtil.getValue(remain, txData.getDecimals()))
                    * Double.parseDouble(trPrice);
            String strRemainUSD = String.format(Locale.getDefault(), "%,.2f", remainUSD);

            Double balanceUSD;
            if (tokenPrice != null) {
                balanceUSD = Double.parseDouble(txtAmount.getText().toString())
                        * Double.parseDouble(tokenPrice);
                txtTransAmount.setText(balanceUSD + " USD");

            } else {
                balanceUSD = Double.parseDouble(txtAmount.getText().toString())
                        * Double.parseDouble(trPrice);
                txtTransAmount.setText(balanceUSD + " USD");
            }

            txtStepTrans.setText(String.format(Locale.getDefault(), "%,.2f",
                    Double.parseDouble(txtStepICX.getText().toString()) * Double.parseDouble(trPrice)));

            txtTransFee.setText(String.format(Locale.getDefault(), "%,.2f USD",
                    Double.parseDouble(txtFee.getText().toString()) * Double.parseDouble(trPrice)));


            if (isNegative) {
                txtRemain.setText(String.format(getString(R.string.txWithdraw), ConvertUtil.getValue(remain, txData.getDecimals())));
                txtTransRemain.setText(String.format(getString(R.string.exchange_usd), String.format(getString(R.string.txWithdraw), strRemainUSD)));
            } else {
                txtRemain.setText(ConvertUtil.getValue(remain, txData.getDecimals()));
                txtTransRemain.setText(String.format(getString(R.string.exchange_usd), strRemainUSD));
            }
        }

        return isNegative;
    }

    private void validateLimit() {
        String limit = editLimit.getText().toString();
        if (limit.isEmpty()) {
            btnSend.setEnabled(false);
            return;
        }

        BigInteger targetLimit = new BigInteger(limit);

        if (targetLimit.compareTo(minLimit) < 0) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);
            txtLimitWarning.setText(String.format(Locale.getDefault(), getString(R.string.errMinStep), minLimit.toString()));

            btnSend.setEnabled(false);
            return;
        } else if (targetLimit.compareTo(maxLimit) > 0) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);
            txtLimitWarning.setText(String.format(Locale.getDefault(), getString(R.string.errMaxStep), maxLimit.toString()));

            btnSend.setEnabled(false);
            return;
        }

        BigInteger fee = targetLimit.multiply(txData.getStepPrice());
        if (txData.getBalance().compareTo(fee) < 0) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);
            txtLimitWarning.setText(getString(R.string.errNeedFee));

            btnSend.setEnabled(false);
            return;
        }

        if (editLimit.hasFocus())
            lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));
        editLimit.setSelection(editLimit.getText().toString().length());
        txtLimitWarning.setVisibility(View.GONE);

        btnSend.setEnabled(true);

        return;
    }

    private void getExchangeTable() {
        String trList;
        if (method == Constants.Method.SendICX)
            trList = txData.getSymbol().toLowerCase() + MyConstants.EXCHANGE_USD.toLowerCase();
        else
            trList = "icx" + MyConstants.EXCHANGE_USD.toLowerCase()
                    + "," + txData.getSymbol().toLowerCase() + MyConstants.EXCHANGE_USD.toLowerCase();

        String url = null;
        switch (network) {
            case MyConstants.NETWORK_MAIN:
                url = ServiceConstants.URL_VERSION_MAIN;
                break;

            case MyConstants.NETWORK_TEST:
                url = ServiceConstants.URL_VERSION_TEST;
                break;

            case MyConstants.NETWORK_DEV:
                url = ServiceConstants.DEV_TRACKER;
                break;
        }

        try {
            LoopChainClient client = new LoopChainClient(url);
            Call<TRResponse> responseCall = client.getExchangeRates(trList);
            responseCall.enqueue(new Callback<TRResponse>() {
                @Override
                public void onResponse(Call<TRResponse> call, Response<TRResponse> response) {
                    int code = response.code();
                    if (code == 200) {
                        String result = response.body().getResult();
                        if (result.equals(MyConstants.RESULT_OK)) {
                            JsonElement data = response.body().getData();
                            JsonArray list = data.getAsJsonArray();
                            for (int i = 0; i < list.size(); i++) {
                                JsonObject item = list.get(i).getAsJsonObject();
                                String tradeName = item.get("tradeName").getAsString();

                                if (tradeName.equals("icx" + MyConstants.EXCHANGE_USD.toLowerCase()))
                                    trPrice = item.get("price").getAsString();
                                else
                                    tokenPrice = item.get("price").getAsString();

                            }
                        }
                    }

                    setRemain();
                }

                @Override
                public void onFailure(Call<TRResponse> call, Throwable t) {

                }
            });
        } catch (Exception e) {

        }
    }

    private IconService iconService;

    private class SendIcx extends AsyncTask<Void, String, String> {

        Transaction transaction;
        foundation.icon.icx.Wallet wallet;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            initIconService();

            wallet = KeyWallet.load(new Bytes(strPrivateKey));

            BigInteger networkId = new BigInteger(Integer.toString(network));
            Address fromAddress = new Address(from);
            Address toAddress = new Address(txtTo.getText().toString());

            BigInteger value = IconAmount.of(txtAmount.getText().toString(), IconAmount.Unit.ICX).toLoop();
            BigInteger stepLimit = new BigInteger(editLimit.getText().toString());
            long timestamp = System.currentTimeMillis() * 1000L;
            BigInteger nonce = new BigInteger("1");

            if (dataType == null) {
                transaction = TransactionBuilder.newBuilder()
                        .nid(networkId)
                        .from(fromAddress)
                        .to(toAddress)
                        .value(value)
                        .stepLimit(stepLimit)
                        .timestamp(new BigInteger(Long.toString(timestamp)))
                        .nonce(nonce)
                        .build();
            } else if (dataType.equals("message")) {
                transaction = TransactionBuilder.newBuilder()
                        .nid(networkId)
                        .from(fromAddress)
                        .to(toAddress)
                        .value(value)
                        .stepLimit(stepLimit)
                        .timestamp(new BigInteger(Long.toString(timestamp)))
                        .nonce(nonce)
                        .message(new String(Hex.decode(data.substring(2))))
                        .build();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            SignedTransaction signedTransaction = new SignedTransaction(transaction, wallet);

            Bytes result;
            try {
                result = iconService.sendTransaction(signedTransaction).execute();
            } catch (IOException e) {
                return null;
            }

            return result.toHexString(true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            BasicDialog dialog = new BasicDialog(SendTxActivity.this);
            if (result != null) {
                dialog.setMessage(getString(R.string.connSendSuccess));
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        IconexConnect.sendResponse(SendTxActivity.this, requestData, result);
                    }
                });
                dialog.show();
            } else {
                dialog.setMessage(getString(R.string.connSendFailed));
                dialog.show();
            }
        }
    }

    private class SendToken extends AsyncTask<Void, String, String> {
        Transaction transaction;
        foundation.icon.icx.Wallet wallet;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            initIconService();

            wallet = KeyWallet.load(new Bytes(strPrivateKey));

            BigInteger networkId = new BigInteger(Integer.toString(network));
            Address fromAddress = wallet.getAddress();
            Address toAddress = new Address(txtTo.getText().toString());
            BigInteger value = IconAmount.of(txtAmount.getText().toString(), txData.getDecimals()).toLoop();
            BigInteger stepLimit = new BigInteger(editLimit.getText().toString());
            long timestamp = System.currentTimeMillis() * 1000L;
            BigInteger nonce = new BigInteger("1");
            String methodName = "transfer";

            RpcObject params = new RpcObject.Builder()
                    .put("_to", new RpcValue(toAddress))
                    .put("_value", new RpcValue(value))
                    .build();

            transaction = TransactionBuilder.newBuilder()
                    .nid(networkId)
                    .from(fromAddress)
                    .to(new Address(contractAddress))
                    .stepLimit(stepLimit)
                    .timestamp(new BigInteger(Long.toString(timestamp)))
                    .nonce(nonce)
                    .call(methodName)
                    .params(params)
                    .build();
        }

        @Override
        protected String doInBackground(Void... voids) {
            SignedTransaction signedTransaction = new SignedTransaction(transaction, wallet);
            Bytes result;
            try {
                result = iconService.sendTransaction(signedTransaction).execute();
            } catch (IOException e) {
                return null;
            }

            return result.toHexString(true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            BasicDialog dialog = new BasicDialog(SendTxActivity.this);
            if (result != null) {
                dialog.setMessage(getString(R.string.connSendSuccess));
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        IconexConnect.sendResponse(SendTxActivity.this, requestData, result);
                    }
                });
                dialog.show();
            } else {
                dialog.setMessage(getString(R.string.connSendFailed));
                dialog.show();
            }
        }
    }

    private void initIconService() {
        if (iconService == null) {
            String url = null;
            switch (network) {
                case MyConstants.NETWORK_MAIN:
                    url = ServiceConstants.TRUSTED_HOST_MAIN + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;

                case MyConstants.NETWORK_TEST:
                    url = ServiceConstants.TRUSTED_HOST_TEST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;

                case MyConstants.NETWORK_DEV:
                    url = ServiceConstants.DEV_HOST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                    break;
            }

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
            iconService = new IconService(new HttpProvider(httpClient, url));
        }
    }

    private BottomSheetMenuDialog.OnItemClickListener mItemListener = new BottomSheetMenuDialog.OnItemClickListener() {
        @Override
        public void onBasicItem(String item) {
            PreferenceUtil preferenceUtil = new PreferenceUtil(SendTxActivity.this);
            if (item.equals(getString(R.string.networkMain))) {
                txtNetwork.setText(getString(R.string.networkMain));
                ICONexApp.network = MyConstants.NETWORK_MAIN;
                preferenceUtil.setNetwork(ICONexApp.network);
            } else {
                txtNetwork.setText(getString(R.string.networkTest));
                ICONexApp.network = MyConstants.NETWORK_TEST;
                preferenceUtil.setNetwork(ICONexApp.network);
            }
        }

        @Override
        public void onCoinItem(int position) {

        }

        @Override
        public void onMenuItem(String tag) {

        }
    };

    @Override
    public void onBackPressed() {
        Basic2ButtonDialog cancleDialog = new Basic2ButtonDialog(this);
        cancleDialog.setMessage(getString(R.string.msgSendCancel));
        cancleDialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
            @Override
            public void onOk() {
                IconexConnect.sendError(SendTxActivity.this, requestData,
                        new ErrorCodes.Error(ErrorCodes.ERR_USER_CANCEL, ErrorCodes.MSG_USER_CANCEL));
            }

            @Override
            public void onCancel() {

            }
        });
        cancleDialog.show();
    }
}
