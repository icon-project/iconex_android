package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;

public class PRepVoteFragment extends Fragment {
    private static final String TAG = PRepVoteFragment.class.getSimpleName();

    private VoteViewModel vm;
    private OnVoteListener mListener;
    private Wallet wallet;

    private RecyclerView list;
    private MyVoteListAdapter adapter;

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
                    + " must implement OnVoteLitener");
        }
    }

    private void initView(View v) {

    }

    public interface OnVoteListener {
    }
}
