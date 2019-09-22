package foundation.icon.iconex.view.ui.detailWallet;

import androidx.lifecycle.MutableLiveData;
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

public class WalletDetailViewModel extends ViewModel {

    // activity
    public final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
    public final MutableLiveData<WalletEntry> walletEntry = new MutableLiveData<>();
    public final MutableLiveData<Integer> entryId = new MutableLiveData<>();

    public final MutableLiveData<Map<String, WalletEntry>> indexedWalletEntry = new MutableLiveData<>();

    // service helper
    public final MutableLiveData<List<TransactionItemViewData>> lstTxData = new MutableLiveData<>();
    public final MutableLiveData<List<String[]>> lstBalanceResults = new MutableLiveData<>();

    // fragment
    public final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoadMore = new MutableLiveData<>();
    public final MutableLiveData<SelectType> selectType = new MutableLiveData<>();

    // actionbar
    public final MutableLiveData<String> name = new MutableLiveData<>();

    // info view
    public final MutableLiveData<List<String>> lstUnit = new MutableLiveData<>();
    public final MutableLiveData<BigDecimal> amount = new MutableLiveData<>();
    public final MutableLiveData<BigDecimal> exchange = new MutableLiveData<>();
    public final MutableLiveData<Map<Integer, BigDecimal>> exchanges = new MutableLiveData<>();
    public final MutableLiveData<String> unit = new MutableLiveData<>();

    // listview
    public final MutableLiveData<List<TransactionItemViewData>> lstItemData =  new MutableLiveData<>();

    public void initialize(Wallet wallet, WalletEntry walletEntry, int entryID) {
        this.wallet.setValue(wallet);
        this.walletEntry.setValue(walletEntry);
        this.entryId.setValue(entryID);

        indexedWalletEntry.setValue(new HashMap<>());
        lstTxData.setValue(new ArrayList<>());
        lstBalanceResults.setValue(new ArrayList<>());
        isRefreshing.setValue(false);
        isLoadMore.setValue(false);
//        selectType.setValue();

        this.name.setValue(wallet.getAlias());

        lstUnit.setValue(new ArrayList<String>() {{
            add(MyConstants.EXCHANGE_USD);
            add(MyConstants.EXCHANGE_BTC);
            add(MyConstants.EXCHANGE_ETH);
        }});

        amount.setValue(BigDecimal.ZERO);
        exchange.setValue(BigDecimal.ZERO);
        exchanges.setValue(new HashMap<>());

    }

}
