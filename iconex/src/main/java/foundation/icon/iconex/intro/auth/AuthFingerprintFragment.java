package foundation.icon.iconex.intro.auth;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import foundation.icon.MyConstants;
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

    private KeyguardManager keyguardManager;

    private FingerprintManager fm;
    private FingerprintAuthBuilder fab;
    private FingerprintAuthHelper helper;

    private BasicDialog dialog;

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
                mListener.onByLockNum(MyConstants.FingerprintState.PASSCODE);
            }
        });

        keyguardManager = (KeyguardManager) getActivity().getSystemService(Context.KEYGUARD_SERVICE);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (dialog == null)
            dialog = new BasicDialog(getActivity());

        if (!keyguardManager.isKeyguardSecure()) {
            if (!dialog.isShowing()) {
                dialog = new BasicDialog(getActivity());
                dialog.setMessage(getString(R.string.errNoKeyguard));
                dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mListener.onByLockNum(MyConstants.FingerprintState.DISABLED);
                    }
                });
                dialog.show();
            }
        } else {
            fab = new FingerprintAuthBuilder(getActivity());
            fm = (FingerprintManager) getActivity().getSystemService(Context.FINGERPRINT_SERVICE);

            try {
                if (fab.initCipher(fab.defaultCipher, fab.DEFAULT_KEY_NAME)) {
                    helper = new FingerprintAuthHelper(fm, this);
                    if (helper.isFingerprintAuthAvailable())
                        helper.startFingerprintAuthListening(new FingerprintManager.CryptoObject(fab.defaultCipher));
                    else {
                        if (!dialog.isShowing()) {
                            dialog.setMessage(getString(R.string.errNoEnrolledFingerprint));
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    mListener.onByLockNum(MyConstants.FingerprintState.NO_ENROLLED);
                                }
                            });
                            dialog.show();
                        }
                    }
                } else {
                    if (!dialog.isShowing()) {
                        dialog.setMessage(getString(R.string.errInvalidatedByBiometricEnrollment));
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mListener.onByLockNum(MyConstants.FingerprintState.INVALID);
                            }
                        });
                        dialog.show();
                    }
                }
            } catch (Exception e) {
                if (!dialog.isShowing()) {
                    dialog.setMessage(getString(R.string.errInvalidatedByBiometricEnrollment));
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mListener.onByLockNum(MyConstants.FingerprintState.INVALID);
                        }
                    });
                    dialog.show();
                }
            }
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
        txtHelper.setText(help);
    }

    @Override
    public void onError(int errMsgId, String error) {
        helper.stopFingerprintAuthListening();
        mListener.onByLockNum(MyConstants.FingerprintState.PASSCODE);
    }

    public interface OnFingerprintLockListener {
        void onFingerprintSuccess();

        void onFingerprintFailed();

        void onByLockNum(MyConstants.FingerprintState state);
    }
}
