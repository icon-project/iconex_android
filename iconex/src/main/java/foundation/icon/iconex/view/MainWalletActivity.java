package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.menu.WalletPwdChangeActivityNew;
import foundation.icon.iconex.menu.lock.SettingLockActivity;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.NetworkErrorActivity;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.DecimalFomatter;
import foundation.icon.iconex.view.ui.mainWallet.MainWalletDataRequester;
import foundation.icon.iconex.view.ui.mainWallet.MainWalletFragment;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletManageMenuDialog;
import foundation.icon.iconex.view.ui.mainWallet.items.TokenWalletItem;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.EntryViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.TotalAssetsViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletViewData;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;

public class MainWalletActivity extends AppCompatActivity implements
        MainWalletDataRequester.OnLoadListener,
        MainWalletFragment.RequestActivity {

    private static String MAIN_WALLET_FRAGMENT_TAG = "main wallet fragment";

    private TotalAssetsViewData totalAssetsVD = new TotalAssetsViewData();
    private List<WalletViewData> walletVDs = Collections.synchronizedList(new ArrayList<>());
    private List<WalletViewData> tokenListVDs = Collections.synchronizedList(new ArrayList<>());

    private boolean patchingData = false;
    private String currentUnit = "USD";
    private Handler handler = new Handler();

    boolean isFingerprintInvalidated = false;

    public class UIupdater {
        private boolean loadCompleteBalance = false;
        private boolean loadCompleteExchange = false;
        private boolean loadCompleteAll = false;

        synchronized public boolean isLoadCompleteBalance() {
            return loadCompleteBalance;
        }

        synchronized public UIupdater setLoadCompleteBalance(boolean loadCompleteBalance) {
            this.loadCompleteBalance = loadCompleteBalance;
            return this;
        }

        synchronized public boolean isLoadCompleteExchange() {
            return loadCompleteExchange;
        }

        synchronized public UIupdater setLoadCompleteExchange(boolean loadCompleteExchange) {
            this.loadCompleteExchange = loadCompleteExchange;
            return this;
        }

        synchronized public boolean isLoadCompleteAll() {
            return loadCompleteAll;
        }

        synchronized public UIupdater setLoadCompleteAll(boolean loadCompleteAll) {
            this.loadCompleteAll = loadCompleteAll;
            return this;
        }

        private boolean isUpdateAssetsView = false;
        private boolean isUpdateAllView = false;
        final List<Integer> updateWalletViews = Collections.synchronizedList(new ArrayList<>());
        final List<Integer> updatetokenViews = Collections.synchronizedList(new ArrayList<>());

        synchronized public boolean isUpdateAssetsView() {
            return isUpdateAssetsView;
        }

        synchronized public UIupdater setUpdateAssetsView(boolean updateAssetsView) {
            isUpdateAssetsView = updateAssetsView;
            return this;
        }

        synchronized public boolean isUpdateAllView() {
            return isUpdateAllView;
        }

        synchronized public UIupdater setUpdateAllView(boolean updateAllView) {
            isUpdateAllView = updateAllView;
            return this;
        }

        private MainWalletFragment mFragment = null;


        public void setFragment(MainWalletFragment fragment) {
            mFragment = fragment;
        }

        public void startListening() {
            new Thread(new Runnable() {
                @Override
                public void run() {

                    boolean _loadCompleteAll;
                    do {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                        }

                        if (mFragment == null) break;

                        synchronized (UIupdater.this) {
                            _loadCompleteAll = loadCompleteAll;
                            if (isUpdateAssetsView) {
                                isUpdateAssetsView = false;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFragment.updateAssetsVD(totalAssetsVD);
                                    }
                                });
                            }

                            if (isUpdateAllView) {
                                isUpdateAllView = false;
                                updateWalletViews.clear();
                                updatetokenViews.clear();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mFragment.updateAllWallet();
                                    }
                                });
                            } else {
                                List<Integer> _wallets = new ArrayList<>(updateWalletViews);
                                updateWalletViews.clear();
                                List<Integer> _tokens = new ArrayList<>(updatetokenViews);
                                updatetokenViews.clear();
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainWalletFragment fragment = findFragment();
                                        if (fragment != null)
                                            fragment.updateWallet(_wallets, _tokens);
                                    }
                                });
                            }

                        }
                    } while (!_loadCompleteAll);
                }
            }).start();
        }
    }

    private UIupdater uiUpdater = null;

    public boolean isAvailablleUIupdater() {
        if (uiUpdater == null) return false;

        synchronized (uiUpdater) {
            return !uiUpdater.loadCompleteAll && uiUpdater.mFragment != null;
        }
    }


    void internalUpdateAssetsView() {
        if (isAvailablleUIupdater())
            uiUpdater.setUpdateAssetsView(true);
        else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    findFragment().updateAssetsVD(totalAssetsVD);
                }
            });
        }
    }

    void internalUpdateAllView() {
        if (isAvailablleUIupdater())
            uiUpdater.setUpdateAllView(true);
        else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    findFragment().updateAllWallet();
                }
            });
        }
    }

    void internalUpdateEntryView(int wallet, int entry) {
        EntryViewData entryVD = walletVDs.get(wallet).getEntryVDs().get(entry);
        Integer intWallet = new Integer(wallet);
        Integer intToken = new Integer(entryVD.pos0);
        if (isAvailablleUIupdater()) {
            synchronized (uiUpdater) {
                if (!uiUpdater.updateWalletViews.contains(intWallet))
                    uiUpdater.updateWalletViews.add(intWallet);

                if (!uiUpdater.updatetokenViews.contains(intToken))
                    uiUpdater.updatetokenViews.add(intToken);
            }
        } else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    findFragment().updateWallet(
                            new ArrayList<Integer>() {{
                                add(intWallet);
                            }},
                            new ArrayList<Integer>() {{
                                add(intToken);
                            }}
                    );
                }
            });
        }

    }

    private void loadViewData() {
        walletVDs.clear();
        tokenListVDs.clear();
        totalAssetsVD = new TotalAssetsViewData();

        Map<String, List<EntryViewData>> mapTokenListEntries = new HashMap<>();
        for (Wallet wallet : ICONexApp.wallets) {
            TokenWalletItem.TokenColor tokenColor = new TokenWalletItem.TokenColor(); // token background color
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
            } else if (key.equals(Constants.KS_COINTYPE_ETH)) {
                ethWallet = tokenListVD;
            } else {
                tokenListVDs.add(tokenListVD);
            }
        }
        if (icxWallet != null) tokenListVDs.add(0, icxWallet);
        if (ethWallet != null)
            tokenListVDs.add(tokenListVDs.size() > 0 && icxWallet != null ? 1 : 0, ethWallet);

        for (int i = 0; tokenListVDs.size() > i; i++) {
            WalletViewData walletVD = tokenListVDs.get(i);
            for (EntryViewData entrVD : walletVD.getEntryVDs()) {
                entrVD.pos0 = i;
            }
        }

        findFragment().initWalletVDs(walletVDs, tokenListVDs);
        findFragment().updateAssetsVD(totalAssetsVD);
    }

    @Override
    public void patchViewData() {
        loadViewData();
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
        combineTopToken();
        patchingData = false;

        findFragment().updateAllWallet();
        findFragment().updateAssetsVD(totalAssetsVD);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (getIntent() != null) {
            boolean bundleDone = getIntent().getBooleanExtra("bundleDone", false);
            if (bundleDone) {
                MessageDialog messageDialog = new MessageDialog(this);
                messageDialog.setSingleButton(true);
                messageDialog.setMessage(getString(R.string.msgLoadBundle));
                messageDialog.show();
            }

            isFingerprintInvalidated = getIntent().getBooleanExtra(AuthActivity.EXTRA_INVALIDATED, false);
        }

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
        combineStake(entryVD, walletPosition);
    }

    @Override
    public void onLoadCompleteBalance() {
        if (isAvailablleUIupdater()) uiUpdater.setLoadCompleteBalance(true);
        combineTotalAssets();
    }

    @Override
    public void onLoadCompleteExchangeTable() {
        if (isAvailablleUIupdater()) uiUpdater.setLoadCompleteExchange(true);
        combineExchanges(-1, -1);
        combineTotalAssets();
    }

    @Override
    public void onLoadNextiScore(Wallet wallet, int walletPosition) {
        EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(0);
        try {
            BigInteger intIScore = wallet.getiScore();
            String strIScore = ConvertUtil.getValue(intIScore, 21);
            String deciIScore = DecimalFomatter.format(new BigDecimal(strIScore));
            entryVD.setTxtIScore(deciIScore);
        } catch (Exception e) {
            entryVD.setTxtIScore(MyConstants.NO_BALANCE);
        }
        entryVD.iscoreLoading = false;
        if (!patchingData) internalUpdateEntryView(walletPosition, 0);
    }

    @Override
    public void onLoadNextStake(Wallet wallet, int walletPosition, BigInteger unstake) {
        EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(0);
        entryVD.unstake = unstake;
        combineStake(entryVD, walletPosition);
    }

    @Override
    public void onLoadNextDelegation(Wallet wallet, int walletPosition) {
        EntryViewData entryVD = walletVDs.get(walletPosition).getEntryVDs().get(0);
        entryVD.prepsLoading = false;
        combineStake(entryVD, walletPosition);
    }

    private void combineStake(EntryViewData entryVD, int walletPosition) {
        Wallet wallet = entryVD.getWallet();
        WalletEntry entry = entryVD.getEntry();
        if (entryVD.amountLoading || entryVD.prepsLoading || entryVD.unstake == null) return;

        try {
            BigDecimal balance = new BigDecimal(ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec()));
            BigDecimal staked = new BigDecimal(ConvertUtil.getValue(wallet.getStaked(), 18));
            BigDecimal unstake = new BigDecimal(ConvertUtil.getValue(entryVD.unstake, 18));

            BigDecimal percent = BigDecimal.ZERO.setScale(1);
            if (balance.compareTo(BigDecimal.ZERO) != 0) {
                percent = staked.multiply(new BigDecimal(100))
                        .divide(balance.add(staked).add(unstake), 1, BigDecimal.ROUND_HALF_UP);
            }

            entryVD.setTxtStacked(DecimalFomatter.format(staked) + " (" + percent + "%)");

        } catch (Exception e) {
            entryVD.setTxtStacked("- ( - %)");
        }

        if (!patchingData) internalUpdateEntryView(walletPosition, 0);
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
        if (!patchingData) internalUpdateAllView();

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
                } catch (Exception e) {
                }
            }
        }

        if (totalStaked.compareTo(BigInteger.ZERO) != 0) {
            BigDecimal percent = new BigDecimal(totalStaked.subtract(totalVoting)).multiply(new BigDecimal(100))
                    .divide(new BigDecimal(totalStaked), 1, RoundingMode.HALF_UP);
            totalAssetsVD.setVotedPower(percent);
        } else {
            totalAssetsVD.setVotedPower(new BigDecimal("0.0"));
        }
        totalAssetsVD.existVotingPower = totalStaked.compareTo(BigInteger.ZERO) != 0;
        totalAssetsVD.loadingVotedpower = false;
        if (!patchingData) internalUpdateAssetsView();
    }

    @Override
    public void onLoadCompleteAll() {
        combineTopToken();
        if (isAvailablleUIupdater()) uiUpdater.setLoadCompleteAll(true);
        handler.post(new Runnable() {
            @Override
            public void run() {
                MainWalletFragment fragment = findFragment();
                if (fragment != null) fragment.notifyCompleteDataLoad();
            }
        });

    }

    @Override
    public void onNetworkError() {
        clearListening();
        startActivity(new Intent(this, NetworkErrorActivity.class));
    }

    private void combineTotalAssets() {
        if (!patchingData && isAvailablleUIupdater() &&
                (!uiUpdater.isLoadCompleteExchange() || !uiUpdater.isLoadCompleteBalance())) return;

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
        if (!patchingData) internalUpdateAssetsView();
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
                } catch (Exception e) {
                }
            }

            topTokenVD.setTxtAmount(totalBalance == null ? MyConstants.NO_BALANCE : DecimalFomatter.format(totalBalance));
            topTokenVD.amountLoading = false;

            BigDecimal exchanged = null;
            try {
                String exchangeKey = entryVDs.get(1).getEntry().getSymbol().toLowerCase() + currentUnit.toLowerCase();
                String strExchanger = ICONexApp.EXCHANGE_TABLE.get(exchangeKey);
                BigDecimal deciExchanger = new BigDecimal(strExchanger);
                exchanged = totalBalance.multiply(deciExchanger);
            } catch (Exception e) {
            }
            topTokenVD.setExchanged(exchanged, currentUnit);
            topTokenVD.exchageLoading = false;
        }
        if (!patchingData) internalUpdateAllView();
    }

    // -1, -1 -> totally refresh
    private void combineExchanges(int walletPosition, int entryPosition) {
        boolean isTotally = walletPosition == -1 || entryPosition == -1;

        if (patchingData || !isAvailablleUIupdater() || isAvailablleUIupdater() && uiUpdater.isLoadCompleteExchange()) {
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
                internalUpdateAllView();
            } else {
                internalUpdateEntryView(walletPosition, entryPosition);
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

    private MainWalletDataRequester requester = null;

    private void clearListening() {
        if (requester != null) {
            requester.setListener(null);
            requester = null;
        }

        if (isAvailablleUIupdater()) {
            uiUpdater.setFragment(null);
        }
        uiUpdater = null;
    }

    @Override
    public void refreshViewData() {
        clearListening();

        loadViewData();

        uiUpdater = new UIupdater();
        uiUpdater.setFragment(findFragment());
        uiUpdater.startListening();

        requester = new MainWalletDataRequester();
        requester.setListener(this);
        requester.requestAllData();
    }

    @Override
    public void changeExchangeUnit(String unit) {
        currentUnit = unit;
        combineExchanges(-1, -1);
        combineTopToken();
        combineTotalAssets();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFingerprintInvalidated) {
            MessageDialog dialog = new MessageDialog(this);
            dialog.setSingleButton(false);
            dialog.setConfirmButtonText(getString(R.string.yes));
            dialog.setCancelButtonText(getString(R.string.no));
            dialog.setMessage(getString(R.string.authMsgRecoverFingerprintAuth));
            dialog.setOnConfirmClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    startActivity(new Intent(MainWalletActivity.this, SettingLockActivity.class)
                            .putExtra(SettingLockActivity.ARG_TYPE, MyConstants.TypeLock.RECOVER));
                    isFingerprintInvalidated = false;
                    return true;
                }
            });
            dialog.setOnCancelClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    isFingerprintInvalidated = false;
                    return null;
                }
            });
            dialog.show();
        }
    }

    @Override
    public void fragmentResume() {
        refreshViewData();
    }

    @Override
    public void fragmentStop() {
        clearListening();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
                    }
                });
            }
            break;
        }
    }
}
