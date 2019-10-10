package foundation.icon.iconex.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.math.BigInteger;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.connect.IconexConnect;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.ClaimIScoreDialog;
import foundation.icon.iconex.service.IconService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomToast;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.data.IconAmount;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Response;

public class PRepIScoreActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PRepIScoreActivity.class.getSimpleName();

    private Wallet wallet;
    private String privateKey;
    private Disposable disposable;

    private TextView txtCurrentIScore, txtEstimatedIcx, txtLimitPrice, txtFee, txtFeeUsd;
    private Button btnClaim;

    private BigInteger currentIScore, estimatedIcx, stepLimit, stepPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_iscore);

        if (getIntent() != null) {
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");
            privateKey = getIntent().getStringExtra("privateKey");
        }

        initView();
        initData();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getData();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!disposable.isDisposed())
            disposable.dispose();
    }

    private void initView() {
        txtCurrentIScore = findViewById(R.id.txt_current_iscore);
        txtEstimatedIcx = findViewById(R.id.txt_estimated_icx);
        txtLimitPrice = findViewById(R.id.txt_limit_price);
        txtFee = findViewById(R.id.txt_fee);
        txtFeeUsd = findViewById(R.id.txt_fee_usd);
        btnClaim = findViewById(R.id.btn_claim);
        btnClaim.setOnClickListener(this);
        findViewById(R.id.btn_start_icon).setOnClickListener(this);
    }

    private void initData() {
        ((TextView) findViewById(R.id.txt_title)).setText(wallet.getAlias());
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_claim:
                showConfirmDialog();
                break;

            case R.id.btn_start_icon:
                finish();
                break;
        }
    }

    private void getData() {
        disposable = Completable.create(new CompletableOnSubscribe() {
            @Override
            public void subscribe(CompletableEmitter emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                RpcItem result = pRepService.getIScore(wallet.getAddress());
                RpcObject object = result.asObject();
                currentIScore = object.getItem("iscore").asValue().asInteger();
                estimatedIcx = object.getItem("estimatedICX").asValue().asInteger();

                IconService iconService = new IconService(ICONexApp.NETWORK.getUrl());
                if (wallet.getWalletEntries().get(0).getBalance().equals(MyConstants.NO_BALANCE)) {
                    BigInteger balance = iconService.getBalance(wallet.getAddress());
                    wallet.getWalletEntries().get(0).setBalance(balance.toString());
                }

                Address fromAddress = new Address(wallet.getAddress());
                Address toAddress = new Address(Constants.ADDRESS_ZERO);

                BigInteger value = IconAmount.of("0", IconAmount.Unit.ICX).toLoop();

                Transaction transaction = TransactionBuilder.newBuilder()
                        .from(fromAddress)
                        .to(toAddress)
                        .value(value)
                        .call("claimIScore")
                        .build();

                stepLimit = IconService.estimateStep(transaction);
                stepPrice = iconService.getStepPrice().asInteger();

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

                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        txtCurrentIScore.setText(Utils.formatFloating(
                                ConvertUtil.getValue(currentIScore, 18), 4));
                        txtEstimatedIcx.setText(Utils.formatFloating(
                                ConvertUtil.getValue(estimatedIcx, 18), 4));

                        String icx = ConvertUtil.getValue(stepPrice, 18);
                        String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtLimitPrice.setText(String.format(Locale.getDefault(), "%,d / %s",
                                stepLimit.intValue(), mIcx));

                        String fee = ConvertUtil.getValue(stepLimit.multiply(stepPrice), 18);
                        String mFee = fee.indexOf(".") < 0 ? fee : fee.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtFee.setText(mFee);

                        String exPrice = ICONexApp.EXCHANGE_TABLE.get("icxusd");
                        txtFeeUsd.setText(String.format(Locale.getDefault(), "$ %,.2f",
                                Double.parseDouble(fee) * Double.parseDouble(exPrice)));

                        if (!currentIScore.equals(BigInteger.ZERO))
                            btnClaim.setEnabled(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void showConfirmDialog() {
        ClaimIScoreDialog claimIScoreDialog = new ClaimIScoreDialog(this);
        claimIScoreDialog.setCurrent(txtCurrentIScore.getText().toString());
        claimIScoreDialog.setReceived(txtEstimatedIcx.getText().toString());
        claimIScoreDialog.setStep(txtLimitPrice.getText().toString());
        claimIScoreDialog.setFee(txtFee.getText().toString());
        claimIScoreDialog.setFeeUsd(txtFeeUsd.getText().toString());
        claimIScoreDialog.setData();

        claimIScoreDialog.setSingleButton(false);
        claimIScoreDialog.setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                claimIScore();
                return true;
            }
        });

        claimIScoreDialog.show();
    }

    private void claimIScore() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                KeyWallet keyWallet = KeyWallet.load(new Bytes(privateKey));
                Transaction transaction = TransactionBuilder.newBuilder()
                        .from(new Address(wallet.getAddress()))
                        .to(new Address(Constants.ADDRESS_ZERO))
                        .value(IconAmount.of("0", IconAmount.Unit.ICX).toLoop())
                        .stepLimit(stepLimit)
                        .nid(ICONexApp.NETWORK.getNid())
                        .call("claimIScore")
                        .build();

                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                pRepService.claimIScore(new SignedTransaction(transaction, keyWallet));
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableCompletableObserver() {
                    @Override
                    public void onComplete() {
                        CustomToast toast = new CustomToast();
                        toast.makeText(PRepIScoreActivity.this, getString(R.string.claimDone), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        CustomToast toast = new CustomToast();
                        toast.makeText(PRepIScoreActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
