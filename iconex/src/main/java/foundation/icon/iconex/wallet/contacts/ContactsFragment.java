package foundation.icon.iconex.wallet.contacts;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.wallet.Wallet;
import loopchain.icon.wallet.core.Constants;

public class ContactsFragment extends Fragment {

    private static final String TAG = ContactsFragment.class.getSimpleName();

    private static final String ARG_COIN_TYPE = "ARG_COIN_TYPE";
    private static final String ARG_TYPE = "ARG_TYPE";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";

    private String mCoinType;
    private String mAddress;
    private String mType;

    private RecyclerView recyclerView;
    private BasicContactsAdapter contactsAdapter;
    private ViewGroup emptyContacts;

    private OnContactsClickListener mListener = null;


    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance(String coinType, String address, String type) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COIN_TYPE, coinType);
        args.putString(ARG_ADDRESS, address);
        args.putString(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoinType = getArguments().getString(ARG_COIN_TYPE);
        mAddress = getArguments().getString(ARG_ADDRESS);
        mType = getArguments().getString(ARG_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = v.findViewById(R.id.recycler_contacts);
        emptyContacts = v.findViewById(R.id.layout_no_contacts);

        if (mType.equals(BasicContactsAdapter.TYPE_RECENT)) {
            List<RecentSendInfo> recentList;
            if (mCoinType.equals(Constants.KS_COINTYPE_ICX)) {
                if (ICONexApp.ICXSendInfo.size() == 0) {
                    emptyContacts.setVisibility(View.VISIBLE);
                    ((TextView) emptyContacts.findViewById(R.id.txt_message)).setText(getString(R.string.noRecent));
                } else {
                    recentList = makeRecentList();
                    contactsAdapter = new BasicContactsAdapter(getActivity(), mAddress, recentList, mCoinType, mType);
                    contactsAdapter.setOnItemClickListener(mItemClickListener);
                    recyclerView.setAdapter(contactsAdapter);
                }
            } else {
                if (ICONexApp.ETHSendInfo.size() == 0) {
                    emptyContacts.setVisibility(View.VISIBLE);
                    ((TextView) emptyContacts.findViewById(R.id.txt_message)).setText(getString(R.string.noRecent));
                } else {
                    recentList = makeRecentList();
                    contactsAdapter = new BasicContactsAdapter(getActivity(), mAddress, recentList, mCoinType, mType);
                    contactsAdapter.setOnItemClickListener(mItemClickListener);
                    recyclerView.setAdapter(contactsAdapter);
                }
            }

        } else {
            List<Wallet> walletList = makeWalletList();
            if (walletList.size() == 0) {
                emptyContacts.setVisibility(View.VISIBLE);
                ((TextView) emptyContacts.findViewById(R.id.txt_message)).setText(getString(R.string.noWallet));
            } else {
                emptyContacts.setVisibility(View.GONE);
                contactsAdapter = new BasicContactsAdapter(getActivity(), mAddress, walletList, mCoinType, mType);
                contactsAdapter.setOnItemClickListener(mItemClickListener);
                recyclerView.setAdapter(contactsAdapter);
            }
        }

        return v;
    }

    private List<Wallet> makeWalletList() {
        List<Wallet> list = new ArrayList<>();
        for (int i = 0; i < ICONexApp.wallets.size(); i++) {
            if (ICONexApp.wallets.get(i).getCoinType().equals(mCoinType)
                    && !ICONexApp.wallets.get(i).getAddress().equals(mAddress)) {
                list.add(ICONexApp.wallets.get(i));
            }
        }

        return list;
    }

    private List<RecentSendInfo> makeRecentList() {
        List<RecentSendInfo> list = new ArrayList<>();
        if (mCoinType.equals(Constants.KS_COINTYPE_ICX)) {
            for (RecentSendInfo recent : ICONexApp.ICXSendInfo) {
                if (!recent.getAddress().equals(mAddress))
                    list.add(recent);
            }

            return list;
        } else {
            for (RecentSendInfo recent : ICONexApp.ETHSendInfo) {
                if (!recent.getAddress().equals(mAddress))
                    list.add(recent);
            }

            return list;
        }
    }

    private BasicContactsAdapter.OnItemClickListener mItemClickListener = new BasicContactsAdapter.OnItemClickListener() {
        @Override
        public void onClick(String address) {
            mListener.onClick(address);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactsClickListener) {
            mListener = (OnContactsClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactsClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnContactsClickListener {
        void onClick(String address);
    }
}
