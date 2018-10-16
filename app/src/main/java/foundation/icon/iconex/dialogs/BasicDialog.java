package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 2. 28..
 */

public class BasicDialog extends Dialog {

    private final Context mContext;
    private String message;

    private TYPE mType = null;
    private int start = 0;
    private int end = 0;

    public BasicDialog(@NonNull Context context) {
        super(context);

        mContext = context;
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

        if (mType == null)
            ((TextView) findViewById(R.id.txt_message)).setText(message);
        else {
            SpannableStringBuilder mSSBuilder = new SpannableStringBuilder(message);
            SuperscriptSpan superS = new SuperscriptSpan();
            mSSBuilder.setSpan(superS, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            RelativeSizeSpan size = new RelativeSizeSpan(.5f);
            mSSBuilder.setSpan(size, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            ((TextView) findViewById(R.id.txt_message)).setText(mSSBuilder);
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
        SUPER
    }
}
