package foundation.icon.iconex.view.ui.mainWallet.viewdata;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.view.ui.mainWallet.items.TokenWalletItem;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class WalletCardViewData {
    private static String TAG = WalletItemViewData.class.getSimpleName();
    public enum WalletType { ICXwallet, ETHwallet, TokenList }

    private WalletType walletType;
    private String title;
    private String address;
    private List<WalletItemViewData> lstWallet = new ArrayList<>();

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

    public String getAddress() {
        return address;
    }

    public WalletCardViewData setAddress(String address) {
        this.address = address;
        return this;
    }

    public List<WalletItemViewData> getLstWallet() {
        return lstWallet;
    }

    public WalletCardViewData setLstWallet(List<WalletItemViewData> lstWallet) {
        this.lstWallet = lstWallet;
        return this;
    }

    public static WalletCardViewData convertWallet2ViewData(Wallet wallet) {
        // create
        WalletCardViewData walletCardViewData = new WalletCardViewData();

        // set aliase
        walletCardViewData.setTitle(wallet.getAlias());

        // set address
        walletCardViewData.setAddress(wallet.getAddress());

        // set Type
        WalletType walletType = null;
        if ("ICX".equals(wallet.getCoinType().toUpperCase())) {
            walletType = WalletType.ICXwallet;
        } else if ("ETH".equals(wallet.getCoinType().toUpperCase())) {
            walletType = WalletType.ETHwallet;
        } else {
            Log.e(TAG, "unknown wallet type " + wallet.getCoinType());
        }
        walletCardViewData.setWalletType(walletType);

        // set wallet entry
        List<WalletItemViewData> walletItemViewDataList = new ArrayList<>();
        TokenWalletItem.TokenColor tokenColor = new TokenWalletItem.TokenColor(); // token background color
        for (WalletEntry walletEntry : wallet.getWalletEntries()) {
            WalletItemViewData itemViewData = WalletItemViewData.convertWalletEntry2ViewItem(walletEntry);

            // if token, set background token color
            if (itemViewData.getWalletItemType() == WalletItemViewData.WalletItemType.Token) {
                itemViewData.setBgSymbolColor(tokenColor.getColor());
                tokenColor.nextColor();
            }

            walletItemViewDataList.add(itemViewData);
        }
        walletCardViewData.setLstWallet(walletItemViewDataList);

        return walletCardViewData;
    }
}
