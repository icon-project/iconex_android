package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;

/**
 * Created by js on 2018. 6. 11..
 */

public class SwapRequestDoneDialog extends Dialog {

    private final Context mContext;

    public SwapRequestDoneDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_swap_request_done);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        TextView txtPolicy = findViewById(R.id.txt_policy);
        txtPolicy.setPaintFlags(txtPolicy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ServiceConstants.URL_TOKEN_SWAP_FAQ)));
            }
        });
        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
