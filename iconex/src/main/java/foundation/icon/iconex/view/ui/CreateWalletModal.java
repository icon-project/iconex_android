package foundation.icon.iconex.view.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.create.CreateWalletStep1Fragment;

public class CreateWalletModal extends BottomSheetDialogFragment implements CreateWalletStep1Fragment.OnStep1Listener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.create_wallet, container, false);

        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, CreateWalletStep1Fragment.newInstance())
                .commit();
        return v;
    }

    private void initView() {

    }

    @Override
    public void onStep1Done(@NonNull String coinType) {

    }
}
