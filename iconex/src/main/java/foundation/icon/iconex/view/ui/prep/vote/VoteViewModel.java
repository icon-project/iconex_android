package foundation.icon.iconex.view.ui.prep.vote;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.wallet.Wallet;

public class VoteViewModel extends ViewModel {

    private MutableLiveData<Wallet> wallet;

    private MutableLiveData<List<Delegation>> delegations;

    public MutableLiveData<Wallet> getWallet() {
        if (wallet == null)
            wallet = new MutableLiveData<>();

        return wallet;
    }

    public void setWallet(Wallet wallet) {
        getWallet().setValue(wallet);
    }

    public MutableLiveData<List<Delegation>> getDelegations() {
        if (delegations == null)
            delegations = new MutableLiveData<>();

        return delegations;
    }

    public void setDelegations(List<Delegation> delegations) {
        getDelegations().setValue(delegations);
    }
}
