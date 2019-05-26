package foundation.icon.iconex.token.manage;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.token.IrcToken;
import foundation.icon.iconex.util.Utils;

public class IrcListFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = IrcListFragment.class.getSimpleName();

    private static final String ARG_ADDRESS = "address";

    private String address;

    private RecyclerView listView;
    private IrcListAdapter ircAdapter;
    private Button btnAdd;

    private List<IrcToken> ircList;

    private IrcListAdapter.OnClickListener listListener = new IrcListAdapter.OnClickListener() {
        @Override
        public void onClick() {
            if (mListener != null)
                mListener.enterInfo();
        }
    };

    public IrcListFragment() {
        // Required empty public constructor
    }

    public static IrcListFragment newInstance(String address) {
        IrcListFragment fragment = new IrcListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_ADDRESS, address);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            address = getArguments().getString(ARG_ADDRESS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_irc_list, container, false);

        listView = v.findViewById(R.id.irc_list);
        ircAdapter = new IrcListAdapter(getActivity(), address, getIrcList());
        ircAdapter.setOnClickListener(listListener);
        listView.setAdapter(ircAdapter);

        btnAdd = v.findViewById(R.id.btn_add);
        btnAdd.setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnIrcListListener) {
            mListener = (OnIrcListListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnIrcListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add:
                addTokens();
                if (mListener != null)
                    mListener.onListClose();
                break;
        }
    }

    private List<IrcToken> getIrcList() {
        List<IrcToken> list = new ArrayList<>();

        String contents = Utils.readAssets(getActivity(), MyConstants.IRC_TOKENS_FILE);

        JsonArray tokens = new Gson().fromJson(contents, JsonArray.class);
        IrcToken irc;
        for (int i = 0; i < tokens.size(); i++) {
            JsonObject token = tokens.get(i).getAsJsonObject();
            String score = token.get("score").getAsString();
            String name = token.get("name").getAsString();
            String symbol = token.get("symbol").getAsString();
            int decimal = token.get("decimals").getAsInt();

            irc = new IrcToken(address, score, name, symbol, decimal);
            list.add(irc);
        }

        return list;
    }

    private void addTokens() {
        List<IrcToken> tokens = ircAdapter.getCheckedList();

        try {
            for (IrcToken token : tokens) {
                RealmUtil.addToken(address, token);
            }

            RealmUtil.loadWallet();
        } catch (Exception e) {

        }
    }

    private OnIrcListListener mListener;

    public interface OnIrcListListener {
        void enterInfo();
        void onListClose();
    }
}
