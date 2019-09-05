package foundation.icon.iconex.dev_mainWallet;

import android.os.Bundle;
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

public class MainWalletActivity extends AppCompatActivity implements MainWalletFragment.SyncRequester {

    private static String MAIN_WALLET_FRAGMENT_TAG = "main wallet fragment";

    private TotalAssetsViewData totalAssetsViewData = null;
    private WalletCardViewData mainWalletCardViewData = null;


    private List<WalletCardViewData> chachedWalletList = null;
    private List<WalletCardViewData> chachedTokenList = null;

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
                .add(R.id.container, fragment,MAIN_WALLET_FRAGMENT_TAG)
                .commit();
    }

    private List<WalletCardViewData> convertWallets2ViewData() {
        if (chachedWalletList == null) {
            List<WalletCardViewData> walletViewDatas = new ArrayList<>();
            for (Wallet wallet : ICONexApp.wallets) {
                walletViewDatas.add(WalletCardViewData.convertWallet2ViewData(wallet));
            }
            chachedWalletList = walletViewDatas;
        }

        return chachedWalletList;
    }

    private List<WalletCardViewData> convertWallets2TokenViewData() {
        if (chachedTokenList == null) {
            List<WalletCardViewData> lstwalletViewData = convertWallets2ViewData();
            Map<String, WalletCardViewData> mapTokenViewData = new HashMap<>();

            for (WalletCardViewData walletViewData: lstwalletViewData) {
                for (WalletItemViewData itemViewData: walletViewData.getLstWallet()) {
                    String tokenName = itemViewData.getName();

                    if(!mapTokenViewData.containsKey(tokenName)) {
                        mapTokenViewData.put(tokenName,
                                new WalletCardViewData()
                                        .setWalletType(WalletCardViewData.WalletType.TokenList)
                                        .setTitle(tokenName)
                                        .setLstWallet(new ArrayList<WalletItemViewData>() {{
                                            add(itemViewData);
                                        }})
                        );
                    }

                    WalletCardViewData lstTokenViewData = mapTokenViewData.get(tokenName);
                    lstTokenViewData.getLstWallet().add(
                            new WalletItemViewData()
                                    .setWalletItemType(WalletItemViewData.WalletItemType.Wallet)
                                    .setSymbol(walletViewData.getTitle())
                                    //.setName() TODO: 앗 주소 빠졌다.
                                    .setAmount("0.00")
                                    .setExchanged("0.00 USD")
                    );
                }
            }
            chachedTokenList = new ArrayList<WalletCardViewData> () {{ addAll(mapTokenViewData.values()); }};
        }

        return chachedTokenList;
    }

    @Override //
    public void onAsyncRequestTotalAssetsData() {
        getMainWalletFragment().asyncResponseTotalAssetsData(
                new TotalAssetsViewData()
                .setTotalAsset(new BigInteger("20000000"))
                .setVotedPower(0.99f));
    }

    @Override
    public void onAsyncRequestWalletListData() {
        getMainWalletFragment()
                .asyncResponseWalletListData(convertWallets2ViewData());
    }

    @Override
    public void onAsyncRequestTokenListData() {
        getMainWalletFragment()
                .asyncResponseTokenListData(convertWallets2TokenViewData());
    }

    private MainWalletFragment getMainWalletFragment() {
        return ((MainWalletFragment) getSupportFragmentManager().findFragmentByTag(MAIN_WALLET_FRAGMENT_TAG));
    }
}