package foundation.icon.iconex.token.swap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import foundation.icon.iconex.wallet.create.CreateWalletStep2Fragment;
import foundation.icon.iconex.wallet.create.CreateWalletStep3Fragment;
import foundation.icon.iconex.wallet.create.CreateWalletStep4Fragment;

/**
 * Created by js on 2018. 5. 6..
 */

public class NoWalletSwapStepAdapter extends FragmentPagerAdapter {

    private final int NUM_PAGE = 5;

    public SwapGuideFragment step1;
    public CreateWalletStep2Fragment step2;
    public CreateWalletStep3Fragment step3;

    private String address;
    private String privKey;

    public NoWalletSwapStepAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                step1 = SwapGuideFragment.newInstance(TokenSwapActivity.TYPE_SWAP.NO_WALLET);
                return step1;
            case 1:
                step2 = CreateWalletStep2Fragment.newInstance();
                return step2;
            case 2:
                step3 = CreateWalletStep3Fragment.newInstance(null);
                return step3;
            case 3:
                return CreateWalletStep4Fragment.newInstance(this.address, this.privKey, true);

            case 4:
                return SwapRequestFragment.newInstance();
        }

        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }

    public void setKeyStore(String keyStore) {
        step3.setKeyStore(keyStore);
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPrivKey(String privKey) {
        this.privKey = privKey;
    }

    public void clearEdit() {
        step2.clearEdit();
    }

    public void setStep1a() {
        step1.setStep1a();
    }
}
