package foundation.icon.iconex.token.swap;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by js on 2018. 5. 17..
 */

public class SwapStepAdapter extends FragmentPagerAdapter {

    private final int NUM_PAGE = 2;

    public SwapStepAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return SwapGuideFragment.newInstance(TokenSwapActivity.TYPE_SWAP.EXIST);

            case 1:
                return SwapRequestFragment.newInstance();
        }

        return null;
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }
}
