package foundation.icon.iconex.view.ui.load;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.TTextInputLayout;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

import static android.view.View.GONE;
import static loopchain.icon.wallet.service.crypto.KeyStoreUtils.validateBundlePassword;
import static loopchain.icon.wallet.service.crypto.KeyStoreUtils.validateKSBundle;
import static loopchain.icon.wallet.service.crypto.KeyStoreUtils.validateKeyStore;

public class LoadSelectKeyStoreFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LoadSelectKeyStoreFragment.class.getSimpleName();

    public static final int RC_READ_FILE = 8001;

    private OnSelectKeyStoreCallback mListener;
    private LoadViewModel vm;

    private TTextInputLayout inputFileName, inputPwd;
    private ImageButton btnUpload;
    private Button btnNext;
    private ProgressBar progress;

    private boolean isBundle = false;
    private JsonObject mKeyStore;
    private List<Wallet> mKSBundle;

    private Disposable disposable;

    public static boolean isSelect = false;

    public LoadSelectKeyStoreFragment() {
        // Required empty public constructor
    }

    public static LoadSelectKeyStoreFragment newInstance() {
        return new LoadSelectKeyStoreFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(LoadViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_wallet_select_key_store, container, false);
        initView(v);

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
        if (!disposable.isDisposed())
            disposable.dispose();

        mListener = null;
    }

    private void initView(View v) {
        inputFileName = v.findViewById(R.id.input_file);

        inputPwd = v.findViewById(R.id.input_password);
        inputPwd.setOnKeyPreImeListener(new TTextInputLayout.OnKeyPreIme() {
            @Override
            public void onDone() {
                validatePassword();
            }
        });

        inputPwd.setOnEditorActionListener(new TTextInputLayout.OnEditorAction() {
            @Override
            public void onDone() {
                validatePassword();
            }
        });

        btnUpload = v.findViewById(R.id.btn_select_keystore);
        btnUpload.setOnClickListener(this);

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);

        v.findViewById(R.id.btn_back).setOnClickListener(this);

        progress = v.findViewById(R.id.progress);
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

            case R.id.btn_next:
                if (isBundle) {
                    vm.setKeystore(mKeyStore);
                    mListener.onBundleFile();
                } else {
                    vm.setBundle(mKSBundle);
                    mListener.onKeyStoreFile();
                }
                break;

            case R.id.btn_back:
                mListener.onKeyStoreBack();
                break;
        }
    }

    private boolean checkAddress(String address) {
        for (Wallet info : ICONexApp.wallets) {
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

    private void validatePassword() {
        if (!isSelect) {
            inputFileName.setError(true, "Select File");
            return;
        }

        btnNext.setEnabled(false);
        progress.setVisibility(View.VISIBLE);

        disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                if (isBundle)
                    emitter.onNext(validateBundlePassword(inputPwd.getText(), mKSBundle));
                else
                    emitter.onNext(KeyStoreUtils.validatePassword(inputPwd.getText(), mKeyStore));

                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        if (o instanceof List) {
                            List<Wallet> bundle = (List<Wallet>) o;
                            if (bundle.size() > 0) {
                                vm.setBundle(bundle);
                                inputPwd.setError(false, null);
                                btnNext.setEnabled(true);
                            } else {
                                inputPwd.setError(true, getString(R.string.errPassword));
                                btnNext.setEnabled(false);
                            }
                        } else {
                            boolean result = (boolean) o;
                            if (result) {
                                vm.setKeystore(mKeyStore);
                                inputPwd.setError(false, null);
                                btnNext.setEnabled(true);
                            } else {
                                inputPwd.setError(true, getString(R.string.errPassword));
                                btnNext.setEnabled(false);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        inputPwd.setError(true, getString(R.string.errPassword));
                        btnNext.setEnabled(false);
                    }

                    @Override
                    public void onComplete() {
                        progress.setVisibility(GONE);
                    }
                });
    }

    public void clear(boolean inputEnabled) {
        inputFileName.setText("");
        inputPwd.setText("");
        inputPwd.setInputEnabled(inputEnabled);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_READ_FILE) {
            if (resultCode == Activity.RESULT_OK) {

                if (!inputPwd.getText().isEmpty())
                    inputPwd.setText("");

                try {
                    boolean result;
                    String fileName = getFileName(data.getData());
                    JsonElement keyStore = new Gson().fromJson(readFile(data.getData()), JsonElement.class);

                    if (keyStore.isJsonObject()) {
                        isBundle = false;
                        result = validateKeyStore(keyStore.getAsJsonObject());
                        if (result) {
                            if (checkAddress(keyStore.getAsJsonObject().get("address").getAsString())) {
                                inputFileName.setFile(fileName);
                                mKeyStore = keyStore.getAsJsonObject();
                                isSelect = true;
                            } else {
                                inputFileName.setFileError(fileName, getString(R.string.duplicateWalletAddress));
                                isSelect = false;
                            }
                        } else {
                            inputFileName.setFileError(fileName, getString(R.string.invalidKeyStoreFile));
                            isSelect = false;
                        }
                    } else if (keyStore.isJsonArray()) {
                        isBundle = true;
                        mKSBundle = validateKSBundle(keyStore.getAsJsonArray());

                        if (mKSBundle == null || mKSBundle.size() == 0) {
                            inputFileName.setFileError(fileName, getString(R.string.invalidKeyStoreFile));
                            isSelect = false;
                        } else {
                            inputFileName.setFile(fileName);
                            isSelect = true;
                        }
                    } else {
                        inputFileName.setFileError(fileName, getString(R.string.invalidKeyStoreFile));
                        isSelect = false;
                    }
                } catch (Exception e) {
                    inputFileName.setFileError("", getString(R.string.invalidKeyStoreFile));
                    isSelect = false;
                }
            }
        }
    }

    public interface OnSelectKeyStoreCallback {
        void onKeyStoreFile();

        void onBundleFile();

        void onKeyStoreBack();
    }
}
