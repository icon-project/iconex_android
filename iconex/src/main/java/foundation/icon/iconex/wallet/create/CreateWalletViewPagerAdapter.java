package foundation.icon.iconex.wallet.create;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import foundation.icon.iconex.wallet.create.CreateWalletStep1Fragment;
import foundation.icon.iconex.wallet.create.CreateWalletStep2Fragment;
import foundation.icon.iconex.wallet.create.CreateWalletStep3Fragment;
import foundation.icon.iconex.wallet.create.CreateWalletStep4Fragment;

/**
 * Created by js on 2018. 2. 22..
 */

public class CreateWalletViewPagerAdapter extends FragmentStatePagerAdapter {

    private final int NUM_PAGE = 4;

    private String keyStore;
    private String address;
    private String privKey;

    private CreateWalletStep1Fragment step1;
    private CreateWalletStep2Fragment step2;
    private CreateWalletStep3Fragment step3;
    private CreateWalletStep4Fragment step4;

    public CreateWalletViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                step1 = CreateWalletStep1Fragment.newInstance();
                return step1;
            case 1:
                step2 = CreateWalletStep2Fragment.newInstance();
                return step2;
            case 2:
                step3 = CreateWalletStep3Fragment.newInstance(this.keyStore);
                return step3;
            case 3:
                step4 = CreateWalletStep4Fragment.newInstance(this.address, this.privKey, false);
                return step4;
            default:
                return null;
        }
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
}
