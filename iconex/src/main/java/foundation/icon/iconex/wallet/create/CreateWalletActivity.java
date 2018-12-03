package foundation.icon.iconex.wallet.create;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.menu.ViewWalletInfoActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.util.KeyStoreIO;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.main.MainActivity;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class CreateWalletActivity extends AppCompatActivity implements CreateWalletStep1Fragment.OnStep1Listener,
        CreateWalletStep2Fragment.OnStep2Listener, CreateWalletStep3Fragment.OnStep3Listener,
        CreateWalletStep4Fragment.OnStep4Listener, KeyEvent.Callback {

    private static final String TAG = CreateWalletActivity.class.getSimpleName();

    private final int STORAGE_PERMISSION_REQUEST = 10001;

    private ViewPager viewPager;
    private CreateWalletViewPagerAdapter pagerAdapter;

    private String coinType;
    private String walletName, pwd;
    private Wallet wallet;
    private String privKey;

    private boolean isDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleCreateWallet));
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == 0) {
                    finish();
                } else {
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
        });

        viewPager = findViewById(R.id.step_view_pager);
        pagerAdapter = new CreateWalletViewPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                RelativeLayout container = findViewById(R.id.layout_step);
                LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View inflated = null;
                switch (position) {
                    case 0:
                        inflated = layoutInflater.inflate(R.layout.layout_create_wallet_step1, null);
                        break;

                    case 1:
                        inflated = layoutInflater.inflate(R.layout.layout_create_wallet_step2, null);
                        break;

                    case 2:
                        inflated = layoutInflater.inflate(R.layout.layout_create_wallet_step3, null);
                        break;

                    case 3:
                        inflated = layoutInflater.inflate(R.layout.layout_create_wallet_step4, null);
                        break;
                }

                container.removeAllViews();
                container.addView(inflated);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        wallet = new Wallet();
    }

    @Override
    public void onStep1Done(String coinType) {
        this.coinType = coinType;
        wallet.setCoinType(coinType);
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        pagerAdapter.clearEdit();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onStep2Done(String name, String pwd) {
        createKeyStore = new CreateKeyStore();
        createKeyStore.execute(name, pwd);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    @Override
    public void onStep3Next() {

        if (isDownloaded) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        } else {
            Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
            dialog.setMessage(getString(R.string.noBackupKeyStoreFileConfirm));
            dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                @Override
                public void onOk() {
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                }

                @Override
                public void onCancel() {

                }
            });
            dialog.show();
        }
    }

    @Override
    public void showWalletInfo(String privateKey) {
        String coinName;
        String address;
        if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
            coinName = MyConstants.NAME_ICX;
            address = wallet.getAddress();
        } else {
            coinName = MyConstants.NAME_ETH;
            address = "0x" + wallet.getAddress();
        }


        startActivity(new Intent(this, ViewWalletInfoActivity.class)
                .putExtra("alias", wallet.getAlias())
                .putExtra("coinName", coinName)
                .putExtra("address", address)
                .putExtra("privateKey", privateKey)
                .putExtra("date", wallet.getCreatedAt()));
    }

    @Override
    public void onStep4Back() {
        // Do nothing.
    }

    @Override
    public void onStep4Next() {
        // Do nothing.
    }

    @Override
    public void onStep4Done() {

        try {
            RealmUtil.addWallet(wallet);
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onStep3Back() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
    }

    @Override
    public void onStep2Back() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
        if (createKeyStore != null) {
            createKeyStore.cancel(true);
        }

        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
    }

    @Override
    public void onShowInputMode() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewPager.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.appbar);
        viewPager.setLayoutParams(layoutParams);
    }

    @Override
    public void onHideInputMode() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) viewPager.getLayoutParams();
        layoutParams.removeRule(RelativeLayout.BELOW);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.layout_step);
        viewPager.setLayoutParams(layoutParams);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Basic2ButtonDialog dialog = new Basic2ButtonDialog(CreateWalletActivity.this);
                    dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm));
                    dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                        @Override
                        public void onOk() {
                            isDownloaded = backupKeyStoreFile();
                            if (isDownloaded) {
                                BasicDialog dialog = new BasicDialog(CreateWalletActivity.this);
                                dialog.setMessage(String.format(getString(R.string.keyStoreDownloadAccomplished), KeyStoreIO.DIR_PATH));
                                dialog.show();
                            }
                        }

                        @Override
                        public void onCancel() {

                        }
                    });
                    dialog.show();

                } else {
                    BasicDialog dialog = new BasicDialog(CreateWalletActivity.this);
                    dialog.setMessage(getString(R.string.permissionStorageDenied));
                    dialog.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean backupKeyStoreFile() {
        try {
            JsonObject keyStore = new Gson().fromJson(wallet.getKeyStore(), JsonObject.class);
            KeyStoreIO.exportKeyStore(keyStore, coinType);
        } catch (Exception e) {
            e.printStackTrace();
            isDownloaded = false;
            return isDownloaded;
        }

        isDownloaded = true;
        return isDownloaded;
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0) {
            finish();
        } else {
            Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
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
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        }
    }

    private CreateKeyStore createKeyStore = null;

    private class CreateKeyStore extends AsyncTask<String, Void, String[]> {
        @Override
        protected void onPostExecute(String[] keyStoreInfo) {
            super.onPostExecute(keyStoreInfo);

            wallet.setAddress(keyStoreInfo[0]);
            privKey = keyStoreInfo[1];
            wallet.setKeyStore(keyStoreInfo[2]);

            List<WalletEntry> entries = new ArrayList<>();
            WalletEntry coin = new WalletEntry();
            coin.setType(MyConstants.TYPE_COIN);
            coin.setAddress(keyStoreInfo[0]);

            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                coin.setName(MyConstants.NAME_ICX);
                coin.setSymbol(Constants.KS_COINTYPE_ICX);
            } else {
                coin.setName(MyConstants.NAME_ETH);
                coin.setSymbol(Constants.KS_COINTYPE_ETH);
            }

            entries.add(coin);

            wallet.setWalletEntries(entries);
            wallet.setCreatedAt(Long.toString(System.currentTimeMillis()));

            pagerAdapter.setKeyStore(wallet.getKeyStore());
            pagerAdapter.setAddress(wallet.getAddress());
            pagerAdapter.setPrivKey(privKey);

            pagerAdapter.clearEdit();
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);

            // TODO: 2018. 3. 27. Create wallet failed.
        }

        @Override
        protected String[] doInBackground(String... params) {
            String name;
            String pwd;

            name = params[0];
            pwd = params[1];

            wallet.setAlias(name);

            String[] keyStoreInfo;
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                keyStoreInfo = KeyStoreUtils.generateICXKeystore(pwd);
            } else {
                keyStoreInfo = KeyStoreUtils.generateEtherKeystore(pwd);
            }
            return keyStoreInfo;
        }
    }
}
