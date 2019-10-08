package foundation.icon.iconex.view.ui.prep.vote;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.wallet.Wallet;

public class VoteViewModel extends ViewModel {

    private MutableLiveData<Wallet> wallet;

    private MutableLiveData<List<Delegation>> delegations;
    private MutableLiveData<BigInteger> voted;
    private MutableLiveData<BigInteger> votingPower;

    private MutableLiveData<List<PRep>> preps;
    private MutableLiveData<BigInteger> prepTotalDelegated;

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
            delegations.setValue(new ArrayList<>());
        }

        return delegations;
    }

    public void setDelegations(List<Delegation> delegations) {
        getDelegations().setValue(delegations);
    }

    public MutableLiveData<BigInteger> getVoted() {
        if (voted == null)
            voted = new MutableLiveData<>();

        return voted;
    }

    public void setVoted(BigInteger voted) {
        getVoted().setValue(voted);
    }

    public MutableLiveData<BigInteger> getVotingPower() {
        if (votingPower == null)
            votingPower = new MutableLiveData<>();

        return votingPower;
    }

    public void setVotingPower(BigInteger votingPower) {
        getVotingPower().setValue(votingPower);
    }

    public MutableLiveData<List<PRep>> getPreps() {
        if (preps == null) {
            preps = new MutableLiveData<>();
            preps.setValue(new ArrayList<>());
        }

        return preps;
    }

    public void setPreps(List<PRep> preps) {
        getPreps().setValue(preps);
    }

    public MutableLiveData<BigInteger> getPrepTotalDelegated() {
        if (prepTotalDelegated == null)
            prepTotalDelegated = new MutableLiveData<>();

        return prepTotalDelegated;
    }

    public void setPrepTotalDelegated(BigInteger totalDelegated) {
        getPrepTotalDelegated().setValue(totalDelegated);
    }
}
