package foundation.icon.iconex.dialogs;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.widgets.CustomToast;
import kotlin.jvm.functions.Function1;

import static foundation.icon.ICONexApp.network;

public class TxHashDialog extends MessageDialog {

    private TextView mTxHash;
    private TextView mLnkTacker;

    private String txHash;

    public TxHashDialog(@NotNull Context context, String txHash) {
        super(context);
        this.txHash = txHash;
        buildDialog();
    }

    private void buildDialog() {
        // set Head
        setHeadText(getContext().getString(R.string.dialogHeadTextTxHash));

        // set Content
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dlg_txhash_content, null, false);
        setContent(v);

        // load content ui
        mTxHash = v.findViewById(R.id.txt_tx_hash);
        mLnkTacker = v.findViewById(R.id.lnk_tracker);

        mTxHash.setText(txHash);

        // set button
        setSingleButton(false);
        setConfirmButtonText(getContext().getString(R.string.dialogTxHashCopyTxHash));

        // add button event
        mLnkTacker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                getContext().startActivity(intent);
                dismiss();
            }
        });

        setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("TxHash", txHash);
                clipboard.setPrimaryClip(data);

                CustomToast.makeText(getContext(), getContext().getString(R.string.msgCopyTxID), Toast.LENGTH_SHORT).show();
                return null;
            }
        });
    }
}
