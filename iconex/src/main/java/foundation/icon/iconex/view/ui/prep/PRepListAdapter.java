package foundation.icon.iconex.view.ui.prep;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.prep.vote.VoteViewModel;
import foundation.icon.iconex.widgets.ToolTip;

public class PRepListAdapter extends RecyclerView.Adapter {
    private static final String TAG = PRepListAdapter.class.getSimpleName();

    private final Context mContext;
    private final Type mType;
    private List<PRep> preps;
    private List<Delegation> delegations;

    private Activity root;
    private VoteViewModel vm;

    public PRepListAdapter(Context context, Type type, List<PRep> preps) {
        mContext = context;
        mType = type;
        this.preps = preps;
    }

    public PRepListAdapter(Context context, Type type, List<PRep> preps, Activity root) {
        mContext = context;
        mType = type;
        this.preps = preps;
        this.root = root;

        vm = ViewModelProviders.of((AppCompatActivity) root).get(VoteViewModel.class);
        delegations = vm.getDelegations().getValue();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.item_voting, parent, false);
        return new ItemVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemVH h = (ItemVH) holder;
        PRep prep = preps.get(position);
        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) h.layoutVotes.getLayoutParams();

        switch (mType) {
            case NORMAL:
                h.btnManage.setVisibility(View.GONE);
                h.layoutMyVotes.setVisibility(View.GONE);
                layoutParams.setMargins(layoutParams.getMarginStart(),
                        (int) mContext.getResources().getDimension(R.dimen.dp12),
                        layoutParams.getMarginEnd(),
                        (int) mContext.getResources().getDimension(R.dimen.dp25));
                h.layoutVotes.setLayoutParams(layoutParams);
                h.tvPrepName.setText(prep.getName());
                h.tvPrepGrade.setText(String.format(Locale.getDefault(),
                        "(%s)", prep.getGrade().getLabel()));
                h.tvTotalVotes.setText(String.format(Locale.getDefault(),
                        "%s(%s%%)",
                        Utils.formatFloating(Double.toString(prep.getDelegated().doubleValue()), 4),
                        Utils.formatFloating(Double.toString(prep.delegatedPercent()), 1)));
                break;

            case VOTE:
                try {
                    for (Delegation d : delegations) {
                        if (prep.getAddress().equals(d.getAddress()))
                            h.btnManage.setSelected(true);
                    }
                } catch (NullPointerException e) {
                    // Do nothing.
                }

                h.layoutMyVotes.setVisibility(View.GONE);
                layoutParams.setMargins(layoutParams.getMarginStart(),
                        (int) mContext.getResources().getDimension(R.dimen.dp12),
                        layoutParams.getMarginEnd(),
                        (int) mContext.getResources().getDimension(R.dimen.dp25));
                h.layoutVotes.setLayoutParams(layoutParams);
                h.tvPrepName.setText(prep.getName());
                h.tvPrepGrade.setText(String.format(Locale.getDefault(),
                        "(%s)", prep.getGrade().getLabel()));
                h.tvTotalVotes.setText(String.format(Locale.getDefault(),
                        "%s(%s%%)",
                        Utils.formatFloating(Double.toString(prep.getDelegated().doubleValue()), 4),
                        Utils.formatFloating(Double.toString(prep.delegatedPercent()), 1)));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return preps.size();
    }

    class ItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvPrepName, tvPrepGrade;
        private ViewGroup layoutVotes, layoutTotalVotes, layoutMyVotes;
        private TextView tvTotalVotes, tvMyVotes;
        private ImageButton btnManage;

        ItemVH(@NonNull View v) {
            super(v);

            tvPrepName = v.findViewById(R.id.prep_name);
            tvPrepGrade = v.findViewById(R.id.prep_grade);

            layoutVotes = v.findViewById(R.id.layout_votes);
            layoutTotalVotes = v.findViewById(R.id.layout_total_votes);
            tvTotalVotes = v.findViewById(R.id.txt_total_votes);
            layoutMyVotes = v.findViewById(R.id.layout_my_votes);
            tvMyVotes = v.findViewById(R.id.txt_my_votes);

            btnManage = v.findViewById(R.id.btn_prep_manage);
            btnManage.setOnClickListener(this);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_prep_manage:
                    ToolTip toolTip = new ToolTip(mContext);
                    if (btnManage.isSelected()) {
                        toolTip.setText(mContext.getString(R.string.tipAddedPRep));
                        toolTip.setPosition(root, btnManage);
                    } else {
                        if (delegations.size() == 10) {
                            toolTip.setText(mContext.getString(R.string.tipPRepMax));
                            toolTip.setPosition(root, btnManage);
                        } else {
                            PRep prep = preps.get(getAdapterPosition());
                            Delegation delegation = new Delegation.Builder()
                                    .address(prep.getAddress())
                                    .grade(prep.getGrade())
                                    .name(prep.getName())
                                    .build();
                            delegations.add(delegation);
                            vm.setDelegations(delegations);

                            notifyItemChanged(getAdapterPosition());

                            if (mAddListener != null)
                                mAddListener.onAdd(prep);
                        }
                    }
                    break;
            }
        }
    }

    public void setData(List<PRep> preps) {
        this.preps = preps;
    }

    public void setDelegations(List<Delegation> delegations) {
        this.delegations = delegations;
    }

    private OnPRepAddListener mAddListener = null;

    public void setOnPRepAddListener(OnPRepAddListener listener) {
        mAddListener = listener;
    }

    public interface OnPRepAddListener {
        void onAdd(PRep prep);
    }

    public enum Type {
        NORMAL,
        VOTE
    }
}
