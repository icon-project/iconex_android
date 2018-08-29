package foundation.icon.iconex.token.manage;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.exceptions.ContractCallException;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;

import ethereum.contract.MyContract;
import ethereum.contract.MyTransactionManager;
import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.control.Token;
import foundation.icon.iconex.control.WalletEntry;
import foundation.icon.iconex.control.WalletInfo;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.RESTClient;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.widgets.MyEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static foundation.icon.iconex.ICONexApp.network;

public class TokenManageFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = TokenManageFragment.class.getSimpleName();

    private static final String ARG_ADDRESS = "ARG_ADDRESS";
    private static final String ARG_MODE = "ARG_MODE";
    private static final String ARG_TOKEN = "ARG_TOKEN";

    private String mWalletAddr;
    private MyConstants.MODE_TOKEN mMode;
    private WalletEntry mToken;

    private MyEditText editAddr, editName, editSym, editDec;
    private View lineAddr, lineName, lineSym, lineDec;
    private Button delAddr, delName, delSym;
    private TextView txtAddrWarning, txtNameWarning, txtSymbolWarning, txtDecWarning;

    private ImageView btnScan;
    private Button btnDel, btnAdd;

    private ViewGroup layoutLoading;

    private boolean isEditable = false;

    private String defaultName;
    private String defaultSym = null;
    private int defaultDec = -1;

    private EDIT_STATUS editStatus;

    private static final int RC_SCAN = 11111;

    private OnKeyPreImeListener onKeyPreImeListener = new OnKeyPreImeListener() {
        @Override
        public void onBackPressed() {
            checkTokenInfo();
        }
    };

    public TokenManageFragment() {
        // Required empty public constructor
    }

    public static TokenManageFragment newInstance(String walletAddress, MyConstants.MODE_TOKEN mode, WalletEntry token) {
        TokenManageFragment fragment = new TokenManageFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ADDRESS, walletAddress);
        bundle.putSerializable(ARG_MODE, mode);
        bundle.putSerializable(ARG_TOKEN, token);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mWalletAddr = getArguments().getString(ARG_ADDRESS);
            mMode = (MyConstants.MODE_TOKEN) getArguments().get(ARG_MODE);
            mToken = (WalletEntry) getArguments().get(ARG_TOKEN);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_token_manage, container, false);

        layoutLoading = v.findViewById(R.id.layout_loading);

        editAddr = v.findViewById(R.id.edit_address);
        editAddr.setOnKeyPreImeListener(onKeyPreImeListener);
        editAddr.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineAddr.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineAddr.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    checkAddress(editAddr.getText().toString());
                }
            }
        });
        editAddr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (mMode != MyConstants.MODE_TOKEN.MOD) {
                        delAddr.setVisibility(View.VISIBLE);

                        if (s.length() == 42) {
                            boolean available = checkAddress(s.toString());
                            if (available)
                                getTokenInfo(s.toString());
                        }
                    } else {
                        delAddr.setVisibility(View.INVISIBLE);
                    }
                } else {
                    delAddr.setVisibility(View.INVISIBLE);
                    txtAddrWarning.setVisibility(View.INVISIBLE);
                    if (editAddr.isFocused())
                        lineAddr.setBackgroundColor(getResources().getColor(R.color.editActivated));
                    else
                        lineAddr.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
//                if (mMode != MyConstants.MODE_TOKEN.MOD) {
//                    if (s.length() == 42) {
//                        boolean available = checkAddress(s.toString());
//                        if (available)
//                            getTokenInfo(s.toString());
//                    }
//                }
            }
        });

        editName = v.findViewById(R.id.edit_name);
        editName.setOnKeyPreImeListener(onKeyPreImeListener);
        editName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mMode == MyConstants.MODE_TOKEN.ADD) {
                    if (s.length() > 0) {
                        delName.setVisibility(View.VISIBLE);
                    } else {
                        delName.setVisibility(View.INVISIBLE);
                        txtNameWarning.setVisibility(View.INVISIBLE);
                        if (editName.isFocused())
                            lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
                        else
                            lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));
                    }
                } else if (mMode == MyConstants.MODE_TOKEN.MOD) {
                    if (s.length() > 0) {
                        if (isEditable)
                            delName.setVisibility(View.VISIBLE);
                        else {
                            delName.setVisibility(View.INVISIBLE);
                            txtNameWarning.setVisibility(View.INVISIBLE);
                            if (editName.isFocused())
                                lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
                            else
                                lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    checkTokenInfo();
                }
                return false;
            }
        });

        editSym = v.findViewById(R.id.edit_symbol);
        editSym.setOnKeyPreImeListener(onKeyPreImeListener);
        editSym.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineSym.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineSym.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });
        editSym.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mMode == MyConstants.MODE_TOKEN.ADD) {
                    if (s.length() > 0) {
                        delSym.setVisibility(View.VISIBLE);
                    } else {
                        delSym.setVisibility(View.INVISIBLE);
                    }
                } else if (mMode == MyConstants.MODE_TOKEN.MOD) {
                    if (s.length() > 0) {
                        if (isEditable)
                            delSym.setVisibility(View.VISIBLE);
                        else
                            delSym.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editDec = v.findViewById(R.id.edit_decimals);
        editDec.setOnKeyPreImeListener(onKeyPreImeListener);
        editDec.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineDec.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineDec.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });

        lineAddr = v.findViewById(R.id.line_address);
        lineName = v.findViewById(R.id.line_name);
        lineSym = v.findViewById(R.id.line_symbol);
        lineDec = v.findViewById(R.id.line_decimals);

        delAddr = v.findViewById(R.id.del_address);
        delAddr.setOnClickListener(this);
        delName = v.findViewById(R.id.del_name);
        delName.setOnClickListener(this);
        delSym = v.findViewById(R.id.del_symbol);
        delSym.setOnClickListener(this);

        btnScan = v.findViewById(R.id.btn_qr_scan);
        btnScan.setOnClickListener(this);

        btnDel = v.findViewById(R.id.btn_delete_token);
        btnDel.setOnClickListener(this);
        btnAdd = v.findViewById(R.id.btn_add_token);
        btnAdd.setOnClickListener(this);

        txtAddrWarning = v.findViewById(R.id.txt_addr_warning);
        txtNameWarning = v.findViewById(R.id.txt_name_warning);
        txtSymbolWarning = v.findViewById(R.id.txt_symbol_warning);
        txtDecWarning = v.findViewById(R.id.txt_dec_warning);

        if (mMode == MyConstants.MODE_TOKEN.MOD) {
            setReadOnly();
        } else {
            btnScan.setVisibility(View.VISIBLE);
            btnDel.setVisibility(View.GONE);
            btnAdd.setVisibility(View.VISIBLE);

            editAddr.requestFocus();
            editName.setEnabled(true);
            lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTokenManageListener) {
            mListener = (OnTokenManageListener) context;
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.del_address:
                editAddr.setText("");
                btnAdd.setEnabled(false);
                txtAddrWarning.setVisibility(View.INVISIBLE);
                if (editAddr.isFocused())
                    lineAddr.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    lineAddr.setBackgroundColor(getResources().getColor(R.color.editNormal));
                break;

            case R.id.del_name:
                editName.setText("");
                btnAdd.setEnabled(false);
                txtNameWarning.setVisibility(View.INVISIBLE);
                if (editName.isFocused())
                    lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));
                break;

            case R.id.del_symbol:
                editSym.setText("");
                btnAdd.setEnabled(false);
                break;

            case R.id.btn_delete_token:
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        deleteToken();
                        mListener.onClose();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.setMessage(String.format(getString(R.string.msgTokenDelete), mToken.getUserName()));
                dialog.show();
                break;

            case R.id.btn_qr_scan:
                startActivityForResult(new Intent(getActivity(), BarcodeCaptureActivity.class)
                        .putExtra(BarcodeCaptureActivity.AutoFocus, true)
                        .putExtra(BarcodeCaptureActivity.UseFlash, false), RC_SCAN);
                break;

            case R.id.btn_add_token:
                if (checkTokenInfo()) {
                    addToken();
                    mListener.onClose();
                }
                break;
        }
    }

    private void getTokenInfo(String address) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editAddr.getWindowToken(), 0);

        if (layoutLoading.getVisibility() != View.VISIBLE)
            layoutLoading.setVisibility(View.VISIBLE);

        GetTokenInfo getTokenInfo = new GetTokenInfo();
        getTokenInfo.execute(address);
    }

    private void setReadOnly() {
        isEditable = false;

        editAddr.setFocusable(false);
        editAddr.setEnabled(false);
        editAddr.setText(mToken.getContractAddress());
        lineAddr.setBackgroundColor(getResources().getColor(R.color.editReadOnly));

        btnScan.setVisibility(View.GONE);

        editName.setEnabled(false);
        editName.setText(mToken.getUserName());
        lineName.setBackgroundColor(getResources().getColor(R.color.editReadOnly));

        editSym.setEnabled(false);
        editSym.setText(mToken.getSymbol());
        lineSym.setBackgroundColor(getResources().getColor(R.color.editReadOnly));

        editDec.setEnabled(false);
        editDec.setText(String.valueOf(mToken.getDefaultDec()));
        lineDec.setBackgroundColor(getResources().getColor(R.color.editReadOnly));

        btnDel.setVisibility(View.GONE);

        editStatus = EDIT_STATUS.READ_ONLY;
    }

    public void setEditable() {
        isEditable = true;

        editName.setEnabled(true);
        editName.setText(editName.getText().toString());
        lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));

//        editSym.setEnabled(true);
//        editSym.setText(editSym.getText().toString());
//        lineSym.setBackgroundColor(getResources().getColor(R.color.editNormal));

        btnDel.setVisibility(View.VISIBLE);

        editStatus = EDIT_STATUS.EDIT;
    }

    public void onEditDone() {
        boolean check = checkTokenInfo();
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
            mListener.onDone(editName.getText().toString());
        }
    }

    private boolean checkAddress(String address) {
        if (address.isEmpty()) {
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(getString(R.string.errNoAddress));
            lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));

            return false;
        }

        if (!address.startsWith(MyConstants.PREFIX_ETH)) {
            lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(getString(R.string.errContractAddress));
            return false;
        } else if (checkAddressDup(address)) {
            lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setVisibility(View.VISIBLE);
            txtAddrWarning.setText(getString(R.string.errTokenDuplication));
            return false;
        }

        if (editAddr.hasFocus())
            lineAddr.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            lineAddr.setBackgroundColor(getResources().getColor(R.color.editNormal));

        txtAddrWarning.setVisibility(View.INVISIBLE);

        return true;
    }

    private boolean checkTokenInfo() {

        boolean resultAddr = true;
        boolean resultName;
        boolean resultSym;

        String address = editAddr.getText().toString();

        if (mMode == MyConstants.MODE_TOKEN.ADD) {
            if (address.isEmpty()) {
                txtAddrWarning.setVisibility(View.VISIBLE);
                txtAddrWarning.setText(getString(R.string.errNoAddress));
                lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                return false;
            } else {
                if (!address.startsWith(MyConstants.PREFIX_ETH)) {
                    txtAddrWarning.setVisibility(View.VISIBLE);
                    txtAddrWarning.setText(getString(R.string.errContractAddress));
                    lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                    resultAddr = false;
                } else {
                    String temp = address.substring(2);
                    if (temp.length() != 40) {
                        txtAddrWarning.setVisibility(View.VISIBLE);
                        txtAddrWarning.setText(getString(R.string.errContractAddress));
                        lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                        resultAddr = false;
                    } else if (checkAddressDup(temp)) {
                        txtAddrWarning.setVisibility(View.VISIBLE);
                        txtAddrWarning.setText(getString(R.string.errTokenDuplication));
                        lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));

                        resultAddr = false;
                    } else {
                        if (editAddr.hasFocus())
                            lineAddr.setBackgroundColor(getResources().getColor(R.color.editActivated));
                        else
                            lineAddr.setBackgroundColor(getResources().getColor(R.color.editNormal));

                        txtAddrWarning.setVisibility(View.INVISIBLE);

                        resultAddr = true;
                    }
                }
            }
        }

//        if (editStatus != EDIT_STATUS.READ_ONLY) {
        if (editName.getText().toString().isEmpty()) {
            txtNameWarning.setVisibility(View.VISIBLE);
            lineName.setBackgroundColor(getResources().getColor(R.color.colorWarning));

            resultName = false;
        } else {
            if (editName.hasFocus())
                lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
            else
                lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));

            txtNameWarning.setVisibility(View.INVISIBLE);

            resultName = true;
        }

        if (mMode == MyConstants.MODE_TOKEN.ADD) {
            if (resultAddr && resultName)
                btnAdd.setEnabled(true);
            else
                btnAdd.setEnabled(false);
        }

        return resultAddr && resultName;
//        }

//        return false;
    }

    private boolean checkAddressDup(String address) {
        for (WalletInfo info : ICONexApp.mWallets) {
            if (mWalletAddr.equals(info.getAddress())) {
                for (WalletEntry entry : info.getWalletEntries()) {
                    if (address.equals(entry.getContractAddress()))
                        return true;
                }
            }
        }

        return false;
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

    private void deleteToken() {
        try {
            RealmUtil.deleteToken(mWalletAddr, editAddr.getText().toString());
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SCAN) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    editAddr.setText(barcode.displayValue);
                } else {
//                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            }
        }
    }

    private OnTokenManageListener mListener;

    public interface OnTokenManageListener {
        void onClose();

        void onDone(String name);
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
//                lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));
//                txtAddrWarning.setText(getString(R.string.errTokenInfo));
//                txtAddrWarning.setVisibility(View.VISIBLE);
//
//                btnAdd.setEnabled(false);
                getEthTokens((String) results.get(KEY_ERROR));
            } else {
                if (layoutLoading.getVisibility() == View.VISIBLE)
                    layoutLoading.setVisibility(View.GONE);

                editName.setText((String) results.get(KEY_NAME));
                defaultName = (String) results.get(KEY_NAME);
                editName.setEnabled(true);

                editSym.setText((String) results.get(KEY_SYMBOL));
                defaultSym = (String) results.get(KEY_SYMBOL);
                delSym.setVisibility(View.INVISIBLE);

                editDec.setText(results.get(KEY_DECIMALS).toString());
                defaultDec = Integer.parseInt(results.get(KEY_DECIMALS).toString());

                editStatus = EDIT_STATUS.LOADED;

                checkTokenInfo();
            }
        }
    }

    private void getEthTokens(String contract) {
        try {
            String contents = Utils.readAssets(getActivity(), MyConstants.ETH_TOKEN_FILE);

            JsonArray tokens = new Gson().fromJson(contents, JsonArray.class);

            for (int i = 0; i < tokens.size(); i++) {
                JsonObject token = tokens.get(i).getAsJsonObject();
                String tokenContract = token.get("address").getAsString();
                if (tokenContract.equalsIgnoreCase(contract)) {
                    Log.d(TAG, "Address=" + tokenContract);
                    Log.d(TAG, "Symbol=" + token.get("symbol").getAsString());
                    Log.d(TAG, "decimal=" + token.get("decimal").getAsInt());
                    Log.d(TAG, "type=" + token.get("type").getAsString());

                    editSym.setText(token.get("symbol").getAsString());
                    defaultSym = token.get("symbol").getAsString();
                    delSym.setVisibility(View.INVISIBLE);

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

            lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));
            txtAddrWarning.setText(getString(R.string.errTokenInfo));
            txtAddrWarning.setVisibility(View.VISIBLE);

            btnAdd.setEnabled(false);
        }
    }

    private void getEthTokensFromServer(final String contract) {
        try {
            RESTClient client = new RESTClient(ServiceConstants.RAW_JSON);
            Call<JsonElement> get = client.getEthTokens();
            get.enqueue(new Callback<JsonElement>() {
                @Override
                public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                    JsonArray tokens = null;
                    if (response.isSuccessful()) {
                        tokens = response.body().getAsJsonArray();

                        Log.d(TAG, "Tokens=" + tokens);
                        Log.d(TAG, "Tokens size=" + tokens.size());

                        Log.d(TAG, "Contract=" + contract);

                        for (int i = 0; i < tokens.size(); i++) {
                            JsonObject token = tokens.get(i).getAsJsonObject();
                            String tokenContract = token.get("address").getAsString();
                            if (tokenContract.equalsIgnoreCase(contract)) {
                                Log.d(TAG, "Address=" + tokenContract);
                                Log.d(TAG, "Symbol=" + token.get("symbol").getAsString());
                                Log.d(TAG, "decimal=" + token.get("decimal").getAsInt());
                                Log.d(TAG, "type=" + token.get("type").getAsString());

                                editSym.setText(token.get("symbol").getAsString());
                                defaultSym = token.get("symbol").getAsString();
                                delSym.setVisibility(View.INVISIBLE);

                                editDec.setText(Integer.toString(token.get("decimal").getAsInt()));
                                defaultDec = token.get("decimal").getAsInt();
                            }
                        }

                        if (defaultSym == null || defaultDec == -1) {
                            lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                            txtAddrWarning.setText(getString(R.string.errTokenInfo));
                            txtAddrWarning.setVisibility(View.VISIBLE);

                            btnAdd.setEnabled(false);
                        }

                        if (layoutLoading.getVisibility() == View.VISIBLE)
                            layoutLoading.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(Call<JsonElement> call, Throwable t) {
                    if (layoutLoading.getVisibility() == View.VISIBLE)
                        layoutLoading.setVisibility(View.GONE);

                    lineAddr.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                    txtAddrWarning.setText(getString(R.string.errTokenInfo));
                    txtAddrWarning.setVisibility(View.VISIBLE);

                    btnAdd.setEnabled(false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private enum EDIT_STATUS {
        READ_ONLY,
        LOADED,
        EDIT
    }
}
