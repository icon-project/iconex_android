package foundation.icon.iconex.menu.appInfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.service.VersionCheck;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.widgets.CustomActionBar;

public class AppInfoFragment extends Fragment implements View.OnClickListener {

    private CustomActionBar appbar;

    private TextView txtCurrentHighlight;
    private TextView txtCurrent;

    private TextView txtLatestHightlight;
    private TextView txtLatest;

    private Button btnUpdate;

    private ViewGroup layoutOpensrouce;
    private ViewGroup layoutDevelop;

    public AppInfoFragment() {
        // Required empty public constructor
    }

    public static AppInfoFragment newInstance() {
        AppInfoFragment fragment = new AppInfoFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_appinfo, container, false);

        // load ui
        appbar = v.findViewById(R.id.appbar);

        txtCurrentHighlight = v.findViewById(R.id.txt_current_highlight);
        txtCurrent = v.findViewById(R.id.txt_current);

        txtLatestHightlight = v.findViewById(R.id.txt_lastest_highlight);
        txtLatest = v.findViewById(R.id.txt_lastest);

        btnUpdate = v.findViewById(R.id.btn_update);

        layoutOpensrouce = v.findViewById(R.id.layout_open_source);
        layoutDevelop = v.findViewById(R.id.layout_dev_mode);

        // init view
        appbar.setOnClickStartIcon(this);
        btnUpdate.setOnClickListener(this);
        layoutOpensrouce.setOnClickListener(this);
        layoutDevelop.setOnClickListener(this);

        txtCurrent.setVisibility(View.VISIBLE);
        txtCurrentHighlight.setVisibility(View.GONE);
        txtCurrent.setText(getString(R.string.currentVersion) + " -");
        txtCurrentHighlight.setText(getString(R.string.currentVersion) + " -");

        txtLatest.setVisibility(View.VISIBLE);
        txtLatestHightlight.setVisibility(View.GONE);
        txtLatest.setText(getString(R.string.latestVersion) + " -");
        txtCurrentHighlight.setText(getString(R.string.latestVersion) + " -");

        btnUpdate.setVisibility(View.GONE);

        bindData();

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                bindData(); // refresh data
            }
        });

        return v;
    }

    private void bindData() {
        // get current version
        try {
            String version = getActivity()
                    .getPackageManager()
                    .getPackageInfo(getActivity().getPackageName(), 0)
                    .versionName;

            txtCurrent.setText(getString(R.string.currentVersion) + " " + version);
            txtCurrentHighlight.setText(getString(R.string.currentVersion) + " " + version);

        } catch (Exception e) { }

        // get latest version
        VersionCheck versionCheck = new VersionCheck(getActivity(), new VersionCheck.VersionCheckCallback() {
            @Override
            public void onNeedUpdate() {

            }

            @Override
            public void onPass() {
                try {
                    txtLatest.setText(getString(R.string.latestVersion) + " " + ICONexApp.version);
                    txtLatestHightlight.setText(getString(R.string.latestVersion) + " " + ICONexApp.version);

                    Utils.RES_VERSION resVersion = Utils.versionCheck(getActivity(), null);
                    if (resVersion == Utils.RES_VERSION.NEW) {
                        btnUpdate.setVisibility(View.VISIBLE);

                        txtCurrent.setVisibility(View.VISIBLE);
                        txtCurrentHighlight.setVisibility(View.GONE);
                        txtLatest.setVisibility(View.GONE);
                        txtLatestHightlight.setVisibility(View.VISIBLE);

                    } else {
                        btnUpdate.setVisibility(View.GONE);

                        txtCurrent.setVisibility(View.GONE);
                        txtCurrentHighlight.setVisibility(View.VISIBLE);
                        txtLatest.setVisibility(View.VISIBLE);
                        txtLatestHightlight.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        versionCheck.execute();

        // set dev mode
        layoutDevelop.setVisibility(ICONexApp.isDeveloper ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start_icon: {
                getActivity().finish();
            } break;
            case R.id.btn_update: {
                FragmentActivity activity = getActivity();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ServiceConstants.URL_STORE));
                activity.startActivity(intent);
                activity.finishAffinity();
            } break;
            case R.id.layout_open_source: {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(android.R.id.content, OpenSourceFragment.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();
            } break;
            case R.id.layout_dev_mode: {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(android.R.id.content, DeveloperModeFragment.newInstance());
                transaction.addToBackStack(null);
                transaction.commit();
            } break;
        }
    }
}
