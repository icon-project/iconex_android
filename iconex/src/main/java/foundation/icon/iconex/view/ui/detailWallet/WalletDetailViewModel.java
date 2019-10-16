package foundation.icon.iconex.view.ui.detailWallet;

import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import foundation.icon.MyConstants;
import foundation.icon.iconex.view.ui.detailWallet.component.SelectType;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionItemViewData;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import loopchain.icon.wallet.core.Constants;

public class WalletDetailViewModel extends ViewModel {

    // activity
    public final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
    public final MutableLiveData<WalletEntry> walletEntry = new MutableLiveData<>();

    public final MutableLiveData<Map<String, WalletEntry>> indexedWalletEntry = new MutableLiveData<>();

    // service helper
    public final MutableLiveData<List<TransactionItemViewData>> lstTxData = new MutableLiveData<>();
    public final MutableLiveData<List<String[]>> lstBalanceResults = new MutableLiveData<>();

    // fragment
    public final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoadMore = new MutableLiveData<>();
    public final MutableLiveData<SelectType> selectType = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isNoLoadMore = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loadingBalance = new MutableLiveData<>();

    // actionbar
    public final MutableLiveData<String> name = new MutableLiveData<>();

    // info view
    public final MediatorLiveData<List<String>> lstUnit = new MediatorLiveData<>();
    public final MutableLiveData<BigDecimal> amount = new MutableLiveData<>();
    public final MutableLiveData<BigDecimal> exchange = new MutableLiveData<>();
    public final MutableLiveData<Map<Integer, BigDecimal>> exchanges = new MutableLiveData<>();
    public final MutableLiveData<String> unit = new MutableLiveData<>();

    // listview
    public final MutableLiveData<List<TransactionItemViewData>> lstItemData =  new MutableLiveData<>();

    public void initialize(Wallet wallet, WalletEntry walletEntry, int entryID) {
        this.wallet.setValue(wallet);
        this.walletEntry.setValue(walletEntry);

        indexedWalletEntry.setValue(new HashMap<>());
        lstTxData.setValue(new ArrayList<>());
        lstBalanceResults.setValue(new ArrayList<>());
        isRefreshing.setValue(false);
        isLoadMore.setValue(false);
//        selectType.setValue();

        this.name.setValue(wallet.getAlias());
        lstUnit.addSource(this.wallet, new Observer<Wallet>() {
            @Override
            public void onChanged(Wallet wallet) {
                combineLstUnit();
            }
        });
        lstUnit.addSource(this.walletEntry, new Observer<WalletEntry>() {
            @Override
            public void onChanged(WalletEntry entry) {
                combineLstUnit();
            }
        });

        amount.setValue(BigDecimal.ZERO);
        exchange.setValue(BigDecimal.ZERO);
        exchanges.setValue(new HashMap<>());
    }

    private void combineLstUnit() {
        Wallet wallet = this.wallet.getValue();
        WalletEntry entry = this.walletEntry.getValue();

        // event filtering
        if (wallet == null || entry == null) return;

        boolean exist = false;
        for (WalletEntry findEntry : wallet.getWalletEntries()) {
            if (findEntry.getId() == entry.getId()) {
                exist = true;
                break;
            }
        }
        if (!exist) return;

        lstUnit.setValue(new ArrayList<String>() {{
            if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                    add("USD");
                    add("BTC");
                    add("ETH");
                } else {
                    add("USD");
                    add("BTC");
                    add("ICX");
                }
            } else {
                if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                    add("USD");
                    add("BTC");
                    add("ICX");
                } else {
                    add("USD");
                    add("BTC");
                    add("ETH");
                }
            }
        }});
    }
}
