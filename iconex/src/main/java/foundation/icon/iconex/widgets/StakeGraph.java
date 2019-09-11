package foundation.icon.iconex.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.util.Locale;

import foundation.icon.iconex.R;

public class StakeGraph extends ConstraintLayout {
    private static final String TAG = StakeGraph.class.getSimpleName();

    private TextView stakePer, unstakePer, delegationPer;

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

        stakePer = v.findViewById(R.id.stake_percentage);
        unstakePer = v.findViewById(R.id.unstake_percentage);
        delegationPer = v.findViewById(R.id.delegation_percentage);

        addView(v);
    }

    public void setStake(float stake) {
        float unstakePercentage = 100 - stake;

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.constraint_stake));
        constraintSet.setHorizontalWeight(R.id.stake, stake);
        constraintSet.setHorizontalWeight(R.id.unstake, unstakePercentage);
        constraintSet.applyTo(findViewById(R.id.constraint_stake));

        stakePer.setText(String.format(Locale.getDefault(), " %.1f%%", stake));
        unstakePer.setText(String.format(Locale.getDefault(), " %.1f%%", unstakePercentage));
    }

    public void setDelegation(float delegation) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.stake));
        constraintSet.setHorizontalWeight(R.id.delegation, delegation);
        constraintSet.setHorizontalWeight(R.id.space, 100 - delegation);
        constraintSet.applyTo(findViewById(R.id.stake));

        delegationPer.setText(String.format(Locale.getDefault(), " %.1f%%", delegation));
    }
}
