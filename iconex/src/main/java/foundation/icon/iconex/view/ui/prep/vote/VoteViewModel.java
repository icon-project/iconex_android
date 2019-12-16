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
    private MutableLiveData<BigInteger> total;
    private MutableLiveData<BigInteger> voted;
    private MutableLiveData<BigInteger> votingPower;

    private MutableLiveData<List<PRep>> preps;
    private MutableLiveData<BigInteger> prepTotalDelegated;

    private MutableLiveData<BigInteger> stepLimit, stepPrice, fee;

    public MutableLiveData<Wallet> getWallet() {
        if (wallet == null)
            wallet = new MutableLiveData<>();

        return wallet;
    }

    public void setWallet(Wallet wallet) {
        getWallet().setValue(wallet);
    }

    public MutableLiveData<BigInteger> getTotal() {
        if (total == null) {
            total = new MutableLiveData<>();
            total.setValue(BigInteger.ZERO);
        }

        return total;
    }

    public void setTotal(BigInteger total) {
        getTotal().setValue(total);
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

    public MutableLiveData<BigInteger> getStepPrice() {
        if (stepPrice == null) {
            stepPrice = new MutableLiveData<>();
            stepPrice.setValue(BigInteger.ZERO);
        }

        return stepPrice;
    }

    public void setStepPrice(BigInteger stepPrice) {
        getStepPrice().setValue(stepPrice);
    }

    public MutableLiveData<BigInteger> getStepLimit() {
        if (stepLimit == null) {
            stepLimit = new MutableLiveData<>();
            stepLimit.setValue(BigInteger.ZERO);
        }

        return stepLimit;
    }

    public void setStepLimit(BigInteger stepLimit) {
        getStepLimit().setValue(stepLimit);
    }

    public MutableLiveData<BigInteger> getFee() {
        if (fee == null) {
            fee = new MutableLiveData<>();
            fee.setValue(BigInteger.ZERO);
        }

        return fee;
    }

    public void setFee(BigInteger fee) {
        getFee().setValue(fee);
    }
}
