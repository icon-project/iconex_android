package foundation.icon.iconex.wallet.load;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by js on 2018. 2. 26..
 */

public class LoadWalletViewPagerAdapter extends FragmentStatePagerAdapter {

    private final int NUM_PAGE = 3;
    private LOAD_TYPE TYPE;

    SelectKeyStoreFragment selectKeyStoreFragment;
    LoadInputWalletNameFragment loadInputWalletNameFragment;
    LoadInputPrivateKeyFragment inputPrivateKeyFragment;
    LoadInputWalletInfoFragment inputWalletInfoFragment;

    public LoadWalletViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public LoadWalletViewPagerAdapter(FragmentManager fm, LOAD_TYPE type) {
        super(fm);

        TYPE = type;
    }

    @Override
    public Fragment getItem(int position) {

        if (TYPE == null) {
            return LoadSelectMethodFragment.newInstance();
        }

        if (TYPE == LOAD_TYPE.KEYSTORE) {
            switch (position) {
                case 1:
                    selectKeyStoreFragment = SelectKeyStoreFragment.newInstance();
                    return selectKeyStoreFragment;
                case 2:
                    loadInputWalletNameFragment = LoadInputWalletNameFragment.newInstance();
                    return loadInputWalletNameFragment;
            }
        } else {
            switch (position) {
                case 1:
                    inputPrivateKeyFragment = LoadInputPrivateKeyFragment.newInstance();
                    return inputPrivateKeyFragment;
                case 2:
                    inputWalletInfoFragment = LoadInputWalletInfoFragment.newInstance();
                    return inputWalletInfoFragment;

            }
        }

        return LoadSelectMethodFragment.newInstance();
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }

    public void setKeyStore(String coinType, String keyStore) {
        loadInputWalletNameFragment.setKeyStore(coinType, keyStore);
    }

    public enum LOAD_TYPE {
        KEYSTORE,
        PRIVATE_KEY
    }
}
