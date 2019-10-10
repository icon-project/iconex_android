package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.view.ui.create.CreateWalletStep1Fragment;
import foundation.icon.iconex.view.ui.create.CreateWalletStep2Fragment;
import foundation.icon.iconex.view.ui.create.CreateWalletStep3Fragment;
import foundation.icon.iconex.view.ui.create.CreateWalletStep4Fragment;
import foundation.icon.iconex.view.ui.wallet.ViewWalletInfoActivity;
import foundation.icon.iconex.wallet.Wallet;

public class CreateWalletActivity extends FragmentActivity implements CreateWalletStep1Fragment.OnStep1Listener,
        CreateWalletStep2Fragment.OnStep2Listener, CreateWalletStep3Fragment.OnStep3Listener,
        CreateWalletStep4Fragment.OnStep4Listener {
    private static final String TAG = CreateWalletActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_wallet);

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(CreateWalletActivity.this);
                dialog.setMessage(getString(R.string.cancelCreateWallet));
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        finish();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.show();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, CreateWalletStep1Fragment.newInstance())
                .commitNow();
    }

    @Override
    public void onStep1Done() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, CreateWalletStep2Fragment.newInstance())
                .addToBackStack("step2")
                .commit();
    }

    @Override
    public void onStep2Done() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, CreateWalletStep3Fragment.newInstance())
                .addToBackStack("step3")
                .commit();
    }

    @Override
    public void onStep2Back() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onStep3Next() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, CreateWalletStep4Fragment.newInstance())
                .addToBackStack("step4")
                .commit();
    }

    @Override
    public void onStep3Back() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onStep4Back() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void showWalletInfo(Wallet wallet, String privateKey) {
        startActivity(new Intent(this, ViewWalletInfoActivity.class)
                .putExtra("wallet", (Serializable) wallet)
                .putExtra("privateKey", privateKey));
    }

    @Override
    public void onBackPressed() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(CreateWalletActivity.this);
        dialog.setMessage(getString(R.string.cancelCreateWallet));
        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
            @Override
            public void onOk() {
                finish();
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.show();
    }
}
