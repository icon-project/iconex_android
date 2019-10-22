package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.VoteGraph;
import io.reactivex.disposables.Disposable;
import kotlin.jvm.functions.Function1;

public class PRepVoteFragment extends Fragment {
    private static final String TAG = PRepVoteFragment.class.getSimpleName();

    private VoteViewModel vm;
    private Wallet wallet;

    private TextView txtVotedCount, txtVotedIcx, txtAvailableIcx;
    private VoteGraph voteGraph;
    private TextView sort, resetVotes;
    private RecyclerView list;
    private MyVoteListAdapter adapter;

    private List<Delegation> delegations = new ArrayList<>();

    private Disposable delegationsDisposable, prepDisposable;

    private BigInteger totalDelegated, votingPower, stepLimit, stepPrice, fee;

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
                    adapter.setOnVoteChangedListener(new MyVoteListAdapter.OnVoteChangedListener() {
                        @Override
                        public void onVoted(List<Delegation> delegations) {
                            Log.i(TAG, "onVoted");
                            PRepVoteFragment.this.delegations = delegations;
                            setData();
                            mListener.onVoted(delegations);
                        }
                    });
                    list.setAdapter(adapter);
                }

                setData();
            }
        });
        stepPrice = vm.getStepPrice().getValue();
        vm.getStepLimit().observe(this, new Observer<BigInteger>() {
            @Override
            public void onChanged(BigInteger stepLimit) {
                if (!stepLimit.equals(BigInteger.ZERO)
                        && !stepPrice.equals(BigInteger.ZERO)) {

                    fee = stepLimit.multiply(stepPrice);
                }
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
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnVoteFragmentListener) {
            mListener = (OnVoteFragmentListener) context;
        } else {
            throw new RuntimeException("must implement OnVoteFragmentListener");
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
        resetVotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialog messageDialog = new MessageDialog(getContext());
                messageDialog.setSingleButton(false);
                messageDialog.setTitleText(getString(R.string.voteReset));
                messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        mListener.onReset(delegations);
                        return true;
                    }
                });
                messageDialog.show();
            }
        });
        list = v.findViewById(R.id.my_votes);
        list.setFocusable(false);
    }

    private void setData() {
        if (delegations != null) {
            txtVotedCount.setText(String.format(Locale.getDefault(), "(%d/10)", delegations.size()));

            if (delegations.size() != 0)
                resetVotes.setTextColor(getResources().getColor(R.color.dark4D));

            BigDecimal total = new BigDecimal(vm.getTotal().getValue());
            BigDecimal voted = BigDecimal.ZERO;
            for (Delegation d : delegations) {
                voted = voted.add(new BigDecimal(d.getValue()));
            }

            vm.setVoted(voted.toBigInteger());
            vm.setVotingPower(total.subtract(voted).toBigInteger());

            voteGraph.setTotal(total);
            voteGraph.setDelegation(voted);
            voteGraph.updateGraph();

            txtVotedIcx.setText(String.format(Locale.getDefault(), "%s", voted.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString()));
            txtAvailableIcx.setText(String.format(Locale.getDefault(), "%s", total.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString()));
        }
    }

    private OnVoteFragmentListener mListener;

    public interface OnVoteFragmentListener {
        void onVoted(List<Delegation> delegations);

        void onReset(List<Delegation> delegations);
    }
}
