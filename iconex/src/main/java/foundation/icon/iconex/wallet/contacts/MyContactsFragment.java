package foundation.icon.iconex.wallet.contacts;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.barcode.BarcodeCaptureActivity;
import foundation.icon.iconex.control.Contacts;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.ContactsDialog;
import foundation.icon.iconex.realm.RealmUtil;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.Constants;

public class MyContactsFragment extends Fragment {

    private static final String TAG = MyContactsFragment.class.getSimpleName();

    private RecyclerView recyclerContacts;
    private MyContactsAdapter myContactsAdapter;

    private ViewGroup noContacts;

    private Button btnAddContacts;

    public static final String ARG_COIN_TYPE = "ARG_COIN_TYPE";
    public static final String ARG_EDITABLE = "ARG_EDITABLE";

    private String mType;
    private List<Contacts> data;
    private boolean mEditable = false;

    private static final int RC_SCAN = 30001;

    public static MyContactsFragment newInstance(String coinType, boolean editable) {
        Bundle args = new Bundle();
        args.putString(ARG_COIN_TYPE, coinType);
        args.putBoolean(ARG_EDITABLE, editable);
        MyContactsFragment fragment = new MyContactsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getString(ARG_COIN_TYPE);
        mEditable = getArguments().getBoolean(ARG_EDITABLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_contacts, container, false);

        recyclerContacts = view.findViewById(R.id.recycler_contacts);
        noContacts = view.findViewById(R.id.layout_no_contacts);

        btnAddContacts = view.findViewById(R.id.btn_add_contacts);
        btnAddContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contactsDialog = new ContactsDialog(getActivity(), mType, ContactsDialog.MODE.ADD, null, onClickListener);
                contactsDialog.show();
            }
        });

        if (mType.equals(Constants.KS_COINTYPE_ICX))
            data = ICONexApp.ICXContacts;
        else
            data = ICONexApp.ETHContacts;

        if (data.size() == 0) {
            noContacts.setVisibility(View.VISIBLE);
        } else {
            noContacts.setVisibility(View.GONE);
            myContactsAdapter = new MyContactsAdapter(getActivity(), data, mEditable);
            myContactsAdapter.setContactsClickListener(mClickListener);
            recyclerContacts.setAdapter(myContactsAdapter);
        }

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnContactListener) {
            mListener = (OnContactListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnContactsListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private ContactsDialog contactsDialog;
    private ContactsDialog.OnClickListener onClickListener = new ContactsDialog.OnClickListener() {
        @Override
        public void onConfirm(ContactsDialog.MODE mode, String name, String address) {
            if (mode == ContactsDialog.MODE.ADD) {
                if (mType.equals(Constants.KS_COINTYPE_ICX)) {
                    RealmUtil.addContacts(MyConstants.Coin.ICX, name, address);
                    RealmUtil.loadContacts();

                    data = ICONexApp.ICXContacts;
                } else {
                    RealmUtil.addContacts(MyConstants.Coin.ETH, name, address);
                    RealmUtil.loadContacts();

                    data = ICONexApp.ETHContacts;
                }

                if (noContacts.getVisibility() == View.VISIBLE)
                    noContacts.setVisibility(View.GONE);

                ((ContactsActivity) getActivity()).setBtnModVisibility(View.VISIBLE);

                myContactsAdapter = new MyContactsAdapter(getActivity(), data, mEditable);
                myContactsAdapter.setContactsClickListener(mClickListener);
                recyclerContacts.setAdapter(myContactsAdapter);
            } else {
                if (mType.equals(Constants.KS_COINTYPE_ICX)) {
                    RealmUtil.modifyContact(mType, address, name);
                    RealmUtil.loadContacts();

                    data = ICONexApp.ICXContacts;
                    myContactsAdapter = new MyContactsAdapter(getActivity(), data, mEditable);
                    myContactsAdapter.setContactsClickListener(mClickListener);
                    recyclerContacts.setAdapter(myContactsAdapter);
                } else {
                    RealmUtil.modifyContact(mType, address, name);
                    RealmUtil.loadContacts();

                    data = ICONexApp.ETHContacts;
                    myContactsAdapter = new MyContactsAdapter(getActivity(), data, mEditable);
                    recyclerContacts.setAdapter(myContactsAdapter);
                }
            }
        }

        @Override
        public void scanQRCode() {
            startActivityForResult(new Intent(getActivity(), BarcodeCaptureActivity.class)
                    .putExtra(BarcodeCaptureActivity.AutoFocus, true)
                    .putExtra(BarcodeCaptureActivity.UseFlash, false), RC_SCAN);
        }
    };

    private MyContactsAdapter.ContactsClickListener mClickListener = new MyContactsAdapter.ContactsClickListener() {
        @Override
        public void onDelete(int position) {
            final int pos = position;
            MessageDialog messageDialog = new MessageDialog(getContext());
            messageDialog.setTitleText(getString(R.string.msgDeleteContact));
            messageDialog.setSingleButton(false);
            messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    if (mType.equals(Constants.KS_COINTYPE_ICX)) {
                        RealmUtil.deleteContact(mType, ICONexApp.ICXContacts.get(pos).getName());
                        RealmUtil.loadContacts();

                        data = ICONexApp.ICXContacts;
                    } else {
                        RealmUtil.deleteContact(mType, ICONexApp.ETHContacts.get(pos).getName());
                        RealmUtil.loadContacts();

                        data = ICONexApp.ETHContacts;
                    }

                    if (data.size() == 0) {
                        noContacts.setVisibility(View.VISIBLE);
                        ((ContactsActivity) getActivity()).setBtnModVisibility(View.INVISIBLE);
                    }

                    myContactsAdapter = new MyContactsAdapter(getActivity(), data, mEditable);
                    myContactsAdapter.setContactsClickListener(mClickListener);
                    recyclerContacts.setAdapter(myContactsAdapter);
                    return true;
                }
            });
            messageDialog.show();
        }

        @Override
        public void onEdit(final int position) {
            String address;
            if (mType.equals(Constants.KS_COINTYPE_ICX)) {
                address = ICONexApp.ICXContacts.get(position).getAddress();
            } else {
                address = ICONexApp.ETHContacts.get(position).getAddress();
            }

            contactsDialog = new ContactsDialog(getActivity(), mType, ContactsDialog.MODE.MOD, address, onClickListener);
            contactsDialog.show();
        }

        @Override
        public void onSelect(String address) {
            mListener.onClick(address);
        }
    };

    private OnContactListener mListener = null;

    public interface OnContactListener {
        void onClick(String address);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SCAN) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if (contactsDialog != null)
                        contactsDialog.setAddress(barcode.displayValue);
                } else {
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
