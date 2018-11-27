package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 2. 28..
 */

public class BasicDialog extends Dialog {

    private final Context mContext;
    private String message;

    private TextView txtMessage;

    private TYPE mType = null;
    private int start = 0;
    private int end = 0;

    public BasicDialog(@NonNull Context context) {
        super(context);

        mContext = context;
    }

    public BasicDialog(@NonNull Context context, TYPE type) {
        super(context);

        mContext = context;
        mType = type;
    }

    public BasicDialog(@NonNull Context context, TYPE type, int start, int end) {
        super(context);

        mContext = context;
        mType = type;
        this.start = start;
        this.end = end;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_basic);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        txtMessage = findViewById(R.id.txt_message);
        txtMessage.setMovementMethod(new ScrollingMovementMethod());

        if (mType == null)
            txtMessage.setText(message);
        else {
            if (mType == TYPE.SUPER) {
                SpannableStringBuilder mSSBuilder = new SpannableStringBuilder(message);
                SuperscriptSpan superS = new SuperscriptSpan();
                mSSBuilder.setSpan(superS, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                RelativeSizeSpan size = new RelativeSizeSpan(.5f);
                mSSBuilder.setSpan(size, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                txtMessage.setText(mSSBuilder);
            } else if (mType == TYPE.PARAMS) {
                txtMessage.setGravity(Gravity.START);
                txtMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9);

                txtMessage.setText(message);
            }
        }

        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setMessage(String msg) {
        message = msg;
    }

    public enum TYPE {
        SUPER,
        PARAMS
    }
}
