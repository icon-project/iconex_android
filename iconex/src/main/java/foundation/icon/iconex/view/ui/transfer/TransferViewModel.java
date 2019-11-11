package foundation.icon.iconex.view.ui.transfer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import foundation.icon.iconex.wallet.Wallet;

public class TransferViewModel extends ViewModel {

    private MutableLiveData<Wallet> wallet;

    public MutableLiveData<Wallet> getWallet() {
        if (wallet == null) {
            wallet = new MutableLiveData<>();
        }

        return wallet;
    }

    public void setWallet(Wallet wallet) {
        getWallet().setValue(wallet);
    }
}
