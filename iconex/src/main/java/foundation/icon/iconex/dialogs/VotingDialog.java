package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import foundation.icon.iconex.R;

public class VotingDialog extends MessageDialog {
    private static final String TAG = VotingDialog.class.getSimpleName();

    private TextView txtVoteCount, txtLimitNPrice, txtFee, txtFeeUsd;

    public VotingDialog(@NotNull Context context) {
        super(context);

        initView();
    }

    private void initView() {
        View v = View.inflate(getContext(), R.layout.dialog_voting, null);
        setContent(v);

        setTitle(getContext().getString(R.string.vote));

        txtVoteCount = v.findViewById(R.id.txt_vote_count);
        txtLimitNPrice = v.findViewById(R.id.txt_limit_price);
        txtFee = v.findViewById(R.id.txt_fee);
        txtFeeUsd = v.findViewById(R.id.txt_fee_usd);

        setSingleButton(false);
    }

    public void setVoteCount(String voteCount) {
        txtVoteCount.setText(voteCount);
    }

    public void setLimitNPrice(String limitNPrice) {
        txtLimitNPrice.setText(limitNPrice);
    }

    public void setFee(String fee) {
        txtFee.setText(fee);
    }

    public void setFeeUsd(String feeUsd) {
        txtFeeUsd.setText(feeUsd);
    }
}
