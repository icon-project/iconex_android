package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.view.ui.create.CreateWalletStep1Fragment;
import foundation.icon.iconex.view.ui.create.CreateWalletStep2Fragment;
import foundation.icon.iconex.view.ui.create.CreateWalletStep3Fragment;
import foundation.icon.iconex.view.ui.create.CreateWalletStep4Fragment;
import foundation.icon.iconex.view.ui.wallet.ViewWalletInfoActivity;
import foundation.icon.iconex.wallet.Wallet;
import kotlin.jvm.functions.Function1;

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
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    MessageDialog dialog = new MessageDialog(CreateWalletActivity.this);
                    dialog.setSingleButton(false);
                    dialog.setTitleText(getString(R.string.cancelCreateWallet));
                    dialog.setConfirmButtonText(getString(R.string.yes));
                    dialog.setCancelButtonText(getString(R.string.no));
                    dialog.setOnConfirmClick(new Function1<View, Boolean>() {
                        @Override
                        public Boolean invoke(View view) {
                            finish();
                            return true;
                        }
                    });
                    dialog.show();
                } else {
                    finish();
                }
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onStep2Done() {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, CreateWalletStep3Fragment.newInstance())
                .addToBackStack("step3")
                .commit();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
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
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            MessageDialog dialog = new MessageDialog(CreateWalletActivity.this);
            dialog.setSingleButton(false);
            dialog.setTitleText(getString(R.string.cancelCreateWallet));
            dialog.setConfirmButtonText(getString(R.string.yes));
            dialog.setCancelButtonText(getString(R.string.no));
            dialog.setOnConfirmClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    finish();
                    return true;
                }
            });
            dialog.show();
        } else {
            finish();
        }
    }
}
