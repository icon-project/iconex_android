package foundation.icon.iconex.dev_mainWallet;

import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private static String MAIN_WALLET_FRAGMENT_TAG = "main wallet fragment";

    private MainWalletServiceHelper mainWalletServiceHelper = null;

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

    @Override // activity
    protected void onResume() {
        super.onResume();
        mainWalletServiceHelper.resume();
    }

    @Override // activity
    protected void onStop() {
        super.onStop();
        mainWalletServiceHelper.stop();
    }

    @Override // MainWalletFragment.AsyncRequester (ViewData)
    public void requestInitData() {
        getMainWalletFragment().asyncResponseInit(
                loadWalletFromLocal(),
                new TotalAssetsViewData()
                    .setTotalAsset("0")
                    .setVotedPower("0")
        );
    }

    @Override // MainWalletFragment.AsyncRequester (ViewData)
    public void requestRefreshData() {
        mainWalletServiceHelper.requestRemoteData();
    }

    @Override // MainWalletServiceHelper.OnLoadRemoteDataListener
    public void onLoadRemoteData(List<String[]> icxBalance, List<String[]> ethBalance, List<String[]> errBalance) {
        mixWalletDataWithBalance(icxBalance, ethBalance, errBalance);
    }

    private MainWalletFragment getMainWalletFragment() {
        return ((MainWalletFragment) getSupportFragmentManager()
                .findFragmentByTag(MAIN_WALLET_FRAGMENT_TAG));
    }

    private List<WalletCardViewData> loadWalletFromLocal() {
        List<WalletCardViewData> walletViewDatas = new ArrayList<>();
        for (Wallet wallet : ICONexApp.wallets) {
            walletViewDatas.add(WalletCardViewData.convertWallet2ViewData(wallet));
        }
        return walletViewDatas;
    }

    private void mixWalletDataWithBalance(
            List<String[]> icxBalance,
            List<String[]> ethBalance,
            List<String[]> errBalance) {

        double totalAsset = 0.0;
        String exchageUnit = "USD";
        List<WalletCardViewData> lstWalletData = loadWalletFromLocal();

        // indexing wallet entry
        Map<String, WalletEntry> mapWalletEntry = new HashMap<>();
        for(Wallet wallet: ICONexApp.wallets) {
            for (WalletEntry entry: wallet.getWalletEntries()) {
                String key = wallet.getAddress() + "," + entry.getId();
                mapWalletEntry.put(key, entry);
            }
        }

        // indexing view data
        Map<String, WalletItemViewData> mapWalletItemData = new HashMap<>();
        for(WalletCardViewData walletViewData: lstWalletData) {
            for (WalletItemViewData itemViewData: walletViewData.getLstWallet()) {
                String key = walletViewData.getAddress() + "," + itemViewData.getEntryID();
                mapWalletItemData.put(key, itemViewData);
            }
        }

        for (String[] param : icxBalance) {
            String id = param[0];
            String address = param[1];
            String result = param[2];
            String key = address + "," + id;
            mapWalletEntry.get(key).setBalance(result);
            WalletItemViewData viewData = mapWalletItemData.get(key);
            String exchageKey = viewData.getSymbol().toLowerCase() + exchageUnit.toLowerCase();
            double exchanger = Double.parseDouble(ICONexApp.EXCHANGE_TABLE.get(exchageKey));
            double balance = Double.parseDouble(result);
            double exchanged = balance * exchanger;
            totalAsset += exchanged;
            viewData
                    .setAmount(result)
                    .setExchanged(exchanged + " " + exchageUnit);
        }

        for (String[] param : ethBalance) {
            String id = param[0];
            String address = param[1];
            String result = param[2];
            String key = address + "," + id;
            mapWalletEntry.get(key).setBalance(result);
            WalletItemViewData viewData = mapWalletItemData.get(key);
            String exchageKey = viewData.getSymbol().toLowerCase() + exchageUnit.toLowerCase();
            double exchanger = Double.parseDouble(ICONexApp.EXCHANGE_TABLE.get(exchageKey));
            double balance = Double.parseDouble(result);
            double exchanged = balance * exchanger;
            totalAsset += exchanged;
            viewData
                    .setAmount(result)
                    .setExchanged(exchanged + " " + exchageUnit);
        }

        for (String[] param : errBalance) {
            String id = param[0];
            String address = param[1];
            String result = param[2];
            String key = address + "," + id;
            mapWalletEntry.get(key).setBalance(result);
            mapWalletItemData.get(key)
                    .setAmount(result)
                    .setExchanged("- " + exchageUnit);
        }

        getMainWalletFragment().asyncResponseRefreash(
                lstWalletData,
                new TotalAssetsViewData()
                    .setTotalAsset(icxBalance.size() > 0 || ethBalance.size() > 0 ? totalAsset + "" : "-")
                    .setVotedPower("0.0")
        );
    }
}