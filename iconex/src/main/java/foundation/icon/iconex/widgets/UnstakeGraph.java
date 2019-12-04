package foundation.icon.iconex.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Locale;

import foundation.icon.iconex.R;

public class UnstakeGraph extends LinearLayout {
    private static final String TAG = UnstakeGraph.class.getSimpleName();

    private TextView txtUnstakedPer, txtStakedIcx, txtStakedPer, txtUnstakeIcx, txtUnstakePer;
    private BigDecimal total, staked, unstake, unstaked;

    private BigDecimal THE_HUNDRED = new BigDecimal("100");

    public UnstakeGraph(Context context) {
        super(context);

        initView();
    }

    public UnstakeGraph(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    public UnstakeGraph(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {
        View v = View.inflate(getContext(), R.layout.layout_unstake_graph, null);

        txtUnstakedPer = v.findViewById(R.id.txt_unstaked_per);
//        txtStakedIcx = v.findViewById(R.id.txt_staked_icx);
        txtStakedPer = v.findViewById(R.id.txt_staked_per);
//        txtUnstakeIcx = v.findViewById(R.id.txt_unstake_icx);
        txtUnstakePer = v.findViewById(R.id.txt_unstake_per);

        addView(v);
    }

    public void updateGraph() {
        float stakedPer = staked.add(unstake).divide(total, 18, RoundingMode.FLOOR).multiply(THE_HUNDRED).floatValue();
        float unstakePer = unstake.divide(total, 18, RoundingMode.FLOOR).multiply(THE_HUNDRED).floatValue();
        float unstakedPer = unstaked.divide(total, 18, RoundingMode.FLOOR).multiply(THE_HUNDRED).floatValue();

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.root));
        constraintSet.setHorizontalWeight(R.id.staked, stakedPer);
        constraintSet.setHorizontalWeight(R.id.unstake, unstakePer);
        constraintSet.setHorizontalWeight(R.id.unstaked, unstakedPer);
        constraintSet.applyTo(findViewById(R.id.root));

        txtUnstakedPer.setText(String.format(Locale.getDefault(), "%.1f%%", unstakedPer));
//        txtStakedIcx.setText(String.format(Locale.getDefault(), "%s ICX", staked.scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR)));
        txtStakedPer.setText(String.format(Locale.getDefault(), "%.1f%%", stakedPer));
//        txtUnstakeIcx.setText(String.format(Locale.getDefault(), "%s ICX", unstake.scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR)));
        txtUnstakePer.setText(String.format(Locale.getDefault(), "%.1f%%", unstakePer));
    }

    public void setTotal(BigInteger total) {
        this.total = new BigDecimal(total);
    }

    public void setStaked(BigInteger staked) {
        this.staked = new BigDecimal(staked);
    }

    public void setUnstake(BigInteger unstake) {
        this.unstake = new BigDecimal(unstake);
    }

    public void setUnstaked(BigInteger unstaked) {
        this.unstaked = new BigDecimal(unstaked);
    }
}
