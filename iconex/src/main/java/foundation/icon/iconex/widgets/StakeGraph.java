package foundation.icon.iconex.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import foundation.icon.iconex.R;

public class StakeGraph extends ConstraintLayout {
    private static final String TAG = StakeGraph.class.getSimpleName();

    private TextView txtStakePer, txtUnstakePer, txtDelegationPer;
    private BigDecimal total, stake, delegation;

    private BigDecimal THE_HUNDRED = new BigDecimal("100");

    public StakeGraph(Context context) {
        super(context);

        initView();
    }

    public StakeGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    public StakeGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.layout_stake_graph, this, false);

        txtStakePer = v.findViewById(R.id.stake_percentage);
        txtUnstakePer = v.findViewById(R.id.unstake_percentage);
        txtDelegationPer = v.findViewById(R.id.delegation_percentage);

        addView(v);
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setStake(BigDecimal stake) {
        this.stake = stake;
    }

    public void setDelegation(BigDecimal delegation) {
        this.delegation = delegation;
    }

    public void updateGraph() {
        float stakePer, delegationPer, totalDelegationPer;

        try {
            stakePer = stake.divide(total, 18, RoundingMode.FLOOR).multiply(THE_HUNDRED).setScale(1, RoundingMode.HALF_UP).floatValue();
            if (stakePer < 0)
                stakePer = 0.0f;
            else if (stakePer > 100)
                stakePer = 100.0f;
        } catch (Exception e) {
            stakePer = 0.0f;
        }

        try {
            delegationPer = delegation.divide(stake, 18, RoundingMode.FLOOR).multiply(THE_HUNDRED).setScale(1, RoundingMode.HALF_UP).floatValue();
            totalDelegationPer = delegation.divide(total, 18, RoundingMode.FLOOR).multiply(THE_HUNDRED).setScale(1, RoundingMode.HALF_UP).floatValue();
            if (delegationPer < 0)
                delegationPer = 0.0f;
            else if (delegationPer > 100)
                delegationPer = 100.0f;

            if (totalDelegationPer < 0)
                totalDelegationPer = 0.0f;
            else if (delegationPer > 100)
                totalDelegationPer = 100.0f;
        } catch (Exception e) {
            delegationPer = 0.0f;
            totalDelegationPer = 0.0f;
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.constraint_stake));
        constraintSet.setHorizontalWeight(R.id.stake, stakePer);
        constraintSet.setHorizontalWeight(R.id.unstake, 100 - stakePer);
        constraintSet.applyTo(findViewById(R.id.constraint_stake));

        txtStakePer.setText(String.format(Locale.getDefault(), " %.1f%%", stakePer));
        txtUnstakePer.setText(String.format(Locale.getDefault(), " %.1f%%", 100 - stakePer));

        constraintSet.clone((ConstraintLayout) findViewById(R.id.stake));
        constraintSet.setHorizontalWeight(R.id.delegation, delegationPer);
        constraintSet.setHorizontalWeight(R.id.space, 100 - delegationPer);
        constraintSet.applyTo(findViewById(R.id.stake));

        txtDelegationPer.setText(String.format(Locale.getDefault(), " %.1f%%", totalDelegationPer));
    }

    public void updateGraph(BigDecimal stake) {
        setStake(stake);
        updateGraph();
    }
}
