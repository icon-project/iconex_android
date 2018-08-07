package foundation.icon.iconex.wallet.create;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import foundation.icon.iconex.R;

public class CreateWalletStep4Fragment extends Fragment {

    private static final String TAG = CreateWalletStep4Fragment.class.getSimpleName();

    private OnStep4Listener mListener;
    private String address = null;
    private String privKey = null;

    private TextView txtPrivateKey;
    private Button btnVisibility;
    private Button btnDone;
    private Button btnCopy, btnInfo;

    private ViewGroup layoutTwoButton;

    private boolean isTwoBtn = false;

    public CreateWalletStep4Fragment() {
        // Required empty public constructor
    }

    public static CreateWalletStep4Fragment newInstance(String address, String privKey, boolean isTwoBtn) {
        CreateWalletStep4Fragment fragment = new CreateWalletStep4Fragment();
        Bundle args = new Bundle();
        args.putString("address", address);
        args.putString("privateKey", privKey);
        args.putBoolean("isTwoButton", isTwoBtn);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            address = getArguments().getString("address");
            privKey = getArguments().getString("privateKey");
            isTwoBtn = getArguments().getBoolean("isTwoButton");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_wallet_step4, container, false);

        txtPrivateKey = v.findViewById(R.id.txt_private_key);
        txtPrivateKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        btnVisibility = v.findViewById(R.id.btn_visibility);
        btnVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnVisibility.isSelected()) {
                    btnVisibility.setSelected(false);
                    txtPrivateKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                } else {
                    btnVisibility.setSelected(true);
                    txtPrivateKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                }
            }
        });

        btnCopy = v.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("priv", privKey);
                clipboard.setPrimaryClip(data);
                Toast.makeText(getActivity(), getString(R.string.msgCopyPrivateKey), Toast.LENGTH_SHORT).show();
            }
        });

        btnInfo = v.findViewById(R.id.btn_view_info);
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.showWalletInfo(privKey);
            }
        });


        btnDone = v.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onStep4Done();
            }
        });

        layoutTwoButton = v.findViewById(R.id.layout_two);
        Button btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onStep4Back();
            }
        });
        Button btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onStep4Next();
            }
        });

        if (isTwoBtn) {
            btnDone.setVisibility(View.GONE);
            layoutTwoButton.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        btnVisibility.setSelected(false);
        txtPrivateKey.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        txtPrivateKey.setText(privKey);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStep4Listener) {
            mListener = (OnStep4Listener) context;
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

    public interface OnStep4Listener {
        void onStep4Done();

        void onStep4Back();

        void onStep4Next();

        void showWalletInfo(String privateKey);
    }
}
