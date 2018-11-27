package foundation.icon.connect;

import android.content.Intent;
import android.graphics.Paint;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.icx.IconService;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.http.HttpProvider;
import loopchain.icon.wallet.core.request.Transaction;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.SendTransactionSigner;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static foundation.icon.ICONexApp.network;

public class EnterPasswordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = EnterPasswordActivity.class.getSimpleName();

    private int id;
    private Constants.Method method;
    private RequestData request;
    private Wallet wallet;

    private TextView txtBalance;
    private ProgressBar progress;
    private ViewGroup layoutTxHash;
    private TextView txtTxHash;

    private TextView txtTxData;

    private MyEditText editPwd;
    private Button btnPwdDelete;
    private TextView txtPwdWarning;
    private View linePwd;

    private Button btnConfirm;
    private ProgressBar progressConfirm;

    private byte[] mPrivateKey;

    private ValidatePassword validatePassword;

    private RequestParser parser;
    private Transaction mTx;
    private JSONObject requestData;

    private TransactionData txData;
    private String symbol;
    private int decimals;
    private String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        if (getIntent() != null) {
            id = getIntent().getIntExtra("id", -1);
            method = (Constants.Method) getIntent().getExtras().get("method");
            request = (RequestData) getIntent().getExtras().get("request");
            wallet = (Wallet) getIntent().getExtras().get("wallet");

            if (method != Constants.Method.SIGN) {
                txData = (TransactionData) getIntent().getExtras().get("txData");
                symbol = txData.getSymbol();
                decimals = txData.getDecimals();
            } else {
                symbol = wallet.getWalletEntries().get(0).getSymbol();
                decimals = wallet.getWalletEntries().get(0).getDefaultDec();
            }
        }

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.enterWalletPassword));
        findViewById(R.id.btn_close).setOnClickListener(this);

        ((TextView) findViewById(R.id.txt_alias)).setText(wallet.getAlias());
        ((TextView) findViewById(R.id.txt_address)).setText(wallet.getAddress());
        txtBalance = findViewById(R.id.txt_balance);
        progress = findViewById(R.id.progress);

        layoutTxHash = findViewById(R.id.layout_tx_hash);
        txtTxHash = findViewById(R.id.txt_tx_hash);

        txtTxData = findViewById(R.id.txt_data);
        txtTxData.setPaintFlags(txtTxData.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtTxData.setOnClickListener(this);

        editPwd = findViewById(R.id.edit_pwd);
        editPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });
        editPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnPwdDelete.setVisibility(View.VISIBLE);
                } else {
                    btnPwdDelete.setVisibility(View.INVISIBLE);
                    btnConfirm.setEnabled(false);
                    txtPwdWarning.setVisibility(View.INVISIBLE);

                    if (editPwd.hasFocus()) {
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    } else {
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editPwd.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                validatePassword = new ValidatePassword();
                validatePassword.execute();
            }
        });
        editPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    validatePassword = new ValidatePassword();
                    validatePassword.execute();
                }
                return false;
            }
        });

        btnPwdDelete = findViewById(R.id.btn_pwd_delete);
        btnPwdDelete.setOnClickListener(this);
        txtPwdWarning = findViewById(R.id.txt_pwd_warning);
        linePwd = findViewById(R.id.line_pwd);

        btnConfirm = findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        progressConfirm = findViewById(R.id.progress_confirm);
        progressConfirm.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (method == Constants.Method.SIGN) {
            try {
                GetBalance getBalance = new GetBalance();
                getBalance.execute();

                parser = RequestParser.newInstance(this);
                requestData = parser.getData(request.getData());

                mTx = makeTx();

                SendTransactionSigner signer = new SendTransactionSigner(mTx);
                txtTxHash.setText(signer.getTxHash());
            } catch (ErrorCodes.Error e) {
                IconexConnect.sendError(EnterPasswordActivity.this, request, e);
            }
        } else {
            layoutTxHash.setVisibility(View.GONE);
            txtTxData.setVisibility(View.GONE);
            txtBalance.setText(ConvertUtil.getValue(txData.getBalance(), 18));
        }
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        ResponseData response;

        switch (v.getId()) {
            case R.id.btn_close:
                Basic2ButtonDialog cancelDialog = new Basic2ButtonDialog(this);
                cancelDialog.setMessage(getString(R.string.msgCancelPassword));
                cancelDialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        IconexConnect.sendError(EnterPasswordActivity.this, request,
                                new ErrorCodes.Error(ErrorCodes.ERR_USER_CANCEL, ErrorCodes.MSG_USER_CANCEL));
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                cancelDialog.show();
                break;

            case R.id.txt_data:
                BasicDialog txDataDialog = new BasicDialog(this, BasicDialog.TYPE.PARAMS);
                txDataDialog.setMessage(parser.paramsToString(requestData));
                txDataDialog.show();
                break;

            case R.id.btn_pwd_delete:
                editPwd.setText("");
                break;

            case R.id.btn_confirm:
                if (method == Constants.Method.SIGN) {
                    String signature = "";
                    try {
                        signature = getSignature(mTx);
                    } catch (ErrorCodes.Error e) {
                        IconexConnect.sendError(EnterPasswordActivity.this, request, e);
                    }

                    IconexConnect.sendResponse(EnterPasswordActivity.this, request, signature);
                } else {
                    startActivity(new Intent(this, SendTxActivity.class)
                            .putExtra("id", id)
                            .putExtra("method", method)
                            .putExtra("request", request)
                            .putExtra("privateKey", Hex.toHexString(mPrivateKey))
                            .putExtra("txData", txData));
                }
                break;
        }
    }

    private boolean validatePwd(String pwd) {
        JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);

        JsonObject crypto;
        if (keyStore.has("crypto"))
            crypto = keyStore.get("crypto").getAsJsonObject();
        else
            crypto = keyStore.get("Crypto").getAsJsonObject();

        byte[] privKey = null;
        try {
            privKey = KeyStoreUtils.decryptPrivateKey(pwd, wallet.getAddress(), crypto, wallet.getCoinType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (privKey == null) {
            return false;
        } else {
            mPrivateKey = privKey;
            return true;
        }
    }

    private Transaction makeTx() throws ErrorCodes.Error {
        Transaction tx;
        try {
            JSONObject params = parser.getParams(requestData);
            tx = new Transaction.Builder()
                    .from(params.getString("from"))
                    .to(params.getString("to"))
                    .value(params.getString("value"))
                    .stepLimit(params.getString("stepLimit"))
                    .timestamp(params.getString("timestamp"))
                    .nid(params.getString("nid"))
                    .nonce(params.getString("nonce"))
                    .build();

            if (params.has("dataType"))
                tx.getBuilder().dataType(params.getString("dataType"));
            if (params.has("data"))
                tx.getBuilder().data(params.getString("data"));

        } catch (JSONException e) {
            throw new ErrorCodes.Error(ErrorCodes.ERR_PARSE, getString(R.string.descParseError));
        }

        return tx;
    }

    private String getSignature(Transaction tx) throws ErrorCodes.Error {
        String signature;

        try {
            SendTransactionSigner signer = new SendTransactionSigner(tx);
            signature = signer.getSignature(signer.getTxHash(), Hex.toHexString(mPrivateKey));
        } catch (Exception e1) {
            throw new ErrorCodes.Error(ErrorCodes.ERR_SIGN_FAILED, getString(R.string.descSignFailed));
        }

        return signature;
    }

    class GetBalance extends AsyncTask<Void, BigInteger, BigInteger> {
        @Override
        protected BigInteger doInBackground(Void... voids) {
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
            IconService iconService = new IconService(new HttpProvider(httpClient, url));

            Address address = new Address(wallet.getAddress());
            BigInteger balance = null;
            try {
                balance = iconService.getBalance(address).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return balance;
        }

        @Override
        protected void onPostExecute(BigInteger result) {
            super.onPostExecute(result);

            progress.setVisibility(View.GONE);
            if (result != null)
                txtBalance.setText(ConvertUtil.getValue(result, 18));
            else
                txtBalance.setText("-");

        }
    }

    class ValidatePassword extends AsyncTask<Void, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btnConfirm.setEnabled(false);
            progressConfirm.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return validatePwd(editPwd.getText().toString());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressConfirm.setVisibility(View.GONE);

            if (result) {
                txtPwdWarning.setVisibility(View.GONE);
                if (editPwd.hasFocus())
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));

                btnConfirm.setEnabled(true);
            } else {
                txtPwdWarning.setVisibility(View.VISIBLE);
                linePwd.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                btnConfirm.setEnabled(false);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Basic2ButtonDialog cancleDialog = new Basic2ButtonDialog(this);
        cancleDialog.setMessage(getString(R.string.msgCancelPassword));
        cancleDialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
            @Override
            public void onOk() {
                IconexConnect.sendError(EnterPasswordActivity.this, request,
                        new ErrorCodes.Error(ErrorCodes.ERR_USER_CANCEL, ErrorCodes.MSG_USER_CANCEL));
            }

            @Override
            public void onCancel() {

            }
        });
        cancleDialog.show();
    }
}
