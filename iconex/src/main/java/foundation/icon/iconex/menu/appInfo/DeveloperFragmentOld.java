package foundation.icon.iconex.menu.appInfo;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.BottomSheetMenuDialog;
import foundation.icon.iconex.util.PreferenceUtil;

public class DeveloperFragmentOld extends Fragment {

    private static final String TAG = DeveloperFragmentOld.class.getSimpleName();

    private Button btnSwitch;
    private ViewGroup btnNetwork;
    private TextView txtNetwork;

    private PreferenceUtil preferenceUtil;

    public DeveloperFragmentOld() {

    }

    public static DeveloperFragmentOld newInstance() {
        DeveloperFragmentOld fragment = new DeveloperFragmentOld();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_developer, container, false);
        preferenceUtil = new PreferenceUtil(getActivity());

        btnSwitch = v.findViewById(R.id.switch_dev);
        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ICONexApp.isDeveloper = false;
                preferenceUtil.setDeveloper(ICONexApp.isDeveloper);

                ICONexApp.network = MyConstants.NETWORK_MAIN;
                preferenceUtil.setNetwork(ICONexApp.network);

                mListener.onOff();
            }
        });

        btnNetwork = v.findViewById(R.id.btn_network);
        btnNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetMenuDialog dialog = new BottomSheetMenuDialog(getActivity(), getString(R.string.selectNetwork),
                        BottomSheetMenuDialog.SHEET_TYPE.BASIC);
                List<String> networks = new ArrayList<>();
                networks.add(getString(R.string.networkMain));
                networks.add(getString(R.string.networkTest));

                dialog.setBasicData(networks);
                dialog.setOnItemClickListener(mItemListener);

                dialog.show();
            }
        });

        txtNetwork = v.findViewById(R.id.txt_network);

        switch (ICONexApp.network) {
            case MyConstants.NETWORK_MAIN:
                txtNetwork.setText(getString(R.string.networkMain));
                break;

            case MyConstants.NETWORK_TEST:
                txtNetwork.setText(getString(R.string.networkTest));
                break;
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof DeveloperOnclick) {
            mListener = (DeveloperOnclick) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAppInfoListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private BottomSheetMenuDialog.OnItemClickListener mItemListener = new BottomSheetMenuDialog.OnItemClickListener() {
        @Override
        public void onBasicItem(String item) {
            txtNetwork.setText(item);
            if (item.equals(getString(R.string.networkMain)))
                ICONexApp.network = MyConstants.NETWORK_MAIN;
            else
                ICONexApp.network = MyConstants.NETWORK_TEST;

            preferenceUtil.setNetwork(ICONexApp.network);
        }

        @Override
        public void onCoinItem(int position) {

        }

        @Override
        public void onMenuItem(String tag) {

        }
    };

    private DeveloperOnclick mListener;

    public interface DeveloperOnclick {
        void onOff();
    }
}

