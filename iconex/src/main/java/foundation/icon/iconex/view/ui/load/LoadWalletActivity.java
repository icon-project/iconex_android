package foundation.icon.iconex.view.ui.load;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.main.MainActivity;
import foundation.icon.iconex.widgets.NonSwipeViewPager;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.PKIUtils;

public class LoadWalletActivity extends AppCompatActivity {

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

        viewPagerAdapter = new LoadWalletViewPagerAdapter(getSupportFragmentManager());

        if (savedInstanceState != null) {
            HashMap<String, Fragment> fragments = new HashMap<>();
            if (getSupportFragmentManager().getFragment(savedInstanceState, "keystore") != null)
                fragments.put("keystore", getSupportFragmentManager().getFragment(savedInstanceState, "keystore"));
            if (getSupportFragmentManager().getFragment(savedInstanceState, "name") != null)
                fragments.put("name", getSupportFragmentManager().getFragment(savedInstanceState, "name"));
            if (getSupportFragmentManager().getFragment(savedInstanceState, "private") != null)
                fragments.put("private", getSupportFragmentManager().getFragment(savedInstanceState, "private"));
            if (getSupportFragmentManager().getFragment(savedInstanceState, "info") != null)
                fragments.put("info", getSupportFragmentManager().getFragment(savedInstanceState, "info"));

            viewPagerAdapter.setFragments(fragments);
        }

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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        HashMap<String, Fragment> fragments = viewPagerAdapter.getFragments();

        if (fragments.containsKey("keystore"))
            getSupportFragmentManager().putFragment(outState, "keystore", fragments.get("keystore"));
        if (fragments.containsKey("name"))
            getSupportFragmentManager().putFragment(outState, "name", fragments.get("name"));
        if (fragments.containsKey("private"))
            getSupportFragmentManager().putFragment(outState, "private", fragments.get("private"));
        if (fragments.containsKey("info"))
            getSupportFragmentManager().putFragment(outState, "info", fragments.get("info"));
    }

    private ArrayList<BundleItem> makeList() {
        ArrayList<BundleItem> list = new ArrayList<>();
        List<Wallet> temp = new ArrayList<>();
        temp.addAll(mBundle);

        for (int i = 0; i < mBundle.size(); i++) {
            Wallet wallet = mBundle.get(i);
//            String alias = wallet.getAlias();
            boolean registered = checkRegister(wallet.getAddress());

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

    private boolean checkRegister(String address) {
        for (Wallet wallet : ICONexApp.mWallets) {
            if (address.equals(wallet.getAddress()))
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BUNDLE_LIST) {
            if (resultCode == LoadBundleActivity.RES_LOAD) {
                try {
                    addBundle();
                    startActivity(new Intent(LoadWalletActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra("popup", MyConstants.MainPopUp.BUNDLE));
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
}
