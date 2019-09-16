package foundation.icon.iconex.view.ui.prep.vote;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.view.ui.prep.PRepListAdapter;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class VotePRepListFragment extends Fragment {
    private static final String TAG = VotePRepListFragment.class.getSimpleName();

    private VoteViewModel vm;
    private OnVotePRepListListener mListener;

    private RecyclerView list;
    private PRepListAdapter adapter;

    private List<PRep> pReps;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_vote_prep_list, container, false);
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

        getPRepList();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;

        if (!disposable.isDisposed())
            disposable.dispose();
    }

    private void getPRepList() {
        disposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {

            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    public interface OnVotePRepListListener {
    }
}
