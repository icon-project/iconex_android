package foundation.icon.iconex.menu.lock;

import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.util.PreferenceUtil;

public class AppLockManageFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = AppLockManageFragment.class.getSimpleName();

    private ViewGroup layoutHeader, layoutUse, layoutFin;

    private Button btnUse, btnFin;
    private ViewGroup btnReset;

    public AppLockManageFragment() {
        // Required empty public constructor
    }

    public static AppLockManageFragment newInstance() {
        AppLockManageFragment fragment = new AppLockManageFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_app_lock_manage, container, false);

        layoutHeader = v.findViewById(R.id.layout_header);
        layoutUse = v.findViewById(R.id.layout_use);
        layoutFin = v.findViewById(R.id.layout_finger);

        btnUse = v.findViewById(R.id.switch_lock);
        btnUse.setOnClickListener(this);
        btnReset = v.findViewById(R.id.layout_reset);
        btnReset.setOnClickListener(this);
        btnFin = v.findViewById(R.id.switch_finger);
        btnFin.setOnClickListener(this);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        refresh();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAppLockManageListener) {
            mListener = (OnAppLockManageListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnAppLockManageListener");
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
            case R.id.switch_lock:
                mListener.onSetLockNumUse(!btnUse.isSelected());
                break;

            case R.id.layout_reset:
                mListener.onResetAppLock();
                break;

            case R.id.switch_finger:
                if (btnFin.isSelected()) {
                    PreferenceUtil preferenceUtil = new PreferenceUtil(getActivity());
                    preferenceUtil.saveUseFingerprint(false);
                    preferenceUtil.loadPreference();

                    refresh();
                } else {
                    checkEnrolledFingerprint();
                }
                break;
        }
    }

    public void refresh() {
        if (ICONexApp.isLocked) {
            layoutHeader.setVisibility(View.GONE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layoutUse.getLayoutParams();
            layoutParams.setMargins(0, 0, 0, 0);
            layoutUse.setLayoutParams(layoutParams);

            btnUse.setSelected(true);
            btnReset.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                FingerprintManager fm = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
                if (fm.isHardwareDetected())
                    layoutFin.setVisibility(View.VISIBLE);
                else
                    layoutFin.setVisibility(View.INVISIBLE);

            } else {
                layoutFin.setVisibility(View.INVISIBLE);
            }

        } else {
            layoutHeader.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) layoutUse.getLayoutParams();
            layoutParams.setMargins(0, (int) getResources().getDimension(R.dimen.dp40), 0, 0);
            layoutUse.setLayoutParams(layoutParams);

            btnUse.setSelected(false);
            btnReset.setVisibility(View.INVISIBLE);
            layoutFin.setVisibility(View.INVISIBLE);
        }

        if (ICONexApp.useFingerprint)
            btnFin.setSelected(true);
        else
            btnFin.setSelected(false);
    }

    private void checkEnrolledFingerprint() {
        BasicDialog dialog = new BasicDialog(getActivity());

        KeyguardManager keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);
        if (!keyguardManager.isKeyguardSecure()) {
            dialog.setMessage(getString(R.string.errNoKeyguard));
            dialog.show();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager fm = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);
            if (fm.hasEnrolledFingerprints())
                mListener.onUnlockFinger();
            else {
                dialog.setMessage(getString(R.string.errNoEnrolledFingerprint));
                dialog.show();
            }
        }
    }

    private OnAppLockManageListener mListener;

    public interface OnAppLockManageListener {
        void onSetLockNumUse(boolean isLocked);

        void onResetAppLock();

        void onUnlockFinger();
    }
}
