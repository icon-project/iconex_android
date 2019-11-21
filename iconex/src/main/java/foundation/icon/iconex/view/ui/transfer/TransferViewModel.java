package foundation.icon.iconex.view.ui.transfer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class TransferViewModel extends ViewModel {

    private MutableLiveData<Wallet> wallet;
    private MutableLiveData<WalletEntry> entry;
    private MutableLiveData<String> privateKey;

    public MutableLiveData<Wallet> getWallet() {
        if (wallet == null) {
            wallet = new MutableLiveData<>();
        }

        return wallet;
    }

    public void setWallet(Wallet wallet) {
        getWallet().setValue(wallet);
    }

    public MutableLiveData<WalletEntry> getEntry() {
        if (entry == null) {
            entry = new MutableLiveData<>();
        }

        return entry;
    }

    public void setEntry(WalletEntry entry) {
        getEntry().setValue(entry);
    }

    public MutableLiveData<String> getPrivateKey() {
        if (privateKey == null)
            privateKey = new MutableLiveData<>();

        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        getPrivateKey().setValue(privateKey);
    }
}
