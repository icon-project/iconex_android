package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
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
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.transfer.data.ErcTxInfo;
import foundation.icon.iconex.wallet.transfer.data.EthTxInfo;
import foundation.icon.iconex.wallet.transfer.data.ICONTxInfo;
import foundation.icon.iconex.wallet.transfer.data.TxInfo;

import static foundation.icon.iconex.ICONexApp.network;
import static foundation.icon.iconex.MyConstants.SYMBOL_ETH;
import static foundation.icon.iconex.MyConstants.SYMBOL_ICON;

/**
 * Created by js on 2018. 3. 15..
 */

public class SendConfirmDialog extends Dialog {

    private static final String TAG = SendConfirmDialog.class.getSimpleName();

    private final Context mContext;
    @NonNull
    private final TxInfo mTxInfo;

    private String message;

    private Button btnSend;
    private ProgressBar progress;

    public SendConfirmDialog(@NonNull Context context, @NonNull TxInfo txInfo) {
        super(context);

        mContext = context;
        mTxInfo = txInfo;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_tx_confirm);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        if (mTxInfo instanceof ErcTxInfo) {
            ErcTxInfo txInfo = (ErcTxInfo) mTxInfo;
            ((TextView) findViewById(R.id.txt_send))
                    .setText(String.format(mContext.getString(R.string.sendAmount), txInfo.getSymbol()));
            ((TextView) findViewById(R.id.txt_fee)).setText(String.format(mContext.getString(R.string.estiFee), SYMBOL_ETH));
        } else if (mTxInfo instanceof EthTxInfo) {
            ((TextView) findViewById(R.id.txt_send))
                    .setText(String.format(mContext.getString(R.string.sendAmount), SYMBOL_ETH));
            ((TextView) findViewById(R.id.txt_fee)).setText(String.format(mContext.getString(R.string.estiFee), SYMBOL_ETH));
        } else {
            ICONTxInfo txInfo = (ICONTxInfo) mTxInfo;
            ((TextView) findViewById(R.id.txt_send))
                    .setText(String.format(mContext.getString(R.string.sendAmount), txInfo.getSymbol()));
            ((TextView) findViewById(R.id.txt_fee))
                    .setText(String.format(mContext.getString(R.string.estiFee), SYMBOL_ICON));
        }

        ((TextView) findViewById(R.id.txt_send_amount)).setText(mTxInfo.getSendAmount());
        ((TextView) findViewById(R.id.txt_fee_amount)).setText(mTxInfo.getFee());

        ((TextView) findViewById(R.id.txt_to_address)).setText(mTxInfo.getToAddress());

        progress = findViewById(R.id.progress);

        findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSend = findViewById(R.id.btn_yes);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTxInfo instanceof ErcTxInfo) {
                    getEstimateERCGas();
                } else if (mTxInfo instanceof EthTxInfo) {
                    getEstimateEtherGas();
                } else {
                    dismiss();
                    mOnDialogListener.onOk();
                }
            }
        });
    }

    private void getEstimateEtherGas() {
        EstimateEthGas estimateGas = new EstimateEthGas();
        estimateGas.execute();
    }

    private void getEstimateERCGas() {
        EstimateERCGas estimateERCGas = new EstimateERCGas();
        estimateERCGas.execute();
    }

    public void setMessage(String msg) {
        message = msg;
    }

    private OnDialogListener mOnDialogListener;

    public void setOnDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }

    public interface OnDialogListener {
        void onOk();
    }

    private class EstimateEthGas extends AsyncTask<String, Void, Integer> {

        private final int OK = 0;
        private final int NOT_ENOUGH = 1;
        private final int EXCEPTION = 2;

        private String errMsg;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress.setVisibility(View.VISIBLE);
            btnSend.setEnabled(false);
        }

        @Override
        protected Integer doInBackground(String... params) {
            EthTxInfo txInfo = (EthTxInfo) mTxInfo;

            String url;
            if (network == MyConstants.NETWORK_MAIN)
                url = ServiceConstants.ETH_HOST;
            else
                url = ServiceConstants.ETH_ROP_HOST;

            Web3j web3j = Web3jFactory.build(new HttpService(url));

            try {
                EthGetTransactionCount nonce = web3j.ethGetTransactionCount(MyConstants.PREFIX_ETH + txInfo.getFromAddress(), DefaultBlockParameterName.LATEST).send();
                EthEstimateGas estimateGas = web3j.ethEstimateGas(Transaction.createFunctionCallTransaction(MyConstants.PREFIX_ETH + txInfo.getFromAddress(),
                        nonce.getTransactionCount(),
                        Convert.toWei(txInfo.getPrice(), Convert.Unit.GWEI).toBigInteger(),
                        new BigInteger(txInfo.getLimit()),
                        txInfo.getToAddress(),
                        ConvertUtil.valueToBigInteger(txInfo.getSendAmount(), 18),
                        txInfo.getData())).send();

                Log.d(TAG, "EstimateGas ETH = " + estimateGas.getAmountUsed());

                if (new BigInteger(txInfo.getLimit()).compareTo(estimateGas.getAmountUsed()) < 0) {
                    return NOT_ENOUGH;
                }
            } catch (Exception e) {
                return NOT_ENOUGH;
            }

            return OK;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            BasicDialog dialog;
            switch (result) {
                case NOT_ENOUGH:
                    dialog = new BasicDialog(mContext);
                    dialog.setMessage(mContext.getString(R.string.errNeedFee));
                    dialog.show();
                    dismiss();
                    break;

                case EXCEPTION:
                    dialog = new BasicDialog(mContext);
                    dialog.setMessage("Exception=" + errMsg);
                    dialog.show();
                    dismiss();
                    break;

                default:
                    dismiss();
                    mOnDialogListener.onOk();
            }
        }
    }

    private class EstimateERCGas extends AsyncTask<Void, Void, Integer> {

        private final int OK = 0;
        private final int NOT_ENOUGH = 1;
        Handler localHandler;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            localHandler = new Handler();
            progress.setVisibility(View.VISIBLE);
            btnSend.setEnabled(false);
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            EthTxInfo txInfo = (EthTxInfo) mTxInfo;

            String url;
            if (network == MyConstants.NETWORK_MAIN)
                url = ServiceConstants.ETH_HOST;
            else
                url = ServiceConstants.ETH_ROP_HOST;

            Web3j web3j = Web3jFactory.build(new HttpService(url));

            try {
                Function function = new Function(
                        "transfer",
                        Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(txInfo.getToAddress()),
                                new org.web3j.abi.datatypes.generated.Uint256(ConvertUtil.valueToBigInteger(txInfo.getSendAmount(), 18))),
                        Collections.<TypeReference<?>>emptyList());
                String data = FunctionEncoder.encode(function);
                Log.d(TAG, "Data=" + data);
                EthGetTransactionCount nonce = web3j.ethGetTransactionCount(MyConstants.PREFIX_ETH + txInfo.getFromAddress(), DefaultBlockParameterName.LATEST).send();
                EthEstimateGas estimateGas = web3j.ethEstimateGas(Transaction.createFunctionCallTransaction(MyConstants.PREFIX_ETH + txInfo.getFromAddress(),
                        nonce.getTransactionCount(),
                        Convert.toWei(txInfo.getPrice(), Convert.Unit.GWEI).toBigInteger(),
                        new BigInteger(txInfo.getLimit()),
                        txInfo.getToAddress(),
                        BigInteger.ZERO,
                        data)).send();

                Log.d(TAG, "EstimateGas ERC20 = " + estimateGas.getAmountUsed());
                BigInteger minLimit = estimateGas.getAmountUsed().add(estimateGas.getAmountUsed().divide(new BigInteger("2")));

                if (new BigInteger(txInfo.getLimit()).compareTo(minLimit) < 0) {
                    return NOT_ENOUGH;
                }
            } catch (Exception e) {
                return NOT_ENOUGH;
            }

            return OK;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            if (result == NOT_ENOUGH) {
                BasicDialog dialog = new BasicDialog(mContext);
                dialog.setMessage(mContext.getString(R.string.errNeedFee));
                dialog.show();
                dismiss();
            } else {
                dismiss();
                mOnDialogListener.onOk();
            }
        }
    }
}
