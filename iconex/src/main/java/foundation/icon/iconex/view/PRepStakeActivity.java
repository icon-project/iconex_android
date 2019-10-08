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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.StakeDialog;
import foundation.icon.iconex.service.IconService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.CustomToast;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.iconex.widgets.StakeGraph;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
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
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Response;

public class PRepStakeActivity extends AppCompatActivity {
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
    private BigInteger totalBalance, staked, delegated, votingPower, availableStake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_stake);

        if (getIntent() != null) {
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");
            privateKey = getIntent().getStringExtra("privateKey");
        }

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getData();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (disposable != null && !disposable.isDisposed())
            disposable.dispose();
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
                if (b) {
                    if (i == 0) {
                        stakeGraph.setStake(i + delegatedPercent);
                        editStaked.setTag("seekbar");
                        editStaked.setText(String.format(Locale.getDefault(), "%.4f",
                                new BigDecimal(delegated).scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR).doubleValue()));
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", delegatedPercent));
                    } else if (i == stakeSeekBar.getMax()) {
                        stakeGraph.setStake(100);
                        editStaked.setTag("seekbar");
                        editStaked.setText(String.format(Locale.getDefault(), "%.4f",
                                new BigDecimal(availableStake).scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR).doubleValue()));
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", 100.0f));
                    } else {
                        stakeGraph.setStake((int) (i + delegatedPercent));
                        editStaked.setTag("seekbar");
                        editStaked.setText(calculateIcx((int) (i + delegatedPercent)));
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", Math.floor((double) (i + delegatedPercent))));
                    }
                } else {
                    stakeGraph.setStake(i + delegatedPercent);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
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
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.w(TAG, "editable=" + editable.toString());
                if (editStaked.getTag() == null) {
                    stakeCheck(editable.toString());
                }
            }
        });

        txtStakedPer = findViewById(R.id.txt_percentage);
        txtDelegation = findViewById(R.id.txt_voted_icx);
        txtTimeRequired = findViewById(R.id.txt_time_required);
        txtStepNPrice = findViewById(R.id.txt_limit_price);
        txtFee = findViewById(R.id.txt_fee);
        txtFeeUsd = findViewById(R.id.txt_fee_usd);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void getData() {
        disposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
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
                        txtBalance.setText(Utils.formatFloating(
                                ConvertUtil.getValue(totalBalance, 18), 4));
                        txtUnstaked.setText(Utils.formatFloating(
                                ConvertUtil.getValue(totalBalance.subtract(staked), 18), 4));

                        float stakePercentage = calculatePercentage(totalBalance, staked);
                        Log.d(TAG, "stakePercent=" + stakePercentage);
                        editStaked.setText(String.format(Locale.getDefault(), "%f",
                                new BigDecimal(staked).scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR)));
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)",
                                stakePercentage));

                        stakeGraph.setTotalBalance(totalBalance);
                        stakeGraph.setStake(staked);

                        if (!delegated.equals(BigInteger.ZERO)) {
                            stakeGraph.setDelegation(delegated);
                            delegatedPercent = calculatePercentage(totalBalance, delegated);
                            txtDelegation.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                                    new BigDecimal(delegated).scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR),
                                    delegatedPercent));

                            stakeSeekBar.setMax(100 - ((int) delegatedPercent));
                        } else {
                            txtDelegation.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                                    new BigDecimal("0").scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR), 0.0f));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private String calculateIcx(int percentage) {
        BigInteger percent = new BigInteger(Integer.toString(percentage));
        BigInteger multiply = availableStake.multiply(percent);
        BigInteger icx = multiply.divide(ONE_HUNDRED);

        return String.format(Locale.getDefault(), "%.4f",
                Double.parseDouble(ConvertUtil.getValue(icx, 18)));
    }

    private float calculatePercentage(BigInteger base, BigInteger value) {
        if (value.equals(BigInteger.ZERO))
            return 0.0f;

        BigDecimal baseDec = new BigDecimal(base);
        BigDecimal valueDec = new BigDecimal(value);
        BigDecimal percentDec = valueDec.divide(baseDec, 18, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(ONE_HUNDRED));

        return percentDec.floatValue();
    }

    private void stakeCheck(String value) {
        BigInteger input = ConvertUtil.valueToBigInteger(value, 18);
        Log.wtf(TAG, "input=" + input);
        if (input.compareTo(availableStake) > 0) {
            editStaked.setText(String.format(Locale.getDefault(), "%.4f",
                    Double.parseDouble(ConvertUtil.getValue(availableStake, 18))));
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", 100.0f));
            stakeSeekBar.setProgress(100);
        } else if (input.compareTo(delegated) < 0) {
            editStaked.setTag("prg");
            editStaked.setText(String.format(Locale.getDefault(), "%.4f",
                    Double.parseDouble(ConvertUtil.getValue(delegated, 18))));
            editStaked.setTag(null);
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", delegatedPercent));
            stakeSeekBar.setProgress((int) delegatedPercent);
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

                String value;
                if (stakeSeekBar.getProgress() == 0)
                    value = ConvertUtil.getValue(delegated, 18);
                else if (stakeSeekBar.getProgress() == stakeSeekBar.getMax())
                    value = ConvertUtil.getValue(availableStake, 18);
                else
                    value = editStaked.getText().toString();

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

                        String icx = ConvertUtil.getValue(stepPrice, 18);
                        String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                        String fee = ConvertUtil.getValue(stepLimit.multiply(stepPrice), 18);
                        String mFee = fee.indexOf(".") < 0 ? fee : fee.replaceAll("0*$", "").replaceAll("\\.$", "");

                        if (stakeValue.compareTo(staked) < 0) {
                            txtTimeRequired.setText(getString(R.string.unstakeTimeRequired));
                        } else if (stakeValue.compareTo(staked) > 0) {
                            txtTimeRequired.setText(getString(R.string.stakeTimeRquired));
                        } else {
                            txtTimeRequired.setText("-");
                        }

                        if (stakeValue.compareTo(staked) == 0) {
                            txtStepNPrice.setText("- / -");
                            txtFee.setText("-");
                            txtFeeUsd.setText("-");
                            btnSubmit.setEnabled(false);
                        } else {
                            txtStepNPrice.setText(String.format(Locale.getDefault(), "%,d / %s",
                                    stepLimit.intValue(), mIcx));
                            txtFee.setText(mFee);
                            txtFeeUsd.setText(String.format(Locale.getDefault(), "$%.2f",
                                    Double.parseDouble(ConvertUtil.getValue(PRepStakeActivity.this.fee, 18))
                                            * Float.parseFloat(ICONexApp.EXCHANGE_TABLE.get("icxusd"))));
                            btnSubmit.setEnabled(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private StakeDialog dialog;

    private void showDialog() {
        dialog = new StakeDialog(this);

        if (txtTimeRequired.getText().toString().equals(getString(R.string.stakeTimeRquired)))
            dialog.setTitle(getString(R.string.stake));
        else
            dialog.setTitle(getString(R.string.unstake));

        dialog.setTimeRequired(txtTimeRequired.getText().toString());
        dialog.setStepLimit(txtStepNPrice.getText().toString());
        dialog.setEstimatedMaxFee(txtFee.getText().toString());
        dialog.setExchangedFee(txtFeeUsd.getText().toString());

        dialog.setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                setStake();
                return true;
            }
        });

        dialog.show();
    }

    private SignedTransaction getSignedTransaction() {
        KeyWallet keyWallet = KeyWallet.load(new Bytes(privateKey));

        String value = editStaked.getText().toString();
        RpcObject params = new RpcObject.Builder()
                .put("value", new RpcValue(ConvertUtil.valueToHexString(value, 18)))
                .build();

        Transaction transaction = TransactionBuilder.newBuilder()
                .from(new Address(wallet.getAddress()))
                .to(new Address(Constants.ADDRESS_ZERO))
                .value(IconAmount.of("0", IconAmount.Unit.ICX).toLoop())
                .stepLimit(stepLimit)
                .nid(ICONexApp.NETWORK.getNid())
                .call("setStake")
                .params(params)
                .build();

        return new SignedTransaction(transaction, keyWallet);
    }

    private void setStake() {
        setStakeDisposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                try {
                    PRepService service = new PRepService(ICONexApp.NETWORK.getUrl());
                    SignedTransaction signedTransaction = getSignedTransaction();
                    String tx = service.setStake(signedTransaction);
                    Log.d(TAG, "Set stake=" + tx);

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
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        CustomToast.makeText(PRepStakeActivity.this,
                                "Failed", Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
