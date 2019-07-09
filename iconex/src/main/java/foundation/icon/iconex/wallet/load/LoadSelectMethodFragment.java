package foundation.icon.iconex.wallet.load;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.ArrayList;

import foundation.icon.iconex.R;

public class LoadSelectMethodFragment extends Fragment {

    private OnSelectMethodCallback mListener;

    private ListView listMethod;
    private MethodListAdapter listAdpater;
    private Button btnNext;

    private LoadWalletViewPagerAdapter.LOAD_TYPE methodType;

    public LoadSelectMethodFragment() {
        // Required empty public constructor
    }

    public static LoadSelectMethodFragment newInstance() {
        LoadSelectMethodFragment fragment = new LoadSelectMethodFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_select_method, container, false);

        listMethod = v.findViewById(R.id.list_view_method);
        listAdpater = new MethodListAdapter(getActivity());
        listMethod.setAdapter(listAdpater);
        listMethod.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listAdpater.setChecked(position);
                methodType = listAdpater.getItem(position).getType();
                btnNext.setEnabled(true);
            }
        });

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNext(methodType);
            }
        });

        methodType = LoadWalletViewPagerAdapter.LOAD_TYPE.KEYSTORE;
        btnNext.setEnabled(true);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSelectMethodCallback) {
            mListener = (OnSelectMethodCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSelectMethodCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSelectMethodCallback {
        void onNext(LoadWalletViewPagerAdapter.LOAD_TYPE type);
    }

    private class MethodListAdapter extends BaseAdapter {

        private final Context mContext;
        private ArrayList<LoadMethod> methods = new ArrayList<>();

        public MethodListAdapter(Context context) {
            super();

            mContext = context;
            LoadMethod method = new LoadMethod(getString(R.string.loadByKeystore), LoadWalletViewPagerAdapter.LOAD_TYPE.KEYSTORE);
            method.setChecked(true);
            methods.add(method);
            method = new LoadMethod(getString(R.string.loadByPrivateKey), LoadWalletViewPagerAdapter.LOAD_TYPE.PRIVATE_KEY);
            methods.add(method);
        }

        @Override
        public int getCount() {
            return methods.size();
        }

        @Override
        public LoadMethod getItem(int position) {
            return methods.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View v, ViewGroup parent) {

            if (v == null) {
                LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = layoutInflater.inflate(R.layout.layout_item_coin_list, parent, false);
            }

            RadioButton radioButton = v.findViewById(R.id.radio_selected);
            TextView txtMethod = v.findViewById(R.id.txt_name);

            LoadMethod method = methods.get(position);

            if (method.isChecked()) {
                radioButton.setChecked(true);
            } else {
                radioButton.setChecked(false);
            }

            txtMethod.setText(method.getName());

            return v;
        }

        public void setChecked(int position) {
            LoadMethod method;
            for (int i = 0; i < methods.size(); i++) {
                method = methods.get(i);
                if (i == position)
                    method.setChecked(true);
                else
                    method.setChecked(false);
            }

            this.notifyDataSetChanged();
        }
    }

    private class LoadMethod {
        String name;
        boolean isChecked = false;
        LoadWalletViewPagerAdapter.LOAD_TYPE type;

        public LoadMethod(String method, LoadWalletViewPagerAdapter.LOAD_TYPE type) {
            setName(method);
            setType(type);
        }

        public String getName() {
            return name;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public void setName(String name) {
            this.name = name;
        }

        public LoadWalletViewPagerAdapter.LOAD_TYPE getType() {
            return type;
        }

        public void setType(LoadWalletViewPagerAdapter.LOAD_TYPE type) {
            this.type = type;
        }
    }
}
