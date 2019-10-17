package foundation.icon.iconex.view.ui.prep.stake;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.io.InterruptedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.StakeDialog;
import foundation.icon.iconex.service.IconService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
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
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;

public class StakeFragment extends Fragment {
    private static final String TAG = StakeFragment.class.getSimpleName();

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

    private PRepService pRepService;
    private BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private BigDecimal total, staked, unstaked, delegated, votingPower, maxStake, remainingBlocks;

    private StakeViewModel vm;

    public StakeFragment() {
        // Required empty public constructor
    }

    public static StakeFragment newInstance() {
        return new StakeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(StakeViewModel.class);
        wallet = vm.getWallet().getValue();
        privateKey = vm.getPrivateKey().getValue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_stake, container, false);
        initView(v);
        setData();

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void initView(View v) {
        stakeGraph = v.findViewById(R.id.stake_graph);
        stakeSeekBar = v.findViewById(R.id.stake_seek_bar);
        stakeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    Log.wtf(TAG, "From User, " + i);

                    if (i == 0) {
                        editStaked.setTag("seekbar");
                        editStaked.setText(delegated.toString());
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", delegatedPercent));
                        stakeGraph.updateGraph(delegated);
                    } else if (i == stakeSeekBar.getMax()) {
                        editStaked.setTag("seekbar");
                        editStaked.setText(maxStake.toString());
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", 100.0f));
                        stakeGraph.updateGraph(total);
                    } else {
                        editStaked.setTag("seekbar");
                        editStaked.setText(calculateIcx(i).toString());
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", Math.floor((double) (i + delegatedPercent))));
                        stakeGraph.updateGraph(new BigDecimal(editStaked.getText().toString()));
                    }
                } else {
                    Log.w(TAG, "From Programmatic, " + i + ", Staked=" + editStaked.getText().toString());
                    if (!editStaked.getText().toString().isEmpty())
                        stakeGraph.updateGraph(new BigDecimal(editStaked.getText().toString()));
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

        txtBalance = v.findViewById(R.id.balance_icx);
        txtUnstaked = v.findViewById(R.id.unstake_icx);

        editStaked = v.findViewById(R.id.edit_value);
        editStaked.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editStaked.getTag() == null) {
                    stakeCheck(editable.toString());
                }
            }
        });
        editStaked.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                BigDecimal value;
                try {
                    value = new BigDecimal(editStaked.getText().toString());
                } catch (Exception e) {
                    editStaked.setText(delegated.toString());
                    return;
                }

                CustomToast toast;
                if (value.compareTo(delegated) < 0) {
                    editStaked.setText(delegated.toString());
                    toast = new CustomToast();
                    toast.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.minLimit), delegated.toString()), Toast.LENGTH_SHORT).show();
                } else if (value.compareTo(maxStake) > 0) {
                    editStaked.setText(maxStake.toString());
                    toast = new CustomToast();
                    toast.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.maxLimit), maxStake.toString()), Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtStakedPer = v.findViewById(R.id.txt_percentage);
        txtDelegation = v.findViewById(R.id.txt_voted_icx);
        txtTimeRequired = v.findViewById(R.id.txt_time_required);
        txtStepNPrice = v.findViewById(R.id.txt_limit_price);
        txtFee = v.findViewById(R.id.txt_fee);
        txtFeeUsd = v.findViewById(R.id.txt_fee_usd);
        btnSubmit = v.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });
    }

    private void setData() {
        total = new BigDecimal(vm.getTotal().getValue()).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR);
        staked = new BigDecimal(vm.getStaked().getValue()).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR);
        unstaked = new BigDecimal(vm.getUnstaked().getValue()).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR);
        delegated = new BigDecimal(vm.getDelegation().getValue()).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR);
        maxStake = total.subtract(delegated).subtract(new BigDecimal("1"));

        txtBalance.setText(total.toString());
        txtUnstaked.setText(unstaked.toString());

        stakeGraph.setTotal(total);
        stakeGraph.setStake(staked);

        if (!delegated.equals(BigInteger.ZERO)) {
            stakeGraph.setDelegation(delegated);
            delegatedPercent = calculatePercentage(total, delegated);
            txtDelegation.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                    delegated.toString(),
                    delegatedPercent));

            stakeSeekBar.setMax(100 - ((int) delegatedPercent));
        } else {
            txtDelegation.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                    new BigDecimal("0").scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR), 0.0f));
        }

        stakeGraph.updateGraph();

        float stakePercentage = calculatePercentage(total, staked);
        Log.d(TAG, "stakePercent=" + stakePercentage);
        editStaked.setText(staked.toString());
        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)",
                stakePercentage));

        stepLimit = vm.getStepLimit().getValue();
        stepPrice = vm.getStepPrice().getValue();
    }

    private BigDecimal calculateIcx(int percentage) {
        if (percentage == 0) {
            return delegated;
        } else if (percentage == stakeSeekBar.getMax()) {
            return total;
        } else {
            BigDecimal percent = new BigDecimal(Integer.toString(percentage));
            BigDecimal multiply = total.multiply(percent);
            return multiply.divide(ONE_HUNDRED).add(delegated);
        }
    }

    private float calculatePercentage(BigDecimal base, BigDecimal value) {
        if (value.equals(BigInteger.ZERO))
            return 0.0f;

        BigDecimal percentDec = value.divide(base, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);

        return percentDec.floatValue();
    }

    private void stakeCheck(String value) {
        BigDecimal input;
        try {
            input = new BigDecimal(value);
        } catch (Exception e) {
            return;
        }

        if (input.compareTo(maxStake) > 0) {
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", 100.0f));
            stakeSeekBar.setProgress(100);
        } else if (input.compareTo(delegated) <= 0) {
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", delegatedPercent));
            stakeSeekBar.setProgress(0);
        } else {
            double percent = calculatePercentage(total, input);
            Log.d(TAG, "edittext campreMin percent=" + percent + ", delegationPercent=" + delegatedPercent);
            stakeSeekBar.setProgress((int) (percent - delegatedPercent));
            Log.d(TAG, "Seek bar progress=" + (int) (percent - delegatedPercent));
            txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", percent));
        }

        try {
            localHandler.removeCallbacks(estimatedStepTask);
            localHandler.postDelayed(estimatedStepTask, 500);
        } catch (Exception e) {
            localHandler.postDelayed(estimatedStepTask, 500);
        }
    }

    private Handler localHandler = new Handler();

    private Runnable estimatedStepTask = new Runnable() {
        @Override
        public void run() {
            getStakingData();
        }
    };

    private void getStakingData() {
        seekDisposable = Observable.create(new ObservableOnSubscribe<BigDecimal>() {
            @Override
            public void subscribe(ObservableEmitter<BigDecimal> emitter) throws Exception {
                Address fromAddress = new Address(wallet.getAddress());
                Address toAddress = new Address(Constants.ADDRESS_ZERO);
                BigInteger defaultValue
                        = IconAmount.of("0", IconAmount.Unit.ICX).toLoop();

                String value;
                if (stakeSeekBar.getProgress() == 0)
                    value = delegated.toString();
                else if (stakeSeekBar.getProgress() == stakeSeekBar.getMax())
                    value = maxStake.toString();
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

                BigDecimal stakeValue = new BigDecimal(editStaked.getText().toString());
                if (stakeValue.compareTo(staked) < 0) {
                    if (pRepService == null) {
                        pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                        remainingBlocks = new BigDecimal(pRepService.estimateUnstakeLockPeriod());
                    }
                }

                emitter.onNext(stakeValue);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<BigDecimal>() {
                    @Override
                    public void onNext(BigDecimal stakeValue) {
                        fee = stepLimit.multiply(stepPrice);

                        String icx = ConvertUtil.getValue(stepPrice, 18);
                        String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                        String fee = ConvertUtil.getValue(stepLimit.multiply(stepPrice), 18);
                        String mFee = fee.indexOf(".") < 0 ? fee : fee.replaceAll("0*$", "").replaceAll("\\.$", "");

                        if (stakeValue.compareTo(staked) < 0) {
                            Calendar calendar = Calendar.getInstance();
                            calendar.add(Calendar.SECOND, remainingBlocks.multiply(new BigDecimal("2")).intValue());

                            txtTimeRequired.setText(String.format(Locale.getDefault(), "%d-%d-%d %02d:%02d:%02d",
                                    calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH) + 1,
                                    calendar.get(Calendar.DAY_OF_MONTH),
                                    calendar.get(Calendar.HOUR),
                                    calendar.get(Calendar.MINUTE),
                                    calendar.get(Calendar.SECOND)));
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
                                    Double.parseDouble(ConvertUtil.getValue(StakeFragment.this.fee, 18))
                                            * Float.parseFloat(ICONexApp.EXCHANGE_TABLE.get("icxusd"))));
                            btnSubmit.setEnabled(true);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private StakeDialog dialog;

    private void showDialog() {
        dialog = new StakeDialog(getContext());

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
                        MessageDialog messageDialog = new MessageDialog(getContext());
                        if (staked.compareTo(new BigDecimal(editStaked.getText().toString())) < 0) {
                            messageDialog.setTitleText(getString(R.string.unstakeDone));
                        } else {
                            messageDialog.setTitleText(getString(R.string.stakeDone));
                        }
                        messageDialog.setSingleButton(true);
                        messageDialog.setOnSingleClick(new Function1<View, Boolean>() {
                            @Override
                            public Boolean invoke(View view) {
                                getActivity().finish();
                                return true;
                            }
                        });
                        messageDialog.show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        CustomToast.makeText(getContext(),
                                "Failed", Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
