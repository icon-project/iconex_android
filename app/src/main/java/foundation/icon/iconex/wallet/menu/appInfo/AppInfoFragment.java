package foundation.icon.iconex.wallet.menu.appInfo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.Utils;

public class AppInfoFragment extends Fragment {

    TextView txtCurrent, txtLatest;
    Button btnUpdate;
    ViewGroup OSS;

    public AppInfoFragment() {
        // Required empty public constructor
    }

    public static AppInfoFragment newInstance() {
        AppInfoFragment fragment = new AppInfoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_info, container, false);

        txtCurrent = v.findViewById(R.id.txt_current);
        txtLatest = v.findViewById(R.id.txt_latest);

        btnUpdate = v.findViewById(R.id.btn_update);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onUpdate();
            }
        });

        OSS = v.findViewById(R.id.oss);
        OSS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onClickOSS();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        String version;
        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = info.versionName;

            txtCurrent.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.currentVersion), version));
        } catch (Exception e) {
            txtCurrent.setText("-");
        }

        txtLatest.setText(String.format(Locale.getDefault(), "%s %s", getString(R.string.latestVersion), ICONexApp.version));

        Utils.RES_VERSION resVersion = Utils.versionCheck(getActivity(), null);
        if (resVersion == Utils.RES_VERSION.NEW)
            btnUpdate.setEnabled(true);
        else
            btnUpdate.setEnabled(false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAppInfoListener) {
            mListener = (OnAppInfoListener) context;
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

    private OnAppInfoListener mListener;

    public interface OnAppInfoListener {
        void onUpdate();
        void onClickOSS();
    }
}
