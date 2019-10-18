package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.node.BigIntegerNode;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.menu.WalletPwdChangeActivityNew;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.DecimalFomatter;
import foundation.icon.iconex.view.ui.mainWallet.MainWalletFragment;
import foundation.icon.iconex.view.ui.mainWallet.MainWalletServiceHelper;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletManageMenuDialog;
import foundation.icon.iconex.view.ui.mainWallet.items.TokenWalletItem;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.EntryViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletViewData;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import loopchain.icon.wallet.core.Constants;

public class MainWalletActivity extends AppCompatActivity implements MainWalletServiceHelper.OnLoadListener, MainWalletFragment.RequestActivity {

    private MainWalletServiceHelper serviceHelper = new MainWalletServiceHelper();

    private static String MAIN_WALLET_FRAGMENT_TAG = "main wallet fragment";

    private TotalAssetsViewData totalAssetsVD = new TotalAssetsViewData();
    private List<WalletViewData> walletVDs = new Vector<>();
    private List<WalletViewData> tokenListVDs = new Vector<>();

    private String currentUnit = "USD";
    private boolean balanceLoading = true;
    private boolean exchangeLoading = true;
    private boolean patchingData = false;


    private void loadViewData() {
        walletVDs.clear();
        tokenListVDs.clear();
        totalAssetsVD = new TotalAssetsViewData();
        balanceLoading = true;
        exchangeLoading = true;

        Map<String, List<EntryViewData>> mapTokenListEntries = new HashMap<>();
        TokenWalletItem.TokenColor tokenColor = new TokenWalletItem.TokenColor(); // token background color
        for(Wallet wallet : ICONexApp.wallets) {
            List<EntryViewData> walletListEntries = new ArrayList<>();
            for (WalletEntry entry : wallet.getWalletEntries()) {
                EntryViewData entryVD = new EntryViewData(wallet, entry);
                if (entry.getType().equals(MyConstants.TYPE_TOKEN)) {
                    entryVD.setBgSymbolColor(tokenColor.getColor());
                    tokenColor.nextColor();
                }
                walletListEntries.add(entryVD);

                String key = wallet.getCoinType() + entry.getContractAddress();
                if (!mapTokenListEntries.containsKey(key)) {
                    mapTokenListEntries.put(key, new ArrayList<>());
                }

                List<EntryViewData> tokenListEntries = mapTokenListEntries.get(key);
                entryVD.pos1 = tokenListEntries.size() + 1;
                tokenListEntries.add(entryVD);
            }
            walletVDs.add(new WalletViewData(wallet, walletListEntries));
        }

        WalletViewData icxWallet = null;
        WalletViewData ethWallet = null;
        for (String key : mapTokenListEntries.keySet()) {
            List<EntryViewData> tokenListEntries = mapTokenListEntries.get(key);
            WalletViewData tokenListVD = new WalletViewData(null, tokenListEntries);
            if (key.equals(Constants.KS_COINTYPE_ICX)) {
                icxWallet = tokenListVD;
            } else if(key.equals(Constants.KS_COINTYPE_ETH)) {
                ethWallet = tokenListVD;
            } else {
                tokenListVDs.add(tokenListVD);
            }
        }
        tokenListVDs.add(0, icxWallet);
        tokenListVDs.add(1, ethWallet);

        for (int i = 0; tokenListVDs.size() > i; i++) {
            WalletViewData walletVD = tokenListVDs.get(i);
            for (EntryViewData entrVD : walletVD.getEntryVDs()) {
                entrVD.pos0 = i;
            }
        }

        findFragment().notifyDataSetChange(walletVDs, tokenListVDs);
        findFragment().notifyTotalAssetsDataChanged(totalAssetsVD);
    }

    @Override
    public void patchViewData() {
        loadViewData();
        balanceLoading = false;
        exchangeLoading = false;
        patchingData = true;

        for (int i = 0; ICONexApp.wallets.size() > i; i++) {
            Wallet wallet = ICONexApp.wallets.get(i);
            for (int j = 0; wallet.getWalletEntries().size() > j; j++) {
                WalletEntry entry = wallet.getWalletEntries().get(j);
                onLoadNextBalance(entry, i, j);
            }

            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                onLoadNextiScore(wallet, i);
                onLoadNextDelegation(wallet, i);
            }
        }

        combineExchanges(-1, -1);
        combineTotalAssets();
        onLoadCompletePReps();
        patchingData = false;

        findFragment().notifyDataSetChange(walletVDs, tokenListVDs);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, MainWalletFragment.newInstance(), MAIN_WALLET_FRAGMENT_TAG)
                .commit();
    }

    private MainWalletFragment findFragment() {
        return ((MainWalletFragment) getSupportFragmentManager()
                .findFragmentByTag(MAIN_WALLET_FRAGMENT_TAG));
    }

    @Override
    public void onLoadNextBalance(WalletEntry entry, int walletPosition, int entryPosition) {
        EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(entryPosition);
        try {
            BigInteger intBalance = new BigInteger(entry.getBalance());
            String strBalance = ConvertUtil.getValue(intBalance, entry.getDefaultDec());
            String deciBalance = DecimalFomatter.format(new BigDecimal(strBalance));
            entryVD.setTxtAmount(deciBalance);
        } catch (Exception e) {
            entryVD.setTxtAmount(MyConstants.NO_BALANCE);
        }
        entryVD.amountLoading = false;
        combineExchanges(walletPosition, entryPosition);
    }

    @Override
    public void onLoadCompleteBalance() {
        balanceLoading = false;
        combineTotalAssets();
    }

    @Override
    public void onLoadCompleteExchangeTable() {
        exchangeLoading = false;
        combineExchanges(-1, -1);
        combineTotalAssets();
    }

    @Override
    public void onLoadNextiScore(Wallet wallet, int walletPosition) {
        EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(0);
        try {
            BigInteger intIScore = wallet.getiScore();
            String strIScore = ConvertUtil.getValue(intIScore, 18);
            String deciIScore = DecimalFomatter.format(new BigDecimal(strIScore));
            entryVD.setTxtIScore(deciIScore);
        } catch (Exception e) {
            entryVD.setTxtIScore(MyConstants.NO_BALANCE);
        }
        entryVD.iscoreLoading = false;
        if (!patchingData) findFragment().notifyItemChange(walletPosition, 0);
    }

    @Override
    public void onLoadNextStake(Wallet wallet, int walletPosition, BigInteger unstake) {
        EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(0);
        entryVD.unstake = unstake;
    }

    @Override
    public void onLoadNextDelegation(Wallet wallet, int walletPosition) {
        WalletEntry entry = wallet.getWalletEntries().get(0);
        EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(0);
        try {
            BigDecimal balance = new BigDecimal(ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec()));
            BigDecimal staked = new BigDecimal(ConvertUtil.getValue(wallet.getStaked(), 18));

            BigDecimal percent = BigDecimal.ZERO.setScale(1);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                percent = staked.multiply(new BigDecimal(100))
                        .divide(balance.add(staked), 1, BigDecimal.ROUND_HALF_UP);
            }

            entryVD.setTxtStacked(DecimalFomatter.format(staked) + " (" + percent + "%)");

        } catch (Exception e) {
            entryVD.setTxtStacked("- ( - %)");
        }
        entryVD.prepsLoading = false;
        if (!patchingData) findFragment().notifyItemChange(walletPosition, 0);
    }

    @Override
    public void onLoadCompletePReps() {
        // update preps data
        for (WalletViewData walletVD : walletVDs) {
            Wallet wallet = walletVD.getWallet();
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                WalletEntry entry = wallet.getWalletEntries().get(0);
                EntryViewData entryVD = walletVD.getEntryVDs().get(0);

                try {
                    BigDecimal balance = new BigDecimal(ConvertUtil.getValue(new BigInteger(entry.getBalance()), 18));
                    BigDecimal staked = new BigDecimal(ConvertUtil.getValue(wallet.getStaked(), 18));
                    BigDecimal unstake = new BigDecimal(ConvertUtil.getValue(entryVD.unstake == null ? BigInteger.ZERO : entryVD.unstake, 18));

                    BigDecimal totalBalance = balance.add(staked).add(unstake);
                    BigDecimal percent = totalBalance.compareTo(BigDecimal.ZERO) == 0 ? new BigDecimal("0.0") :
                            staked.multiply(new BigDecimal(100)).divide(totalBalance, 1, BigDecimal.ROUND_HALF_UP);
                    entryVD.setTxtStacked(DecimalFomatter.format(staked) + " (" + percent + "%)");
                } catch (Exception e) {
                    entryVD.setTxtStacked("- ( - %)");
                }
                entryVD.prepsLoading = false;
            }
        }
        if (!patchingData) findFragment().notifyDataSetChange(walletVDs, tokenListVDs);

        // update total assets
        BigInteger totalVoting = BigInteger.ZERO;
        BigInteger totalStaked = BigInteger.ZERO;

        for (Wallet wallet : ICONexApp.wallets) {
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                try {
                    BigInteger votingSum = wallet.getVotingPower().add(totalVoting);
                    BigInteger stakedSum = wallet.getStaked().add(totalStaked);
                    totalVoting = votingSum;
                    totalStaked = stakedSum;
                } catch (Exception e) { }
            }
        }

        if (totalStaked.compareTo(BigInteger.ZERO) != 0) {
            BigDecimal percent = new BigDecimal(totalStaked.subtract(totalVoting))
                    .divide(new BigDecimal(totalStaked).multiply(new BigDecimal(100)), 1, RoundingMode.HALF_UP);
            totalAssetsVD.setVotedPower(percent);
        } else {
            totalAssetsVD.setVotedPower(new BigDecimal("0.0"));
        }
        totalAssetsVD.loadingVotedpower = false;
        if (!patchingData) findFragment().notifyTotalAssetsDataChanged(totalAssetsVD);
    }

    @Override
    public void onLoadCompleteAll() {
        combineTopToken();
        findFragment().notifyCompleteDataLoad();
    }

    @Override
    public void onNetworkError() {

    }

    private void combineTotalAssets() {
        if (exchangeLoading || balanceLoading) return;

        BigDecimal totalBalance = BigDecimal.ZERO;
        for (WalletViewData walletVD : walletVDs) {
            for (EntryViewData entryVD : walletVD.getEntryVDs()) {
                BigDecimal exchanged = entryVD.getExchanged();
                if (exchanged != null) {
                    totalBalance = totalBalance.add(exchanged);
                }
            }
        }
        totalAssetsVD.setTotalAsset(totalBalance, currentUnit);
        totalAssetsVD.loadingTotalAssets = false;
        findFragment().notifyTotalAssetsDataChanged(totalAssetsVD);
    }

    private void combineTopToken() {
        for (WalletViewData walletVD : tokenListVDs) {

            List<EntryViewData> entryVDs = walletVD.getEntryVDs();
            EntryViewData topTokenVD = entryVDs.get(0);

            BigDecimal totalBalance = null;
            for (int i = 1; entryVDs.size() > i; i++) {
                try {
                    EntryViewData entryVD = entryVDs.get(i);
                    BigDecimal balance = new BigDecimal(ConvertUtil.getValue(new BigInteger(entryVD.getEntry().getBalance()), entryVD.getEntry().getDefaultDec()));
                    totalBalance = balance.add(totalBalance == null ? BigDecimal.ZERO : totalBalance);
                } catch (Exception e) { }
            }

            topTokenVD.setTxtAmount(totalBalance == null ? MyConstants.NO_BALANCE : DecimalFomatter.format(totalBalance));
            topTokenVD.amountLoading = false;

            BigDecimal exchanged = null;
            try {
                String exchangeKey = entryVDs.get(1).getEntry().getSymbol().toLowerCase() + currentUnit.toLowerCase();
                String strExchanger = ICONexApp.EXCHANGE_TABLE.get(exchangeKey);
                BigDecimal deciExchanger = new BigDecimal(strExchanger);
                exchanged = totalBalance.multiply(deciExchanger);
            } catch (Exception e) { }
            topTokenVD.setExchanged(exchanged, currentUnit);
            topTokenVD.exchageLoading = false;
        }
        if (!patchingData) findFragment().notifyDataSetChange(walletVDs, tokenListVDs);
    }

    // -1, -1 -> totally refresh
    private void combineExchanges(int walletPosition, int entryPosition) {
        boolean isTotally = walletPosition == -1 || entryPosition == -1;

        if (!exchangeLoading) {
            if (isTotally) {
                for (int i = 0; walletVDs.size() > i; i++) {
                    WalletViewData walletVD = walletVDs.get(i);
                    for (int j = 0; walletVD.getEntryVDs().size() > j; j++) {
                        EntryViewData entryVD = walletVD.getEntryVDs().get(j);
                        if (!entryVD.amountLoading) {
                            WalletEntry entry = ICONexApp.wallets.get(i).getWalletEntries().get(j);
                            entryVD.setExchanged(calcExchange(entry), currentUnit);
                            entryVD.exchageLoading = false;
                        }
                    }
                }
            } else {
                WalletEntry entry = ICONexApp.wallets.get(walletPosition).getWalletEntries().get(entryPosition);
                EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(entryPosition);
                entryVD.setExchanged(calcExchange(entry), currentUnit);
                entryVD.exchageLoading = false;
            }
        }

        if (!patchingData)
            if (isTotally) {
                findFragment().notifyDataSetChange(walletVDs, tokenListVDs);
            } else {
                findFragment().notifyItemChange(walletPosition, entryPosition);
            }
    }

    private BigDecimal calcExchange(WalletEntry entry) {
        try {
            BigInteger intBalance = new BigInteger(entry.getBalance());
            String strBalance = ConvertUtil.getValue(intBalance, entry.getDefaultDec());
            BigDecimal deciBalance = new BigDecimal(strBalance);

            String exchangeKey = entry.getSymbol().toLowerCase() + currentUnit.toLowerCase();
            String strExchanger = ICONexApp.EXCHANGE_TABLE.get(exchangeKey);
            BigDecimal deciExchanger = new BigDecimal(strExchanger);

            BigDecimal exchanged = deciBalance.multiply(deciExchanger);
            return exchanged;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void refreashViewData() {
        loadViewData();
        serviceHelper.setListener(this);
        serviceHelper.requestAllData();
    }

    @Override
    public void chagneExchageUnit(String unit) {
        currentUnit = unit;
        combineExchanges(-1, -1);
        combineTopToken();
        combineTotalAssets();
    }

    @Override
    public void fragmentStart() {
        loadViewData();
        serviceHelper.setListener(this);
        serviceHelper.requestAllData();
    }


    @Override
    public void fragmenetResume() {
        serviceHelper.setListener(this);
        patchViewData();
    }

    @Override
    public void fragmentStop() {
        serviceHelper.clearListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case WalletManageMenuDialog.REQ_PASSWORD_CHANGE: {
                WalletPwdChangeActivityNew.getActivityResult(resultCode, data, new WalletPwdChangeActivityNew.OnResultListener() {
                    @Override
                    public void onResult(Wallet wallet) {
                        try {
                            RealmUtil.loadWallet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        loadViewData();
                        serviceHelper.requestAllData();
                    }
                });
            }
            break;
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
