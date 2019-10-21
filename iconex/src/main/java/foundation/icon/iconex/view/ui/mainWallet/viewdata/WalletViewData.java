package foundation.icon.iconex.view.ui.mainWallet.viewdata;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import loopchain.icon.wallet.core.Constants;

public class WalletViewData {
    private static String TAG = EntryViewData.class.getSimpleName();

    private Wallet wallet = null;
    private String title;

    private EntryViewData topToken = null;
    private List<EntryViewData> lstEntryViewData = new ArrayList<>();

    public Wallet getWallet() {
        return wallet;
    }

    public String getTitle() {
        return title;
    }

    public List<EntryViewData> getEntryVDs() {
        return lstEntryViewData;
    }

    public WalletViewData(Wallet wallet, List<EntryViewData> lstEntryViewData) {
        this.wallet = wallet;
        this.lstEntryViewData = lstEntryViewData;

        if (wallet != null) {
            title = wallet.getAlias();
        } else {
            EntryViewData entryVD = lstEntryViewData.get(0);
            WalletEntry entry = entryVD.getEntry();
            title = entry.getName();

            topToken = new EntryViewData(entry.getName(), entry.getSymbol());
            topToken.setBgSymbolColor(entryVD.getBgSymbolColor());

            if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                if (entryVD.getWallet().getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                    topToken.setDrawableSymbolresId(R.drawable.img_logo_icon_sel);
                } else {
                    topToken.setDrawableSymbolresId(R.drawable.img_logo_ethereum_nor);
                }
            }

            lstEntryViewData.add(0, topToken);
        }

    }
}
