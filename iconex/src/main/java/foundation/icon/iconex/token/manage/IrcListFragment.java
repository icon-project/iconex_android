package foundation.icon.iconex.token.manage;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
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
import foundation.icon.iconex.util.ScreenUnit;
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

        int dp0_5 = ScreenUnit.dp2px(getContext(), 0.5f);
        int dp20 = ScreenUnit.dp2px(getContext(), 20);
        listView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int itemCount = state.getItemCount();

                Paint paint = new Paint();
                paint.setColor(ContextCompat.getColor(getContext(), R.color.darkE6));
                paint.setStrokeWidth(dp0_5);

                int left =  dp20;
                int right = parent.getWidth() - dp20;

                int count = parent.getChildCount();
                for (int i = 0; i < count; i++ ) {
                    View child = parent.getChildAt(i);

                    int position = parent.getChildAdapterPosition(child);
                    if (position == itemCount - 1) continue;

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin - dp0_5;
                    c.drawLine(left, top, right, top, paint);
                }
            }
        });

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
