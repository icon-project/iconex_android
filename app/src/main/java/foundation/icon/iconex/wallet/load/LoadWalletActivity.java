package foundation.icon.iconex.wallet.load;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.wallet.main.MainActivity;
import foundation.icon.iconex.widgets.NonSwipeViewPager;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.PKIUtils;

public class LoadWalletActivity extends AppCompatActivity implements LoadSelectMethodFragment.OnSelectMethodCallback,
        SelectKeyStoreFragment.OnSelectKeyStoreCallback, LoadInputWalletNameFragment.OnInputWalletNameCallback,
        LoadInputPrivateKeyFragment.OnLoadPrivateKeyListener, LoadInputWalletInfoFragment.OnInputWalletInfoListener {

    private static final String TAG = LoadWalletActivity.class.getSimpleName();

    private NonSwipeViewPager viewPager;
    private LoadWalletViewPagerAdapter viewPagerAdapter;

    private String mCoinType;
    private String mAlias;
    private JsonObject mKeyStore;
    private List<Wallet> mBundle;
    private String mPrivateKey;

    private ArrayList<BundleItem> bundleItems;
    private final int RC_BUNDLE_LIST = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_wallet);

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleLoadWallet));
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == 0) {
                    finish();
                } else {
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
        });

        viewPager = findViewById(R.id.load_view_pager);
        viewPager.setAdapter(new LoadWalletViewPagerAdapter(getSupportFragmentManager()));
    }

    private ArrayList<BundleItem> makeList() {
        ArrayList<BundleItem> list = new ArrayList<>();
        List<Wallet> temp = new ArrayList<>();
        temp.addAll(mBundle);

        for (int i = 0; i < mBundle.size(); i++) {
            Wallet wallet = mBundle.get(i);
            String alias = wallet.getAlias();
            boolean registered = checkRegisterAlias(alias);

            BundleItem item = new BundleItem();
            item.setId(new Random().nextInt(999999) + 100000);
            item.setCoinType(wallet.getCoinType());
            item.setAlias(wallet.getAlias());
            item.setAddress(wallet.getAddress());
            item.setRegistered(registered);
            item.setSymbol(wallet.getCoinType());

            list.add(item);

            if (registered)
                temp.remove(wallet);
        }

        mBundle = new ArrayList<>();
        mBundle.addAll(temp);

        return list;
    }

    private boolean checkRegisterAlias(String alias) {
        for (Wallet wallet : ICONexApp.mWallets) {
            if (alias.equals(wallet.getAlias()))
                return true;
        }

        return false;
    }

    private void addBundle() throws Exception {
        for (Wallet wallet : mBundle) {

            if (checkAddress(wallet.getAddress())) {
                RealmUtil.overwriteWallet(wallet.getAddress(), wallet);
            } else {
                RealmUtil.addWallet(wallet);
            }
        }

        RealmUtil.loadWallet();
    }

    private boolean checkAddress(String address) {
        for (Wallet wallet : ICONexApp.mWallets) {
            if (address.equals(wallet.getAddress()))
                return true;
        }

        return false;
    }

    @Override
    public void onNext(LoadWalletViewPagerAdapter.LOAD_TYPE type) {
        viewPagerAdapter = new LoadWalletViewPagerAdapter(getSupportFragmentManager(), type);
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void onKeyStoreSelected(JsonObject keyStore) {
        mKeyStore = keyStore;

        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
        InputMethodManager mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mImm.toggleSoftInput(0, 0);
    }

    @Override
    public void onKeyStoreBundleSelected(List<Wallet> wallets) {
        mBundle = wallets;
        bundleItems = makeList();

        startActivityForResult(new Intent(this, LoadBundleActivity.class)
                .putExtra(LoadBundleActivity.EXTRA_LIST, bundleItems), RC_BUNDLE_LIST);
    }

    @Override
    public void onDoneLoadWalletByKeyStore(String alias) {
        mAlias = alias;
        Wallet wallet = new Wallet();
        wallet.setAlias(mAlias);
        wallet.setAddress(mKeyStore.get("address").getAsString());
        wallet.setKeyStore(mKeyStore.toString());

        List<WalletEntry> walletEntries = new ArrayList<>();
        WalletEntry coin = new WalletEntry();
        coin.setType(MyConstants.TYPE_COIN);
        coin.setAddress(mKeyStore.get("address").getAsString());

        if (mKeyStore.has("coinType")) {
            wallet.setCoinType(Constants.KS_COINTYPE_ICX);
            coin.setSymbol(Constants.KS_COINTYPE_ICX);
            coin.setName(MyConstants.NAME_ICX);
        } else {
            wallet.setCoinType(Constants.KS_COINTYPE_ETH);
            coin.setSymbol(Constants.KS_COINTYPE_ETH);
            coin.setName(MyConstants.NAME_ETH);
        }
        walletEntries.add(coin);

        wallet.setWalletEntries(walletEntries);
        wallet.setCreatedAt(Long.toString(System.currentTimeMillis()));

        try {
            RealmUtil.addWallet(wallet);
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onLoadPrivateKeyNext(String coinType, String privKey) {
        mCoinType = coinType;
        mPrivateKey = privKey;

        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void onDoneInputWalletInfo(String name, String pwd) {
        byte[] privateKey = PKIUtils.hexDecode(mPrivateKey);
        String[] result = null;
        Wallet info = new Wallet();

        try {
            if (mCoinType.equals(Constants.KS_COINTYPE_ICX)) {
                result = KeyStoreUtils.generateICXKeyStoreByPriv(pwd, privateKey);

            } else {
                result = KeyStoreUtils.generateETHKeyStoreByPriv(pwd, privateKey);
            }

            info.setCoinType(mCoinType);
            info.setAlias(name);
            info.setAddress(result[0]);
            info.setKeyStore(result[2]);

            JsonObject keyStore = new Gson().fromJson(result[2], JsonObject.class);

            List<WalletEntry> walletEntries = new ArrayList<>();
            WalletEntry coin = new WalletEntry();
            coin.setType(MyConstants.TYPE_COIN);
            coin.setAddress(result[0]);

            if (keyStore.has("coinType")) {
                info.setCoinType(Constants.KS_COINTYPE_ICX);
                coin.setSymbol(Constants.KS_COINTYPE_ICX);
                coin.setName(MyConstants.NAME_ICX);
            } else {
                info.setCoinType(Constants.KS_COINTYPE_ETH);
                coin.setSymbol(Constants.KS_COINTYPE_ETH);
                coin.setName(MyConstants.NAME_ETH);
            }
            walletEntries.add(coin);

            info.setWalletEntries(walletEntries);

            info.setCreatedAt(Long.toString(System.currentTimeMillis()));

            RealmUtil.addWallet(info);
            RealmUtil.loadWallet();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        startActivity(new Intent(this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BUNDLE_LIST) {
            if (resultCode == LoadBundleActivity.RES_LOAD) {
                try {
                    addBundle();
                    startActivity(new Intent(LoadWalletActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                } catch (Exception e) {
                    // TODO: 2018. 5. 5. Notify to user that load has failed.
                }

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 0)
            super.onBackPressed();
        else
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
    }
}
