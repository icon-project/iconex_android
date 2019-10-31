package foundation.icon.iconex.view.ui.prep.stake;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
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
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                if (fromUser) {
                    if (i == 0) {
                        editStaked.setTag("seekbar");
                        editStaked.setText(delegated.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                    } else if (i == stakeSeekBar.getMax()) {
                        Log.w(TAG, "progress=" + i + ", max=" + stakeSeekBar.getMax());
                        editStaked.setTag("seekbar");
                        editStaked.setText(total.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                    } else {
                        editStaked.setTag("seekbar");
                        editStaked.setText(calculateIcx(i).toString());
                        editStaked.setSelection(editStaked.getText().toString().length());
                        editStaked.setTag(null);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        txtBalance = v.findViewById(R.id.balance_icx);
        txtUnstaked = v.findViewById(R.id.unstake_icx);

        editStaked = v.findViewById(R.id.edit_value);
        editStaked.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editStaked.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputString = s.toString();

                if (inputString.isEmpty()) {
                    txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", delegatedPercent));
                    stakeSeekBar.setProgress(0);
                    stakeGraph.updateGraph(delegated);
                    return;
                }

                if (inputString.charAt(0) == '.') {
                    editStaked.setText(inputString.substring(1));
                    editStaked.setSelection(inputString.substring(1).length());

                    return;
                } else if (inputString.contains(".")) {
                    String[] split = inputString.split("\\.");
                    if (split.length < 2) {
                        return;
                    } else if (split.length > 2) {
                        int index = inputString.indexOf(".");
                        inputString = inputString.substring(0, index);

                        editStaked.setText(inputString);
                        editStaked.setSelection(inputString.length());

                        return;
                    } else {
                        if (split[1].length() > 4) {
                            split[1] = split[1].substring(0, 4);
                            inputString = split[0] + "." + split[1];

                            editStaked.setText(inputString);
                            editStaked.setSelection(inputString.length());

                            return;
                        }
                    }
                }

                BigDecimal input;
                try {
                    input = new BigDecimal(new BigDecimal(inputString).scaleByPowerOfTen(18).toBigInteger());
                } catch (Exception e) {
                    return;
                }

                if (editStaked.getTag() == null) {
                    if (input.compareTo(total) > 0) {
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", 100.0f));
                        stakeSeekBar.setProgress(100);
                        stakeGraph.updateGraph(total);
                    } else if (input.compareTo(delegated) <= 0) {
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", delegatedPercent));
                        stakeSeekBar.setProgress(0);
                        stakeGraph.updateGraph(delegated);
                    } else {
                        double percent = calculatePercentage(total, input);
                        Log.d(TAG, "edittext campreMin percent=" + percent + ", delegationPercent=" + delegatedPercent);
                        stakeSeekBar.setProgress((int) (percent - delegatedPercent));
                        Log.d(TAG, "Seek bar progress=" + (int) (percent - delegatedPercent));
                        txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", percent));
                        stakeGraph.updateGraph(input);
                    }
                } else {
                    double percent = calculatePercentage(total, input);
                    Log.d(TAG, "edittext campreMin percent=" + percent + ", delegationPercent=" + delegatedPercent);
                    Log.d(TAG, "Seek bar progress=" + (int) (percent - delegatedPercent));
                    txtStakedPer.setText(String.format(Locale.getDefault(), "(%.1f%%)", percent));
                    stakeGraph.updateGraph(input);
                }

                localHandler.removeCallbacks(estimatedStepTask);
                localHandler.postDelayed(estimatedStepTask, 1000);
            }
        });
        editStaked.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                BigDecimal value;
                try {
                    value = new BigDecimal(editStaked.getText().toString()).scaleByPowerOfTen(18);
                } catch (Exception e) {
                    editStaked.setText(delegated.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                    return;
                }

                CustomToast toast;
                if (value.compareTo(delegated) < 0) {
                    editStaked.setText(delegated.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                    stakeSeekBar.setProgress(0);
                    toast = new CustomToast();
                    toast.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.minLimit),
                            delegated.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString()), Toast.LENGTH_SHORT).show();
                } else if (value.compareTo(total) > 0) {
                    editStaked.setText(total.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                    stakeSeekBar.setProgress(100);
                    toast = new CustomToast();
                    toast.makeText(getContext(), String.format(Locale.getDefault(), getString(R.string.maxLimit),
                            total.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString()), Toast.LENGTH_SHORT).show();
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
        BigDecimal ONE_ICX = new BigDecimal("1").scaleByPowerOfTen(18);
        total = new BigDecimal(vm.getTotal().getValue()).subtract(ONE_ICX);
        staked = new BigDecimal(vm.getStaked().getValue());
        unstaked = new BigDecimal(vm.getUnstaked().getValue());
        delegated = new BigDecimal(vm.getDelegation().getValue());

        txtBalance.setText(total.add(ONE_ICX).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
        txtUnstaked.setText(unstaked.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());

        stakeGraph.setTotal(total);
        stakeGraph.setStake(staked);

        if (!delegated.equals(BigInteger.ZERO)) {
            stakeGraph.setDelegation(delegated);
            delegatedPercent = calculatePercentage(total, delegated);
            txtDelegation.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                    delegated.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString(),
                    delegatedPercent));

            stakeSeekBar.setMax(100 - ((int) delegatedPercent));
        } else {
            txtDelegation.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                    new BigDecimal("0").scaleByPowerOfTen(-18).setScale(4, BigDecimal.ROUND_FLOOR), 0.0f));
        }

        stakeGraph.updateGraph();

        float stakePercentage = calculatePercentage(total, staked);
        Log.d(TAG, "stakePercent=" + stakePercentage);
        editStaked.setText(staked.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
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
            Log.d(TAG, "calculateIcx=" + multiply.divide(ONE_HUNDRED).add(delegated).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
            return multiply.divide(ONE_HUNDRED).add(delegated).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR);
        }
    }

    private float calculatePercentage(BigDecimal base, BigDecimal value) {
        if (value.equals(BigInteger.ZERO))
            return 0.0f;

        return (value.floatValue() / base.floatValue()) * 100;
    }

    private Handler localHandler = new Handler();

    private Runnable estimatedStepTask = new Runnable() {
        @Override
        public void run() {
            Log.i(TAG, "estimatedStepTask Run!!");
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
                    value = delegated.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString();
                else if (stakeSeekBar.getProgress() == stakeSeekBar.getMax())
                    value = total.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString();
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

                BigDecimal stakeValue = new BigDecimal(new BigDecimal(editStaked.getText().toString()).scaleByPowerOfTen(18).toBigInteger());
                Log.w(TAG, "getStakingData:stakeValue=" + stakeValue);
                Log.w(TAG, "getStakingData:staked=" + staked);
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

        Log.i(TAG, "setEstimatedFee=" + txtFee.getText().toString());
        Log.i(TAG, "StakeDialog=" + dialog.getEstimatedMaxFee());

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
                        BigDecimal value = new BigDecimal(new BigDecimal(editStaked.getText().toString()).scaleByPowerOfTen(18).toBigInteger());
                        if (value.compareTo(staked) < 0) {
                            messageDialog.setMessage(getString(R.string.unstakeDone));
                        } else {
                            messageDialog.setMessage(getString(R.string.stakeDone));
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
