package foundation.icon.iconex.view.ui.prep.vote;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.iconex.widgets.ToolTip;

public class MyVoteListAdapter extends RecyclerView.Adapter {
    private static final String TAG = MyVoteListAdapter.class.getSimpleName();

    private Context context;
    private Activity root;
    private List<Delegation> delegations = new ArrayList<>();

    public MyVoteListAdapter(Context context, List<Delegation> delegations, Activity root) {
        this.context = context;

        if (delegations != null)
            this.delegations = delegations;

        this.root = root;
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
        if (holder instanceof MenuView.ItemView) {
            ItemViewHolder h = (ItemViewHolder) holder;
            Delegation delegation = delegations.get(position);

            h.tvPrepName.setText(delegation.getPrepName());
            h.tvPrepGrade.setText(String.format(Locale.getDefault(),
                    "(%s)", delegation.getGrade().getLabel()));
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

            v.findViewById(R.id.layout_info).setOnClickListener(this);

            tvPrepName = v.findViewById(R.id.prep_name);
            tvPrepGrade = v.findViewById(R.id.prep_grade);

            layoutVotes = v.findViewById(R.id.layout_votes);
            layoutTotalVotes = v.findViewById(R.id.layout_total_votes);
            tvTotalVotes = v.findViewById(R.id.txt_total_votes);
            layoutMyVotes = v.findViewById(R.id.layout_my_votes);
            tvMyVotes = v.findViewById(R.id.txt_my_votes);

            btnManage = v.findViewById(R.id.btn_prep_manage);
            btnManage.setBackgroundResource(R.drawable.bg_btn_prep_delete);
            btnManage.setOnClickListener(this);

            if (!delegations.get(getAdapterPosition()).getValue().equals(BigInteger.ZERO))
                btnManage.setEnabled(false);

            layoutGraph = v.findViewById(R.id.layout_graph);
            editDelegation = v.findViewById(R.id.edit_value);
            txtPercent = v.findViewById(R.id.txt_percentage);
            seekbar = v.findViewById(R.id.vote_seek_bar);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.layout_info:
                    if (layoutGraph.getVisibility() == View.GONE) {
                        layoutGraph.setVisibility(View.VISIBLE);
                        layoutMyVotes.setVisibility(View.GONE);
                    } else {
                        layoutGraph.setVisibility(View.GONE);
                        layoutMyVotes.setVisibility(View.VISIBLE);
                    }
                    break;

                case R.id.btn_prep_manage:
                    ToolTip toolTip = new ToolTip(context);
                    Delegation delegation = delegations.get(getAdapterPosition());
                    if (delegation.getValue().compareTo(BigInteger.ZERO) > 0) {
                        toolTip.setText(context.getString(R.string.tipHasDelegation));
                        toolTip.setPosition(root, btnManage);
                    } else {

                    }
                    break;
            }
        }
    }

    public void setData(List<Delegation> delegations) {
        this.delegations = delegations;
    }

    public interface OnClickListener {
        void onRemove();

        void onManage();
    }
}
