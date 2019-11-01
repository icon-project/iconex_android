package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.LoadingDialog;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.VotingDialog;
import foundation.icon.iconex.service.IconService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.view.ui.prep.vote.PRepVoteFragment;
import foundation.icon.iconex.view.ui.prep.vote.VotePRepListFragment;
import foundation.icon.iconex.view.ui.prep.vote.VoteViewModel;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomToast;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;

public class PRepVoteActivity extends AppCompatActivity implements PRepVoteFragment.OnVoteFragmentListener,
        VotePRepListFragment.OnVotePRepListListener {
    private static final String TAG = PRepVoteActivity.class.getSimpleName();

    private VoteViewModel vm;
    private Wallet wallet;
    private String privateKey;

    private NestedScrollView scroll;
    private TabLayout tabLayout;
    private ImageButton btnSearch;
    private ViewGroup layoutButton;
    private Button btnSubmit;

    private FragmentManager fragmentManager;
    private PRepVoteFragment voteFragment;
    private VotePRepListFragment prepsFragment;

    private LoadingDialog loading;

    private BigInteger stepLimit, stepPrice, fee;
    private Disposable delegationsDisposable, prepListDisposable, prepDisposable;

    private List<Delegation> delegations;
    private VotingDialog votingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_vote);

        if (getIntent() != null) {
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");
            privateKey = getIntent().getStringExtra("privateKey");
        }

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
                if (delegations != null) {
                    boolean isEdited = false;
                    for (Delegation d : delegations) {
                        isEdited = isEdited || d.isEdited();
                        Log.d(TAG, "Delegations is edited=" + isEdited);
                    }

                    if (isEdited) {
                        MessageDialog messageDialog = new MessageDialog(PRepVoteActivity.this);
                        messageDialog.setSingleButton(false);
                        messageDialog.setMessage(getString(R.string.voteNotExecute));
                        messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                            @Override
                            public Boolean invoke(View view) {
                                finish();
                                return true;
                            }
                        });
                        messageDialog.setConfirmButtonText(getString(R.string.yes));
                        messageDialog.setCancelButtonText(getString(R.string.no));
                        messageDialog.show();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
        });

        scroll = findViewById(R.id.scroll);

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(PRepVoteActivity.this, PRepSearchActivity.class)
                                .putExtra("preps", (Serializable) vm.getPreps().getValue()),
                        1000);
            }
        });

        layoutButton = findViewById(R.id.layout_button);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new BigInteger(wallet.getWalletEntries().get(0).getBalance()).compareTo(fee) < 0) {
                    MessageDialog messageDialog = new MessageDialog(PRepVoteActivity.this);
                    messageDialog.setSingleButton(true);
                    messageDialog.setMessage(getString(R.string.errIcxOwnNotEnough));
                    messageDialog.show();

                    return;
                }

                votingDialog = new VotingDialog(PRepVoteActivity.this);
                int voteCount = 0;
                for (Delegation d : delegations) {
                    if (d.isEdited())
                        voteCount++;
                }

                String icx = ConvertUtil.getValue(stepPrice, 18);
                String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                String fee = ConvertUtil.getValue(PRepVoteActivity.this.fee, 18);
                String mFee = fee.indexOf(".") < 0 ? fee : fee.replaceAll("0*$", "").replaceAll("\\.$", "");

                votingDialog.setVoteCount(String.format(Locale.getDefault(), "%d/10", voteCount));
                votingDialog.setLimitNPrice(String.format(Locale.getDefault(), "%s/%s",
                        Utils.formatFloating(stepLimit.toString(), 0), mIcx));
                votingDialog.setFee(mFee);
                votingDialog.setFeeUsd(String.format(Locale.getDefault(), "$%.2f",
                        Double.parseDouble(ConvertUtil.getValue(PRepVoteActivity.this.fee, 18))
                                * Float.parseFloat(ICONexApp.EXCHANGE_TABLE.get("icxusd"))));

                votingDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        setDelegations();
                        return false;
                    }
                });
                votingDialog.show();
            }
        });

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
                IconService iconService = new IconService(ICONexApp.NETWORK.getUrl());

                try {
                    RpcItem result = pRepService.getPreps();
                    stepPrice = iconService.getStepPrice().asInteger();

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
                        List<RpcItem> prepList = result.asObject().getItem("preps").asArray().asList();
                        for (int i = 0; i < prepList.size(); i++) {
                            RpcObject object = prepList.get(i).asObject();
                            PRep prep = PRep.valueOf(object);
                            prep = prep.newBuilder().rank(i + 1).build();
                            prep.setTotalDelegated(totalDelegated);
                            list.add(prep);
                        }

                        vm.setPrepTotalDelegated(totalDelegated);
                        vm.setPreps(list);
                        vm.setStepPrice(stepPrice);
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

                        vm.setTotal(totalDelegated.add(votingPower));
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

    private void setDelegations() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                KeyWallet keyWallet = KeyWallet.load(new Bytes(privateKey));
                if (stepLimit == null)
                    stepLimit = new BigInteger("200000");
                pRepService.setDelegation(keyWallet, delegations, stepLimit);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        if (votingDialog != null && votingDialog.isShowing())
                            votingDialog.dismiss();

                        CustomToast toast = new CustomToast();
                        toast.makeText(PRepVoteActivity.this, getString(R.string.completeVoting), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private Handler localHandler = new Handler();
    private Runnable estimateLimitTask = new Runnable() {
        @Override
        public void run() {
            estimateLimit();
        }
    };

    private void estimateLimit() {
        RpcArray.Builder arrayBuilder = new RpcArray.Builder();
        for (Delegation d : delegations) {
            RpcObject object = new RpcObject.Builder()
                    .put("address", new RpcValue(d.getPrep().getAddress()))
                    .put("value", new RpcValue(ConvertUtil.valueToHexString(ConvertUtil.getValue(d.getValue(), 18), 18)))
                    .build();
            arrayBuilder.add(object);
        }

        RpcObject params = new RpcObject.Builder()
                .put("delegations", arrayBuilder.build())
                .build();

        Transaction transaction = TransactionBuilder.newBuilder()
                .from(new Address(wallet.getAddress()))
                .to(new Address(Constants.ADDRESS_ZERO))
                .value(IconAmount.of("0", IconAmount.Unit.ICX).toLoop())
                .nid(ICONexApp.NETWORK.getNid())
                .call("setDelegation")
                .params(params)
                .build();

        Observable.just(transaction)
                .map(new Function<Transaction, BigInteger>() {
                    @Override
                    public BigInteger apply(Transaction transaction) throws Exception {
                        IconService iconService = new IconService(ICONexApp.NETWORK.getUrl());
                        return iconService.estimateStep(transaction);
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BigInteger>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BigInteger result) {
                        stepLimit = result;
                        fee = stepLimit.multiply(stepPrice);

                        vm.setStepLimit(stepLimit);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        vm.setStepLimit(stepLimit);
                    }
                });
    }

    @Override
    public void onVoted(List<Delegation> delegations) {
        if (delegations == null) {
            btnSubmit.setEnabled(false);
            return;
        }

        if (!btnSubmit.isEnabled())
            btnSubmit.setEnabled(true);

        this.delegations = delegations;

        try {
            localHandler.removeCallbacks(estimateLimitTask);
            localHandler.postDelayed(estimateLimitTask, 500);
        } catch (Exception e) {
            localHandler.postDelayed(estimateLimitTask, 500);
        }
    }

    @Override
    public void onReset(List<Delegation> delegations) {
        this.delegations = delegations;
        if (this.delegations != null && this.delegations.size() > 0) {
            for (int i = 0; i < this.delegations.size(); i++) {
                Delegation reset = this.delegations.get(i).newBuilder().value(BigInteger.ZERO).build();
                this.delegations.set(i, reset);
            }

            setDelegations();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1000) {

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (delegations != null) {
            boolean isEdited = false;
            for (Delegation d : delegations) {
                isEdited = isEdited || d.isEdited();
            }

            if (isEdited) {
                MessageDialog messageDialog = new MessageDialog(PRepVoteActivity.this);
                messageDialog.setSingleButton(false);
                messageDialog.setMessage(getString(R.string.voteNotExecute));
                messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                    @Override
                    public Boolean invoke(View view) {
                        finish();
                        return true;
                    }
                });
                messageDialog.setConfirmButtonText(getString(R.string.yes));
                messageDialog.setCancelButtonText(getString(R.string.no));
                messageDialog.show();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }
}