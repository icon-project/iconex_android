package foundation.icon.iconex.dev_mainWallet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;

import foundation.icon.iconex.dev_mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.EditTextDialog;
import foundation.icon.iconex.menu.WalletBackUpActivity;
import foundation.icon.iconex.menu.WalletPwdChangeActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.token.manage.TokenManageActivity;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.CreateWalletActivity;
import foundation.icon.iconex.view.IScoreActivity;
import foundation.icon.iconex.view.IntroActivity;
import foundation.icon.iconex.view.LoadWalletActivity;
import foundation.icon.iconex.view.PRepListActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.main.MainActivity;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class MainWalletActivity extends AppCompatActivity implements
        MainWalletFragment.AsyncRequester,
        MainWalletFragment.PRepsMenu,
        MainWalletFragment.ManageWallet,
        MainWalletFragment.SideMenu,
        MainWalletServiceHelper.OnLoadRemoteDataListener {

    public static String TAG = MainWalletActivity.class.getSimpleName();

    // findFragmentByTag
    private static String MAIN_WALLET_FRAGMENT_TAG = "main wallet fragment";

    // service connect
    private MainWalletServiceHelper mainWalletServiceHelper = null;

    // cache data
    private List<WalletCardViewData> cachedlstWalletData = new ArrayList<>();
    private List<String[]> cachedIcxBalance = new ArrayList<>();
    private List<String[]> cachedEthBalance = new ArrayList<>();
    private List<String[]> cachedErrBalance = new ArrayList<>();

    private Map<String, WalletEntry> indexedWalletEntry = new HashMap<>();
    private Map<String, WalletItemViewData> indexedWalletItemData = new HashMap<>();

    // ================== activity life cycle
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FrameLayout container = new FrameLayout(this);
        container.setId(R.id.container);
        setContentView(container, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));


        MainWalletFragment fragment = MainWalletFragment.newInstance();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container, fragment, MAIN_WALLET_FRAGMENT_TAG)
                .commit();

        mainWalletServiceHelper = new MainWalletServiceHelper(this, this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainWalletServiceHelper.resume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainWalletServiceHelper.stop();
    }

    // ===================== ViewData listener (MainWalletFragment.AsyncRequester)
    @Override
    public void asyncRequestInitData() {
        cachingWalletItemData();
        getMainWalletFragment().asyncResponseInit(cachedlstWalletData);
    }

    @Override
    public void asyncRequestRefreshData() {
        mainWalletServiceHelper.requestRemoteData();
    }

    @Override
    public void asyncRequestChangeExchangeUnit(MainWalletFragment.ExchangeUnit exchangeUnit) {
        setExchange(exchangeUnit);
    }

    // =================== Service listener (MainWalletServiceHelper.OnLoadRemoteDataListener)
    @Override
    public void onLoadRemoteData(List<String[]> icxBalance, List<String[]> ethBalance, List<String[]> errBalance) {
        Log.d(TAG, "load remote data, icx: " + icxBalance.size() + ", eth: " + ethBalance.size() + ", err: " + errBalance.size());
        cachedIcxBalance = icxBalance;
        cachedEthBalance = ethBalance;
        cachedErrBalance = errBalance;

        setBalances(cachedIcxBalance, cachedEthBalance, cachedErrBalance);
    }

    // =================== private methods
    private void cachingWalletItemData() {
        // load Item
        cachedlstWalletData = new ArrayList<>();
        for (Wallet wallet : ICONexApp.wallets) {
            cachedlstWalletData.add(WalletCardViewData.convertWallet2ViewData(wallet));
        }

        // indexing wallet entry
        indexedWalletEntry = new HashMap<>();
        for(Wallet wallet: ICONexApp.wallets) {
            for (WalletEntry entry: wallet.getWalletEntries()) {
                String key = wallet.getAddress() + "," + entry.getId();
                indexedWalletEntry.put(key, entry);
            }
        }

        // indexing view data
        indexedWalletItemData = new HashMap<>();
        for(WalletCardViewData walletViewData: cachedlstWalletData) {
            for (WalletItemViewData itemViewData: walletViewData.getLstWallet()) {
                String key = walletViewData.getAddress() + "," + itemViewData.getEntryID();
                indexedWalletItemData.put(key, itemViewData);
            }
        }
    }

    private BigDecimal setBalance (String id, String address, String result, String unit) {
        String key = address + "," + id;
        WalletEntry walletEntry = indexedWalletEntry.get(key);
        WalletItemViewData viewData = indexedWalletItemData.get(key);
        if (walletEntry == null || viewData == null) return null;

        try {
            walletEntry.setBalance(result);
            String strDecimal = ConvertUtil.getValue(new BigInteger(walletEntry.getBalance()), walletEntry.getDefaultDec());
            BigDecimal balance = new BigDecimal(strDecimal);

            String exchangeKey = viewData.getSymbol().toLowerCase() + unit;
            BigDecimal exchanger = new BigDecimal(ICONexApp.EXCHANGE_TABLE.get(exchangeKey));
            BigDecimal exchanged = balance.multiply(exchanger);

            viewData.setAmount(balance).setExchanged(exchanged);
            return exchanged;
        } catch (Exception e) {
            viewData.setAmount(null).setExchanged(null);
            return null;
        }
    }

    private void setBalances(
            List<String[]> icxBalance,
            List<String[]> ethBalance,
            List<String[]> errBalance) {

        BigDecimal totalAsset = icxBalance.size() == 0 && ethBalance.size() == 0 ? null : BigDecimal.ZERO;
        MainWalletFragment.ExchangeUnit exchageUnit = getMainWalletFragment().getCurrentExchangeUnit();
        String strUnit = exchageUnit.name().toLowerCase();

        cachingWalletItemData();

        for (String[] param : icxBalance) {
            BigDecimal exchanged = setBalance(param[0], param[1], param[2], strUnit);
            if (exchanged != null) {
                totalAsset = totalAsset.add(exchanged);
            } // else
        }

        for (String[] param : ethBalance) {
            BigDecimal exchanged = setBalance(param[0], param[1], param[2], strUnit);
            if (exchanged != null) {
                totalAsset = totalAsset.add(exchanged);
            } // else
        }

        for (String[] param : errBalance) {
            setBalance(param[0], param[1], param[2], strUnit);
        }

        getMainWalletFragment().asyncResponseRefreash(
                cachedlstWalletData,
                new TotalAssetsViewData().setTotalAsset(totalAsset)
        );
    }

    private void setExchange(MainWalletFragment.ExchangeUnit exchangeUnit) {
        String unit = exchangeUnit.name();

        BigDecimal totalAsset = null;
        for(WalletItemViewData viewData : indexedWalletItemData.values()) {
            try {
                BigDecimal balance = viewData.getAmount();

                String exchangeKey = viewData.getSymbol().toLowerCase() + unit.toLowerCase();
                BigDecimal exchanger = new BigDecimal(ICONexApp.EXCHANGE_TABLE.get(exchangeKey));
                BigDecimal exchanged = balance.multiply(exchanger);

                viewData.setExchanged(exchanged);
                if (totalAsset == null) totalAsset = BigDecimal.ZERO;
                totalAsset = totalAsset.add(exchanged);
            } catch (Exception e) {
                viewData.setExchanged(null);
            }

        }
        getMainWalletFragment().asyncResponseChangeExchangeUnit(
                exchangeUnit,
                new TotalAssetsViewData().setTotalAsset(totalAsset)
        );
    }

    private MainWalletFragment getMainWalletFragment() {
        return ((MainWalletFragment) getSupportFragmentManager()
                .findFragmentByTag(MAIN_WALLET_FRAGMENT_TAG));
    }

    private Wallet findWalletByViewData(WalletCardViewData viewData) {
        String address = viewData.getAddress();
        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getAddress().equals(address)) {
                return wallet;
            }
        }

        return null;
    }

    // ============================== manage wallet
    @Override
    public void renameWallet(WalletCardViewData viewData) {
        Wallet targetWallet = findWalletByViewData(viewData);

        EditTextDialog editTextDialog = new EditTextDialog(this, getString(R.string.modWalletAlias));
        editTextDialog.setHint(getString(R.string.hintWalletAlias));
        editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.ALIAS);
        editTextDialog.setAlias(targetWallet.getAlias());
        editTextDialog.setOnConfirmCallback(new EditTextDialog.OnConfirmCallback() {
            @Override
            public void onConfirm(String text) {
                String alias = Utils.strip(text);

                if (alias.isEmpty()) {
                    editTextDialog.setError(getString(R.string.errWhiteSpace));
                    return;
                }

                if (alias.trim().length() == 0) {
                    editTextDialog.setError(getString(R.string.errWhiteSpace));
                    return;
                }

                for (Wallet info : ICONexApp.wallets) {
                    if (info.getAlias().equals(alias)) {
                        editTextDialog.setError(getString(R.string.duplicateWalletAlias));
                        return;
                    }
                }

                RealmUtil.modWalletAlias(targetWallet.getAddress(), alias);
                targetWallet.setAlias(alias);
                setBalances(cachedIcxBalance, cachedEthBalance, cachedErrBalance);
                editTextDialog.dismiss();
            }
        });
        editTextDialog.show();
    }

    @Override
    public void manageToken(WalletCardViewData viewData) {
        Wallet targetWallet = findWalletByViewData(viewData);

        Intent intent = new Intent(this, TokenManageActivity.class);
        intent.putExtra("walletInfo", (Serializable) targetWallet);

        if (targetWallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
            intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.IRC);
        else
            intent.putExtra("type", TokenManageActivity.TOKEN_TYPE.ERC);

        startActivity(intent);
    }

    @Override
    public void backupWallet(WalletCardViewData viewData) {
        Wallet targetWallet = findWalletByViewData(viewData);

        EditTextDialog editTextDialog = new EditTextDialog(this, getString(R.string.enterWalletPassword));
        editTextDialog.setHint(getString(R.string.hintWalletPassword));
        editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
        editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.BACKUP);
        editTextDialog.setOnPasswordCallback(new EditTextDialog.OnPasswordCallback() {
            @Override
            public void onConfirm(EditTextDialog.RESULT_PWD result, String pwd) {
                JsonObject keyStore = new Gson().fromJson(targetWallet.getKeyStore(), JsonObject.class);
                byte[] bytePrivKey;
                try {
                    JsonObject crypto = null;
                    if (keyStore.has("crypto"))
                        crypto = keyStore.get("crypto").getAsJsonObject();
                    else
                        crypto = keyStore.get("Crypto").getAsJsonObject();

                    bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, targetWallet.getAddress(), crypto, targetWallet.getCoinType());
                    if (bytePrivKey != null) {
                        startActivity(new Intent(MainWalletActivity.this, WalletBackUpActivity.class)
                                .putExtra("walletInfo", (Serializable) targetWallet)
                                .putExtra("privateKey", Hex.toHexString(bytePrivKey)));

                        editTextDialog.dismiss();
                    } else {
                        editTextDialog.setError(getString(R.string.errPassword));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        editTextDialog.show();
    }

    @Override
    public void changeWalletPassword(WalletCardViewData viewData) {
        Wallet targetWallet = findWalletByViewData(viewData);
        startActivity(new Intent(this, WalletPwdChangeActivity.class)
                .putExtra("walletInfo", (Serializable) targetWallet));
    }

    @Override
    public void removeWallet(WalletCardViewData viewData) {
        Wallet targetWallet = findWalletByViewData(viewData);

        final Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
        dialog.setMessage(getString(R.string.warningRemoveWallet));
        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
            @Override
            public void onOk() {
                EditTextDialog editTextDialog = new EditTextDialog(MainWalletActivity.this, getString(R.string.enterWalletPassword));
                editTextDialog.setHint(getString(R.string.hintWalletPassword));
                editTextDialog.setInputType(EditTextDialog.TYPE_INPUT.PASSWORD);
                editTextDialog.setPasswordType(EditTextDialog.RESULT_PWD.REMOVE);
                editTextDialog.setOnPasswordCallback(new EditTextDialog.OnPasswordCallback() {
                    @Override
                    public void onConfirm(EditTextDialog.RESULT_PWD result, String pwd) {
                        JsonObject keyStore = new Gson().fromJson(targetWallet.getKeyStore(), JsonObject.class);
                        byte[] bytePrivKey;
                        try {
                            JsonObject crypto = null;
                            if (keyStore.has("crypto"))
                                crypto = keyStore.get("crypto").getAsJsonObject();
                            else
                                crypto = keyStore.get("Crypto").getAsJsonObject();

                            bytePrivKey = KeyStoreUtils.decryptPrivateKey(pwd, targetWallet.getAddress(), crypto, targetWallet.getCoinType());
                            if (bytePrivKey != null) {

                                RealmUtil.removeWallet(targetWallet.getAddress());
                                try {
                                    RealmUtil.loadWallet();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (ICONexApp.wallets.size() == 0) {
                                    startActivity(new Intent(MainWalletActivity.this, IntroActivity.class)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                                } else {
                                    setBalances(cachedIcxBalance, cachedEthBalance, cachedErrBalance);
                                }

                                editTextDialog.dismiss();
                            } else {
                                editTextDialog.setError(getString(R.string.errPassword));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                editTextDialog.show();
                dialog.dismiss();
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.show();
    }

    // ==================================== side menu item
    @Override
    public void createWallet() {
        startActivity(new Intent(this, CreateWalletActivity.class));
    }

    @Override
    public void loadWallet() {
        startActivity(new Intent(this, LoadWalletActivity.class));
    }

    @Override
    public void exportWalletBundle() {
        Toast.makeText(this, "not implement", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void screenLock() {
        Toast.makeText(this, "not implement", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void appVer() {
        Toast.makeText(this, "not implement", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void iconexDisclamers() {
        Toast.makeText(this, "not implement", Toast.LENGTH_SHORT).show();
    }

    // ========================= P-Peps Menu
    @Override
    public void pReps(WalletCardViewData viewData) {
        startActivity(new Intent(this, PRepListActivity.class));
    }

    @Override
    public void stake(WalletCardViewData viewData) {
        Toast.makeText(this, "not implement", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void vote(WalletCardViewData viewData) {
        Toast.makeText(this, "not implement", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void iScore(WalletCardViewData viewData) {
        Wallet wallet = findWalletByViewData(viewData);
        startActivity(new Intent(this, IScoreActivity.class)
                .putExtra("wallet", (Serializable) wallet));
    }
}