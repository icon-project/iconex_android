package foundation.icon.iconex.view;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.IconService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.iconex.widgets.StakeGraph;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Response;

public class PRepStakeActivity extends AppCompatActivity {
    private static final String TAG = PRepStakeActivity.class.getSimpleName();

    private Wallet wallet;
    private BigInteger stepLimit, stepPrice, fee;

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

    private BigInteger ONE_HUNDRED = new BigInteger("100");
    private BigInteger totalBalance, staked, delegated, votingPower, availableStake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_stake);

        if (getIntent() != null)
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getData();
    }

    private int previousProgress;

    private void initView() {
        if (wallet != null)
            ((TextView) findViewById(R.id.txt_title)).setText(wallet.getAlias());

        findViewById(R.id.btn_start_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        stakeGraph = findViewById(R.id.stake_graph);
        stakeSeekBar = findViewById(R.id.stake_seek_bar);
        stakeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                stakeGraph.setStake(i);

                if (!editStaked.isFocused()) {
                    editStaked.setText(calculateIcx(i));
                    editStaked.setSelection(editStaked.getText().toString().length());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (editStaked.isFocused())
                    editStaked.clearFocus();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                getStakingData();
            }
        });

        txtBalance = findViewById(R.id.balance_icx);
        txtUnstaked = findViewById(R.id.unstake_icx);

        editStaked = findViewById(R.id.edit_value);
        editStaked.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    stakeCheck(charSequence.toString());
                } else {
                    txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", (float) 0));
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        txtStakedPer = findViewById(R.id.txt_percentage);
        txtDelegation = findViewById(R.id.delegation_percentage);
        txtTimeRequired = findViewById(R.id.txt_time_required);
        txtStepNPrice = findViewById(R.id.txt_limit_price);
        txtFee = findViewById(R.id.txt_fee);
        txtFeeUsd = findViewById(R.id.txt_fee_usd);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    private void getData() {
        disposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                RpcObject getDelegationResult = pRepService.getDelegation(wallet.getAddress()).asObject();
                delegated = getDelegationResult.getItem("totalDelegated").asInteger();
                votingPower = getDelegationResult.getItem("votingPower").asInteger();
                staked = delegated.add(votingPower);

                IconService iconService = new IconService(ICONexApp.NETWORK.getUrl());
                if (wallet.getWalletEntries().get(0).getBalance().equals("-")) {
                    BigInteger balance = iconService.getBalance(wallet.getAddress());
                    wallet.getWalletEntries().get(0).setBalance(balance.toString());
                }

                stepPrice = iconService.getStepPrice().asInteger();

                BigInteger remainer = new BigInteger(wallet.getWalletEntries().get(0).getBalance());
                totalBalance = remainer.add(staked);
                availableStake = totalBalance.subtract(delegated);

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
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        txtBalance.setText(Utils.formatFloating(
                                ConvertUtil.getValue(totalBalance, 18), 4));
                        txtUnstaked.setText(Utils.formatFloating(
                                ConvertUtil.getValue(totalBalance.subtract(staked), 18), 4));

                        float stakePercentage = calculatePercentage(totalBalance, staked);
                        float delegatedPercentage = calculatePercentage(staked, delegated);
                        stakeGraph.setStake(stakePercentage);
                        stakeGraph.setDelegation(delegatedPercentage);

                        editStaked.setText(String.format(Locale.getDefault(), "%.4f",
                                Double.parseDouble(ConvertUtil.getValue(staked, 18))));
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)",
                                stakePercentage));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }

    private String calculateIcx(int percentage) {
        BigInteger percent = new BigInteger(Integer.toString(percentage));
        Log.d(TAG, "percent=" + percent);
        BigInteger multiply = availableStake.multiply(percent);
        BigInteger icx = multiply.divide(ONE_HUNDRED);

        Log.d(TAG, "icx=" + ConvertUtil.getValue(icx, 18));

        return String.format(Locale.getDefault(), "%,.4f",
                Double.parseDouble(ConvertUtil.getValue(icx, 18)));
    }

    private float calculatePercentage(BigInteger base, BigInteger value) {
        Log.d(TAG, "value=" + value);
        if (value.equals(BigInteger.ZERO))
            return 0.0f;

        BigDecimal baseDec = new BigDecimal(base);
        BigDecimal valueDec = new BigDecimal(value);
        BigDecimal percentDec = valueDec.divide(baseDec).multiply(new BigDecimal(ONE_HUNDRED));

        return percentDec.floatValue();
    }

    private void stakeCheck(String value) {
        BigInteger input = ConvertUtil.valueToBigInteger(value, 18);
        if (input.compareTo(delegated) < 0) {
            editStaked.setText(String.format(Locale.getDefault(), "%.4f",
                    Double.parseDouble(ConvertUtil.getValue(staked, 18))));
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)",
                    calculatePercentage(totalBalance, staked)));
            stakeSeekBar.setProgress((int) calculatePercentage(availableStake, staked));
        } else if (input.compareTo(availableStake) > 0) {
            editStaked.setText(String.format(Locale.getDefault(), "%.4f",
                    Double.parseDouble(ConvertUtil.getValue(availableStake, 18))));
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", 100.0f));
            stakeSeekBar.setProgress(100);
        } else {
            double percent = calculatePercentage(availableStake, input);
            Log.d(TAG, "edittext campreMin percent=" + percent);
            stakeSeekBar.setProgress((int) percent);
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", percent));
        }

        getStakingData();
    }

    private Handler localHandler;

    private Runnable estimatedStepTask = new Runnable() {
        @Override
        public void run() {
            getStakingData();
        }
    };

    private void getStakingData() {
        seekDisposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                Address fromAddress = new Address(wallet.getAddress());
                Address toAddress = new Address(Constants.ADDRESS_ZERO);
                BigInteger defaultValue
                        = IconAmount.of("0", IconAmount.Unit.ICX).toLoop();

                String value = editStaked.getText().toString();
                RpcObject params = new RpcObject.Builder()
                        .put("value", new RpcValue(ConvertUtil.valueToHexString(value, 18)))
                        .build();

                Transaction transaction = TransactionBuilder.newBuilder()
                        .from(fromAddress)
                        .to(toAddress)
                        .value(defaultValue)
                        .call("setStake")
                        .params(params)
                        .build();

                stepLimit = IconService.estimateStep(transaction);

                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        String value = editStaked.getText().toString();
                        if (value.isEmpty())
                            return;
                        BigInteger stakeValue = ConvertUtil.valueToBigInteger(value, 18);
                        fee = stepLimit.multiply(stepPrice);

                        if (stakeValue.compareTo(staked) < 0) {
                            txtTimeRequired.setText(getString(R.string.unstakeTimeRequired));
                        } else {
                            txtTimeRequired.setText(getString(R.string.stakeTimeRquired));
                        }

                        String icx = ConvertUtil.getValue(stepPrice, 18);
                        String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtStepNPrice.setText(String.format(Locale.getDefault(), "%,d / %s",
                                stepLimit.intValue(), mIcx));

                        String fee = ConvertUtil.getValue(stepLimit.multiply(stepPrice), 18);
                        String mFee = fee.indexOf(".") < 0 ? fee : fee.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtFee.setText(mFee);
                        txtFeeUsd.setText(String.format(Locale.getDefault(), "$%.2f",
                                Double.parseDouble(ConvertUtil.getValue(PRepStakeActivity.this.fee, 18))
                                        * Float.parseFloat(ICONexApp.EXCHANGE_TABLE.get("icxusd"))));

                        btnSubmit.setEnabled(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void setStake() {

    }
}
