package foundation.icon.iconex.control;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 2. 22..
 */

public class CoinListAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<ItemCoin> mCoinList;

    public CoinListAdapter(Context context, ArrayList<ItemCoin> coinList) {
        super();

        mContext = context;
        mCoinList = coinList;
    }

    @Override
    public int getCount() {
        return mCoinList.size();
    }

    @Override
    public ItemCoin getItem(int position) {
        return mCoinList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        RadioButton coinSelect;
        TextView coinName;
        ItemCoin mCoin;

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.layout_item_coin_list, parent, false);
        }

        coinSelect = view.findViewById(R.id.radio_selected);
        coinName = view.findViewById(R.id.txt_name);

        mCoin = mCoinList.get(position);

        if (mCoin.isChecked()) {
            coinSelect.setChecked(true);
        } else {
            coinSelect.setChecked(false);
        }

        coinName.setText(mCoin.getName());

        return view;
    }

    public void setChecked(int position) {
        ItemCoin coin;
        for (int i = 0; i < mCoinList.size(); i++) {
            coin = mCoinList.get(i);
            if (i == position)
                coin.setChecked(true);
            else
                coin.setChecked(false);
        }

        this.notifyDataSetChanged();
    }
}
