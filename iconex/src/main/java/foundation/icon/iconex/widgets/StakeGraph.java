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
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Locale;

import foundation.icon.iconex.R;

public class StakeGraph extends ConstraintLayout {
    private static final String TAG = StakeGraph.class.getSimpleName();

    private TextView txtStakePer, txtUnstakePer, txtDelegationPer;
    private BigInteger totalBalance = BigInteger.ZERO;
    private BigInteger stake = BigInteger.ZERO;
    private BigInteger delegation = BigInteger.ZERO;

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

    public void setTotalBalance(BigInteger totalBalance) {
        this.totalBalance = totalBalance;
    }

    public void setStake(BigInteger stake) {
        this.stake = stake;

        Log.d(TAG, "setStake(BigInteger)=" + stake);
        setStakeGraph(calculatePercentage(totalBalance, stake));

        if (!delegation.equals(BigInteger.ZERO))
            setDelegationGraph(calculatePercentage(this.stake, delegation));
        else
            setDelegationGraph(0.0f);
    }

    public void setStake(float stakePercent) {
        setStakeGraph(stakePercent);
        Log.d(TAG, "setDelegation(float)=" + delegation);
        setDelegationGraph(calculatePercentage(calculateIcx(stakePercent), delegation));
    }

    public void setDelegation(BigInteger delegation) {
        this.delegation = delegation;
        Log.d(TAG, "setDelegation(BigInteger)=" + delegation);
        setDelegationGraph(calculatePercentage(this.stake, delegation));
    }

    private void setStakeGraph(float stake) {
        float unstakePercentage = 100 - stake;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.constraint_stake));
        constraintSet.setHorizontalWeight(R.id.stake, stake);
        constraintSet.setHorizontalWeight(R.id.unstake, unstakePercentage);
        constraintSet.applyTo(findViewById(R.id.constraint_stake));

        txtStakePer.setText(String.format(Locale.getDefault(), " %.1f%%", stake));
        txtUnstakePer.setText(String.format(Locale.getDefault(), " %.1f%%", unstakePercentage));
    }

    private void setDelegationGraph(float delegation) {
        float delegationPercent = delegation;
        if (delegationPercent >= 100)
            delegationPercent = 100;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.stake));
        constraintSet.setHorizontalWeight(R.id.delegation, delegationPercent);
        constraintSet.setHorizontalWeight(R.id.space, 100 - delegationPercent);
        constraintSet.applyTo(findViewById(R.id.stake));

        txtDelegationPer.setText(String.format(Locale.getDefault(), " %.1f%%", delegationPercent));
    }

    private BigInteger calculateIcx(float percentage) {
        BigInteger percent = new BigInteger(Integer.toString((int) percentage));
        BigInteger multiply = totalBalance.multiply(percent);
        BigInteger icx = multiply.divide(new BigInteger("100"));

        return icx;
    }

    private float calculatePercentage(BigInteger base, BigInteger value) {
        if (value.equals(BigInteger.ZERO))
            return 0.0f;

        BigDecimal baseDec = new BigDecimal(base);
        BigDecimal valueDec = new BigDecimal(value);
        BigDecimal percentDec = valueDec.divide(baseDec, 18, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        Log.d(TAG, "calculatePercent=" + percentDec.floatValue());

        return percentDec.floatValue();
    }
}
