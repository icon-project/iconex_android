package foundation.icon.iconex.wallet.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.List;

import foundation.icon.iconex.wallet.Wallet;

/**
 * Created by js on 2018. 3. 29..
 */

public class CoinViewPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = CoinViewPagerAdapter.class.getSimpleName();

    private List<String> mCoins;
    private HashMap<String, List<Wallet>> mCoinsMap;
    private List<CoinsViewItem> mList;
    public SparseArray<CoinFragment> fragments;

    public CoinViewPagerAdapter(FragmentManager fm, List<CoinsViewItem> list) {
        super(fm);

        mList = list;
        fragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        CoinFragment fragment = CoinFragment.newInstance(mList.get(position));
        fragments.append(position, fragment);

        return fragment;
    }

    @Override
    public int getItemPosition(Object object) {
        CoinFragment fragment = (CoinFragment) object;
        int position = fragments.indexOfValue(fragment);

        if (position >= 0) {
            fragment.coinsNotifyDataChanged();
            return position;
        } else {
            return POSITION_NONE;
        }
    }

    @Override
    public int getCount() {
        return mList.size();
    }
}
