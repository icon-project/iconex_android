package foundation.icon.iconex.view.ui.create;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import foundation.icon.iconex.R;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.wallet.main.MainActivity;
import foundation.icon.iconex.widgets.TTextInputLayout;

public class CreateWalletStep4Fragment extends Fragment implements View.OnClickListener {
    private static final String TAG = CreateWalletStep4Fragment.class.getSimpleName();

    public static CreateWalletStep4Fragment newInstance() {
        return new CreateWalletStep4Fragment();
    }

    private OnStep4Listener mListener;
    private CreateWalletViewModel vm;

    private TTextInputLayout inputPrivateKey;
    private String address, privateKey;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(getActivity()).get(CreateWalletViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_create_wallet_step4, container, false);
        initView(v);
        setData();

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnStep4Listener) {
            mListener = (OnStep4Listener) context;
        } else {
            throw new RuntimeException(context + " must implements OnStep4Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    private void initView(View v) {
        Button btnCopy = v.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(this);

        Button btnInfo = v.findViewById(R.id.btn_view_info);
        btnInfo.setOnClickListener(this);

        Button btnComplete = v.findViewById(R.id.btn_complete);
        btnComplete.setOnClickListener(this);

        Button btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        inputPrivateKey = v.findViewById(R.id.input_private_key);
    }

    private void setData() {
        address = vm.getWallet().getValue().getAddress();
        privateKey = vm.getPrivateKey().getValue();

        inputPrivateKey.setText(privateKey);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_copy:
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("priv", vm.getPrivateKey().getValue());
                clipboard.setPrimaryClip(data);
                Toast.makeText(getActivity(), getString(R.string.msgCopyPrivateKey), Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_view_info:
                mListener.showWalletInfo();
                break;

            case R.id.btn_complete:
                saveWallet();
                break;

            case R.id.btn_back:
                mListener.onStep4Back();
                break;
        }
    }

    private void saveWallet() {
        try {
            RealmUtil.addWallet(vm.getWallet().getValue());
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(new Intent(getActivity(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    public interface OnStep4Listener {
        void onStep4Back();

        void showWalletInfo();
    }
}
