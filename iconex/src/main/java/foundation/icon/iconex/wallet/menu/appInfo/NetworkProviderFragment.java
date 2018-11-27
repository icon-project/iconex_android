package foundation.icon.iconex.wallet.menu.appInfo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.PreferenceUtil;

public class NetworkProviderFragment extends Fragment {

    private static final String TAG = NetworkProviderFragment.class.getSimpleName();

    private RadioButton radioMain, radioTest, radioDev;

    private PreferenceUtil preferenceUtil;
    private int isMain = 0;

    public NetworkProviderFragment() {

    }

    public static NetworkProviderFragment newInstance() {
        NetworkProviderFragment fragment = new NetworkProviderFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_network, container, false);

        preferenceUtil = new PreferenceUtil(getActivity());
        isMain = preferenceUtil.getNetwork();

        ViewGroup main = v.findViewById(R.id.main);
        main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!radioMain.isChecked()) {
                    radioMain.setChecked(true);
                    radioTest.setChecked(false);
                    radioDev.setChecked(false);

                    preferenceUtil.setNetwork(MyConstants.NETWORK_MAIN);
                    ICONexApp.network = preferenceUtil.getNetwork();
                }
            }
        });

        ViewGroup test = v.findViewById(R.id.test);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!radioTest.isChecked()) {
                    radioMain.setChecked(false);
                    radioTest.setChecked(true);
                    radioDev.setChecked(false);

                    preferenceUtil.setNetwork(MyConstants.NETWORK_TEST);
                    ICONexApp.network = preferenceUtil.getNetwork();
                }
            }
        });

        ViewGroup dev = v.findViewById(R.id.dev);
        dev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!radioDev.isChecked()) {
                    radioMain.setChecked(false);
                    radioTest.setChecked(false);
                    radioDev.setChecked(true);

                    preferenceUtil.setNetwork(MyConstants.NETWORK_DEV);
                    ICONexApp.network = preferenceUtil.getNetwork();
                }
            }
        });

        radioMain = v.findViewById(R.id.radio_main);
        radioTest = v.findViewById(R.id.radio_test);
        radioDev = v.findViewById(R.id.radio_dev);

        switch (preferenceUtil.getNetwork()) {
            case MyConstants.NETWORK_MAIN:
                radioMain.setChecked(true);
                radioTest.setChecked(false);
                radioDev.setChecked(false);
                break;

            case MyConstants.NETWORK_TEST:
                radioMain.setChecked(false);
                radioTest.setChecked(true);
                radioDev.setChecked(false);
                break;

            case MyConstants.NETWORK_DEV:
                radioMain.setChecked(false);
                radioTest.setChecked(false);
                radioDev.setChecked(true);
                break;
        }

        return v;
    }
}

