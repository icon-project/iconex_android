package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 4. 2..
 */

public class TitleMsgDialog extends Dialog {

    private String title, message;

    private TextView txtTitle, txtMsg;
    private Button btnOK;

    public TitleMsgDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_title_msg);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        txtTitle = findViewById(R.id.txt_title);
        txtTitle.setText(title);
        txtMsg = findViewById(R.id.txt_msg);
        txtMsg.setText(message);

        btnOK = findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }
}
