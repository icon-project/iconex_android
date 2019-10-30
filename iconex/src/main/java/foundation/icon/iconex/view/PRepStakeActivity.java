package foundation.icon.iconex.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InterruptedIOException;
import java.math.BigInteger;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.LoadingDialog;
import foundation.icon.iconex.service.IconService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.prep.stake.StakeFragment;
import foundation.icon.iconex.view.ui.prep.stake.StakeViewModel;
import foundation.icon.iconex.view.ui.prep.stake.UnstakeFragment;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.iconex.widgets.StakeGraph;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Response;

public class PRepStakeActivity extends AppCompatActivity implements UnstakeFragment.OnUnstakeFragmentListener {
    private static final String TAG = PRepStakeActivity.class.getSimpleName();

    private Wallet wallet;
    private String privateKey;
    private BigInteger stepLimit, stepPrice, fee;
    private BigInteger voted;
    private float delegatedPercent = 0;

    private StakeGraph stakeGraph;
    private CustomSeekbar stakeSeekBar;

    private TextView txtBalance, txtUnstaked;
    private MyEditText editStaked;
    private TextView txtStakedPer, txtDelegation;
    private TextView txtTimeRequired, txtStepNPrice;
    private TextView txtFee, txtFeeUsd;
    private Button btnSubmit;

    private CompositeDisposable compositeDisposable;
    private Disposable disposable;
    private Disposable seekDisposable;
    private Disposable setStakeDisposable;

    private BigInteger ONE_HUNDRED = new BigInteger("100");
    private BigInteger totalBalance, staked, unstake, delegated, votingPower, availableStake;
    private BigInteger blockHeight, remainingBlocks;

    private StakeViewModel vm;
    private LoadingDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_stake);

        if (getIntent() != null) {
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");
            privateKey = getIntent().getStringExtra("privateKey");
        }

        vm = ViewModelProviders.of(this).get(StakeViewModel.class);
        vm.setWallet(wallet);
        vm.setPrivateKey(privateKey);

        loading = new LoadingDialog(this, R.style.DialogActivity);

        initView();
        getData();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
    }

    private void initView() {
        ((TextView) findViewById(R.id.txt_title)).setText(wallet.getAlias());
        findViewById(R.id.btn_start_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getData() {
        loading.show();
        disposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                    RpcObject getStakeResult = pRepService.getStake(wallet.getAddress()).asObject();
                    RpcObject getDelegationResult = pRepService.getDelegation(wallet.getAddress()).asObject();

                    if (getStakeResult.getItem("unstake") != null) {
                        unstake = getStakeResult.getItem("unstake").asInteger();
                        blockHeight = getStakeResult.getItem("unstakeBlockHeight").asInteger();
                        remainingBlocks = getStakeResult.getItem("remainingBlocks").asInteger();
                    } else {
                        unstake = BigInteger.ZERO;
                        blockHeight = BigInteger.ZERO;
                        remainingBlocks = BigInteger.ZERO;
                    }

                    staked = getStakeResult.getItem("stake").asInteger();

                    delegated = getDelegationResult.getItem("totalDelegated").asInteger();
                    votingPower = getDelegationResult.getItem("votingPower").asInteger();

                    IconService iconService = new IconService(ICONexApp.NETWORK.getUrl());
                    if (wallet.getWalletEntries().get(0).getBalance().equals("-")) {
                        BigInteger balance = iconService.getBalance(wallet.getAddress());
                        wallet.getWalletEntries().get(0).setBalance(balance.toString());
                    }

                    stepPrice = iconService.getStepPrice().asInteger();

                    BigInteger remained = new BigInteger(wallet.getWalletEntries().get(0).getBalance());
                    totalBalance = remained.add(staked).add(unstake);
                    availableStake = totalBalance.subtract(delegated).subtract(new BigInteger("1"));

                    if (ICONexApp.EXCHANGE_TABLE.get("icxusd") == null) {
                        LoopChainClient client = new LoopChainClient(ServiceConstants.DEV_TRACKER);
                        Response<TRResponse> response = client.getExchangeRates("icxusd").execute();
                        JsonElement data = response.body().getData();
                        JsonArray list = data.getAsJsonArray();
                        JsonObject item = list.get(0).getAsJsonObject();
                        String tradeName = item.get("tradeName").getAsString();
                        String price = item.get("price").getAsString();
                        ICONexApp.EXCHANGE_TABLE.put(tradeName, price);
                    }

                    Log.d(TAG, "totalBalance=" + ConvertUtil.getValue(totalBalance, 18)
                            + "\nStake=" + ConvertUtil.getValue(staked, 18)
                            + "\nDelegation=" + ConvertUtil.getValue(delegated, 18)
                            + "\nAvailable=" + ConvertUtil.getValue(availableStake, 18));

                    emitter.onComplete();
                } catch (InterruptedIOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        vm.setTotal(totalBalance);
                        vm.setUnstake(unstake);
                        vm.setStaked(staked);
                        vm.setDelegation(delegated);
                        vm.setUnstaked(totalBalance.subtract(staked));

                        vm.setStepLimit(stepLimit);
                        vm.setStepPrice(stepPrice);

                        if (unstake.equals(BigInteger.ZERO)) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, StakeFragment.newInstance())
                                    .commit();
                        } else {
                            vm.setBlockHeight(blockHeight);
                            vm.setRemainingBlock(remainingBlocks);

                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.container, UnstakeFragment.newInstance())
                                    .commit();
                        }

                        loading.dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        loading.dismiss();
                    }
                });
    }

    @Override
    public void onAdjust() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, StakeFragment.newInstance())
                .commit();
    }
}
