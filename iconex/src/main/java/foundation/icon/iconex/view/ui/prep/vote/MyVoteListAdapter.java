package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.MyEditText;

public class MyVoteListAdapter extends RecyclerView.Adapter {
    private static final String TAG = MyVoteListAdapter.class.getSimpleName();

    private Context context;
    private List<Delegation> delegations = new ArrayList<>();

    public MyVoteListAdapter(Context context, List<Delegation> delegations) {
        this.context = context;

        if (delegations != null)
            this.delegations = delegations;
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
            btnManage.setOnClickListener(this);

            layoutGraph = v.findViewById(R.id.layout_graph);
            editDelegation = v.findViewById(R.id.edit_value);
            txtPercent = v.findViewById(R.id.txt_percentage);
            seekbar = v.findViewById(R.id.vote_seek_bar);
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.layout_info:
                    if (layoutGraph.getVisibility() == View.GONE)
                        layoutGraph.setVisibility(View.VISIBLE);
                    else
                        layoutGraph.setVisibility(View.GONE);
                    break;

                case R.id.btn_prep_manage:
                    break;
            }
        }
    }

    public interface OnClickListener {
        void onRemove();

        void onManage();
    }
}
