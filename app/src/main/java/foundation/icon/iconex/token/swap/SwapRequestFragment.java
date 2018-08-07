package foundation.icon.iconex.token.swap;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.control.WalletEntry;
import foundation.icon.iconex.control.WalletInfo;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.widgets.MyEditText;

import static foundation.icon.iconex.ICONexApp.isMain;

public class SwapRequestFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SwapRequestFragment.class.getSimpleName();

    private WalletInfo mWallet;
    private WalletEntry mToken;

    private final BigInteger mLimit = new BigInteger("55000");
    private final BigInteger mPrice = new BigInteger("21");
    private static final BigInteger ETH_MULTI = new BigInteger("1000000000");

    private String FEE;

    private TextView txtRemain, txtTransRemain;
    private MyEditText editSend;
    private TextView txtTrans, txtWarning;
    private View lineSend;
    private Button btnDelAmount;
    private Button btnPlus10, btnPlus100, btnPlus1000, btnPlusMax;
    private Button btnComplete;
    private TextView txtPrice, txtLimit;
    private TextView txtFee, txtFeeTr;

    private ProgressBar progressBar;

    private final String PRICE_FACTOR = "10";
    private final String MIN_PRICE = "21";
    private final String MAX_PRICE = "99";
    private EstimateERCGas estimateERCGas = null;

    public SwapRequestFragment() {
        // Required empty public constructor
    }

    public static SwapRequestFragment newInstance() {
        SwapRequestFragment fragment = new SwapRequestFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWallet = ((TokenSwapActivity) getActivity()).getWallet();
        mToken = ((TokenSwapActivity) getActivity()).getToken();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_swap_request, container, false);

        TextView txtBalance = v.findViewById(R.id.txt_balance);
        txtBalance.setText(ConvertUtil.getValue(new BigInteger(mToken.getBalance()), 18));

        String exchange = ICONexApp.EXCHANGE_TABLE.get("icxusd");
        Double doubBalance = Double.parseDouble(ConvertUtil.getValue(new BigInteger(mToken.getBalance()), 18));
        Double doubEx = Double.parseDouble(exchange);
        ((TextView) v.findViewById(R.id.txt_trans)).setText(String.format(Locale.getDefault(), "%,.2f USD", doubBalance * doubEx));

        TextView txtIncineration = v.findViewById(R.id.txt_swap_addr);
        txtIncineration.setText(MyConstants.ETH_INCINERATION);

        ((TextView) v.findViewById(R.id.txt_icx_addr)).setText(((TokenSwapActivity) getActivity()).getICXAddr());

        txtLimit = v.findViewById(R.id.txt_limit);
        txtPrice = v.findViewById(R.id.txt_price);

        txtFee = v.findViewById(R.id.txt_fee);

        txtFeeTr = v.findViewById(R.id.txt_fee_trans);

        txtRemain = v.findViewById(R.id.txt_remain);
        txtTransRemain = v.findViewById(R.id.txt_remain_trans);

        txtTrans = v.findViewById(R.id.txt_trans_amount);
        txtWarning = v.findViewById(R.id.txt_warning);
        lineSend = v.findViewById(R.id.line_amount);

        editSend = v.findViewById(R.id.edit_send_amount);
        editSend.setLongClickable(false);
        editSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        editSend.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                if (validateSendAmount(editSend.getText().toString())) {
                    Log.d(TAG, "BackPressed");
                    getEstimateGas();
                } else
                    btnComplete.setEnabled(false);
            }
        });
        editSend.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineSend.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineSend.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    if (validateSendAmount(editSend.getText().toString())) {
                        Log.d(TAG, "Focus changed");
                        getEstimateGas();
                    } else
                        btnComplete.setEnabled(false);
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
                                } else if (below.length() > 18) {
                                    below = below.substring(0, 18);
                                    editSend.setText(decimal + "." + below);
                                    editSend.setSelection(editSend.getText().toString().length());
                                }
                            }
                        }

                        amount = editSend.getText().toString();
                        String strPrice = ICONexApp.EXCHANGE_TABLE.get("icxusd");
                        if (strPrice != null) {
                            Double transUSD = Double.parseDouble(amount)
                                    * Double.parseDouble(strPrice);
                            String strTransUSD = String.format("%,.2f", transUSD);

                            txtTrans.setText(String.format("%s USD", strTransUSD));
                        }
                        setRemain(amount);
                    }
                } else {
                    btnDelAmount.setVisibility(View.INVISIBLE);
                    txtTrans.setText(String.format("%s USD", "0.00"));
                    btnComplete.setEnabled(false);
                    txtWarning.setVisibility(View.GONE);
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
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (validateSendAmount(editSend.getText().toString())) {
                        Log.d(TAG, "OnEditorAction");
                        getEstimateGas();
                    } else
                        btnComplete.setEnabled(false);
                }
                return false;
            }
        });

        btnDelAmount = v.findViewById(R.id.del_amount);
        btnDelAmount.setOnClickListener(this);

        btnPlus10 = v.findViewById(R.id.btn_plus_10);
        btnPlus10.setOnClickListener(this);
        btnPlus100 = v.findViewById(R.id.btn_plus_100);
        btnPlus100.setOnClickListener(this);
        btnPlus1000 = v.findViewById(R.id.btn_plus_1000);
        btnPlus1000.setOnClickListener(this);
        btnPlusMax = v.findViewById(R.id.btn_all);
        btnPlusMax.setOnClickListener(this);

        btnComplete = v.findViewById(R.id.btn_complete);
        btnComplete.setOnClickListener(this);

        progressBar = v.findViewById(R.id.progress);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSwapRequestListener) {
            mListener = (OnSwapRequestListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSwapRequestListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (estimateERCGas != null)
            estimateERCGas.cancel(true);
        estimateERCGas = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.del_amount:
                editSend.setText("");
                btnComplete.setEnabled(false);
                break;

            case R.id.btn_plus_10:
                addPlus(10);
                if (validateSendAmount(editSend.getText().toString())) {
                    Log.d(TAG, "10");
                    getEstimateGas();
                } else
                    btnComplete.setEnabled(false);
                editSend.setSelection(editSend.getText().toString().length());
                break;

            case R.id.btn_plus_100:
                addPlus(100);
                if (validateSendAmount(editSend.getText().toString())) {
                    Log.d(TAG, "100");
                    getEstimateGas();
                } else
                    btnComplete.setEnabled(false);
                editSend.setSelection(editSend.getText().toString().length());
                break;

            case R.id.btn_plus_1000:
                addPlus(1000);
                if (validateSendAmount(editSend.getText().toString())) {
                    Log.d(TAG, "1000");
                    getEstimateGas();
                } else
                    btnComplete.setEnabled(false);
                editSend.setSelection(editSend.getText().toString().length());
                break;

            case R.id.btn_all:
                editSend.setText(ConvertUtil.getValue(new BigInteger(mToken.getBalance()), 18));
                if (validateSendAmount(editSend.getText().toString())) {
                    Log.d(TAG, "Max");
                    getEstimateGas();
                } else
                    btnComplete.setEnabled(false);

                editSend.setSelection(editSend.getText().toString().length());

                break;

            case R.id.btn_complete:
                if (validateOwnBalance())
                    mListener.onSwapRequest(editSend.getText().toString(), txtPrice.getText().toString().substring(0,2),
                            txtLimit.getText().toString(), txtFee.getText().toString());
                else
                    btnComplete.setEnabled(false);
                break;
        }
    }

    private void setRemain(String value) {
        BigInteger bigRemain = null;
        BigInteger bigSend;

        String strPrice = ICONexApp.EXCHANGE_TABLE.get("icxusd");

        boolean isNegative = false;

        if (editSend.getText().toString().isEmpty()) {
            bigRemain = new BigInteger(mToken.getBalance());
            isNegative = false;
        } else {
            bigSend = ConvertUtil.valueToBigInteger(value, mToken.getDefaultDec());
            switch (new BigInteger(mToken.getBalance()).compareTo(bigSend)) {
                case -1:
                    bigRemain = bigSend.subtract(new BigInteger(mToken.getBalance()));
                    isNegative = true;
                    break;
                case 0:
                    bigRemain = new BigInteger(mToken.getBalance()).subtract(bigSend);
                    isNegative = false;
                    break;
                case 1:

                    bigRemain = new BigInteger(mToken.getBalance()).subtract(bigSend);
                    isNegative = false;
                    break;
            }
        }

        String remainValue = ConvertUtil.getValue(bigRemain, mToken.getDefaultDec());
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
            txtWarning.setVisibility(View.GONE);
            return false;
        }

        BigInteger sendAmount = ConvertUtil.valueToBigInteger(value, 18);

        if (sendAmount.equals(BigInteger.ZERO)) {
            lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText(getString(R.string.errNonZero));

            return false;
        } else if (new BigInteger(mToken.getBalance()).compareTo(sendAmount) < 0) {
            lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText(getString(R.string.errNotEnough));

            return false;
        }

        if (editSend.hasFocus())
            lineSend.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            lineSend.setBackgroundColor(getResources().getColor(R.color.editNormal));
        editSend.setSelection(editSend.getText().toString().length());
        txtWarning.setVisibility(View.INVISIBLE);
        return true;
    }

    private boolean validateOwnBalance() {
        BigInteger fee = ConvertUtil.valueToBigInteger(txtFee.getText().toString(), 18);

        WalletEntry own = mWallet.getWalletEntries().get(0);
        BigInteger ownBalance = new BigInteger(own.getBalance());

        if (ownBalance.compareTo(fee) < 0) {
            lineSend.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtWarning.setVisibility(View.VISIBLE);
            txtWarning.setText(getString(R.string.swapMsgNotEnoughFee));

            return false;
        } else {
            return true;
        }
    }

    private OnSwapRequestListener mListener;

    public interface OnSwapRequestListener {
        void onSwapRequest(String amount, String price, String limit, String fee);
    }

    private void getEstimateGas() {
        if (estimateERCGas != null)
            return;

        estimateERCGas = new EstimateERCGas();
        estimateERCGas.execute(mWallet.getAddress(), editSend.getText().toString());
    }

    private class EstimateERCGas extends AsyncTask<String, Void, BigInteger[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btnComplete.setEnabled(false);
            btnComplete.setText("");
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected BigInteger[] doInBackground(String... params) {

            String from = params[0];
            String amount = params[1];

            BigInteger gasPrice;
            BigInteger estLimit;

            String url;
            if (isMain)
                url = ServiceConstants.ETH_HOST;
            else
                url = ServiceConstants.ETH_ROP_HOST;

            Web3j web3j = Web3jFactory.build(new HttpService(url));

            try {
                EthGasPrice getGasPrice = web3j.ethGasPrice().send();
                gasPrice = getGasPrice.getGasPrice().add(new BigInteger(PRICE_FACTOR));

                if (gasPrice.compareTo(new BigInteger(MIN_PRICE)) < 0)
                    gasPrice = new BigInteger(MIN_PRICE);

                if (gasPrice.compareTo(new BigInteger(MAX_PRICE)) > 0)
                    gasPrice = new BigInteger(MAX_PRICE);

                Function function = new Function(
                        "transfer",
                        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(MyConstants.ETH_INCINERATION),
                                new org.web3j.abi.datatypes.generated.Uint256(ConvertUtil.valueToBigInteger(amount, 18))),
                        Collections.<TypeReference<?>>emptyList());
                String data = FunctionEncoder.encode(function);
                Log.d(TAG, "Data=" + data);
                EthGetTransactionCount nonce = web3j.ethGetTransactionCount(MyConstants.PREFIX_ETH + from, DefaultBlockParameterName.LATEST).send();
                EthEstimateGas estimateGas = web3j.ethEstimateGas(Transaction.createFunctionCallTransaction(MyConstants.PREFIX_ETH + from,
                        nonce.getTransactionCount(),
                        Convert.toWei(gasPrice.toString(), Convert.Unit.GWEI).toBigInteger(),
                        mLimit,
                        MyConstants.ETH_INCINERATION,
                        BigInteger.ZERO,
                        data)).send();

                Log.d(TAG, "EstimateGas ERC20 = " + estimateGas.getAmountUsed());
                estLimit = estimateGas.getAmountUsed().multiply(new BigInteger("2"));

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return new BigInteger[]{gasPrice, estLimit};
        }

        @Override
        protected void onPostExecute(BigInteger[] result) {
            super.onPostExecute(result);

            if (result == null) {
                btnComplete.setEnabled(false);

                Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity(), null, getString(R.string.retry));
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setMessage(getString(R.string.networkRetry));
                dialog.setOnDialogListener(dialogListener);
                dialog.show();
            } else {
                BigInteger price = result[0];
                BigInteger limit = result[1];

                txtPrice.setText(price + " Gwei");
                txtLimit.setText(limit.toString());
                BigInteger fee = price.multiply(ETH_MULTI).multiply(limit);
                String ethFee = Convert.fromWei(fee.toString(), Convert.Unit.ETHER).toPlainString();
                txtFee.setText(ethFee);

                Double doubEthFee = Double.parseDouble(ethFee) * Double.parseDouble(ICONexApp.EXCHANGE_TABLE.get("icxusd"));
                txtFeeTr.setText(String.format(Locale.getDefault(), "%,.2f USD", doubEthFee));

                if (validateSendAmount(editSend.getText().toString()))
                    btnComplete.setEnabled(true);
                else
                    btnComplete.setEnabled(false);
            }

            estimateERCGas.cancel(true);
            estimateERCGas = null;
            progressBar.setVisibility(View.GONE);
            btnComplete.setText(getString(R.string.complete));
        }
    }

    private Basic2ButtonDialog.OnDialogListener dialogListener = new Basic2ButtonDialog.OnDialogListener() {
        @Override
        public void onOk() {
            EstimateERCGas estimateERCGas = new EstimateERCGas();
            estimateERCGas.execute(mWallet.getAddress(), editSend.getText().toString());
        }

        @Override
        public void onCancel() {

        }
    };
}
