package foundation.icon.iconex.view.ui.prep.stake;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.math.BigInteger;

import foundation.icon.iconex.wallet.Wallet;

public class StakeViewModel extends ViewModel {

    private MutableLiveData<Wallet> wallet;
    private MutableLiveData<String> privateKey;
    private MutableLiveData<BigInteger> total, staked, unstake, unstaked, blockHeight, remainingBlock;
    private MutableLiveData<BigInteger> delegation;

    private MutableLiveData<BigInteger> stepLimit, stepPrice;

    private MutableLiveData<Integer> isEdit;

    public MutableLiveData<Wallet> getWallet() {
        if (wallet == null)
            wallet = new MutableLiveData<>();

        return wallet;
    }

    public void setWallet(Wallet wallet) {
        getWallet().setValue(wallet);
    }

    public MutableLiveData<String> getPrivateKey() {
        if (privateKey == null)
            privateKey = new MutableLiveData<>();

        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        getPrivateKey().setValue(privateKey);
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

    public MutableLiveData<BigInteger> getStaked() {
        if (staked == null) {
            staked = new MutableLiveData<>();
            staked.setValue(BigInteger.ZERO);
        }

        return staked;
    }

    public void setStaked(BigInteger staked) {
        getStaked().setValue(staked);
    }

    public MutableLiveData<BigInteger> getUnstake() {
        if (unstake == null) {
            unstake = new MutableLiveData<>();
            unstake.setValue(BigInteger.ZERO);
        }

        return unstake;
    }

    public void setUnstake(BigInteger unstake) {
        getUnstake().setValue(unstake);
    }

    public MutableLiveData<BigInteger> getUnstaked() {
        if (unstaked == null) {
            unstaked = new MutableLiveData<>();
            unstaked.setValue(BigInteger.ZERO);
        }

        return unstaked;
    }

    public void setUnstaked(BigInteger unstaked) {
        getUnstaked().setValue(unstaked);
    }

    public MutableLiveData<BigInteger> getBlockHeight() {
        if (blockHeight == null) {
            blockHeight = new MutableLiveData<>();
            blockHeight.setValue(BigInteger.ZERO);
        }

        return blockHeight;
    }

    public void setBlockHeight(BigInteger blockHeight) {
        getBlockHeight().setValue(blockHeight);
    }

    public MutableLiveData<BigInteger> getRemainingBlock() {
        if (remainingBlock == null) {
            remainingBlock = new MutableLiveData<>();
            remainingBlock.setValue(BigInteger.ZERO);
        }

        return remainingBlock;
    }

    public void setRemainingBlock(BigInteger remainingBlock) {
        getRemainingBlock().setValue(remainingBlock);
    }

    public MutableLiveData<BigInteger> getDelegation() {
        if (delegation == null) {
            delegation = new MutableLiveData<>();
            delegation.setValue(BigInteger.ZERO);
        }

        return delegation;
    }

    public void setDelegation(BigInteger delegation) {
        getDelegation().setValue(delegation);
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

    public MutableLiveData<Integer> isEdit() {
        if (isEdit == null) {
            isEdit = new MutableLiveData<>();
            isEdit.setValue(0);
        }

        return isEdit;
    }

    public void isEdit(int isEdit) {
        isEdit().setValue(isEdit);
    }
}
