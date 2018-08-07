package foundation.icon.iconex.wallet.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.util.SparseArray;

import foundation.icon.iconex.ICONexApp;

/**
 * Created by js on 2018. 3. 6..
 */

public class WalletViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = WalletViewPagerAdapter.class.getSimpleName();

    private int NUM_PAGE = ICONexApp.mWallets.size();
    public SparseArray<WalletFragment> fragments;


    public WalletViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        WalletFragment walletFragment = WalletFragment.newInstance(ICONexApp.mWallets.get(position));
        fragments.put(position, walletFragment);

        return walletFragment;
    }

    @Override
    public int getCount() {
        return NUM_PAGE;
    }

    public String getName() {
        return WalletViewPagerAdapter.class.getSimpleName();
    }

    @Override
    public int getItemPosition(Object object) {
        WalletFragment fragment = (WalletFragment) object;
        int position = fragments.indexOfValue(fragment);

        if (position >= 0) {
            fragment.walletNotifyDataChanged();
            return position;
        } else
            return POSITION_NONE;
    }
}
