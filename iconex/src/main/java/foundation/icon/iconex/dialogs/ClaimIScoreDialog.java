package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;

public class ClaimIScoreDialog extends MessageDialog {
    private static final String TAG = ClaimIScoreDialog.class.getSimpleName();

    private View v;
    private String current, received, step, fee, feeUsd;

    public ClaimIScoreDialog(Context context) {
        super(context);

        initView();
    }

    private void initView() {
        v = View.inflate(getContext(), R.layout.dialog_claim_iscore, null);
        setContent(v);

        setHeadText(getContext().getString(R.string.IScoreClaim));
    }

    public void setData() {
        ((TextView) v.findViewById(R.id.txt_current_iscore)).setText(current);
        ((TextView) v.findViewById(R.id.txt_receive_icx)).setText(received);
        ((TextView) v.findViewById(R.id.txt_limit_price)).setText(step);
        ((TextView) v.findViewById(R.id.txt_fee)).setText(fee);
        ((TextView) v.findViewById(R.id.txt_fee_usd)).setText(feeUsd);
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public void setFeeUsd(String feeUsd) {
        this.feeUsd = feeUsd;
    }
}
