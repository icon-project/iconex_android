package foundation.icon.iconex.view.ui.auth;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.wallet.Wallet;

public class UserVerificationFragment extends Fragment implements WalletListAdapter.OnWalletClickListener {

    private static final String TAG = UserVerificationFragment.class.getSimpleName();

    private TextView txtWalletCount;

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

        txtWalletCount = v.findViewById(R.id.wallet_count);

        txtWalletCount.setText(String.format(getString(R.string.totalWalletCount), ICONexApp.wallets.size()));

        Button btnClose = v.findViewById(R.id.btn_close);
        btnClose.setBackgroundResource(R.drawable.ic_arrow_right);
        btnClose.setRotation(180f);

        btnClose.setOnClickListener(new View.OnClickListener() {
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
                new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                    @Override
                    public void onPass(byte[] bytePrivateKey) {
                        mListener.onVerification();
                    }
                }).show();
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

    @Override
    public void onWalletClick(Wallet wallet) {

    }

    private OnVerificationListener mListener;

    public interface OnVerificationListener {
        void onVerification();

        void onVerificationBack();
    }
}
