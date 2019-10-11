package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.tabs.TabLayout;

import java.io.InterruptedIOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.LoadingDialog;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.view.ui.prep.vote.PRepVoteFragment;
import foundation.icon.iconex.view.ui.prep.vote.VotePRepListFragment;
import foundation.icon.iconex.view.ui.prep.vote.VoteViewModel;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PRepVoteActivity extends AppCompatActivity implements PRepVoteFragment.OnVoteListener,
        VotePRepListFragment.OnVotePRepListListener {
    private static final String TAG = PRepVoteActivity.class.getSimpleName();

    private VoteViewModel vm;
    private Wallet wallet;

    private NestedScrollView scroll;
    private TabLayout tabLayout;
    private ImageButton btnSearch;
    private ViewGroup layoutButton;

    private FragmentManager fragmentManager;
    private PRepVoteFragment voteFragment;
    private VotePRepListFragment prepsFragment;

    private LoadingDialog loading;

    private Disposable delegationsDisposable, prepListDisposable, prepDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_vote);

        if (getIntent() != null)
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");

        loading = new LoadingDialog(this, R.style.DialogActivity);

        initData();
        initView();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (delegationsDisposable != null && !delegationsDisposable.isDisposed())
            delegationsDisposable.dispose();

        if (prepListDisposable != null && !prepListDisposable.isDisposed())
            prepListDisposable.dispose();
    }

    private void initData() {
        vm = ViewModelProviders.of(this).get(VoteViewModel.class);
        vm.setWallet(wallet);
        vm.setDelegations(new ArrayList<>());

        fragmentManager = getSupportFragmentManager();
        voteFragment = PRepVoteFragment.newInstance();
        prepsFragment = VotePRepListFragment.newInstance();

        getData();
    }

    private void initView() {
        ((TextView) findViewById(R.id.txt_title)).setText(wallet.getAlias());

        findViewById(R.id.btn_start_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        scroll = findViewById(R.id.scroll);

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(PRepVoteActivity.this, PRepSearchActivity.class)
                                .putExtra("preps", (Serializable) vm.getPreps().getValue())
                                .putExtra("delegations", (Serializable) vm.getDelegations().getValue()),
                        1000);
            }
        });

        layoutButton = findViewById(R.id.layout_button);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        if (fragmentManager.findFragmentByTag("vote") == null) {
                            fragmentManager.beginTransaction().add(R.id.container, voteFragment, "vote").commit();
                        } else {
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("vote")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("list") != null)
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("list")).commit();

                        btnSearch.setVisibility(View.GONE);
                        layoutButton.setVisibility(View.VISIBLE);

                        scroll.fullScroll(ScrollView.FOCUS_DOWN);
                        break;

                    case 1:
                        if (fragmentManager.findFragmentByTag("list") == null) {
                            fragmentManager.beginTransaction().add(R.id.container, prepsFragment, "list").commit();
                        } else {
                            fragmentManager.beginTransaction().show(fragmentManager.findFragmentByTag("list")).commit();
                        }
                        if (fragmentManager.findFragmentByTag("vote") != null)
                            fragmentManager.beginTransaction().hide(fragmentManager.findFragmentByTag("vote")).commit();

                        btnSearch.setVisibility(View.VISIBLE);
                        layoutButton.setVisibility(View.GONE);

                        scroll.fullScroll(ScrollView.FOCUS_DOWN);
                        break;
                }

                ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(tab.getPosition());
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTextAppearance(PRepVoteActivity.this,
                                R.style.TabTextAppearanceS);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(tab.getPosition());
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTextAppearance(PRepVoteActivity.this,
                                R.style.TabTextAppearanceN);
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.addTab(tabLayout.newTab().setText(R.string.myVotes), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.preps));
    }

    private void getData() {
        loading.show();
        getPRepList();
    }

    private void getPRepList() {
        prepListDisposable = Observable.create(new ObservableOnSubscribe<RpcItem>() {
            @Override
            public void subscribe(ObservableEmitter<RpcItem> emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                try {
                    RpcItem result = pRepService.getPreps();

                    emitter.onNext(result);
                    emitter.onComplete();
                } catch (InterruptedIOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<RpcItem>() {
                    @Override
                    public void onNext(RpcItem result) {
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

                        vm.setPrepTotalDelegated(totalDelegated);
                        vm.setPreps(list);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loading.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        getDelegations();
                    }
                });
    }

    private void getDelegations() {
        delegationsDisposable = Observable.create(new ObservableOnSubscribe<RpcObject>() {
            @Override
            public void subscribe(ObservableEmitter<RpcObject> emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                try {
                    RpcItem result = pRepService.getDelegation(wallet.getAddress());
                    RpcObject object = result.asObject();

                    emitter.onNext(object);
                    emitter.onComplete();
                } catch (InterruptedIOException e) {
                    e.printStackTrace();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<RpcObject>() {
                    @Override
                    public void onNext(RpcObject rpcObject) {
                        BigInteger totalDelegated = rpcObject.getItem("totalDelegated").asInteger();
                        BigInteger votingPower = rpcObject.getItem("votingPower").asInteger();

                        List<Delegation> delegations = new ArrayList<>();
                        if (rpcObject.getItem("delegations") != null) {
                            RpcArray array = rpcObject.getItem("delegations").asArray();
                            for (RpcItem i : array.asList()) {
                                RpcObject o = i.asObject();

                                PRep prep = null;
                                for (PRep p : vm.getPreps().getValue()) {
                                    if (p.getAddress().equals(o.getItem("address").asString()))
                                        prep = p;
                                }

                                Delegation delegation = new Delegation.Builder()
                                        .prep(prep)
                                        .value(o.getItem("value").asInteger())
                                        .build();
                                delegations.add(delegation);
                            }
                        }

                        vm.setVoted(totalDelegated);
                        vm.setVotingPower(votingPower);
                        vm.setDelegations(delegations);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loading.dismiss();
                    }

                    @Override
                    public void onComplete() {
                        loading.dismiss();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1000) {

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}