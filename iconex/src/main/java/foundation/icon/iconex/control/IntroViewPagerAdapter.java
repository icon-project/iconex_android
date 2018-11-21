package foundation.icon.iconex.control;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import foundation.icon.iconex.intro.Intro1Fragment;
import foundation.icon.iconex.intro.Intro2Fragment;

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
