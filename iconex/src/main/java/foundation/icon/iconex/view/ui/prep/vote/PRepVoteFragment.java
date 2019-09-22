package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.VoteGraph;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

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
        delegations = RealmUtil.loadMyVotes(wallet.getAddress());
        vm.setDelegations(delegations);
        vm.getDelegations().observe(this, new Observer<List<Delegation>>() {
            @Override
            public void onChanged(List<Delegation> delegations) {
                if (adapter != null && delegations != null) {
                    adapter.setData(delegations);
                    adapter.notifyDataSetChanged();
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
    public void onStart() {
        super.onStart();

        getDelegations();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (mListener != null)
            mListener = null;

        if (prepDisposable != null && !prepDisposable.isDisposed())
            prepDisposable.dispose();

        if (delegationsDisposable != null && !delegationsDisposable.isDisposed())
            delegationsDisposable.dispose();
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

    private void getDelegations() {
        delegationsDisposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                try {
                    RpcItem result = pRepService.getDelegation(wallet.getAddress());
                    RpcObject object = result.asObject();
                    totalDelegated = object.getItem("totalDelegated").asInteger();
                    votingPower = object.getItem("votingPower").asInteger();

                    if (object.getItem("delegations") != null) {
                        RpcArray array = object.getItem("delegations").asArray();
                        for (RpcItem i : array.asList()) {
                            RpcObject o = i.asObject();
                            Delegation delegation = new Delegation.Builder()
                                    .address(o.getItem("address").asString())
                                    .value(o.getItem("value").asInteger())
                                    .build();
                            delegations.add(delegation);
                        }
                    }

                    emitter.onComplete();
                } catch (InterruptedIOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        getPRep();

                        txtVotedCount.setText(String.format(Locale.getDefault(), "(%d/10)",
                                delegations.size()));
                        txtVotedIcx.setText(Utils.formatFloating(
                                ConvertUtil.getValue(totalDelegated, 18), 4));
                        txtAvailableIcx.setText(Utils.formatFloating(
                                ConvertUtil.getValue(votingPower, 18), 4));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void getPRep() {
        prepDisposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                RpcItem result;
                RpcObject o;

                for (int i = 0; i < delegations.size(); i++) {
                    Delegation d = delegations.get(i);
                    result = pRepService.getPrep(d.getAddress());
                    o = result.asObject();
                    d = d.newBuilder()
                            .name(o.getItem("name").asString())
                            .grade(PRep.Grade.fromGrade(
                                    o.getItem("grade").asInteger().intValue()))
                            .build();
                    delegations.set(i, d);
                }

                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        adapter = new MyVoteListAdapter(PRepVoteFragment.this.getContext(),
                                delegations);
                        list.setAdapter(adapter);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    public interface OnVoteListener {
    }
}
