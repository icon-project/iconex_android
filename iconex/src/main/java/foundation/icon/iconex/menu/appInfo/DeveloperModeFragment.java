package foundation.icon.iconex.menu.appInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.TDropdownLayout;

public class DeveloperModeFragment extends Fragment {

    private CustomActionBar appbar;
    private Button btnDevmode;
    private TDropdownLayout selectNetwokr;

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
        selectNetwokr = v.findViewById(R.id.select_network);

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
                ICONexApp.isDeveloper = false;
                preferenceUtil.setDeveloper(ICONexApp.isDeveloper);

                ICONexApp.network = MyConstants.NETWORK_MAIN;
                preferenceUtil.setNetwork(ICONexApp.network);

                getFragmentManager().popBackStackImmediate();
            }
        });

        selectNetwokr.setOnClickListener(new TDropdownLayout.OnDropDownClickListener() {
            @Override
            public void onClick() {
                new SelectNetworkDialog(getContext(), new ArrayList<String>() {{
                    add(getString(R.string.networkMain));
                    add(getString(R.string.networkTest));
                }}, new SelectNetworkDialog.OnSelectItemListener() {
                    @Override
                    public void onSelect(String network) {
                        selectNetwokr.setText(network);
                        if (network.equals(getString(R.string.networkMain)))
                            ICONexApp.network = MyConstants.NETWORK_MAIN;
                        else
                            ICONexApp.network = MyConstants.NETWORK_TEST;

                        new PreferenceUtil(getActivity()).setNetwork(ICONexApp.network);
                    }
                }).show();
            }
        });

        // set data
        switch (ICONexApp.network) {
            case MyConstants.NETWORK_MAIN:
                selectNetwokr.setText(getString(R.string.networkMain));
                break;

            case MyConstants.NETWORK_TEST:
                selectNetwokr.setText(getString(R.string.networkTest));
                break;
        }

        return v;
    }
}
