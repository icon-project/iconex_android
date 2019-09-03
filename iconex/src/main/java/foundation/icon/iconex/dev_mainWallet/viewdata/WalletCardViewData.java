package foundation.icon.iconex.dev_mainWallet.viewdata;

import java.util.ArrayList;
import java.util.List;

public class WalletCardViewData {
    public enum WalletType { ICXwallet, ETHwallet, TokenList }

    private WalletType walletType;
    private String title;
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

    public List<WalletItemViewData> getLstWallet() {
        return lstWallet;
    }

    public WalletCardViewData setLstWallet(List<WalletItemViewData> lstWallet) {
        this.lstWallet = lstWallet;
        return this;
    }
}
