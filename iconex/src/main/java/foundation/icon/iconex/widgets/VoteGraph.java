package foundation.icon.iconex.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import foundation.icon.iconex.R;

public class VoteGraph extends LinearLayout {
    private static final String TAG = VoteGraph.class.getSimpleName();

    private View voted, available;
    private TextView votedPercent, availablePercent;

    private BigDecimal total, delegation;

    public VoteGraph(Context context) {
        super(context);

        initView();
    }

    public VoteGraph(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    public VoteGraph(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initView();
    }

    private void initView() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.layout_vote_graph, this, false);

        voted = v.findViewById(R.id.voted);
        votedPercent = v.findViewById(R.id.txt_voted_percent);
        available = v.findViewById(R.id.available);
        availablePercent = v.findViewById(R.id.txt_available_percent);

        addView(v);
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public void setDelegation(BigDecimal delegation) {
        this.delegation = delegation;
    }

    public void updateGraph() {
        float delegationPer;

        try {
            delegationPer = delegation.divide(total, 4, RoundingMode.FLOOR).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP).floatValue();
        } catch (Exception e) {
            delegationPer = 0.0f;
        }

        if (delegationPer == 0) {
            available.setBackgroundResource(R.drawable.bg_graph_unstake);
        } else if (delegationPer == 100) {
            voted.setBackgroundResource(R.drawable.bg_graph_stake);
        } else {
            voted.setBackgroundResource(R.drawable.bg_graph_stake_p);
            available.setBackgroundResource(R.drawable.bg_graph_unstake_p);
        }

        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone((ConstraintLayout) findViewById(R.id.root));
        constraintSet.setHorizontalWeight(R.id.voted, delegationPer);
        constraintSet.setHorizontalWeight(R.id.available, 100 - delegationPer);
        constraintSet.applyTo(findViewById(R.id.root));

        votedPercent.setText(String.format(Locale.getDefault(), "%.1f%%", delegationPer));
        availablePercent.setText(String.format(Locale.getDefault(), "%.1f%%", 100 - delegationPer));
    }

    public void updateGraph(BigDecimal delegation) {
        setDelegation(delegation);
        updateGraph();
    }
}
