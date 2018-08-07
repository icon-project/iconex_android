package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 2. 28..
 */

public class BasicDialog extends Dialog {

    private final Context mContext;
    private String message;

    public BasicDialog(@NonNull Context context) {
        super(context);

        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_basic);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.txt_message)).setText(message);
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
}
