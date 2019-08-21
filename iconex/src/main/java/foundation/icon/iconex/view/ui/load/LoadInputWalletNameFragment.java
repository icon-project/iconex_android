package foundation.icon.iconex.view.ui.load;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import foundation.icon.iconex.R;
import foundation.icon.iconex.widgets.TTextInputLayout;

public class LoadInputWalletNameFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LoadInputWalletNameFragment.class.getSimpleName();

    private TTextInputLayout inputAlias;
    private Button btnDone, btnBack;

    private final int OK = 0;
    private final int ALIAS_DUP = 1;
    private final int ALIAS_EMPTY = 2;

    private OnInputWalletAliasListener mListener;

    public LoadInputWalletNameFragment() {
        // Required empty public constructor
    }

    public static LoadInputWalletNameFragment newInstance() {
        LoadInputWalletNameFragment fragment = new LoadInputWalletNameFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_input_wallet_name, container, false);

        inputAlias = v.findViewById(R.id.input_alias);

        btnDone = v.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_done:
                mListener.onDoneLoadWalletByKeyStore();
                break;

            case R.id.btn_back:
                mListener.onNameBack();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInputWalletAliasListener) {
            mListener = (OnInputWalletAliasListener) context;
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

    public interface OnInputWalletAliasListener {
        void onDoneLoadWalletByKeyStore();

        void onNameBack();
    }
}
