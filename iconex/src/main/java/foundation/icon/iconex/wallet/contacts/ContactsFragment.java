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
import foundation.icon.iconex.wallet.WalletEntry;
import loopchain.icon.wallet.core.Constants;

public class ContactsFragment extends Fragment {

    private static final String TAG = ContactsFragment.class.getSimpleName();

    private static final String ARG_COIN_TYPE = "ARG_COIN_TYPE";
    private static final String ARG_TOKEN_TYPE = "ARG_TOKEN_TYPE";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";

    private String mCoinType;
    private String mTokenType;
    private String mAddress;

    private RecyclerView recyclerView;
    private BasicContactsAdapter contactsAdapter;
    private ViewGroup emptyContacts;

    private OnContactsClickListener mListener = null;


    public ContactsFragment() {
        // Required empty public constructor
    }

    public static ContactsFragment newInstance(String coinType, String tokenType, String address) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COIN_TYPE, coinType);
        args.putString(ARG_TOKEN_TYPE, tokenType);
        args.putString(ARG_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCoinType = getArguments().getString(ARG_COIN_TYPE);
        mTokenType = getArguments().getString(ARG_TOKEN_TYPE);
        mAddress = getArguments().getString(ARG_ADDRESS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = v.findViewById(R.id.recycler_contacts);
        emptyContacts = v.findViewById(R.id.layout_no_contacts);

        List<Wallet> walletList = makeWalletList();
        if (walletList.size() == 0) {
            emptyContacts.setVisibility(View.VISIBLE);
            ((TextView) emptyContacts.findViewById(R.id.txt_message)).setText(getString(R.string.noWallet));
        } else {
            emptyContacts.setVisibility(View.GONE);
            contactsAdapter = new BasicContactsAdapter(getActivity(), mAddress, walletList, mTokenType);
            contactsAdapter.setOnItemClickListener(mItemClickListener);
            recyclerView.setAdapter(contactsAdapter);
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
