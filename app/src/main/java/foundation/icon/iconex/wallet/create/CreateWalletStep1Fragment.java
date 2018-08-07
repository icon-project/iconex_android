package foundation.icon.iconex.wallet.create;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.CoinListAdapter;
import foundation.icon.iconex.control.ItemCoin;
import loopchain.icon.wallet.core.Constants;

public class CreateWalletStep1Fragment extends Fragment {

    private static final String TAG = CreateWalletStep1Fragment.class.getSimpleName();

    private OnStep1Listener mListener;
    private Button btnNext;

    private ListView listViewCoin;
    private CoinListAdapter listAdapter;
    private ArrayList<ItemCoin> mCoinList;

    private String coinType = null;

    public CreateWalletStep1Fragment() {
        // Required empty public constructor
    }

    public static CreateWalletStep1Fragment newInstance() {
        CreateWalletStep1Fragment fragment = new CreateWalletStep1Fragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_create_wallet_step1, container, false);

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onStep1Done(coinType);
            }
        });

        listViewCoin = v.findViewById(R.id.list_view_coin);

        View listViewHeader = getActivity().getLayoutInflater().inflate(R.layout.layout_step1_listview_header, null);
        listViewCoin.addHeaderView(listViewHeader);

        ItemCoin ICX = new ItemCoin(getString(R.string.coin_icx), Constants.KS_COINTYPE_ICX);
        coinType = ICX.getCoinType();
        ICX.setChecked(true);
        ItemCoin ETH = new ItemCoin(getString(R.string.coin_eth), Constants.KS_COINTYPE_ETH);

        mCoinList = new ArrayList<>();
        mCoinList.add(ICX);
        mCoinList.add(ETH);

        listAdapter = new CoinListAdapter(getActivity(), mCoinList);
        listViewCoin.setAdapter(listAdapter);

        listViewCoin.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listAdapter.setChecked(position);
                coinType = listAdapter.getItem(position).getCoinType();
                btnNext.setEnabled(true);
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnStep1Listener) {
            mListener = (OnStep1Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStep1Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnStep1Listener {
        void onStep1Done(@NonNull String coinType);
    }
}
