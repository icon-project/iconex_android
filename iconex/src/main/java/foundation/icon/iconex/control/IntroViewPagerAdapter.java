package foundation.icon.iconex.control;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import foundation.icon.iconex.view.ui.intro.Intro1Fragment;
import foundation.icon.iconex.view.ui.intro.Intro2Fragment;

/**
 * Created by js on 2018. 2. 19..
 */

public class IntroViewPagerAdapter extends FragmentStatePagerAdapter {

    private final int NUM_PAGE = 2;

    public IntroViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                Intro1Fragment intro1 = Intro1Fragment.newInstance();
                return intro1;
            case 1:
                Intro2Fragment intro2 = Intro2Fragment.newInstance();
                return intro2;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }
}
