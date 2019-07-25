package foundation.icon.iconex.menu.bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

/**
 * Created by js on 2018. 3. 23..
 */

public class BundleViewPagerAdapter extends FragmentStatePagerAdapter {

    private int NUM_PAGE = 2;

    public BundleViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            MakeBundleFragment bundleFragment = MakeBundleFragment.newInstance();
            return bundleFragment;
        } else {
            BundlePwdFragment pwdFragment = BundlePwdFragment.newInstance();
            return pwdFragment;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }
}
