package foundation.icon.connect;

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

import foundation.icon.iconex.R;

import static foundation.icon.MyConstants.SYMBOL_ICON;

/**
 * Created by js on 2018. 3. 15..
 */

public class SendConfirmDialog extends Dialog {

    private static final String TAG = SendConfirmDialog.class.getSimpleName();

    private final Context mContext;
    private String value, fee, to;

    private String message;

    private Button btnSend;
    private ProgressBar progress;

    public SendConfirmDialog(@NonNull Context context, String value, String fee, String to) {
        super(context);

        mContext = context;
        this.value = value;
        this.fee = fee;
        this.to = to;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_tx_confirm);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.txt_send))
                .setText(String.format(mContext.getString(R.string.sendAmount), SYMBOL_ICON));
        ((TextView) findViewById(R.id.txt_fee))
                .setText(String.format(mContext.getString(R.string.estiFee), SYMBOL_ICON));

        ((TextView) findViewById(R.id.txt_send_amount)).setText(value);
        ((TextView) findViewById(R.id.txt_fee_amount)).setText(fee);

        ((TextView) findViewById(R.id.txt_to_address)).setText(to);

        findViewById(R.id.btn_no).setOnClickListener(v -> dismiss());

        btnSend = findViewById(R.id.btn_yes);
        btnSend.setOnClickListener(v -> {
            dismiss();
            mOnDialogListener.onOk();
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
}
