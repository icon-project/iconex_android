package foundation.icon.iconex.token.manage;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.exceptions.ContractCallException;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import ethereum.contract.MyContract;
import ethereum.contract.MyTransactionManager;
import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.token.Token;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.TTextInputLayout;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static foundation.icon.ICONexApp.network;

public class TokenManageFragment extends Fragment implements TTextInputLayout.OnKeyPreIme, View.OnClickListener {

    private static final String TAG = TokenManageFragment.class.getSimpleName();

    private static final String ARG_ADDRESS = "ARG_ADDRESS";
    private static final String ARG_MODE = "ARG_MODE";
    private static final String ARG_TOKEN = "ARG_TOKEN";
    private static final String ARG_TOKEN_TYPE = "ARG_TOKEN_TYPE";

    private TokenManageActivity.TOKEN_TYPE tokenType;

    private String mWalletAddr;
    private MyConstants.MODE_TOKEN mMode;
    private WalletEntry mToken;

    private TTextInputLayout editAddr, editName, editSym, editDec;

    private ImageView btnScan;
    // private Button btnDel; move to actionbar
    private Button btnAdd;
    private Button btnComplete; // add complete button
    private ViewGroup layoutAdd;
    private ViewGroup layoutComplete; // add complete button

    private ViewGroup layoutLoading;

    private boolean isEditable = false;

    private String defaultName;
    private String defaultSym = null;
    private int defaultDec = -1;

    public interface OnTokenManageListener {
        void onClose();
        void onDoneEditToken(String name);
        void onDoneAddToken();
    }

    private OnTokenManageListener mListener;

    private enum EDIT_STATUS {
        READ_ONLY,
        LOADED,
        EDIT
    }

    private EDIT_STATUS editStatus;

    private static final int RC_SCAN = 11111;

    public TokenManageFragment() {
        // Required empty public constructor
    }

    public static TokenManageFragment newInstance(String walletAddress, MyConstants.MODE_TOKEN mode, TokenManageActivity.TOKEN_TYPE type, WalletEntry token) {
        TokenManageFragment fragment = new TokenManageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ADDRESS, walletAddress);
        bundle.putSerializable(ARG_MODE, mode);
        bundle.putSerializable(ARG_TOKEN_TYPE, type);
        bundle.putSerializable(ARG_TOKEN, token);
        fragment.setArguments(bundle);
        return fragment;
    }

    // =========================================== fragment lifecycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mWalletAddr = getArguments().getString(ARG_ADDRESS);
            mMode = (MyConstants.MODE_TOKEN) getArguments().get(ARG_MODE);
            tokenType = (TokenManageActivity.TOKEN_TYPE) getArguments().get(ARG_TOKEN_TYPE);
            mToken = (WalletEntry) getArguments().get(ARG_TOKEN);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_token_manage_new, container, false);

        layoutLoading = v.findViewById(R.id.layout_loading);

        editAddr = v.findViewById(R.id.edit_address);
        editName = v.findViewById(R.id.edit_name);
        editSym = v.findViewById(R.id.edit_symbol);
        editDec = v.findViewById(R.id.edit_decimals);

        editSym.setInputEnabled(false);
        editDec.setInputEnabled(false);

        btnScan = v.findViewById(R.id.btn_qr_scan);
        btnAdd = v.findViewById(R.id.btn_add_token);
        btnComplete = v.findViewById(R.id.btn_complete);

        layoutAdd = v.findViewById(R.id.layout_add_token);
        layoutComplete = v.findViewById(R.id.layout_complete);

        initView();

        if (mMode == MyConstants.MODE_TOKEN.MOD) {
            setReadOnly();
        } else {
            btnScan.setVisibility(View.VISIBLE);
            layoutAdd.setVisibility(View.VISIBLE);
            editName.setEnabled(true);
            editAddr.setFocus(true);
            editAddr.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editAddr.getEditView(), InputMethodManager.SHOW_FORCED);
                }
            }, 100);
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TokenManageFragment.OnTokenManageListener) {
            mListener = (TokenManageFragment.OnTokenManageListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnTokenManageListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onPause() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editAddr.getWindowToken(), 0);
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SCAN) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    editAddr.setText(barcode.displayValue);
                    editAddr.setSelection(editAddr.getText().toString().length());
                } else {
//                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            }
        }
    }

    // ============================= public method
    public void setEditable() {
        isEditable = true;

        editName.setInputEnabled(true);
        editName.setText(editName.getText().toString());

        layoutComplete.setVisibility(View.VISIBLE);

        editStatus = EDIT_STATUS.EDIT;
    }

    public void deleteToken() {
        MessageDialog messageDialog = new MessageDialog(getContext());
        messageDialog.setMessage(String.format(Locale.getDefault(), getString(R.string.msgTokenDelete),
                mToken.getUserName()));
        messageDialog.setSingleButton(false);
        messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
            @Override
            public Boolean invoke(View view) {
                try {
                    RealmUtil.deleteToken(mWalletAddr, editAddr.getText().toString());
                    RealmUtil.loadWallet();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mListener.onClose();
                return true;
            }
        });
        messageDialog.show();
    }

    public boolean isEmpty() {
        return editAddr.getText().toString().trim().isEmpty()
                && editName.getText().toString().trim().isEmpty()
                && editSym.getText().toString().isEmpty()
                && editDec.getText().toString().trim().isEmpty();
    }

    public boolean isEdited() {
        return !mToken.getUserName().equals(editName.getText());
    }

    // =================================== private method
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_qr_scan: {
                startActivityForResult(new Intent(getActivity(), BarcodeCaptureActivity.class)
                        .putExtra(BarcodeCaptureActivity.PARAM_SCANTYPE,
                                tokenType == TokenManageActivity.TOKEN_TYPE.IRC ?
                                        BarcodeCaptureActivity.ScanType.ICX_Address.name() :
                                        BarcodeCaptureActivity.ScanType.ETH_Address.name()
                        )
                        .putExtra(BarcodeCaptureActivity.AutoFocus, true)
                        .putExtra(BarcodeCaptureActivity.UseFlash, false), RC_SCAN);
            }
            break;
            case R.id.btn_complete: {
                completeToken();
            }
            break;
            case R.id.btn_add_token: {
                if (validateToken(true)) {
                    addToken();
                    mListener.onDoneAddToken();
                }
            }
            break;
        }
    }

    private void addToken() {
        Token token = new Token();

        String userName = editName.getText().toString();
        String userSym = editSym.getText().toString();
        int userDec = Integer.parseInt(editDec.getText().toString());

        token.setContractAddress(editAddr.getText().toString());
        token.setUserName(userName);
        if (defaultName == null)
            defaultName = userName;
        token.setDefaultName(defaultName);
        token.setUserSymbol(userSym);
        token.setDefaultSymbol(defaultSym);
        token.setUserDec(userDec);
        token.setDefaultDec(defaultDec);

        try {
            RealmUtil.addToken(mWalletAddr, token);
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void completeToken() {
        boolean check = validateToken(false);
        if (check) {
            try {
                RealmUtil.modToken(mWalletAddr, editAddr.getText().toString(),
                        editName.getText().toString(), editSym.getText().toString(),
                        Integer.parseInt(editDec.getText().toString()));
                RealmUtil.loadWallet();
            } catch (Exception e) {
                e.printStackTrace();
            }

            mToken.setUserName(editName.getText().toString());
            mToken.setSymbol(editSym.getText().toString());
            mToken.setUserDec(Integer.parseInt(editDec.getText().toString()));

            setReadOnly();
            mListener.onDoneEditToken(editName.getText().toString());
        }
    }

    // ============================= init View
    private void initView() {
        editAddr.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editAddr.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validateToken(true);
            }
        });
        editAddr.setOnFocusChangedListener(new TTextInputLayout.OnMyFocusChangedListener() {
            @Override
            public void onFocused() {

            }

            @Override
            public void onReleased() {
                if (!editAddr.getText().isEmpty())
                    validateAddress(editAddr.getText(), true);
            }
        });
        editAddr.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (mMode != MyConstants.MODE_TOKEN.MOD) {
                        if (s.length() == 42) {
                            boolean available = validateAddress(s.toString(), true);
                            if (available)
                                if (tokenType == TokenManageActivity.TOKEN_TYPE.IRC)
                                    getIrcToken(s.toString());
                                else
                                    getErcToken(s.toString());
                        }
                    }
                } else {
                    editAddr.setError(false, null);
                    btnAdd.setEnabled(false);
                    btnComplete.setEnabled(false);
                }
            }
        });

        editName.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                editName.clearFocus();
                validateToken(false);
            }
        });
        editName.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (mMode == MyConstants.MODE_TOKEN.ADD) {
                    if (s.length() <= 0) editName.setError(false, null);
                } else if (mMode == MyConstants.MODE_TOKEN.MOD) {
                    if (s.length() <= 0 && !isEditable)
                        editName.setError(false, null);
                }

                if (s.length() == 0) {
                    btnAdd.setEnabled(false);
                    btnComplete.setEnabled(false);
                }
            }
        });
        editName.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                editName.clearFocus();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editName.getWindowToken(), 0);
                validateToken(false);
            }
        });

        editSym.setOnKeyPreImeListener(this);
        editDec.setOnKeyPreImeListener(this);

        btnScan.setOnClickListener(this);
        btnAdd.setOnClickListener(this);
        btnComplete.setOnClickListener(this);
    }

    private void setReadOnly() {
        isEditable = false;

        editAddr.setInputEnabled(false);
        editAddr.setText(mToken.getContractAddress());

        btnScan.setVisibility(View.GONE);

        editName.setInputEnabled(false);
        editName.setText(mToken.getUserName());

        editSym.setInputEnabled(false);
        editSym.setText(mToken.getSymbol());

        editDec.setInputEnabled(false);
        editDec.setText(String.valueOf(mToken.getDefaultDec()));

        layoutAdd.setVisibility(View.GONE);
        layoutComplete.setVisibility(View.GONE);

        editStatus = EDIT_STATUS.READ_ONLY;
    }

    // ====================================== validate methods
    @Override // OnKeyPreImeListener
    public void onDone() {
        validateToken(false);
    }

    private boolean validateAddress(String address, boolean showErr) {
        if (address.isEmpty()) {
            if (showErr) editAddr.setError(true, getString(R.string.errNoAddress));
            return false;
        } else if (checkAddressDup(address)) {
            if (showErr) editAddr.setError(true, getString(R.string.errTokenDuplication));
            return false;
        }

        if (tokenType == TokenManageActivity.TOKEN_TYPE.IRC) {
            if (!address.startsWith(MyConstants.PREFIX_IRC)) {
                if (showErr) editAddr.setError(true, getString(R.string.errContractAddress));
                return false;
            }
        } else {
            if (!address.startsWith(MyConstants.PREFIX_HEX)) {
                if (showErr) editAddr.setError(true, getString(R.string.errContractAddress));
                return false;
            }
        }

        editAddr.setError(false, null);
        return true;
    }

    private boolean checkAddressDup(String address) {
        for (Wallet info : ICONexApp.wallets) {
            if (mWalletAddr.equals(info.getAddress())) {
                for (WalletEntry entry : info.getWalletEntries()) {
                    if (address.equals(entry.getContractAddress()))
                        return true;
                }
            }
        }

        return false;
    }

    private boolean validateToken(boolean showAddressErr) {
        boolean resultAddr = true;
        boolean resultName;

        String address = editAddr.getText().toString();

        if (mMode == MyConstants.MODE_TOKEN.ADD) {
            resultAddr = validateAddress(address,showAddressErr);
        }

        resultName = !editName.getText().isEmpty();

        if (mMode == MyConstants.MODE_TOKEN.ADD) {
            btnAdd.setEnabled(resultAddr && resultName);
        } else {
            btnComplete.setEnabled(resultName);
        }

        checkEnteredInfo();

        return resultAddr && resultName;
    }

    private void checkEnteredInfo() {
        if (!editName.getText().isEmpty()
                && !editSym.getText().isEmpty()
                && !editDec.getText().isEmpty()) {

            if (layoutLoading.getVisibility() == View.VISIBLE)
                layoutLoading.setVisibility(View.GONE);

            btnAdd.setEnabled(true);
        } else
            btnAdd.setEnabled(false);
    }

    // ======================================== Get Token method
    private void getErcToken(String address) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editAddr.getWindowToken(), 0);

        if (layoutLoading.getVisibility() != View.VISIBLE)
            layoutLoading.setVisibility(View.VISIBLE);

        GetTokenInfo getTokenInfo = new GetTokenInfo();
        getTokenInfo.execute(address);
    }

    private void getIrcToken(String address) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editAddr.getWindowToken(), 0);

        if (layoutLoading.getVisibility() != View.VISIBLE)
            layoutLoading.setVisibility(View.VISIBLE);

        editName.setText("");
        editSym.setText("");
        editDec.setText("");

        String url;
        switch (ICONexApp.NETWORK.getNid().intValue()) {
            case MyConstants.NETWORK_MAIN:
                url = ServiceConstants.TRUSTED_HOST_MAIN;
                break;

            case MyConstants.NETWORK_TEST:
                url = ServiceConstants.TRUSTED_HOST_TEST;
                break;

            default:
            case MyConstants.NETWORK_DEV:
                url = ServiceConstants.DEV_HOST;
                break;
        }

        try {
            int id = new Random().nextInt(999999) + 100000;
            LoopChainClient LCClient = new LoopChainClient(url);
            Call<LCResponse> responseCall = LCClient.getScoreApi(id, address);
            responseCall.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.errorBody() == null) {
                        getIrcTokenInfo(LCClient, address);
                    } else {
                        if (layoutLoading.getVisibility() == View.VISIBLE)
                            layoutLoading.setVisibility(View.GONE);

                        editAddr.setError(true, getString(R.string.errTokenInfo));
                        btnAdd.setEnabled(false);
                    }
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class GetTokenInfo extends AsyncTask<String, Void, HashMap<String, Object>> {

        private static final String KEY_NAME = "NAME";
        private static final String KEY_DECIMALS = "DECIMALS";
        private static final String KEY_SYMBOL = "SYMBOL";

        private static final String KEY_ERROR = "ERROR";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            defaultName = null;
            defaultSym = null;
            defaultDec = -1;

            editName.setText("");
            editSym.setText("");
            editDec.setText("");
        }

        @Override
        protected HashMap<String, Object> doInBackground(String... params) {
            HashMap<String, Object> results = new HashMap<>();
            String address = params[0];

            String url;
            if (network == MyConstants.NETWORK_MAIN)
                url = ServiceConstants.ETH_HOST;
            else
                url = ServiceConstants.ETH_ROP_HOST;

            try {
                Web3j web3j = Web3jFactory.build(new HttpService(url));
                MyTransactionManager transactionManager = new MyTransactionManager(web3j, address, Collections.EMPTY_LIST);
                MyContract contract = MyContract.load(address, web3j, transactionManager, Contract.GAS_PRICE, Contract.GAS_LIMIT);
                BigInteger decimals = contract.decimals().send();
                String symbol = contract.symbol().send();

                String name = contract.name().send();

                results.put(KEY_NAME, name);
                results.put(KEY_DECIMALS, decimals);
                results.put(KEY_SYMBOL, symbol);

                if (symbol.isEmpty() || decimals.compareTo(BigInteger.ZERO) < 0) {
                    results.put(KEY_ERROR, address);
                    return results;
                }

            } catch (ContractCallException contractException) {
                results.put(KEY_ERROR, address);
                return results;
            } catch (Exception e) {
                results.put(KEY_ERROR, address);
                return results;
            }

            return results;
        }

        @Override
        protected void onPostExecute(HashMap<String, Object> results) {
            super.onPostExecute(results);

            if (results.containsKey(KEY_ERROR)) {
                getEthTokenInfo((String) results.get(KEY_ERROR));
            } else {
                if (layoutLoading.getVisibility() == View.VISIBLE)
                    layoutLoading.setVisibility(View.GONE);

                editName.setText((String) results.get(KEY_NAME));
                defaultName = (String) results.get(KEY_NAME);
                editName.setEnabled(true);

                editSym.setText((String) results.get(KEY_SYMBOL));
                defaultSym = (String) results.get(KEY_SYMBOL);

                editDec.setText(results.get(KEY_DECIMALS).toString());
                defaultDec = Integer.parseInt(results.get(KEY_DECIMALS).toString());

                editStatus = EDIT_STATUS.LOADED;

                validateToken(true);
            }
        }
    }

    private void getEthTokenInfo(String contract) {
        try {
            String contents = Utils.readAssets(getActivity(), MyConstants.ETH_TOKEN_FILE);

            JsonArray tokens = new Gson().fromJson(contents, JsonArray.class);

            for (int i = 0; i < tokens.size(); i++) {
                JsonObject token = tokens.get(i).getAsJsonObject();
                String tokenContract = token.get("address").getAsString();
                if (tokenContract.equalsIgnoreCase(contract)) {
                    editSym.setText(token.get("symbol").getAsString());
                    defaultSym = token.get("symbol").getAsString();

                    editDec.setText(Integer.toString(token.get("decimal").getAsInt()));
                    defaultDec = token.get("decimal").getAsInt();

                    break;
                }
            }

            if (layoutLoading.getVisibility() == View.VISIBLE)
                layoutLoading.setVisibility(View.GONE);

            if (defaultSym == null || defaultDec == -1)
                throw new RuntimeException("No info");
        } catch (Exception e) {
            if (layoutLoading.getVisibility() == View.VISIBLE)
                layoutLoading.setVisibility(View.GONE);

            editAddr.setError(true, getString(R.string.errTokenInfo));
            btnAdd.setEnabled(false);
        }
    }

    private void getIrcTokenInfo(LoopChainClient client, String address) {
        JsonObject tokenName = new JsonObject();
        tokenName.addProperty("method", "name");
        JsonObject tokenDecimals = new JsonObject();
        tokenDecimals.addProperty("method", "decimals");
        JsonObject tokenSymbol = new JsonObject();
        tokenSymbol.addProperty("method", "symbol");

        try {
            Call<LCResponse> nameRes = client.sendIcxCall(111111, mWalletAddr, address, tokenName);
            nameRes.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        String name = response.body().getResult().getAsString();
                        defaultName = name;
                        editName.setText(name);
                        checkEnteredInfo();
                    }
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {

                }
            });

            Call<LCResponse> symbolRes = client.sendIcxCall(111112, mWalletAddr, address, tokenSymbol);
            symbolRes.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        String symbol = response.body().getResult().getAsString();
                        defaultSym = symbol;
                        editSym.setText(symbol);
                        checkEnteredInfo();
                    }
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {

                }
            });

            Call<LCResponse> decimalRes = client.sendIcxCall(111112, mWalletAddr, address, tokenDecimals);
            decimalRes.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        String decimals = response.body().getResult().getAsString();
                        defaultDec = Integer.decode(decimals).intValue();
                        editDec.setText(Integer.decode(decimals).toString());
                        checkEnteredInfo();
                    }
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {

                }
            });
        } catch (Exception e) {

        }
    }
}
