package foundation.icon.iconex.wallet.create;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.HashMap;

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
                if (step1 == null)
                    step1 = CreateWalletStep1Fragment.newInstance();
                return step1;
            case 1:
                if (step2 == null)
                    step2 = CreateWalletStep2Fragment.newInstance();
                return step2;
            case 2:
                if (step3 == null)
                    step3 = CreateWalletStep3Fragment.newInstance(this.keyStore);
                return step3;
            case 3:
                if (step4 == null)
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

    public HashMap<String, Fragment> getFragments() {
        HashMap<String, Fragment> map = new HashMap<>();

        if (step1 != null)
            map.put("step1", step1);

        if (step2 != null)
            map.put("step2", step2);

        if (step3 != null)
            map.put("step3", step3);

        if (step4 != null)
            map.put("step4", step4);

        return map;
    }

    public void setFragments(HashMap<String, Fragment> map) {
        if (map.containsKey("step1"))
            step1 = (CreateWalletStep1Fragment) map.get("step1");

        if (map.containsKey("step2"))
            step2 = (CreateWalletStep2Fragment) map.get("step2");

        if (map.containsKey("step3"))
            step3 = (CreateWalletStep3Fragment) map.get("step3");

        if (map.containsKey("step4"))
            step4 = (CreateWalletStep4Fragment) map.get("step4");
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
