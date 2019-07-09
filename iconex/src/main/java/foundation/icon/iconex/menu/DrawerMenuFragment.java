package foundation.icon.iconex.menu;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.Utils;

public class DrawerMenuFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = DrawerMenuFragment.class.getSimpleName();

    private OnMenuSelectListener mListener;

    public DrawerMenuFragment() {
        // Required empty public constructor
    }

    public static DrawerMenuFragment newInstance() {
        DrawerMenuFragment fragment = new DrawerMenuFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_drawer_menu, container, false);

        v.findViewById(R.id.btn_close).setOnClickListener(this);
        v.findViewById(R.id.menu_create_wallet).setOnClickListener(this);
        v.findViewById(R.id.menu_import_wallet).setOnClickListener(this);
        v.findViewById(R.id.menu_export_wallet_bundle).setOnClickListener(this);
        v.findViewById(R.id.menu_setting_lock).setOnClickListener(this);
        v.findViewById(R.id.menu_setting_language).setOnClickListener(this);
        v.findViewById(R.id.menu_app_info).setOnClickListener(this);
        v.findViewById(R.id.menu_disclaimers).setOnClickListener(this);

        try {
            PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String version = info.versionName;
            ((TextView) v.findViewById(R.id.txt_version)).setText(version);
        } catch (Exception e) {
            // Nothing.
        }

        Utils.RES_VERSION resVersion = Utils.versionCheck(getActivity(), null);
        if (resVersion == Utils.RES_VERSION.NEW)
            v.findViewById(R.id.new_version).setVisibility(View.VISIBLE);
        else
            v.findViewById(R.id.new_version).setVisibility(View.GONE);


        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMenuSelectListener) {
            mListener = (OnMenuSelectListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMenuSelectListener");
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
            case R.id.btn_close:
                mListener.onClose();
                break;

            case R.id.menu_create_wallet:
                mListener.onMenuClicked(SIDE_MENU.CREATE_WALLET);
                break;

            case R.id.menu_import_wallet:
                mListener.onMenuClicked(SIDE_MENU.IMPORT_WALLET);
                break;

            case R.id.menu_export_wallet_bundle:
                mListener.onMenuClicked(SIDE_MENU.EXPORT_WALLET_BUNDLE);
                break;

            case R.id.menu_setting_lock:
                mListener.onMenuClicked(SIDE_MENU.SETTING_LOCK);
                break;

            case R.id.menu_app_info:
                mListener.onMenuClicked(SIDE_MENU.APP_INFO);
                break;

            case R.id.menu_disclaimers:
                mListener.onMenuClicked(SIDE_MENU.ICONex_DISCLAIMER);
                break;
        }
    }

    public void setOMenuSelectListener(OnMenuSelectListener listener) {
        mListener = listener;
    }

    public interface OnMenuSelectListener {
        void onClose();

        void onMenuClicked(SIDE_MENU menu);
    }

    public enum SIDE_MENU {
        CREATE_WALLET,
        IMPORT_WALLET,
        EXPORT_WALLET_BUNDLE,
        SETTING_LOCK,
        APP_INFO,
        ICONex_DISCLAIMER
    }
}
