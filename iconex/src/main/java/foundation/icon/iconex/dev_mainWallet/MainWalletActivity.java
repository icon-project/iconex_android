package foundation.icon.iconex.dev_mainWallet;

import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.datatypes.Int;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;

import foundation.icon.iconex.dev_mainWallet.component.WalletCardView;
import foundation.icon.iconex.dev_mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.EditTextDialog;
import foundation.icon.iconex.dialogs.TitleMsgDialog;
import foundation.icon.iconex.menu.WalletBackUpActivity;
import foundation.icon.iconex.menu.WalletPwdChangeActivity;
import foundation.icon.iconex.menu.appInfo.AppInfoActivity;
import foundation.icon.iconex.menu.bundle.ExportWalletBundleActivity;
import foundation.icon.iconex.menu.lock.SettingLockActivity;
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
import foundation.icon.iconex.dev2_detail.WalletDetailActivity;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;

public class MainWalletActivity extends AppCompatActivity implements
        MainWalletFragment.AsyncRequester,
        WalletCardView.OnClickWalletItemListner,
        MainWalletFragment.SideMenu,
        MainWalletFragment.PRepsMenu,
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
    private Map<Integer, WalletEntry> indexedByIdWalletEntry = new HashMap<>();
    private Map<Integer, Wallet> indexedByIdWallet = new HashMap<>();

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

    @Override
    public void notifyWalletDatachage() {
        setBalances(cachedIcxBalance, cachedEthBalance, cachedErrBalance);
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
        indexedByIdWalletEntry = new HashMap<>();
        indexedByIdWallet = new HashMap<>();
        for(Wallet wallet: ICONexApp.wallets) {
            for (WalletEntry entry: wallet.getWalletEntries()) {
                String key = wallet.getAddress() + "," + entry.getId();
                indexedWalletEntry.put(key, entry);
                indexedByIdWallet.put(entry.getId(), wallet);
                indexedByIdWalletEntry.put(entry.getId(), entry);
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

    // ======================= on click item listenr
    @Override
    public void onClickWalletItem(WalletItemViewData itemViewData) {
        Integer entryID = itemViewData.getEntryID();
        Wallet wallet = indexedByIdWallet.get(entryID);
        WalletEntry walletEntry = indexedByIdWalletEntry.get(entryID);

        startActivity(
            new Intent(this, WalletDetailActivity.class)
                .putExtra(WalletDetailActivity.PARAM_ENTRY_ID, entryID)
                .putExtra(WalletDetailActivity.PARAM_WALLET, ((Serializable) wallet))
                .putExtra(WalletDetailActivity.PARAM_WALLET_ENTRY, ((Serializable) walletEntry))
        );
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
        startActivity(new Intent(this, ExportWalletBundleActivity.class));
    }

    @Override
    public void screenLock() {
        startActivity(new Intent(this, SettingLockActivity.class)
                .putExtra(SettingLockActivity.ARG_TYPE, MyConstants.TypeLock.DEFAULT));
    }

    @Override
    public void appVer() {
        startActivity(new Intent(this, AppInfoActivity.class));
    }

    @Override
    public void iconexDisclamers() {
        TitleMsgDialog dialog = new TitleMsgDialog(this);
        dialog.setTitle(getString(R.string.ICONexDisclaimers));
        SpannableStringBuilder builder = new SpannableStringBuilder(getString(R.string.disclaimersHeader)
                + "\n\n" + getString(R.string.disclaimersContents));
        builder.setSpan(new StyleSpan(Typeface.BOLD), 0, getString(R.string.disclaimersHeader).length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        dialog.setMessage(builder.toString());
        dialog.show();
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