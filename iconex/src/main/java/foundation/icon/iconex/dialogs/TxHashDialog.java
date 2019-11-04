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

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.widgets.CustomToast;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;

public class TxHashDialog extends MessageDialog {

    private TextView mTxHash;
    private TextView mLnkTacker;

    private String coinType;
    private String txHash;

    public TxHashDialog(@NotNull Context context, String txHash, String coinType) {
        super(context);
        this.txHash = txHash;
        this.coinType = coinType;
        buildDialog();
    }

    private void buildDialog() {
        // set Head
        setTitle(getContext().getString(R.string.dialogHeadTextTxHash));

        // set Content
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dlg_txhash_content, null, false);
        setContent(v);

        // load content ui
        mTxHash = v.findViewById(R.id.txt_tx_hash);
        mLnkTacker = v.findViewById(R.id.lnk_tracker);

        int resText = Constants.KS_COINTYPE_ICX.equals(coinType) ?
                R.string.dialogTxHashTrackerLink_ICON : R.string.dialogTxHashTrackerLink_ETHER;
        mLnkTacker.setText(getContext().getString(resText));
        mTxHash.setText(txHash);

        // set button
        setSingleButton(false);
        setConfirmButtonText(getContext().getString(R.string.dialogTxHashCopyTxHash));
        setCancelButtonText(getContext().getString(R.string.close));

        // add button event
        mLnkTacker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tracker;
                String url;
                if (Constants.KS_COINTYPE_ICX.equals(coinType)) {
                    switch (ICONexApp.NETWORK.getNid().intValue()) {
                        case MyConstants.NETWORK_MAIN:
                            tracker = ServiceConstants.URL_TRACKER_MAIN;
                            break;

                        default:
                        case MyConstants.NETWORK_TEST:
                            tracker = ServiceConstants.URL_TRACKER_TEST;
                            break;

                        case MyConstants.NETWORK_DEV:
                            tracker = ServiceConstants.DEV_TRACKER;
                            break;
                    }
                    url = tracker + txHash;

                } else {
                    switch (ICONexApp.NETWORK.getNid().intValue()) {
                        case MyConstants.NETWORK_MAIN:
                            tracker = ServiceConstants.URL_ETHERSCAN;
                            break;
                        default:
                            tracker = ServiceConstants.URL_ROPSTEN;
                            break;
                    }
                    url = tracker + "tx/" + txHash;
                }

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
