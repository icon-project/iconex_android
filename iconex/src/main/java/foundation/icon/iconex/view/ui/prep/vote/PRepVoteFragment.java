package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.VoteGraph;
import io.reactivex.disposables.Disposable;

public class PRepVoteFragment extends Fragment {
    private static final String TAG = PRepVoteFragment.class.getSimpleName();

    private VoteViewModel vm;
    private OnVoteListener mListener;
    private Wallet wallet;

    private TextView txtVotedCount, txtVotedIcx, txtAvailableIcx;
    private VoteGraph voteGraph;
    private TextView sort, resetVotes;
    private RecyclerView list;
    private MyVoteListAdapter adapter;

    private List<Delegation> delegations = new ArrayList<>();

    private Disposable delegationsDisposable, prepDisposable;

    private BigInteger totalDelegated, votingPower;

    public PRepVoteFragment() {
        // Required empty public constructor
    }

    public static PRepVoteFragment newInstance() {
        return new PRepVoteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(VoteViewModel.class);
        wallet = vm.getWallet().getValue();
        vm.getDelegations().observe(this, new Observer<List<Delegation>>() {
            @Override
            public void onChanged(List<Delegation> delegations) {
                Log.d(TAG, "Delegate observer onChanged");
                PRepVoteFragment.this.delegations = delegations;
                if (adapter != null && delegations != null) {
                    adapter.setData(delegations);
                    list.setAdapter(adapter);
                } else {
                    adapter = new MyVoteListAdapter(PRepVoteFragment.this.getContext(),
                            delegations, getActivity());
                    list.setAdapter(adapter);
                }

                setDelegation();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_prep_vote, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVoteListener) {
            mListener = (OnVoteListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVoteListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mListener != null)
            mListener = null;
    }

    private void initView(View v) {
        txtVotedCount = v.findViewById(R.id.txt_vote_count);
        voteGraph = v.findViewById(R.id.vote_graph);
        txtVotedIcx = v.findViewById(R.id.txt_voted_icx);
        txtAvailableIcx = v.findViewById(R.id.txt_available_icx);
        sort = v.findViewById(R.id.sort);
        resetVotes = v.findViewById(R.id.reset_votes);
        list = v.findViewById(R.id.my_votes);
    }

    private void setDelegation() {
        txtVotedCount.setText(String.format(Locale.getDefault(), "(%d/10)", delegations.size()));
        if (vm.getVoted().getValue() != null) {
            BigDecimal voted = new BigDecimal(vm.getVoted().getValue());
            BigDecimal votingPower = new BigDecimal(vm.getVotingPower().getValue());
            BigDecimal staked = voted.add(votingPower);

            float votePercent;
            if (voted.equals(BigDecimal.ZERO))
                votePercent = 0.0f;
            else {
                votePercent = voted.floatValue() / staked.floatValue() * 100;
            }

            voteGraph.setVoted(votePercent);
            txtVotedIcx.setText(Utils.formatFloating(ConvertUtil.getValue(vm.getVoted().getValue(), 18), 4));
            txtAvailableIcx.setText(Utils.formatFloating(ConvertUtil.getValue(vm.getVotingPower().getValue(), 18), 4));
        }
    }

    public interface OnVoteListener {
    }
}
