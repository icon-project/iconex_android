package foundation.icon.iconex.view.ui.create;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import foundation.icon.MyConstants;
import foundation.icon.iconex.wallet.Wallet;

public class CreateWalletViewModel extends ViewModel {

    private MutableLiveData<MyConstants.Coin> coinType;
    private MutableLiveData<String> privateKey;
    private MutableLiveData<Wallet> wallet;

    public MutableLiveData<MyConstants.Coin> getCoin() {
        if (coinType == null)
            coinType = new MutableLiveData<>();

        return coinType;
    }

    public void setCoinType(MyConstants.Coin coin) {
        getCoin().setValue(coin);
    }

    public MutableLiveData<String> getPrivateKey() {
        if (privateKey == null)
            privateKey = new MutableLiveData<>();

        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        getPrivateKey().setValue(privateKey);
    }

    public MutableLiveData<Wallet> getWallet() {
        if (wallet == null)
            wallet = new MutableLiveData<>();

        return wallet;
    }

    public void setWallet(Wallet wallet) {
        getWallet().setValue(wallet);
    }
}
