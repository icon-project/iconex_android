package foundation.icon.iconex.wallet.load;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.MyEditText;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class SelectKeyStoreFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SelectKeyStoreFragment.class.getSimpleName();

    public static final int RC_READ_FILE = 8001;

    private OnSelectKeyStoreCallback mListener;

    private Button btnSelecFile, btnNext;
    private TextView txtFileWarning;
    private ViewGroup layoutFile;
    private TextView txtFileName;
    private Button btnFileCancel;

    private MyEditText editPwd;
    private View linePwd;
    private TextView txtPwdWarning;
    private Button btnPwdDelete;

    private ProgressBar progress;

    private boolean isBundle = false;
    private JsonObject mKeyStore;
    private List<Wallet> mKSBundle;

    public static boolean isSelect = false;

    public SelectKeyStoreFragment() {
        // Required empty public constructor
    }

    public static SelectKeyStoreFragment newInstance() {
        SelectKeyStoreFragment fragment = new SelectKeyStoreFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_wallet_select_key_store, container, false);

        btnSelecFile = v.findViewById(R.id.btn_select_keystore);
        btnSelecFile.setOnClickListener(this);

        txtFileWarning = v.findViewById(R.id.txt_file_warning);

        layoutFile = v.findViewById(R.id.layout_file);
        txtFileName = v.findViewById(R.id.txt_file_name);
        btnFileCancel = v.findViewById(R.id.btn_delete);
        btnFileCancel.setOnClickListener(this);

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
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editPwd.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                if (layoutFile.getVisibility() == View.VISIBLE) {
                    if (isBundle) {
                        List<Wallet> correctWallets = validateBundlePassword(editPwd.getText().toString(), mKSBundle);
                        if (correctWallets == null || correctWallets.size() == 0)
                            showPwdError(getString(R.string.errPassword));
                        else {
                            mKSBundle = correctWallets;
                            hidePwdError();
                            btnNext.setEnabled(true);
                        }
                    } else {
                        if (validatePassword(editPwd.getText().toString(), mKeyStore)) {
                            hidePwdError();
                            btnNext.setEnabled(true);
                        } else
                            showPwdError(getString(R.string.errPassword));
                    }
                } else {
                    if (!editPwd.getText().toString().isEmpty()) {
                        showPwdError(getString(R.string.errNoKeystoreFile));
                    }
                }
            }
        });
        editPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    if (layoutFile.getVisibility() == View.VISIBLE) {
                        if (isBundle) {
                            List<Wallet> correctWallets = validateBundlePassword(editPwd.getText().toString(), mKSBundle);
                            if (correctWallets == null || correctWallets.size() == 0)
                                showPwdError(getString(R.string.errPassword));
                            else {
                                mKSBundle = correctWallets;
                                hidePwdError();
                                btnNext.setEnabled(true);
                            }
                        } else {
                            if (validatePassword(editPwd.getText().toString(), mKeyStore)) {
                                hidePwdError();
                                btnNext.setEnabled(true);
                            } else
                                showPwdError(getString(R.string.errPassword));
                        }
                    } else {
                        if (!editPwd.getText().toString().isEmpty()) {
                            showPwdError(getString(R.string.errNoKeystoreFile));
                        }
                    }
                }
                return false;
            }
        });

        linePwd = v.findViewById(R.id.line_pwd);
        txtPwdWarning = v.findViewById(R.id.txt_pwd_warning);
        btnPwdDelete = v.findViewById(R.id.btn_pwd_delete);
        btnPwdDelete.setOnClickListener(this);

        progress = v.findViewById(R.id.progress);

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectKeyStoreCallback) {
            mListener = (OnSelectKeyStoreCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInputWalletNameCallback");
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

        isSelect = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_keystore:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, RC_READ_FILE);
                isSelect = true;
                break;

            case R.id.btn_delete:
                mKeyStore = null;
                layoutFile.setVisibility(View.GONE);
                btnNext.setEnabled(false);
                break;

            case R.id.btn_pwd_delete:
                editPwd.setText("");
                break;

            case R.id.btn_next:
                if (isBundle)
                    mListener.onKeyStoreBundleSelected(mKSBundle);
                else
                    mListener.onKeyStoreSelected(mKeyStore);
                break;
        }
    }

    public interface OnSelectKeyStoreCallback {
        void onKeyStoreSelected(JsonObject keyStore);

        void onKeyStoreBundleSelected(List<Wallet> wallets);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_READ_FILE) {
            if (resultCode == Activity.RESULT_OK) {
                editPwd.setText("");
                try {
                    boolean result;
                    JsonElement keyStore = new Gson().fromJson(readFile(data.getData()), JsonElement.class);

                    if (keyStore.isJsonObject()) {
                        isBundle = false;
                        result = validateKeyStore(keyStore.getAsJsonObject());
                        if (result) {
                            if (checkAddress(keyStore.getAsJsonObject().get("address").getAsString())) {
                                txtFileWarning.setVisibility(View.GONE);
                                layoutFile.setVisibility(View.VISIBLE);
                                String fileName = getFileName(data.getData());
                                txtFileName.setText(getFileName(data.getData()));

                                mKeyStore = keyStore.getAsJsonObject();
                            } else {
                                layoutFile.setVisibility(View.GONE);
                                txtFileWarning.setVisibility(View.VISIBLE);
                                txtFileWarning.setText(getString(R.string.duplicateWalletAddress));
                            }
                        } else {
                            layoutFile.setVisibility(View.GONE);
                            txtFileWarning.setVisibility(View.VISIBLE);
                            txtFileWarning.setText(getString(R.string.invalidKeyStoreFile));
                        }
                    } else if (keyStore.isJsonArray()) {
                        isBundle = true;
                        mKSBundle = validateKSBundle(keyStore.getAsJsonArray());

                        if (mKSBundle == null || mKSBundle.size() == 0) {
                            layoutFile.setVisibility(View.GONE);
                            txtFileWarning.setVisibility(View.VISIBLE);
                            txtFileWarning.setText(getString(R.string.invalidKeyStoreFile));
                        } else {
                            txtFileWarning.setVisibility(View.GONE);
                            layoutFile.setVisibility(View.VISIBLE);
                            txtFileName.setText(getFileName(data.getData()));
                        }
                    } else {

                    }

                    hidePwdError();
                } catch (Exception e) {
                    layoutFile.setVisibility(View.GONE);
                    txtFileWarning.setVisibility(View.VISIBLE);
                    txtFileWarning.setText(getString(R.string.invalidKeyStoreFile));

                    hidePwdError();
                }
            }
        }
    }

    private void showPwdError(String errMsg) {
        linePwd.setBackgroundColor(getResources().getColor(R.color.colorWarning));
        txtPwdWarning.setVisibility(View.VISIBLE);
        txtPwdWarning.setText(errMsg);
        btnNext.setEnabled(false);
    }

    private void hidePwdError() {
        if (editPwd.hasFocus())
            linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
        else
            linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));

        txtPwdWarning.setVisibility(View.INVISIBLE);
    }

    private boolean checkAddress(String address) {
        for (Wallet info : ICONexApp.mWallets) {
            if (info.getAddress().equals(address))
                return false;
        }

        return true;
    }

    private boolean validatePassword(String pwd, JsonObject keyStore) {
        String address;
        try {
            address = keyStore.get("address").getAsString();
        } catch (Exception e) {
            return false;
        }

        JsonObject crypto;
        String coinType;

        if (keyStore.has("coinType")) {
            coinType = Constants.KS_COINTYPE_ICX;
        } else {
            coinType = Constants.KS_COINTYPE_ETH;
        }

        if (keyStore.has("crypto")) {
            crypto = keyStore.get("crypto").getAsJsonObject();
        } else {
            crypto = keyStore.get("Crypto").getAsJsonObject();
        }

        byte[] privKey = null;
        try {
            privKey = KeyStoreUtils.decryptPrivateKey(pwd, address, crypto, coinType);
        } catch (Exception e) {
            return false;
        }

        if (privKey == null)
            return false;

        return true;
    }

    private List<Wallet> validateBundlePassword(String pwd, List<Wallet> wallets) {
        List<Wallet> tempWallets = new ArrayList<>();
        tempWallets.addAll(wallets);

        for (int i = 0; i < wallets.size(); i++) {
            Wallet wallet = wallets.get(i);
            try {
                JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);
                boolean result = validatePassword(pwd, keyStore);
                if (!result)
                    tempWallets.remove(wallet);
            } catch (Exception e) {
                tempWallets.remove(wallet);
            }
        }

        return tempWallets;
    }

    private boolean validateKeyStore(JsonObject keyStore) {
        Exception e;

        try {
            if (!keyStore.has("address") && !keyStore.has("version")
                    && (!keyStore.has("crypto") || !keyStore.has("Crypto"))) {
                e = new Exception("Invalid Keystore : Has no properties");
                throw e;
            }

            if (keyStore.get("version") == null || keyStore.get("version").isJsonNull()) {
                e = new Exception("Invalid Keystore : version");
                throw e;
            }

            if (keyStore.get("address") == null || keyStore.get("address").isJsonNull()
                    || keyStore.get("address").getAsString().isEmpty()) {
                e = new Exception("Invalid Keystore : address");
                throw e;
            }

            JsonObject crypto = null;
            if (keyStore.has("crypto")) {
                if (keyStore.get("crypto") == null || keyStore.get("crypto").isJsonNull()) {
                    e = new Exception("Invalid Keystore : crypto");
                    throw e;
                } else {
                    crypto = keyStore.get("crypto").getAsJsonObject();
                }
            } else if (keyStore.has("Crypto")) {
                if (keyStore.get("Crypto") == null || keyStore.get("Crypto").isJsonNull()) {
                    e = new Exception("Invalid Keystore : Crypto");
                    throw e;
                } else {
                    crypto = keyStore.get("Crypto").getAsJsonObject();
                }
            }

            if (crypto.get("ciphertext") == null || crypto.get("ciphertext").isJsonNull()
                    || crypto.get("ciphertext").getAsString().isEmpty()) {
                e = new Exception("Invalid Keystore : ciphertext");
                throw e;
            }

            if (crypto.get("cipherparams") == null || crypto.get("cipherparams").isJsonNull()) {
                e = new Exception("Invalid Keystore : cipherparams");
                throw e;
            } else {
                JsonObject cipherparams = crypto.get("cipherparams").getAsJsonObject();
                if (cipherparams.get("iv") == null || cipherparams.get("iv").isJsonNull()
                        || cipherparams.get("iv").getAsString().isEmpty()) {
                    e = new Exception("Invalid Keystore : iv");
                    throw e;
                }
            }

            if (crypto.get("cipher") == null || crypto.get("cipher").isJsonNull()
                    || crypto.get("cipher").getAsString().isEmpty()) {
                e = new Exception("Invalid Keystore : cipher");
                throw e;
            }

            if (crypto.get("kdf") == null || crypto.get("kdf").isJsonNull()
                    || crypto.get("kdf").getAsString().isEmpty()) {
                e = new Exception("Invalid Keystore : kdf");
                throw e;
            }

            if (crypto.get("kdfparams") == null || crypto.get("kdfparams").isJsonNull()) {
                e = new Exception("Invalid Keystore : kdfparams");
                throw e;
            } else {
                JsonObject kdfparams = crypto.get("kdfparams").getAsJsonObject();
                String kdf = crypto.get("kdf").getAsString();

                if (kdfparams.get("dklen") == null || kdfparams.get("dklen").isJsonNull()) {
                    e = new Exception("Invalid Keystore : dklen");
                    throw e;
                }

                if (kdfparams.get("salt") == null || kdfparams.get("salt").isJsonNull()
                        || kdfparams.get("salt").getAsString().isEmpty()) {
                    e = new Exception("Invalid Keystore : salt");
                    throw e;
                }

                if (kdf.equals(Constants.KDF_PBKDF2)) {

                    if (kdfparams.get("c") == null || kdfparams.get("c").isJsonNull()) {
                        e = new Exception("Invalid Keystore : c");
                        throw e;
                    }

                    if (kdfparams.get("prf") == null || kdfparams.get("prf").isJsonNull()
                            || kdfparams.get("prf").getAsString().isEmpty()) {
                        e = new Exception("Invalid Keystore : prf");
                        throw e;
                    }
                } else if (kdf.equals(Constants.KDF_SCRYPT)) {

                    if (kdfparams.get("n") == null || kdfparams.get("n").isJsonNull()) {
                        e = new Exception("Invalid Keystore : n");
                        throw e;
                    }

                    if (kdfparams.get("r") == null || kdfparams.get("r").isJsonNull()) {
                        e = new Exception("Invalid Keystore : r");
                        throw e;
                    }

                    if (kdfparams.get("p") == null || kdfparams.get("p").isJsonNull()) {
                        e = new Exception("Invalid Keystore : p");
                        throw e;
                    }
                }
            }

            if (crypto.get("mac") == null || crypto.get("mac").isJsonNull()
                    || crypto.get("mac").getAsString().isEmpty()) {
                e = new Exception("Invalid Keystore : mac");
                throw e;
            }
        } catch (Exception exception) {
            return false;
        }

        return true;
    }

    private List<Wallet> validateKSBundle(JsonArray bundle) {
        List<Wallet> wallets = new ArrayList<>();

        for (JsonElement element : bundle) {
            JsonObject eleObj = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> item : eleObj.entrySet()) {
                String address = item.getKey();

                if (address == null || address.isEmpty())
                    continue;

                try {
                    Wallet info = new Wallet();
                    JsonObject bundleInfo = item.getValue().getAsJsonObject();

                    String name = bundleInfo.get("name").getAsString();
                    String type = bundleInfo.get("type").getAsString();
                    String strPriv = bundleInfo.get("priv").getAsString();
                    JsonObject priv = new Gson().fromJson(strPriv, JsonObject.class);
                    JsonArray tokens = bundleInfo.get("tokens").getAsJsonArray();

                    if (name == null || name.isEmpty())
                        throw new Exception("Name is null");

                    if (type == null || type.isEmpty())
                        throw new Exception("type is null");

                    if (priv == null || priv.isJsonNull())
                        throw new Exception("Invalid priv");

                    if (tokens == null || tokens.isJsonNull())
                        throw new Exception("Invalid tokens");

                    boolean result = validateKeyStore(priv);
                    if (!result)
                        throw new Exception("Invalid Keystore");

                    info.setCoinType(type.toUpperCase());
                    info.setAlias(name);
                    info.setAddress(priv.get("address").getAsString());
                    info.setKeyStore(priv.toString());

                    List<WalletEntry> entries = new ArrayList<>();
                    WalletEntry entry = new WalletEntry();
                    entry.setType(MyConstants.TYPE_COIN);
                    entry.setAddress(priv.get("address").getAsString());

                    if (type.equals(Constants.KS_COINTYPE_ICX.toLowerCase()))
                        entry.setName(MyConstants.NAME_ICX);
                    else
                        entry.setName(MyConstants.NAME_ETH);

                    entry.setSymbol(type.toUpperCase());

                    entries.add(entry);

                    for (JsonElement tEle : tokens) {
                        try {
                            JsonObject token = tEle.getAsJsonObject();
                            boolean tokenResult = validateToken(token);
                            if (!tokenResult)
                                throw new Exception("Invalid token");

                            entry = new WalletEntry();
                            entry.setType(MyConstants.TYPE_TOKEN);
                            entry.setAddress(priv.get("address").getAsString());
                            entry.setContractAddress(token.get("address").getAsString());
                            entry.setDefaultDec(token.get("defaultDecimals").getAsInt());
                            entry.setUserDec(token.get("decimals").getAsInt());
                            entry.setUserName(token.get("name").getAsString());
                            entry.setName(token.get("defaultName").getAsString());
                            entry.setSymbol(token.get("defaultSymbol").getAsString());
                            entry.setUserSymbol(token.get("symbol").getAsString());
                            entry.setCreatedAt(token.get("createdAt").getAsString());

                            entries.add(entry);
                        } catch (Exception tokenException) {
                        }
                    }

                    info.setWalletEntries(entries);
                    info.setCreatedAt(bundleInfo.get("createdAt").getAsString());
                    wallets.add(info);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return wallets;
    }

    private boolean validateToken(JsonObject token) {
        try {
            if (!token.has("address") || !token.has("decimals") || !token.has("defaultDecimals")
                    || !token.has("defaultName") || !token.has("defaultSymbol") || !token.has("name")
                    || !token.has("symbol"))
                throw new Exception("Invalid Token");

            if (token.get("address") == null || token.get("address").isJsonNull()
                    || token.get("address").getAsString().isEmpty())
                throw new Exception("Invalid Token : Address");

            if (token.get("decimals") == null || token.get("decimals").isJsonNull())
                throw new Exception("Invalid Token : Decimals");

            if (token.get("defaultDecimals") == null || token.get("defaultDecimals").isJsonNull())
                throw new Exception("Invalid Token : Default decimals");

            if (token.get("defaultName") == null || token.get("defaultName").isJsonNull()
                    || token.get("defaultName").getAsString().isEmpty())
                throw new Exception("Invalid Token : Default Name");

            if (token.get("defaultSymbol") == null || token.get("defaultSymbol").isJsonNull()
                    || token.get("defaultSymbol").getAsString().isEmpty())
                throw new Exception("Invalid Token : Default Symbol");

            if (token.get("name") == null || token.get("name").isJsonNull()
                    || token.get("name").getAsString().isEmpty())
                throw new Exception("Invalid Token : Name");

            if (token.get("symbol") == null || token.get("symbol").isJsonNull()
                    || token.get("symbol").getAsString().isEmpty())
                throw new Exception("Invalid Token : Symbol");

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String readFile(Uri currentUri) {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(currentUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            inputStream.close();
            reader.close();

            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    private String getFileName(Uri currentUri) {
        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        String fileName = null;
        Cursor cursor = getActivity().getContentResolver()
                .query(currentUri, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            try {
                fileName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                return fileName;
            } finally {
                cursor.close();
            }
        } else {
            String strUri = currentUri.toString();
            int index = strUri.lastIndexOf("/");
            fileName = strUri.substring(index + 1, strUri.length());

            return fileName;
        }
    }
}
