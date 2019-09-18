package foundation.icon.iconex.view.ui.prep.vote;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.wallet.Wallet;

public class VoteViewModel extends ViewModel {

    private MutableLiveData<Wallet> wallet;
    private MutableLiveData<List<Delegation>> delegations;
    private MutableLiveData<List<PRep>> preps;

    public MutableLiveData<Wallet> getWallet() {
        if (wallet == null)
            wallet = new MutableLiveData<>();

        return wallet;
    }

    public void setWallet(Wallet wallet) {
        getWallet().setValue(wallet);
    }

    public MutableLiveData<List<Delegation>> getDelegations() {
        if (delegations == null) {
            delegations = new MutableLiveData<>();
        }

        return delegations;
    }

    public void setDelegations(List<Delegation> delegations) {
        getDelegations().setValue(delegations);
    }

    public MutableLiveData<List<PRep>> getPreps() {
        if (preps == null)
            preps = new MutableLiveData<>();

        return preps;
    }

    public void setPreps(List<PRep> preps) {
        getPreps().setValue(preps);
    }
}
