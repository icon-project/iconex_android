package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import org.jetbrains.annotations.NotNull;
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

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.transfer.data.ErcTxInfo;
import foundation.icon.iconex.wallet.transfer.data.EthTxInfo;
import foundation.icon.iconex.wallet.transfer.data.ICONTxInfo;
import foundation.icon.iconex.wallet.transfer.data.TxInfo;
import kotlin.jvm.functions.Function1;

import static foundation.icon.ICONexApp.network;
import static foundation.icon.MyConstants.SYMBOL_ETH;
import static foundation.icon.MyConstants.SYMBOL_ICON;

public class TransactionSendDialog extends MessageDialog {

    // send balance
    private TextView labelSendBalance;
    private TextView symbolSendBalance;
    private TextView txtSendBalance;

    // limit / price
    private TextView labelLimitPrice;
    private TextView symbolLimitPrice;
    private TextView txtLimitPrice;
    private Group groupLimitPrice;

    // estimated fee
    private TextView labelFee;
    private TextView symbolFee;
    private TextView txtFee;
    private TextView txtTransFee;

    // receive address
    private TextView labelAddress;
    private TextView txtAddress;

    private final TxInfo mTxInfo;

    public TransactionSendDialog(@NotNull Context context, TxInfo txInfo) {
        super(context);
        mTxInfo = txInfo;
        buildDialog();
        initView();
    }

    private void buildDialog() {
        // set head
        setHeadText(getContext().getString(R.string.transfer));
        // set content
        View content = View.inflate(getContext(), R.layout.layout_send_transaction_dialog_content, null);
        setContent(content);
        // load view
        // send balance
        labelSendBalance = findViewById(R.id.lb_send_balance);
        symbolSendBalance = findViewById(R.id.symbol_send_balance);
        txtSendBalance = findViewById(R.id.txt_send_balance);
        // limit / price
        labelLimitPrice = findViewById(R.id.lb_limit_price);
        symbolLimitPrice = findViewById(R.id.symbol_limit_price);
        txtLimitPrice = findViewById(R.id.txt_limit_price);
        groupLimitPrice = findViewById(R.id.group_limit_price);
        // estimated fee
        labelFee = findViewById(R.id.lb_fee);
        symbolFee = findViewById(R.id.symbol_fee);
        txtFee = findViewById(R.id.txt_fee);
        txtTransFee = findViewById(R.id.txt_trans_fee);
        // receive address
        labelAddress = findViewById(R.id.lb_receive);
        txtAddress = findViewById(R.id.txt_receive);
        // set button
        setSingleButton(false);
        setConfirmButtonText(getContext().getString(R.string.withdraw));
    }

    private void initView() {
        // set symbol
        if (mTxInfo instanceof ErcTxInfo) {
            ErcTxInfo txInfo = (ErcTxInfo) mTxInfo;
            symbolSendBalance.setText("(" + txInfo.getSymbol() + ")");
            groupLimitPrice.setVisibility(View.GONE);
            symbolFee.setText("(" + SYMBOL_ETH);
        } else if (mTxInfo instanceof EthTxInfo) {
            symbolSendBalance.setText("(" + SYMBOL_ETH + ")");
            groupLimitPrice.setVisibility(View.GONE);
            symbolFee.setText("(" + SYMBOL_ETH + ")");
        } else {
            ICONTxInfo txInfo = (ICONTxInfo) mTxInfo;
            symbolSendBalance.setText("(" + txInfo.getSymbol() + ")");
            symbolLimitPrice.setText("(" + SYMBOL_ICON + ")");
            symbolFee.setText("(" + SYMBOL_ICON + ")");
            // tx icon -> set limit/price value
            txtLimitPrice.setText(txInfo.getLimitPrice());
        }
        // set value
        txtSendBalance.setText(mTxInfo.getSendAmount());
        txtFee.setText(mTxInfo.getFee());
        txtTransFee.setText(mTxInfo.getTransFee());
        txtAddress.setText(mTxInfo.getToAddress());
        // set confirm button
        setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                if (mTxInfo instanceof ErcTxInfo) {
                    getEstimateERCGas();
                } else if (mTxInfo instanceof EthTxInfo) {
                    getEstimateEtherGas();
                } else {
                    mOnDialogListener.onOk();
                    return true;
                }
                return false;
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

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            setProgressVisible(true);
            setConfirmEnable(false);
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
                EthGetTransactionCount nonce = web3j.ethGetTransactionCount(MyConstants.PREFIX_HEX + txInfo.getFromAddress(), DefaultBlockParameterName.LATEST).send();
                EthEstimateGas estimateGas = web3j.ethEstimateGas(Transaction.createFunctionCallTransaction(MyConstants.PREFIX_HEX + txInfo.getFromAddress(),
                        nonce.getTransactionCount(),
                        Convert.toWei(txInfo.getPrice(), Convert.Unit.GWEI).toBigInteger(),
                        new BigInteger(txInfo.getLimit()),
                        txInfo.getToAddress(),
                        ConvertUtil.valueToBigInteger(txInfo.getSendAmount(), 18),
                        txInfo.getData())).send();

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
                    dialog = new BasicDialog(getContext());
                    dialog.setMessage(getContext().getString(R.string.errNeedFee));
                    dialog.show();
                    dismiss();
                    break;

                case EXCEPTION:
                    dialog = new BasicDialog(getContext());
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
            setProgressVisible(true);
            setConfirmEnable(false);
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
                EthGetTransactionCount nonce = web3j.ethGetTransactionCount(MyConstants.PREFIX_HEX + txInfo.getFromAddress(), DefaultBlockParameterName.LATEST).send();
                EthEstimateGas estimateGas = web3j.ethEstimateGas(Transaction.createFunctionCallTransaction(MyConstants.PREFIX_HEX + txInfo.getFromAddress(),
                        nonce.getTransactionCount(),
                        Convert.toWei(txInfo.getPrice(), Convert.Unit.GWEI).toBigInteger(),
                        new BigInteger(txInfo.getLimit()),
                        txInfo.getToAddress(),
                        BigInteger.ZERO,
                        data)).send();

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
                BasicDialog dialog = new BasicDialog(getContext());
                dialog.setMessage(getContext().getString(R.string.errNeedFee));
                dialog.show();
                dismiss();
            } else {
                dismiss();
                mOnDialogListener.onOk();
            }
        }
    }
}
