package foundation.icon.iconex.wallet.create;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.util.KeyStoreIO;

public class CreateWalletStep3Fragment extends Fragment implements View.OnClickListener {

    private static final String TAG = CreateWalletStep3Fragment.class.getSimpleName();

    private OnStep3Listener mListener;
    private String keyStore = null;

    private final int STORAGE_PERMISSION_REQUEST = 10001;

    private Button btnBackUp;
    private Button btnPrev, btnNext;

    private boolean isAccomplished = false;

    public CreateWalletStep3Fragment() {
        // Required empty public constructor
    }

    public static CreateWalletStep3Fragment newInstance(String keyStore) {
        CreateWalletStep3Fragment fragment = new CreateWalletStep3Fragment();
        Bundle args = new Bundle();
        args.putString("KeyStore", keyStore);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            keyStore = getArguments().getString("KeyStore");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_wallet_step3, container, false);

        btnPrev = v.findViewById(R.id.btn_prev);
        btnPrev.setOnClickListener(this);
        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);

        btnBackUp = v.findViewById(R.id.btn_back_up);
        btnBackUp.setOnClickListener(this);
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStep3Listener) {
            mListener = (OnStep3Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStep2Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());

        switch (v.getId()) {
            case R.id.btn_prev:
                mListener.onStep3Back();
                break;

            case R.id.btn_next:
                mListener.onStep3Next();
                break;

            case R.id.btn_back_up:
                checkPermission();
                break;
        }
    }

    private void checkPermission() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm));
            dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                @Override
                public void onOk() {
                    if (getActivity() instanceof CreateWalletActivity)
                        isAccomplished = ((CreateWalletActivity) getActivity()).backupKeyStoreFile();

                    if (isAccomplished) {
                        BasicDialog dialog = new BasicDialog(getActivity());
                        dialog.setMessage(String.format(getString(R.string.keyStoreDownloadAccomplished), KeyStoreIO.DIR_PATH));
                        dialog.show();
                    }
                }

                @Override
                public void onCancel() {

                }
            });
            dialog.show();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_REQUEST);
        }
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public interface OnStep3Listener {
        void onStep3Next();

        void onStep3Back();
    }
}
