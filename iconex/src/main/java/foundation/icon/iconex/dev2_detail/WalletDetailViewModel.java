package foundation.icon.iconex.dev2_detail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.dev2_detail.component.TransactionItemView;
import foundation.icon.iconex.dev2_detail.component.TransactionItemViewData;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class WalletDetailViewModel extends ViewModel {

    // activity
    public final MutableLiveData<Wallet> wallet = new MutableLiveData<>();
    public final MutableLiveData<WalletEntry> walletEntry = new MutableLiveData<>();
    public final MutableLiveData<Integer> entryId = new MutableLiveData<>();

    // service helper
    public final MutableLiveData<List<TransactionItemViewData>> lstTxData = new MutableLiveData<>();

    // fragment
    public final MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>();
    public final MutableLiveData<Boolean> isLoadMore = new MutableLiveData<>();

    // actionbar
    public final MutableLiveData<String> name = new MutableLiveData<>();

    // info view
    public final MutableLiveData<List<String>> lstUnit = new MutableLiveData<>();
    public final MutableLiveData<BigDecimal> amount = new MutableLiveData<>();
    public final MutableLiveData<BigDecimal> exchange = new MutableLiveData<>();

    // listview
    public final MutableLiveData<List<TransactionItemViewData>> lstItemData =  new MutableLiveData<>();


    public void initialize(Wallet wallet, WalletEntry walletEntry, int entryID) {
        this.wallet.setValue(wallet);
        this.walletEntry.setValue(walletEntry);
        this.entryId.setValue(entryID);

        this.name.setValue(wallet.getAlias());

        lstUnit.setValue(new ArrayList<String>() {{
            add(MyConstants.EXCHANGE_USD);
            add(MyConstants.EXCHANGE_BTC);
            add(MyConstants.EXCHANGE_ETH);
        }});
    }

}
