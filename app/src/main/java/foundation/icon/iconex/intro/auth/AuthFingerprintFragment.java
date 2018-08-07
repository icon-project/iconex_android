package foundation.icon.iconex.intro.auth;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.util.FingerprintAuthBuilder;
import foundation.icon.iconex.util.FingerprintAuthHelper;

@TargetApi(Build.VERSION_CODES.M)
public class AuthFingerprintFragment extends Fragment implements FingerprintAuthHelper.Callback {
    private static final String TAG = AuthFingerprintFragment.class.getSimpleName();

    private OnFingerprintLockListener mListener;

    private TextView txtHelper;
    private ViewGroup btnLockNum;

    private FingerprintManager fm;
    private FingerprintAuthBuilder fab;
    private FingerprintAuthHelper helper;

    public AuthFingerprintFragment() {
        // Required empty public constructor
    }

    public static AuthFingerprintFragment newInstance() {
        AuthFingerprintFragment fragment = new AuthFingerprintFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_auth_fingerprint, container, false);

        txtHelper = v.findViewById(R.id.txt_helper);

        btnLockNum = v.findViewById(R.id.layout_lock_num);
        btnLockNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onByLockNum(false);
            }
        });

        fm = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        fab = new FingerprintAuthBuilder(getActivity());
//        fab.createKey(FingerprintAuthBuilder.DEFAULT_KEY_NAME, true);

        try {
            if (fab.initCipher(fab.defaultCipher, fab.DEFAULT_KEY_NAME)) {
                helper = new FingerprintAuthHelper(fm, this);
                helper.startFingerprintAuthListening(new FingerprintManager.CryptoObject(fab.defaultCipher));
            } else {

            }
        } catch (Exception e) {
            BasicDialog dialog = new BasicDialog(getActivity());
            dialog.setMessage(getString(R.string.errInvalidatedByBiometricEnrollment));
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mListener.onByLockNum(true);
                }
            });
            dialog.show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFingerprintLockListener) {
            mListener = (OnFingerprintLockListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFingerprintLockListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (helper != null)
            helper.stopFingerprintAuthListening();
        mListener = null;
    }

    @Override
    public void onAuthenticated() {
        helper.stopFingerprintAuthListening();
        mListener.onFingerprintSuccess();
    }

    @Override
    public void onFailed() {
        txtHelper.setText(getString(R.string.fingerprint_not_recognized));
    }

    @Override
    public void onHelp(int helpMsgId, String help) {
        Log.d(TAG, "onHelp : helpMsgId=" + helpMsgId + ", " + "helpString=" + help);
        txtHelper.setText(help);
    }

    @Override
    public void onError(int errMsgId, String error) {
        Log.d(TAG, "onError : errorMsgId=" + errMsgId + ", " + "ErrorString=" + error);
        helper.stopFingerprintAuthListening();
        mListener.onByLockNum(false);
    }

    public interface OnFingerprintLockListener {
        void onFingerprintSuccess();

        void onFingerprintFailed();

        void onByLockNum(boolean isInvalidated);
    }
}
