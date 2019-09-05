package foundation.icon.iconex.dev_mainWallet.viewdata;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.items.TokenWalletItem;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class WalletCardViewData {
    private static String TAG = WalletItemViewData.class.getSimpleName();
    public enum WalletType { ICXwallet, ETHwallet, TokenList }

    private WalletType walletType;
    private String title;
    private List<WalletItemViewData> lstWallet = new ArrayList<>();

    public static WalletCardViewData convertWallet2ViewData(Wallet wallet) {
        WalletCardViewData walletCardViewData = new WalletCardViewData();
        walletCardViewData.setTitle(wallet.getAlias());

        // Wallet Type check
        WalletType walletType = null;
        if ("ICX".equals(wallet.getCoinType().toUpperCase())) {
            walletType = WalletType.ICXwallet;
        } else if ("ETH".equals(wallet.getCoinType().toUpperCase())) {
            walletType = WalletType.ETHwallet;
        } else {
            Log.e(TAG, "unknown wallet type " + wallet.getCoinType());
        }
        walletCardViewData.setWalletType(walletType);

        List<WalletItemViewData> walletItemViewDataList = new ArrayList<>();
        TokenWalletItem.TokenColor tokenColor = new TokenWalletItem.TokenColor();
        for (WalletEntry walletEntry : wallet.getWalletEntries()) {
            WalletItemViewData itemViewData = WalletItemViewData.convertWalletEntry2ViewItem(walletEntry);

            // if token then init symbol background
            if (itemViewData.getWalletItemType() == WalletItemViewData.WalletItemType.Token) {
                itemViewData.setBgSymbolColor(tokenColor.getColor());
                tokenColor.nextColor();
            }

            walletItemViewDataList.add(itemViewData);
        }
        walletCardViewData.setLstWallet(walletItemViewDataList);

        return walletCardViewData;
    }

    public WalletType getWalletType() {
        return walletType;
    }

    public WalletCardViewData setWalletType(WalletType walletType) {
        this.walletType = walletType;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public WalletCardViewData setTitle(String title) {
        this.title = title;
        return this;
    }

    public List<WalletItemViewData> getLstWallet() {
        return lstWallet;
    }

    public WalletCardViewData setLstWallet(List<WalletItemViewData> lstWallet) {
        this.lstWallet = lstWallet;
        return this;
    }
}
