package foundation.icon.iconex.menu.appInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.Urls;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.TDropdownLayout;

public class DeveloperModeFragment extends Fragment {

    private CustomActionBar appbar;
    private ImageButton btnDevmode;
    private TDropdownLayout selectNetwork;

    public DeveloperModeFragment() {

    }

    public static DeveloperModeFragment newInstance() {
        DeveloperModeFragment fragment = new DeveloperModeFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_developer_mode, container, false);

        // load UI
        appbar = v.findViewById(R.id.appbar);
        btnDevmode = v.findViewById(R.id.btn_devmode);
        selectNetwork = v.findViewById(R.id.select_network);

        // init UI
        appbar.setOnClickStartIcon(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        btnDevmode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtil preferenceUtil = new PreferenceUtil(getActivity());
                boolean isDev = !ICONexApp.isDeveloper;

                ICONexApp.isDeveloper = isDev;
                preferenceUtil.setDeveloper(isDev);
                btnDevmode.setImageResource(isDev ? R.drawable.btn_switch_on : R.drawable.btn_switch_off);
                selectNetwork.setOnClickListener(isDev ? onDropDownClickListener : null);
                selectNetwork.setEnable(isDev);

                if (!isDev) {
                    selectNetwork.setText(getString(R.string.networkMain));
                    ICONexApp.NETWORK = Urls.Network.MainNet;
                    preferenceUtil.setNetwork(ICONexApp.network);
                }

            }
        });


        // set data
        btnDevmode.setImageResource(ICONexApp.isDeveloper ? R.drawable.btn_switch_on : R.drawable.btn_switch_off);
        selectNetwork.setOnClickListener(ICONexApp.isDeveloper ? onDropDownClickListener : null);
        switch (ICONexApp.NETWORK.getNid().intValue()) {
            case MyConstants.NETWORK_MAIN: selectNetwork.setText(getString(R.string.networkMain)); break;
            case MyConstants.NETWORK_TEST: selectNetwork.setText(getString(R.string.networkTest)); break;
        }

        return v;
    }

    private TDropdownLayout.OnDropDownClickListener onDropDownClickListener = new TDropdownLayout.OnDropDownClickListener() {
        @Override
        public void onClick() {
            new SelectNetworkDialog(getContext(), new ArrayList<String>() {{
                add(getString(R.string.networkMain));
                add(getString(R.string.networkTest));
            }}, new SelectNetworkDialog.OnSelectItemListener() {
                @Override
                public void onSelect(String network) {
                    selectNetwork.setText(network);
                    if (network.equals(getString(R.string.networkMain)))
                        ICONexApp.NETWORK = Urls.Network.MainNet;
                    else
                        ICONexApp.NETWORK = Urls.Network.Euljiro;

                    new PreferenceUtil(getActivity()).setNetwork(ICONexApp.NETWORK.getNid().intValue());
                }
            }).show();
        }
    };
}
