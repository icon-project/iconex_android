package foundation.icon.connect;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.math.BigInteger;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.transport.http.HttpProvider;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import static foundation.icon.ICONexApp.network;

public class PasswordFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = PasswordFragment.class.getSimpleName();

    private static final String ARG_WALLET = "wallet";

    private Wallet wallet;

    private TextView txtBalance;
    private ProgressBar progress;
    private ViewGroup layoutTxHash;
    private TextView txtTxHash;

    private TextView txtTxData;

    private MyEditText editPwd;
    private Button btnPwdDelete;
    private TextView txtPwdWarning;
    private View linePwd;

    private Button btnConfirm;
    private ProgressBar progressConfirm;

    private byte[] mPrivateKey;

    private ValidatePassword validatePassword;

    public PasswordFragment() {
        // Required empty public constructor
    }

    public static PasswordFragment newInstance(Wallet wallet) {
        PasswordFragment fragment = new PasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_WALLET, wallet);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            wallet = (Wallet) getArguments().getSerializable(ARG_WALLET);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_password, container, false);
        initView(v);

        return v;
    }

    private void initView(View v) {
        ((TextView) v.findViewById(R.id.txt_title)).setText(getString(R.string.enterWalletPassword));
        v.findViewById(R.id.btn_close).setOnClickListener(this);

        ((TextView) v.findViewById(R.id.txt_alias)).setText(wallet.getAlias());
        ((TextView) v.findViewById(R.id.txt_address)).setText(wallet.getAddress());
        txtBalance = v.findViewById(R.id.txt_balance);
        progress = v.findViewById(R.id.progress);

        editPwd = v.findViewById(R.id.edit_pwd);
        editPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });
        editPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    btnPwdDelete.setVisibility(View.VISIBLE);
                } else {
                    btnPwdDelete.setVisibility(View.INVISIBLE);
                    btnConfirm.setEnabled(false);
                    txtPwdWarning.setVisibility(View.INVISIBLE);

                    if (editPwd.hasFocus()) {
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    } else {
                        linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editPwd.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                validatePassword = new ValidatePassword();
                validatePassword.execute();
            }
        });
        editPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    validatePassword = new ValidatePassword();
                    validatePassword.execute();
                }
                return false;
            }
        });

        btnPwdDelete = v.findViewById(R.id.btn_pwd_delete);
        btnPwdDelete.setOnClickListener(this);
        txtPwdWarning = v.findViewById(R.id.txt_pwd_warning);
        linePwd = v.findViewById(R.id.line_pwd);

        btnConfirm = v.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        progressConfirm = v.findViewById(R.id.progress_confirm);
        progressConfirm.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                Basic2ButtonDialog cancelDialog = new Basic2ButtonDialog(getContext());
                cancelDialog.setMessage(getString(R.string.msgCancelPassword));
                cancelDialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        mListener.onPasswordCancel();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                cancelDialog.show();
                break;

            case R.id.btn_pwd_delete:
                editPwd.setText("");
                break;

            case R.id.btn_confirm:
                mListener.onValidatedPassword(mPrivateKey);
                break;
        }
    }

    private boolean validatePwd(String pwd) {
        JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);

        JsonObject crypto;
        if (keyStore.has("crypto"))
            crypto = keyStore.get("crypto").getAsJsonObject();
        else
            crypto = keyStore.get("Crypto").getAsJsonObject();

        byte[] privKey = null;
        try {
            privKey = KeyStoreUtils.decryptPrivateKey(pwd, wallet.getAddress(), crypto, wallet.getCoinType());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (privKey == null) {
            return false;
        } else {
            mPrivateKey = privKey;
            return true;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PasswordFragmentListener) {
            mListener = (PasswordFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PasswordFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        getBalance();
    }

    private void getBalance() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();

        String url = null;
        switch (network) {
            case MyConstants.NETWORK_MAIN:
                url = ServiceConstants.TRUSTED_HOST_MAIN + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                break;

            case MyConstants.NETWORK_TEST:
                url = ServiceConstants.TRUSTED_HOST_TEST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                break;

            case MyConstants.NETWORK_DEV:
                url = ServiceConstants.DEV_HOST + ServiceConstants.LC_API_HEADER + ServiceConstants.LC_API_V3;
                break;
        }

        IconService iconService = new IconService(new HttpProvider(httpClient, url));

        io.reactivex.Observable.just(wallet.getAddress())
                .map(address -> iconService.getBalance(new Address(address)).execute())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BigInteger>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(BigInteger bigInteger) {
                        progress.setVisibility(View.GONE);
                        if (bigInteger != null)
                            txtBalance.setText(ConvertUtil.getValue(bigInteger, 18));
                        else
                            txtBalance.setText("-");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    class ValidatePassword extends AsyncTask<Void, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btnConfirm.setEnabled(false);
            progressConfirm.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return validatePwd(editPwd.getText().toString());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            progressConfirm.setVisibility(View.GONE);

            if (result) {
                txtPwdWarning.setVisibility(View.GONE);
                if (editPwd.hasFocus())
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));

                btnConfirm.setEnabled(true);
            } else {
                txtPwdWarning.setVisibility(View.VISIBLE);
                linePwd.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                btnConfirm.setEnabled(false);
            }
        }
    }

    private PasswordFragmentListener mListener;

    public interface PasswordFragmentListener {

        void onValidatedPassword(byte[] privateKey);

        void onPasswordCancel();
    }
}
