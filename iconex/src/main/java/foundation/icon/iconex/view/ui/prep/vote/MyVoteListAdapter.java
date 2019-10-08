package foundation.icon.iconex.view.ui.prep.vote;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.iconex.widgets.ToolTip;

public class MyVoteListAdapter extends RecyclerView.Adapter {
    private static final String TAG = MyVoteListAdapter.class.getSimpleName();

    private Context context;
    private Activity root;
    private VoteViewModel vm;

    private List<Delegation> delegations = new ArrayList<>();
    private List<Delegation> votingList = new ArrayList<>();

    private int currentManage = -1;

    public MyVoteListAdapter(Context context, List<Delegation> delegations, Activity root) {
        this.context = context;

        if (delegations != null)
            this.delegations = delegations;

        this.root = root;

        vm = ViewModelProviders.of((FragmentActivity) root).get(VoteViewModel.class);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;
        if (delegations.size() == 0) {
            v = inflater.inflate(R.layout.layout_my_vote_empty, parent, false);
            return new EmptyViewHolder(v);
        } else {
            v = inflater.inflate(R.layout.item_voting, parent, false);
            return new ItemViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            ItemViewHolder h = (ItemViewHolder) holder;
            Delegation delegation = delegations.get(position);
            PRep pRep = delegation.getPrep();

            h.tvPrepName.setText(pRep.getName());
            h.tvPrepGrade.setText(String.format(Locale.getDefault(),
                    "(%s)", pRep.getGrade().getLabel()));

            h.tvTotalVotes.setText(String.format(Locale.getDefault(), "%s (%s%%)",
                    Utils.formatFloating(ConvertUtil.getValue(pRep.getDelegated(), 18), 4),
                    Utils.formatFloating(Double.toString(pRep.delegatedPercent()), 1)));

            float votePercent;
            if (delegation.getValue().equals(BigInteger.ZERO))
                votePercent = 0.0f;
            else {
                votePercent = delegation.getValue().floatValue() / pRep.getDelegated().floatValue() * 100;
            }
            h.tvMyVotes.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                    Utils.formatFloating(ConvertUtil.getValue(delegation.getValue(), 18), 4),
                    votePercent));

            h.btnManage.setImageResource(R.drawable.bg_btn_prep_delete);
            if (!delegation.getValue().equals(BigInteger.ZERO))
                h.btnManage.setSelected(true);

            h.layoutGraph.setVisibility(View.GONE);
            h.layoutMyVotes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (delegations.size() == 0)
            return 1;
        else
            return delegations.size();
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ViewGroup rootView;
        private TextView tvPrepName, tvPrepGrade;
        private ViewGroup layoutVotes, layoutTotalVotes, layoutMyVotes;
        private TextView tvTotalVotes, tvMyVotes;
        private ImageButton btnManage;
        private ViewGroup layoutGraph;
        private MyEditText editDelegation;
        private TextView txtPercent;
        private CustomSeekbar seekbar;

        public ItemViewHolder(@NonNull View v) {
            super(v);

            rootView = v.findViewById(R.id.root);

            v.findViewById(R.id.layout_info).setOnClickListener(this);

            tvPrepName = v.findViewById(R.id.prep_name);
            tvPrepGrade = v.findViewById(R.id.prep_grade);

            layoutVotes = v.findViewById(R.id.layout_votes);
            layoutTotalVotes = v.findViewById(R.id.layout_total_votes);
            tvTotalVotes = v.findViewById(R.id.txt_total_votes);
            layoutMyVotes = v.findViewById(R.id.layout_my_votes);
            tvMyVotes = v.findViewById(R.id.txt_my_votes);

            btnManage = v.findViewById(R.id.btn_prep_manage);
            btnManage.setOnClickListener(this);

            layoutGraph = v.findViewById(R.id.layout_graph);
            editDelegation = v.findViewById(R.id.edit_value);
            editDelegation.addTextChangedListener(textWatcher);
            txtPercent = v.findViewById(R.id.txt_percentage);
            seekbar = v.findViewById(R.id.vote_seek_bar);
            seekbar.setOnSeekBarChangeListener(seekBarChangeListener);
        }

        @Override
        public void onClick(View view) {
            Delegation delegation = delegations.get(getAdapterPosition());

            switch (view.getId()) {
                case R.id.layout_info:
                    if (layoutGraph.getVisibility() == View.GONE) {
                        if (currentManage > -1)
                            notifyItemChanged(currentManage);

                        layoutGraph.setVisibility(View.VISIBLE);
                        layoutMyVotes.setVisibility(View.GONE);

                        currentManage = getAdapterPosition();

                        BigInteger votingPower = vm.getVotingPower().getValue();
                        BigInteger available = votingPower.add(delegation.getValue());

                        float votePercent;
                        if (delegation.getValue().equals(BigInteger.ZERO)) {
                            votePercent = 0.0f;
                            editDelegation.setText("0.0000");
                        } else {
                            votePercent = delegation.getValue().floatValue() / available.floatValue() * 100;
                            editDelegation.setText(Utils.formatFloating(ConvertUtil.getValue(delegation.getValue(), 18), 4));
                        }

                        txtPercent.setText(String.format(Locale.getDefault(), "(%.1f%%)", votePercent));
                        seekbar.setProgress(90);
                    } else {
                        layoutGraph.setVisibility(View.GONE);
                        layoutMyVotes.setVisibility(View.VISIBLE);

                        currentManage = -1;
                    }
                    break;

                case R.id.btn_prep_manage:
                    ToolTip toolTip = new ToolTip(context);
                    if (delegation.getValue().compareTo(BigInteger.ZERO) > 0) {
                        toolTip.setText(context.getString(R.string.tipHasDelegation));
                        toolTip.setPosition(root, btnManage);
                    } else {
                        delegations.remove(getAdapterPosition());
                        vm.setDelegations(delegations);
                        notifyDataSetChanged();
                    }
                    break;
            }
        }

        private TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }

    public void setData(List<Delegation> delegations) {
        this.delegations = delegations;
    }

    public void setMax(int max) {

    }

    private OnClickListener mListener = null;

    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        void onRemove();

        void onManage();

        void onDetail();
    }
}
