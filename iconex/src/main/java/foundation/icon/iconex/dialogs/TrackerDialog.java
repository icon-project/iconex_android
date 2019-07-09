package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;

import static foundation.icon.ICONexApp.network;

/**
 * Created by js on 2018. 3. 29..
 */

public class TrackerDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private String txHash;

    private TextView txtTxHash;
    private Button btnCopy, btnTracker;
    private Button btnClose;

    public TrackerDialog(@NonNull Context context, @NonNull String txHash) {
        super(context);

        mContext = context;
        this.txHash = txHash;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.dialog_tracker);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        txtTxHash = findViewById(R.id.txt_tx_hash);
        txtTxHash.setText(txHash);

        btnCopy = findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(this);
        btnTracker = findViewById(R.id.btn_tracker);
        btnTracker.setOnClickListener(this);

        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_copy:
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("TxHash", txHash);
                clipboard.setPrimaryClip(data);

                Toast.makeText(mContext, mContext.getString(R.string.msgCopyTxID), Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_tracker:
                String tracker = null;
                switch (network) {
                    case MyConstants.NETWORK_MAIN:
                        tracker = ServiceConstants.URL_TRACKER_MAIN;
                        break;

                    case MyConstants.NETWORK_TEST:
                        tracker = ServiceConstants.URL_TRACKER_TEST;
                        break;

                    case MyConstants.NETWORK_DEV:
                        tracker = ServiceConstants.DEV_TRACKER;
                        break;
                }

                String url = tracker + txHash;
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mContext.startActivity(intent);
                dismiss();
                break;

            case R.id.btn_close:
                dismiss();
                break;
        }
    }
}
