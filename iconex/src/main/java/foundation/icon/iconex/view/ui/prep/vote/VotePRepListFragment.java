package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.PRepListActivity;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.view.ui.prep.PRepListAdapter;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.DividerItemDecorator;
import io.reactivex.disposables.Disposable;

import static foundation.icon.iconex.view.PRepListActivity.Sort.RankAscending;

public class VotePRepListFragment extends Fragment {
    private static final String TAG = VotePRepListFragment.class.getSimpleName();

    private VoteViewModel vm;
    private OnVotePRepListListener mListener;

    private RecyclerView list;
    private PRepListAdapter adapter;
    private ViewGroup sort;
    private TextView sortRank, sortName;
    private PRepListActivity.Sort sortType = RankAscending;

    private Wallet wallet;
    private List<PRep> prepList;
    private List<Delegation> delegations = new ArrayList<>();
    private List<PRep> sortList = new ArrayList<>();

    private Disposable disposable;

    public VotePRepListFragment() {
        // Required empty public constructor
    }

    public static VotePRepListFragment newInstance() {
        return new VotePRepListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(VoteViewModel.class);
        wallet = vm.getWallet().getValue();
        delegations = vm.getDelegations().getValue();
        prepList = vm.getPreps().getValue();

        vm.getDelegations().observe(this, new Observer<List<Delegation>>() {
            @Override
            public void onChanged(List<Delegation> delegations) {
                VotePRepListFragment.this.delegations = delegations;
                if (adapter != null) {
                    adapter.setDelegations(delegations);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vote_prep_list, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVotePRepListListener) {
            mListener = (OnVotePRepListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVotePRepListListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (prepList != null && prepList.size() > 0) {
            adapter = new PRepListAdapter(getContext(),
                    PRepListAdapter.Type.VOTE,
                    prepList, getActivity());
            adapter.setDelegations(delegations);
            list.setAdapter(adapter);
        }
    }

    private void initView(View v) {
        list = v.findViewById(R.id.list);
        list.setFocusable(false);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecorator(
                        getContext(),
                        ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        list.addItemDecoration(itemDecoration);

        sortRank = v.findViewById(R.id.sort_rank);
        sortName = v.findViewById(R.id.sort_name);
        sort = v.findViewById(R.id.sort);
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (sortType) {
                    case RankAscending:
                        sortList.addAll(prepList);

                        Collections.reverse(sortList);
                        adapter = new PRepListAdapter(
                                getActivity(),
                                PRepListAdapter.Type.VOTE,
                                sortList);

                        list.setAdapter(adapter);
                        sortType = PRepListActivity.Sort.RankDescending;
                        sortRank.setText(getString(R.string.rankAscending));
                        break;

                    case RankDescending:
                        sortList = new ArrayList<>();
                        sortList.addAll(prepList);

                        Collections.sort(sortList, new Comparator<PRep>() {
                            @Override
                            public int compare(PRep o1, PRep o2) {
                                try {
                                    Integer i1 = Integer.parseInt(o1.getName());
                                    Integer i2 = Integer.parseInt(o2.getName());

                                    return i1.compareTo(i2);
                                } catch (Exception e) {
                                    return o1.getName().compareToIgnoreCase(o2.getName());
                                }
                            }
                        });

                        adapter = new PRepListAdapter(
                                getActivity(),
                                PRepListAdapter.Type.VOTE,
                                sortList);

                        list.setAdapter(adapter);
                        sortType = PRepListActivity.Sort.NameAscending;

                        TextViewCompat.setTextAppearance(sortRank, R.style.SearchTextAppearanceN);
                        TextViewCompat.setTextAppearance(sortName, R.style.SearchTextAppearanceS);
                        break;

                    case NameAscending:
                        Collections.reverse(sortList);

                        adapter = new PRepListAdapter(
                                getActivity(),
                                PRepListAdapter.Type.VOTE,
                                sortList);

                        list.setAdapter(adapter);
                        sortType = PRepListActivity.Sort.NameDescending;
                        break;

                    case NameDescending:
                        if (sortList != null) {
                            sortList = new ArrayList<>();
                            sortList.addAll(prepList);
                        } else {
                            sortList.addAll(prepList);
                        }

                        adapter = new PRepListAdapter(
                                getActivity(),
                                PRepListAdapter.Type.VOTE,
                                sortList);

                        list.setAdapter(adapter);
                        sortType = RankAscending;

                        TextViewCompat.setTextAppearance(sortRank, R.style.SearchTextAppearanceS);
                        TextViewCompat.setTextAppearance(sortName, R.style.SearchTextAppearanceN);
                        sortRank.setText(getString(R.string.rankDecending));
                        break;
                }
            }
        });
    }

    public void updatePRepList(List<Delegation> delegations) {
        this.delegations = delegations;
        adapter.setDelegations(this.delegations);
        adapter.notifyDataSetChanged();

        vm.setDelegations(this.delegations);
    }

    public interface OnVotePRepListListener {
    }
}
