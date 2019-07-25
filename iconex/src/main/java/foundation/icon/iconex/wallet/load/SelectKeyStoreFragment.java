package foundation.icon.iconex.wallet.load;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import androidx.fragment.app.Fragment;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.MyEditText;

import static android.view.View.GONE;
import static loopchain.icon.wallet.service.crypto.KeyStoreUtils.validateBundlePassword;
import static loopchain.icon.wallet.service.crypto.KeyStoreUtils.validateKSBundle;
import static loopchain.icon.wallet.service.crypto.KeyStoreUtils.validateKeyStore;
import static loopchain.icon.wallet.service.crypto.KeyStoreUtils.validatePassword;

public class SelectKeyStoreFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SelectKeyStoreFragment.class.getSimpleName();

    public static final int RC_READ_FILE = 8001;

    private OnSelectKeyStoreCallback mListener;

    private Button btnSelecFile, btnNext, btnBack;
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

    private ValidatePassword validate;

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
                    btnNext.setEnabled(false);
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
                validate = new ValidatePassword();
                validate.execute();
            }
        });
        editPwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                    validate = new ValidatePassword();
                    validate.execute();
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
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

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
                layoutFile.setVisibility(GONE);
                editPwd.setText("");
                txtPwdWarning.setVisibility(GONE);
                if (editPwd.hasFocus())
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editActivated));
                else
                    linePwd.setBackgroundColor(getResources().getColor(R.color.editNormal));
                btnNext.setEnabled(false);
                break;

            case R.id.btn_pwd_delete:
                editPwd.setText("");
                break;

            case R.id.btn_next:
                clear();
                if (isBundle)
                    mListener.onKeyStoreBundleSelected(mKSBundle);
                else
                    mListener.onKeyStoreSelected(mKeyStore);
                break;

            case R.id.btn_back:
                if (validate != null)
                    validate.cancel(true);

                clear();
                mListener.onKeyStoreBack();
                break;
        }
    }

    public interface OnSelectKeyStoreCallback {
        void onKeyStoreSelected(JsonObject keyStore);

        void onKeyStoreBundleSelected(List<Wallet> wallets);

        void onKeyStoreBack();
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
                                txtFileWarning.setVisibility(GONE);
                                layoutFile.setVisibility(View.VISIBLE);
                                String fileName = getFileName(data.getData());
                                txtFileName.setText(getFileName(data.getData()));

                                mKeyStore = keyStore.getAsJsonObject();
                            } else {
                                layoutFile.setVisibility(GONE);
                                txtFileWarning.setVisibility(View.VISIBLE);
                                txtFileWarning.setText(getString(R.string.duplicateWalletAddress));
                            }
                        } else {
                            layoutFile.setVisibility(GONE);
                            txtFileWarning.setVisibility(View.VISIBLE);
                            txtFileWarning.setText(getString(R.string.invalidKeyStoreFile));
                        }
                    } else if (keyStore.isJsonArray()) {
                        isBundle = true;
                        mKSBundle = validateKSBundle(keyStore.getAsJsonArray());

                        if (mKSBundle == null || mKSBundle.size() == 0) {
                            layoutFile.setVisibility(GONE);
                            txtFileWarning.setVisibility(View.VISIBLE);
                            txtFileWarning.setText(getString(R.string.invalidKeyStoreFile));
                        } else {
                            txtFileWarning.setVisibility(GONE);
                            layoutFile.setVisibility(View.VISIBLE);
                            txtFileName.setText(getFileName(data.getData()));
                        }
                    } else {

                    }

                    hidePwdError();
                } catch (Exception e) {
                    layoutFile.setVisibility(GONE);
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

    private void clear() {
        layoutFile.setVisibility(GONE);
        editPwd.setText("");
    }

    private class ValidatePassword extends AsyncTask<Void, RESULT, RESULT> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            btnNext.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected RESULT doInBackground(Void... voids) {

            if (layoutFile.getVisibility() == View.VISIBLE) {
                if (isBundle) {
                    List<Wallet> correctWallets = validateBundlePassword(editPwd.getText().toString(), mKSBundle);
                    if (correctWallets == null || correctWallets.size() == 0)
                        return RESULT.ERROR;
                    else
                        mKSBundle = correctWallets;

                } else {
                    if (!validatePassword(editPwd.getText().toString(), mKeyStore))
                        return RESULT.ERROR;
                }
            } else {
                if (!editPwd.getText().toString().isEmpty())
                    return RESULT.NO_FILE;
            }

            return RESULT.SUCCESS;
        }

        @Override
        protected void onPostExecute(RESULT result) {
            super.onPostExecute(result);

            progress.setVisibility(GONE);

            switch (result) {
                case SUCCESS:
                    hidePwdError();
                    if (editPwd.getText().toString().isEmpty())
                        btnNext.setEnabled(false);
                    else
                        btnNext.setEnabled(true);
                    break;

                case ERROR:
                    showPwdError(getString(R.string.errPassword));
                    break;

                case NO_FILE:
                    showPwdError(getString(R.string.errNoKeystoreFile));
                    break;
            }
        }
    }

    public enum RESULT {
        SUCCESS,
        ERROR,
        NO_FILE
    }
}
