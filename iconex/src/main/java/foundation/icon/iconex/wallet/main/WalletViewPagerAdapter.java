package foundation.icon.iconex.wallet.main;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;

import foundation.icon.ICONexApp;

/**
 * Created by js on 2018. 3. 6..
 */

public class WalletViewPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = WalletViewPagerAdapter.class.getSimpleName();

    private int NUM_PAGE = ICONexApp.wallets.size();
    public SparseArray<WalletFragment> fragments;


    public WalletViewPagerAdapter(FragmentManager fm) {
        super(fm);
        fragments = new SparseArray<>();

        for (int i = 0; i < ICONexApp.wallets.size(); i++)
            fragments.put(i, WalletFragment.newInstance(ICONexApp.wallets.get(i)));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
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
