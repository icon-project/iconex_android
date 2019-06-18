package foundation.icon.connect;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import foundation.icon.iconex.R;

public class SelectWalletFragment extends Fragment {
    private static final String TAG = SelectWalletFragment.class.getSimpleName();

    private RecyclerView walletList;
    private SelectWalletAdapter adapter;

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
    }

    private SelectWalletListener mListener;

    public interface SelectWalletListener {
        void onSelectClose();
    }
}
