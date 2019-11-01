package foundation.icon.iconex.view.ui.create;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Objects;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.util.KeyStoreIO;
import kotlin.jvm.functions.Function1;

import static foundation.icon.iconex.menu.bundle.ExportWalletBundleActivity.STORAGE_PERMISSION_REQUEST;

public class CreateWalletStep3Fragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CreateWalletStep3Fragment.class.getSimpleName();

    public static CreateWalletStep3Fragment newInstance() {
        return new CreateWalletStep3Fragment();
    }

    private OnStep3Listener mListener;
    private CreateWalletViewModel vm;
    private boolean isAccomplished = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(CreateWalletViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_create_wallet_step3, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnStep3Listener) {
            mListener = (OnStep3Listener) context;
        } else {
            throw new RuntimeException(context + " must implement OnStep3Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    private void initView(View v) {
        Button btnNext, btnBack, btnDownload;

        btnDownload = v.findViewById(R.id.btn_back_up);
        btnDownload.setOnClickListener(this);
        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back_up:
                checkPermission();
                break;

            case R.id.btn_next:
                if (isAccomplished)
                    mListener.onStep3Next();
                else {
                    MessageDialog dialog = new MessageDialog(getContext());
                    dialog.setSingleButton(false);
                    dialog.setConfirmButtonText(getString(R.string.yes));
                    dialog.setCancelButtonText(getString(R.string.no));
                    dialog.setMessage(getString(R.string.noBackupKeyStoreFileConfirm));
                    dialog.setOnConfirmClick(new Function1<View, Boolean>() {
                        @Override
                        public Boolean invoke(View view) {
                            mListener.onStep3Next();
                            return true;
                        }
                    });
                    dialog.show();
                }
                break;

            case R.id.btn_back:
                mListener.onStep3Back();
                break;
        }
    }

    private void checkPermission() {
        MessageDialog dialog = new MessageDialog(getContext());
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            dialog.setSingleButton(false);
            dialog.setConfirmButtonText(getString(R.string.yes));
            dialog.setCancelButtonText(getString(R.string.no));
            dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm));
            dialog.setOnConfirmClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    isAccomplished = backupKeyStoreFile();

                    if (isAccomplished) {
                        MessageDialog dialog = new MessageDialog(getActivity());
                        dialog.setSingleButton(true);
                        dialog.setMessage(String.format(getString(R.string.keyStoreDownloadAccomplished), KeyStoreIO.DIR_PATH));
                        dialog.show();
                    }
                    return true;
                }
            });
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean backupKeyStoreFile() {
        try {
            JsonObject keyStore = new Gson().fromJson(vm.getWallet().getValue().getKeyStore(),
                    JsonObject.class);
            KeyStoreIO.exportKeyStore(keyStore, vm.getCoin().getValue().getSymbol());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public interface OnStep3Listener {
        void onStep3Next();

        void onStep3Back();
    }
}
