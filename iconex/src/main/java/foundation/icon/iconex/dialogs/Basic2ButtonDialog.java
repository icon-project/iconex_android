package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 2. 26..
 */

public class Basic2ButtonDialog extends Dialog {

    private final Context mContext;
    private String message;

    private String nName = null;
    private String pName = null;

    private OnDialogListener mOnDialogListener;

    public Basic2ButtonDialog(@NonNull Context context) {
        super(context);

        mContext = context;
    }

    public Basic2ButtonDialog(@NonNull Context context, @Nullable String negativeName, @Nullable String positiveName) {
        super(context);

        mContext = context;
        nName = negativeName;
        pName = positiveName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_basic_2_button);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.txt_message)).setText(message);

        if (nName != null)
            ((Button) findViewById(R.id.btn_no)).setText(nName);

        if (pName != null)
            ((Button) findViewById(R.id.btn_yes)).setText(pName);

        findViewById(R.id.btn_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mOnDialogListener.onCancel();
            }
        });

        findViewById(R.id.btn_yes).setOnClickListener(new View.OnClickListener() {
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

    public void setOnDialogListener(OnDialogListener listener) {
        mOnDialogListener = listener;
    }

    public interface OnDialogListener {
        void onOk();
        void onCancel();
    }
}
