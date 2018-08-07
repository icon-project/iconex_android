package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.web3j.crypto.Credentials;

import java.math.BigInteger;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.WalletInfo;

/**
 * Created by js on 2018. 6. 11..
 */

public class SwapConfirmDialog extends Dialog {

    private final Context mContext;
    private final Credentials mCredentials;
    private final String mContract;
    private final String mSendAmount;
    private final String mFee;
    private final BigInteger mLimit;
    private final String mPrice;
    private final String mTo;
    private final String mICXAddr;

    private String message;

    private Button btnSend;
    private ProgressBar progress;

    public SwapConfirmDialog(@NonNull Context context, @NonNull Credentials credentials, @NonNull String contract,
                             String sendAmount, String fee, BigInteger limit, String price, String to, String icxAddr) {
        super(context);

        mContext = context;
        mCredentials = credentials;
        mContract = contract;
        mSendAmount = sendAmount;
        mFee = fee;
        mLimit = limit;
        mPrice = price;
        mTo = to;
        mICXAddr = icxAddr;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_swap_confirm);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.txt_send_amount)).setText(mSendAmount);
        ((TextView) findViewById(R.id.txt_fee_amount)).setText(mFee);
        ((TextView) findViewById(R.id.txt_swap_wallet)).setText(getWalletName());
        ((TextView) findViewById(R.id.txt_to_address)).setText(mICXAddr);

        progress = findViewById(R.id.progress);

        findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnSend = findViewById(R.id.btn_request);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mOnDialogListener.onOk();
            }
        });
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

    private String getWalletName() {
        for (WalletInfo wallet : ICONexApp.mWallets) {
            if (wallet.getAddress().equals(mICXAddr))
                return wallet.getAlias();
        }

        return "";
    }
}
