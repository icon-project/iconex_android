package foundation.icon.connect;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;

import static android.view.View.GONE;
import static foundation.icon.ICONexApp.network;

public class SendTransactionFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = SendTransactionFragment.class.getSimpleName();

    private static final String ARG_TX = "tx";
    private static final String ARG_PK = "privateKey";
    private static final String ARG_ALIAS = "alias";

    private String txString;
    private Transaction transaction;
    private KeyWallet keyWallet;
    private String alias;

    private IconService iconService;

    private ScrollView scroll;
    private ViewGroup layoutNetwork, btnNetwork;
    private TextView txtNetwork;

    private ViewGroup infoLimit, infoPrice, infoFee;

    private TextView txtSend, txtAmount, txtTransAmount, txtTo,
            txtFee, txtTransFee, txtRemainAmount, txtRemain, txtTransRemain;
    private MyEditText editLimit;
    private Button btnLimitDel;
    private View lineLimit;
    private TextView txtLimitWarning;
    private TextView txtStepICX, txtStepGloop, txtStepTrans;

    private ViewGroup layoutTxData;
    private TextView txtOpenState;
    private ImageView imgArrow;
    private TextView txtTxData;

    private Button btnSend;

    private BigInteger balance;
    private BigInteger step;
    private BigInteger stepPrice;

    private String trPrice;
    private String tokenPrice;

    private String dataType;
    private String data;

    public SendTransactionFragment() {
        // Required empty public constructor
    }

    public static SendTransactionFragment newInstance(
            String transaction, byte[] privateKey, String alias) {
        SendTransactionFragment fragment = new SendTransactionFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_TX, transaction);
        bundle.putByteArray(ARG_PK, privateKey);
        bundle.putString(ARG_ALIAS, alias);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            txString = getArguments().getString(ARG_TX);
            parseTransaction(txString);
            loadKeyWallet(getArguments().getByteArray(ARG_PK));
            alias = getArguments().getString(ARG_ALIAS);
        }

        initIconService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_transaction, container, false);
        initView(v);
        setData();

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SendTransactionFragmentListener) {
            mListener = (SendTransactionFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SendTransactionFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initView(View v) {
        scroll = v.findViewById(R.id.scroll);

        ((TextView) v.findViewById(R.id.txt_title)).setText(alias);
        v.findViewById(R.id.btn_close).setOnClickListener(this);
        ((TextView) v.findViewById(R.id.txt_send_amount)).setText(String.format(Locale.getDefault(), getString(R.string.sendAmount), "ICX"));
        ((TextView) v.findViewById(R.id.txt_send_fee)).setText(String.format(Locale.getDefault(), getString(R.string.estiFee), "ICX"));
        ((TextView) v.findViewById(R.id.txt_remain_amount)).setText(String.format(Locale.getDefault(), getString(R.string.estiRemain), "ICX"));

        layoutNetwork = v.findViewById(R.id.layout_network);
        btnNetwork = v.findViewById(R.id.btn_network);
        btnNetwork.setOnClickListener(this);
        txtNetwork = v.findViewById(R.id.txt_network);

        txtSend = v.findViewById(R.id.txt_send_amount);
        txtAmount = v.findViewById(R.id.txt_amount);
        txtTo = v.findViewById(R.id.txt_to);
        editLimit = v.findViewById(R.id.edit_step_limit);
        editLimit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnLimitDel.setVisibility(View.VISIBLE);
                    btnSend.setEnabled(true);
                } else {
                    btnLimitDel.setVisibility(View.INVISIBLE);
                    btnSend.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        editLimit.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                validateStep();
            }
        });
        editLimit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    validateStep();
                }
                return false;
            }
        });

        btnLimitDel = v.findViewById(R.id.del_step_limit);
        btnLimitDel.setOnClickListener(this);
        lineLimit = v.findViewById(R.id.line_step_limit);
        txtLimitWarning = v.findViewById(R.id.txt_step_limit_warning);

        txtFee = v.findViewById(R.id.txt_fee);
        txtTransFee = v.findViewById(R.id.txt_trans_fee);

        txtRemainAmount = v.findViewById(R.id.txt_remain_amount);
        txtRemain = v.findViewById(R.id.txt_remain);
        txtTransRemain = v.findViewById(R.id.txt_trans_remain);
        txtTransAmount = v.findViewById(R.id.txt_trans_amount);

        txtStepICX = v.findViewById(R.id.txt_step_icx);
        txtStepGloop = v.findViewById(R.id.txt_step_gloop);
        txtStepTrans = v.findViewById(R.id.txt_step_trans);

        infoLimit = v.findViewById(R.id.info_step_limit);
        infoLimit.setOnClickListener(this);
        infoPrice = v.findViewById(R.id.info_step_price);
        infoPrice.setOnClickListener(this);
        infoFee = v.findViewById(R.id.info_fee);
        infoFee.setOnClickListener(this);

        layoutTxData = v.findViewById(R.id.layout_tx_data);
        v.findViewById(R.id.btn_open).setOnClickListener(this);
        txtOpenState = v.findViewById(R.id.txt_open_state);
        imgArrow = v.findViewById(R.id.img_arrow);
        txtTxData = v.findViewById(R.id.txt_tx_data);

        btnSend = v.findViewById(R.id.btn_send);
        btnSend.setEnabled(false);
        btnSend.setOnClickListener(this);

        if (ICONexApp.isDeveloper) {
            layoutNetwork.setVisibility(View.VISIBLE);
            if (network == 1)
                txtNetwork.setText(R.string.networkMain);
            else if (network == 2)
                txtNetwork.setText(R.string.networkTest);
        } else {
            layoutNetwork.setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        BasicDialog info = new BasicDialog(getContext());
        String message;
        switch (v.getId()) {
            case R.id.btn_close:
                Basic2ButtonDialog cancleDialog = new Basic2ButtonDialog(getContext());
                cancleDialog.setMessage(getString(R.string.msgSendCancel));
                cancleDialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        mListener.onSendTxCancel();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                cancleDialog.show();
                break;

            case R.id.btn_network:
                BottomSheetMenuDialog menuDialog = new BottomSheetMenuDialog(getContext(), getString(R.string.selectNetwork),
                        BottomSheetMenuDialog.SHEET_TYPE.BASIC);
                List<String> networks = new ArrayList<>();
                networks.add(getString(R.string.networkMain));
                networks.add(getString(R.string.networkTest));

                menuDialog.setBasicData(networks);
                menuDialog.setOnItemClickListener(mItemListener);

                menuDialog.show();
                break;

            case R.id.del_step_limit:
                editLimit.setText("");
                break;

            case R.id.info_step_limit:
                info.setMessage(getString(R.string.msgStepLimit));
                info.show();
                break;

            case R.id.info_step_price:
                message = getString(R.string.msgStepPrice);
                info = new BasicDialog(getActivity(), BasicDialog.TYPE.SUPER, message.indexOf("-"), message.indexOf("-") + 3);
                info.setMessage(getString(R.string.msgStepPrice));
                info.show();
                break;

            case R.id.info_data:
                info.setMessage(getString(R.string.msgIcxData));
                info.show();
                break;

            case R.id.info_fee:
                info.setMessage(getString(R.string.msgICXEstimateFee));
                info.show();
                break;

            case R.id.btn_send:
                SendConfirmDialog sendConfirmDialog =
                        new SendConfirmDialog(getActivity(),
                                txtAmount.getText().toString(),
                                txtFee.getText().toString(),
                                txtTo.getText().toString());
                sendConfirmDialog.setOnDialogListener(() -> {
                    SignedTransaction signedTransaction =
                            new SignedTransaction(transaction, keyWallet, step);

                    Observable.just(0)
                            .map(i -> iconService.sendTransaction(signedTransaction).execute())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Observer<Bytes>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onNext(Bytes bytes) {
                                    Log.d(TAG, "txhash=" + bytes);
                                    mListener.sendTransaction(bytes);
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onComplete() {

                                }
                            });
                });
                sendConfirmDialog.show();
                break;

            case R.id.btn_open:
                if (txtTxData.getVisibility() == View.VISIBLE) {
                    txtTxData.setVisibility(GONE);
                    txtOpenState.setText(getString(R.string.view));
                    imgArrow.setBackgroundResource(R.drawable.ic_arrow_down);
                } else {
                    txtTxData.setVisibility(View.VISIBLE);
                    txtOpenState.setText(getString(R.string.fold));
                    imgArrow.setBackgroundResource(R.drawable.ic_arrow_up);
                    txtTxData.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
                break;
        }
    }

    private void setData() {
        BigInteger value = transaction.getValue();
        if (value == null) {
            txtAmount.setText("0");
        } else {
            txtAmount.setText(ConvertUtil.getValue(value, 18));
        }

        Address to = transaction.getTo();
        if (to != null)
            txtTo.setText(to.toString());

        try {
            String dataType = transaction.getDataType();
            if (dataType != null) {
                layoutTxData.setVisibility(View.VISIBLE);
                JsonObject jsonObject = new Gson().fromJson(txString, JsonObject.class);
                JsonObject params = jsonObject.get("params").getAsJsonObject();

                JsonObject dataObject = new JsonObject();
                dataObject.addProperty("dataType", params.get("dataType").getAsString());

                if (params.get("dataType").getAsString().equals("message"))
                    dataObject.addProperty("data", params.get("data").getAsString());
                else
                    dataObject.add("data", params.get("data").getAsJsonObject());

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                txtTxData.setText(String.format(Locale.getDefault(), "%s",
                        gson.toJson(dataObject)));
            } else {
                layoutTxData.setVisibility(GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            mListener.parseError();
        }

        setInfoData();
    }

    private void parseTransaction(String txString) {
        JsonObject jsonObject = new Gson().fromJson(txString, JsonObject.class);
        Log.d(TAG, txString);

        JsonObject params = jsonObject.get("params").getAsJsonObject();
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();
        JsonParser parser;

        try {
            parser = factory.createParser(params.toString());
            transaction = RequestDeserializer.deserialize(parser);
        } catch (IOException e) {
            e.printStackTrace();
            mListener.parseError();
        }
    }

    private void loadKeyWallet(byte[] privateKey) {
        keyWallet = KeyWallet.load(new Bytes(privateKey));
    }

    private void initIconService() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        String url = null;
        switch (ICONexApp.NETWORK.getNid().intValue()) {
            case MyConstants.NETWORK_MAIN:
                url = ServiceConstants.TRUSTED_HOST_MAIN;
                break;

            case MyConstants.NETWORK_TEST:
                url = ServiceConstants.TRUSTED_HOST_TEST;
                break;

            case MyConstants.NETWORK_DEV:
                url = ServiceConstants.DEV_HOST;
                break;
        }

        iconService = new IconService(new HttpProvider(httpClient, url, 3));
    }

    private void setInfoData() {

        Completable.fromAction(() -> {
            balance = iconService.getBalance(transaction.getFrom()).execute();

            final Address scoreAddress = new Address("cx0000000000000000000000000000000000000001");

            foundation.icon.icx.Call<RpcItem> call = new foundation.icon.icx.Call.Builder()
                    .to(scoreAddress)
                    .method("getStepPrice")
                    .build();

            RpcItem result = iconService.call(call).execute();
            stepPrice = ConvertUtil.hexStringToBigInt(result.toString(), 18);

            String trList = "icxusd";

            String url = null;
            switch (ICONexApp.NETWORK.getNid().intValue()) {
                case MyConstants.NETWORK_MAIN:
                    url = ServiceConstants.URL_VERSION_MAIN;
                    break;

                case MyConstants.NETWORK_TEST:
                    url = ServiceConstants.URL_VERSION_TEST;
                    break;

                case MyConstants.NETWORK_DEV:
                    url = ServiceConstants.DEV_TRACKER;
                    break;
            }

            LoopChainClient client = new LoopChainClient(url);
            Call<TRResponse> responseCall = client.getExchangeRates(trList);
            TRResponse trResponse = responseCall.execute().body();
            String trResponseResult = trResponse.getResult();
            if (trResponseResult.equals(MyConstants.RESULT_OK)) {
                JsonElement data = trResponse.getData();
                JsonArray list = data.getAsJsonArray();
                for (int i = 0; i < list.size(); i++) {
                    JsonObject item = list.get(i).getAsJsonObject();
                    trPrice = item.get("price").getAsString();
                }
            }

            if (transaction.getData() == null)
                step = new BigInteger("100000");
            else
                step = new BigInteger("1000000");
//            step = iconService.estimateStep(transaction).execute();
//            long requestId = System.currentTimeMillis();
//            foundation.icon.icx.transport.jsonrpc.Request request = new foundation.icon.icx.transport.jsonrpc.Request(
//                    requestId, "debug_estimateStep", transaction.getProperties());
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {

                        String icx = ConvertUtil.getValue(stepPrice, 18);
                        String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtStepICX.setText(mIcx);

                        String gloop = ConvertUtil.getValue(stepPrice, 9);
                        String mGloop = gloop.indexOf(".") < 0 ? gloop : gloop.replaceAll("0*$", "").replaceAll("\\.$", "");
                        txtStepGloop.setText(String.format(Locale.getDefault(), "ICX (%s Gloop)", mGloop));

                        JsonObject jsonObject = new Gson().fromJson(txString, JsonObject.class);
                        jsonObject.get("params").getAsJsonObject().addProperty("stepLimit", "0x" + step.toString(16));

                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        String stepLimitAdded = gson.toJson(jsonObject);

                        parseTransaction(stepLimitAdded);

                        editLimit.setText(String.format(Locale.getDefault(), "%s",
                                step.toString()));

                        BigInteger fee = step.multiply(stepPrice);
                        txtFee.setText(String.format(Locale.getDefault(), "%s",
                                ConvertUtil.getValue(fee, 18)));

                        txtRemain.setText(String.format(Locale.getDefault(), "%s",
                                ConvertUtil.getValue(balance.subtract(fee), 18)));

                        btnSend.setEnabled(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void validateStep() {
        BigInteger step = new BigInteger(editLimit.getText().toString());
        if (step == BigInteger.ZERO) {
            btnSend.setEnabled(false);
        } else if (step.compareTo(new BigInteger("2500000")) > 0) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);
            txtLimitWarning.setText(String.format(Locale.getDefault(), getString(R.string.errMaxStep), "2500000"));

            btnSend.setEnabled(false);
        } else if (step.compareTo(new BigInteger("100000")) < 0) {
            lineLimit.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtLimitWarning.setVisibility(View.VISIBLE);
            txtLimitWarning.setText(String.format(Locale.getDefault(), getString(R.string.errMinStep), "100000"));

            btnSend.setEnabled(false);
        } else {
            if (editLimit.hasFocus())
                lineLimit.setBackgroundColor(getResources().getColor(R.color.editActivated));
            else
                lineLimit.setBackgroundColor(getResources().getColor(R.color.editNormal));
            editLimit.setSelection(editLimit.getText().toString().length());
            txtLimitWarning.setVisibility(View.GONE);

            btnSend.setEnabled(true);
        }
    }

    private BottomSheetMenuDialog.OnItemClickListener mItemListener = new BottomSheetMenuDialog.OnItemClickListener() {
        @Override
        public void onBasicItem(String item) {
            PreferenceUtil preferenceUtil = new PreferenceUtil(getContext());
            if (item.equals(getString(R.string.networkMain))) {
                txtNetwork.setText(getString(R.string.networkMain));
                ICONexApp.network = MyConstants.NETWORK_MAIN;
                preferenceUtil.setNetwork(ICONexApp.network);
            } else {
                txtNetwork.setText(getString(R.string.networkTest));
                ICONexApp.network = MyConstants.NETWORK_TEST;
                preferenceUtil.setNetwork(ICONexApp.network);
            }
        }

        @Override
        public void onCoinItem(int position) {

        }

        @Override
        public void onMenuItem(String tag) {

        }
    };

    private SendTransactionFragmentListener mListener;

    public interface SendTransactionFragmentListener {

        void sendTransaction(Bytes txHash);

        void parseError();

        void onSendTxCancel();
    }
}
