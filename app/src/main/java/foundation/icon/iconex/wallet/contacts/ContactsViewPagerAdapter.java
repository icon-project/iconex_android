package foundation.icon.iconex.wallet.contacts;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 3. 19..
 */

public class ContactsViewPagerAdapter extends FragmentStatePagerAdapter {

    private final int PAGE_COUNT = 3;

    private final int CONTACTS = 0;
    private final int RECENT = 1;
    private final int MY_WALLET = 2;
    private String tabTitles[];
    private Context mContext;
    private String mCoinType;
    private String mAddress;
    private boolean mEditable;

    public ContactsViewPagerAdapter(Context context, FragmentManager fm, String address, String coinType, boolean editable) {
        super(fm);

        mContext = context;
        tabTitles = new String[]{mContext.getString(R.string.contacts),
                mContext.getString(R.string.contactsRecent), mContext.getString(R.string.contactsWallet)};
        mCoinType = coinType;
        mAddress = address;
        mEditable = false;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == CONTACTS) {
            MyContactsFragment myContactsFragment = MyContactsFragment.newInstance(mCoinType, mEditable);
            return myContactsFragment;
        } else if (position == RECENT) {
            ContactsFragment recent = ContactsFragment.newInstance(mCoinType, mAddress, BasicContactsAdapter.TYPE_RECENT);
            return recent;
        } else {
            ContactsFragment wallet = ContactsFragment.newInstance(mCoinType, mAddress, BasicContactsAdapter.TYPE_WALLET);
            return wallet;
        }
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    public void setEditable(boolean editable) {
        mEditable = editable;
        notifyDataSetChanged();
    }
}
