package foundation.icon.iconex.dialogs;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 2. 12..
 */

public class WalletInfoDialog extends Dialog implements View.OnClickListener {

    private final Context mContext;
    private final DialogCallback mCallback;
    private final String mCoinType;

    private EditText editAlias, editPwd;
    private Button btnCancel, btnOK;

    public WalletInfoDialog(Context context, String coinType, DialogCallback callback) {
        super(context);

        mContext = context;
        mCallback = callback;
        mCoinType = coinType;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCanceledOnTouchOutside(false);
        setContentView(R.layout.dialog_wallet_info);

        editAlias = findViewById(R.id.edit_alias);
        editPwd = findViewById(R.id.edit_pwd);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        btnOK = findViewById(R.id.btn_ok);
        btnOK.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;

            case R.id.btn_ok:
                mCallback.onOK(editAlias.getText().toString(), editPwd.getText().toString(),
                        mCoinType);
                dismiss();
                break;
        }
    }

    public interface DialogCallback {
        public void onOK(String alias, String pwd, String coinType);
    }
}
