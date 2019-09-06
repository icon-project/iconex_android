package foundation.icon.iconex.dev_mainWallet;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class MainWalletActivity extends AppCompatActivity implements
        MainWalletFragment.AsyncRequester, MainWalletServiceHelper.OnLoadRemoteDataListener {

    // findFragmentByTag
    private static String MAIN_WALLET_FRAGMENT_TAG = "main wallet fragment";

    // service connect
    private MainWalletServiceHelper mainWalletServiceHelper = null;

    // cache data
    private List<WalletCardViewData> cachedlstWalletData = new ArrayList<>();
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
        setBalances(icxBalance, ethBalance, errBalance);
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

    private Double setBalance (String id, String address, String result, String unit) {
        String key = address + "," + id;
        indexedWalletEntry.get(key).setBalance(result);

        WalletItemViewData viewData = indexedWalletItemData.get(key);

        try {
            double balance = Double.parseDouble(result);
            String exchageKey = viewData.getSymbol().toLowerCase() + unit;
            double exchanger = Double.parseDouble(ICONexApp.EXCHANGE_TABLE.get(exchageKey));
            double exchanged = balance * exchanger;
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

        double totalAsset = 0.0;
        MainWalletFragment.ExchangeUnit exchageUnit = getMainWalletFragment().getCurrentExchangeUnit();
        String unit = exchageUnit.name().toLowerCase();

        cachingWalletItemData();

        for (String[] param : icxBalance) {
            totalAsset += setBalance(param[0],param[1], param[2], unit);
        }

        for (String[] param : ethBalance) {
            totalAsset += setBalance(param[0], param[1], param[2], unit);
        }

        for (String[] param : errBalance) {
            setBalance(param[0], param[1], param[2], unit);
        }

        boolean isAllErr = icxBalance.size() == 0 && ethBalance.size() == 0;
        getMainWalletFragment().asyncResponseRefreash(
                cachedlstWalletData,
                genTotalAssetsViewData(isAllErr, exchageUnit, totalAsset)
        );
    }

    private void setExchange(MainWalletFragment.ExchangeUnit exchangeUnit) {
        String unit = exchangeUnit.name();

        double totalAsset = 0.0;
        boolean isAllErr = true;
        for(WalletItemViewData viewData : indexedWalletItemData.values()) {
            try {
                Double balance = viewData.getAmount();
                String exchangeKey = viewData.getSymbol().toLowerCase() + unit.toLowerCase();
                double exchanger = Double.parseDouble(ICONexApp.EXCHANGE_TABLE.get(exchangeKey));
                double exchanged = balance * exchanger;
                viewData.setExchanged(exchanged);
                isAllErr = false;
            } catch (Exception e) {
                viewData.setExchanged(null);
            }

        }
        getMainWalletFragment().asyncResponseChangeExchangeUnit(
                exchangeUnit,
                genTotalAssetsViewData(isAllErr, exchangeUnit, totalAsset)
        );
    }

    private TotalAssetsViewData genTotalAssetsViewData(boolean isErr, MainWalletFragment.ExchangeUnit unit, double totalAsset) {

        String formatTotalAsset = unit == MainWalletFragment.ExchangeUnit.USD ? "%,.2f" : "%,.4f";

        TotalAssetsViewData totalAssetsViewData = new TotalAssetsViewData()
                .setTotalAsset(isErr ? "-" : String.format(Locale.getDefault(), formatTotalAsset, totalAsset) )
                .setVotedPower("-");

        return totalAssetsViewData;
    }

    private MainWalletFragment getMainWalletFragment() {
        return ((MainWalletFragment) getSupportFragmentManager()
                .findFragmentByTag(MAIN_WALLET_FRAGMENT_TAG));
    }
}