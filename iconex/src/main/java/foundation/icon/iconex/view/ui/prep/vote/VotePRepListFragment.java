package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InterruptedIOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.view.ui.prep.PRepListAdapter;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.DividerItemDecorator;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class VotePRepListFragment extends Fragment {
    private static final String TAG = VotePRepListFragment.class.getSimpleName();

    private VoteViewModel vm;
    private OnVotePRepListListener mListener;

    private ImageButton btnSearch;
    private RecyclerView list;
    private PRepListAdapter adapter;

    private Wallet wallet;
    private List<PRep> prepList;
    private List<Delegation> delegations = new ArrayList<>();

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
            adapter.setOnPRepAddListener(mAddListener);
            list.setAdapter(adapter);
        }

        getPRepList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        if (!disposable.isDisposed())
            disposable.dispose();
    }

    private void initView(View v) {
        list = v.findViewById(R.id.list);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecorator(
                        getContext(),
                        ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        list.addItemDecoration(itemDecoration);
    }

    private void getPRepList() {
        disposable = Observable.create(new ObservableOnSubscribe<List<PRep>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PRep>> emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                try {
                    RpcItem result = pRepService.getPreps();
                    BigInteger totalDelegated =
                            ConvertUtil.hexStringToBigInt(
                                    result.asObject().getItem("totalDelegated").asString(), 0);
                    List<PRep> list = new ArrayList<>();
                    for (RpcItem i : result.asObject().getItem("preps").asArray().asList()) {
                        RpcObject object = i.asObject();
                        PRep prep = PRep.valueOf(object);
                        prep.setTotalDelegated(totalDelegated);
                        list.add(prep);
                    }

                    emitter.onNext(list);
                    emitter.onComplete();
                } catch (InterruptedIOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<PRep>>() {

                    @Override
                    public void onNext(List<PRep> pReps) {
                        prepList = pReps;
                        vm.setPreps(prepList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                        adapter = new PRepListAdapter(
                                getContext(),
                                PRepListAdapter.Type.VOTE,
                                prepList, getActivity());
                        adapter.setOnPRepAddListener(mAddListener);

                        list.setAdapter(adapter);
                    }
                });
    }

    private PRepListAdapter.OnPRepAddListener mAddListener = new PRepListAdapter.OnPRepAddListener() {
        @Override
        public void onAdd(PRep prep) {
            RealmUtil.addMyVote(wallet.getAddress(), prep);
        }
    };

    public interface OnVotePRepListListener {
    }
}
