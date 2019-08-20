package foundation.icon.iconex.view.ui.load;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.HashMap;

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
                    if (selectKeyStoreFragment == null)
                        selectKeyStoreFragment = SelectKeyStoreFragment.newInstance();
                    return selectKeyStoreFragment;
                case 2:
                    if (loadInputWalletNameFragment == null)
                        loadInputWalletNameFragment = LoadInputWalletNameFragment.newInstance();
                    return loadInputWalletNameFragment;
            }
        } else {
            switch (position) {
                case 1:
                    if (inputPrivateKeyFragment == null)
                        inputPrivateKeyFragment = LoadInputPrivateKeyFragment.newInstance();
                    return inputPrivateKeyFragment;
                case 2:
                    if (inputWalletInfoFragment == null)
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

    public HashMap<String, Fragment> getFragments() {
        HashMap<String, Fragment> fragments = new HashMap<>();
        if (selectKeyStoreFragment != null)
            fragments.put("keystore", selectKeyStoreFragment);

        if (loadInputWalletNameFragment != null)
            fragments.put("name", loadInputWalletNameFragment);

        if (inputPrivateKeyFragment != null)
            fragments.put("private", inputPrivateKeyFragment);

        if (inputWalletInfoFragment != null)
            fragments.put("info", inputWalletInfoFragment);

        return fragments;
    }

    public void setFragments(HashMap<String, Fragment> fragments) {
        if (fragments.containsKey("keystore"))
            selectKeyStoreFragment = (SelectKeyStoreFragment) fragments.get("keystore");

        if (fragments.containsKey("name"))
            loadInputWalletNameFragment = (LoadInputWalletNameFragment) fragments.get("name");

        if (fragments.containsKey("private"))
            inputPrivateKeyFragment = (LoadInputPrivateKeyFragment) fragments.get("private");

        if (fragments.containsKey("info"))
            inputWalletInfoFragment = (LoadInputWalletInfoFragment) fragments.get("info");
    }

    public void setKeyStore(String coinType, String keyStore) {
        loadInputWalletNameFragment.setKeyStore(coinType, keyStore);
    }

    public void clearInfo() {
        inputWalletInfoFragment.clear();
    }

    public enum LOAD_TYPE {
        KEYSTORE,
        PRIVATE_KEY
    }
}
