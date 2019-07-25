package foundation.icon.iconex.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.FingerprintAuthBuilder;
import foundation.icon.iconex.util.FingerprintAuthHelper;

/**
 * Created by js on 2018. 4. 25..
 */

@SuppressLint("NewApi")
public class FingerprintDialog extends Dialog implements FingerprintAuthHelper.Callback {

    private Context mContext;

    private ImageView icon;
    private TextView txtStatus;

    private FingerprintAuthHelper helper;

    public FingerprintDialog(@NonNull Context context) {
        super(context);

        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_fingerprint);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        icon = findViewById(R.id.fingerprint_icon);
        txtStatus = findViewById(R.id.fingerprint_status);

        FingerprintManager fm = (FingerprintManager) mContext.getSystemService(Context.FINGERPRINT_SERVICE);
        FingerprintAuthBuilder fab = new FingerprintAuthBuilder(mContext);
        helper = new FingerprintAuthHelper(fm, this);
        try {
            if (fab.initCipher(fab.defaultCipher, FingerprintAuthBuilder.DEFAULT_KEY_NAME)) {
                helper.startFingerprintAuthListening(new FingerprintManager.CryptoObject(fab.defaultCipher));
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.stopFingerprintAuthListening();
                dismiss();
            }
        });
    }

    @Override
    public void onAuthenticated() {
        mListener.onChecked();
        dismiss();
    }

    @Override
    public void onFailed() {
        txtStatus.setText(mContext.getString(R.string.fingerprint_not_recognized));
        txtStatus.setTextColor(mContext.getResources().getColor(R.color.finger_warning));
    }

    @Override
    public void onHelp(int helpMsgId, String help) {
        txtStatus.setText(help);
        txtStatus.setTextColor(mContext.getResources().getColor(R.color.finger_warning));
    }

    @Override
    public void onError(int errMsgId, String error) {
        txtStatus.setText(error);
        txtStatus.setTextColor(mContext.getResources().getColor(R.color.finger_warning));

        helper.stopFingerprintAuthListening();
    }

    private OnCheckFingerprintListener mListener = null;

    public void setOnCheckFingerprintListener(OnCheckFingerprintListener listener) {
        mListener = listener;
    }

    public interface OnCheckFingerprintListener {
        void onChecked();
    }
}
