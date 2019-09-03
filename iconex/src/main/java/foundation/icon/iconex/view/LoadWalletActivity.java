package foundation.icon.iconex.view;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.view.ui.load.LoadBundleFragment;
import foundation.icon.iconex.view.ui.load.LoadInputPrivateKeyFragment;
import foundation.icon.iconex.view.ui.load.LoadInputWalletAliasFragment;
import foundation.icon.iconex.view.ui.load.LoadInputWalletInfoFragment;
import foundation.icon.iconex.view.ui.load.LoadSelectKeyStoreFragment;
import foundation.icon.iconex.view.ui.load.LoadSelectMethodFragment;
import foundation.icon.iconex.view.ui.load.LoadViewModel;

public class LoadWalletActivity extends AppCompatActivity implements LoadSelectMethodFragment.OnSelectMethodListener,
        LoadSelectKeyStoreFragment.OnSelectKeyStoreCallback, LoadInputPrivateKeyFragment.OnLoadPrivateKeyListener,
        LoadInputWalletInfoFragment.OnInputWalletInfoListener, LoadInputWalletAliasFragment.OnInputWalletAliasListener,
        LoadBundleFragment.OnLoadBundleListener {

    private static final String TAG = LoadWalletActivity.class.getSimpleName();

    private LoadViewModel vm;

    private LoadSelectKeyStoreFragment fragmentSelectKeystore = LoadSelectKeyStoreFragment.newInstance();
    private LoadInputPrivateKeyFragment fragmentInputPrivateKey = LoadInputPrivateKeyFragment.newInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_wallet);

        vm = ViewModelProviders.of(this).get(LoadViewModel.class);

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleLoadWallet));
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(LoadWalletActivity.this);
                dialog.setMessage(getString(R.string.cancelLoadWallet));
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
                .replace(R.id.container, LoadSelectMethodFragment.newInstance())
                .commitNow();
    }

    @Override
    public void onSelect() {
        LoadViewModel.LoadMethod method = vm.getMethod().getValue();
        if (method == LoadViewModel.LoadMethod.KEYSTORE)
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragmentSelectKeystore)
                    .addToBackStack("keystore")
                    .commit();
        else
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragmentInputPrivateKey)
                    .addToBackStack("privateKey")
                    .commit();
    }

    @Override
    public void onKeyStoreFile() {
        fragmentSelectKeystore.clear(false);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, LoadInputWalletAliasFragment.newInstance())
                .addToBackStack("walletAlias")
                .commit();
    }

    @Override
    public void onBundleFile() {
        fragmentSelectKeystore.clear(false);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, LoadBundleFragment.newInstance())
                .addToBackStack("bundle")
                .commit();
    }

    @Override
    public void onKeyStoreBack() {
        fragmentSelectKeystore.clear(true);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onPrivateKeyNext() {
        fragmentInputPrivateKey.clear(false);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, LoadInputWalletInfoFragment.newInstance())
                .addToBackStack("info")
                .commit();
    }

    @Override
    public void onPrivateKeyBack() {
        fragmentInputPrivateKey.clear(true);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onInfoBack() {
        fragmentInputPrivateKey.clear(true);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onAliasBack() {
        fragmentSelectKeystore.clear(true);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBundleBack() {
        fragmentSelectKeystore.clear(true);
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onBackPressed() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(LoadWalletActivity.this);
        dialog.setMessage(getString(R.string.cancelLoadWallet));
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
