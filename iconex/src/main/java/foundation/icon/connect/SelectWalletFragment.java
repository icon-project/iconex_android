package foundation.icon.connect;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.wallet.Wallet;
import loopchain.icon.wallet.core.Constants;

public class SelectWalletFragment extends Fragment implements View.OnClickListener,
        SelectWalletAdapter.OnWalletSelectListener {
    private static final String TAG = SelectWalletFragment.class.getSimpleName();

    private RecyclerView walletList;
    private SelectWalletAdapter adapter;
    private Button btnConfirm;

    private List<Wallet> mList;

    public SelectWalletFragment() {
        // Required empty public constructor
    }

    public static SelectWalletFragment newInstance() {
        return new SelectWalletFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_select_wallet, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SelectWalletListener) {
            mListener = (SelectWalletListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initView(View v) {
        ((TextView) v.findViewById(R.id.txt_title)).setText(getString(R.string.selectWallet));
        v.findViewById(R.id.btn_close).setOnClickListener(view -> mListener.onSelectClose());

        walletList = v.findViewById(R.id.list_wallets);

        ((TextView) v.findViewById(R.id.txt_title)).setText(getString(R.string.selectWallet));
        v.findViewById(R.id.btn_close).setOnClickListener(this);
        btnConfirm = v.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(this);

        walletList = v.findViewById(R.id.list_wallets);

        mList = makeWalletList();

        if (mList.size() > 0) {
            adapter = new SelectWalletAdapter(getContext(), mList, this);
            walletList.setAdapter(adapter);
        } else {

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(getContext());
                dialog.setMessage(getString(R.string.msgCancelBind));
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        mListener.onSelectClose();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.show();
                break;
            case R.id.btn_confirm:
                mListener.onSelected(adapter.getSelected());
                break;
        }
    }

    private List<Wallet> makeWalletList() {
        List<Wallet> list = new ArrayList<>();

        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
                list.add(wallet);
        }

        return list;
    }

    @Override
    public void onSelect() {
        btnConfirm.setEnabled(true);
    }

    private SelectWalletListener mListener;

    public interface SelectWalletListener {
        void onSelected(String address);

        void onSelectClose();

        void hasNoWallet();
    }
}
