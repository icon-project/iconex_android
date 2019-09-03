package foundation.icon.iconex.view.ui.auth;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.dialogs.EditTextDialog;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class UserVerificationFragment extends Fragment implements WalletListAdapter.OnWalletClickListener {

    private static final String TAG = UserVerificationFragment.class.getSimpleName();

    public UserVerificationFragment() {
        // Required empty public constructor
    }

    public static UserVerificationFragment newInstance() {
        UserVerificationFragment fragment = new UserVerificationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user_verification, container, false);

        ((TextView) v.findViewById(R.id.txt_title)).setText(getString(R.string.titleResetLockNum));
        v.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onVerificationBack();
            }
        });

        RecyclerView recyclerView = v.findViewById(R.id.recycler_wallets);
        WalletListAdapter adapter = new WalletListAdapter(getActivity());
        adapter.setOnWalletClickListener(new WalletListAdapter.OnWalletClickListener() {
            @Override
            public void onWalletClick(Wallet wallet) {
                mWallet = wallet;
                dialog = new EditTextDialog(getActivity(), getString(R.string.enterWalletPassword));
                dialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                dialog.setPasswordType(EditTextDialog.RESULT_PWD.TRANSFER);
                dialog.setOnPasswordCallback(onPasswordCallback);
                dialog.setHint(getString(R.string.hintWalletPassword));
                dialog.show();
            }
        });
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVerificationListener) {
            mListener = (OnVerificationListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnVerificationListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private Wallet mWallet;
    private EditTextDialog dialog;

    private EditTextDialog.OnPasswordCallback onPasswordCallback = new EditTextDialog.OnPasswordCallback() {
        @Override
        public void onConfirm(EditTextDialog.RESULT_PWD result, String text) {
            JsonObject keyStore = new Gson().fromJson(mWallet.getKeyStore(), JsonObject.class);
            byte[] bytePrivKey;
            try {
                JsonObject crypto;
                if (keyStore.has("crypto"))
                    crypto = keyStore.get("crypto").getAsJsonObject();
                else
                    crypto = keyStore.get("Crypto").getAsJsonObject();

                bytePrivKey = KeyStoreUtils.decryptPrivateKey(text, mWallet.getAddress(), crypto, mWallet.getCoinType());
                if (bytePrivKey != null) {
                    mListener.onVerification();

                    dialog.dismiss();
                } else {
                    dialog.setError(getString(R.string.errPassword));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onWalletClick(Wallet wallet) {

    }

    private OnVerificationListener mListener;

    public interface OnVerificationListener {
        void onVerification();

        void onVerificationBack();
    }
}
