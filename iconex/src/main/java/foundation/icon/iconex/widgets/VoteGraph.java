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

import java.util.Locale;

import foundation.icon.iconex.R;

public class VoteGraph extends LinearLayout {
    private static final String TAG = VoteGraph.class.getSimpleName();

    private ConstraintLayout graph;
    private View voted, available;
    private TextView votedPercent, availablePercent;

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

        graph = v.findViewById(R.id.graph);
        voted = v.findViewById(R.id.voted);
        available = v.findViewById(R.id.available);

        votedPercent = v.findViewById(R.id.txt_voted_percent);
        availablePercent = v.findViewById(R.id.txt_available_percent);

        setVoted(0);

        addView(v);
    }

    private void setVoted(float percent) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(graph);
        constraintSet.setHorizontalWeight(R.id.voted, percent);
        constraintSet.setHorizontalWeight(R.id.available, 100 - percent);
        constraintSet.applyTo(graph);

        votedPercent.setText(String.format(Locale.getDefault(), "%.1f", percent));
        availablePercent.setText(String.format(Locale.getDefault(), "%.1f", 100 - percent));
    }
}
