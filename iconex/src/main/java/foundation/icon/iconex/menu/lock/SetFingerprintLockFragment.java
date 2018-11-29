package foundation.icon.iconex.menu.lock;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.FingerprintDialog;
import foundation.icon.iconex.util.FingerprintAuthBuilder;
import foundation.icon.iconex.util.PreferenceUtil;

public class SetFingerprintLockFragment extends Fragment {

    private OnSetFingerprintLockListener mListener;

    public SetFingerprintLockFragment() {
        // Required empty public constructor
    }

    public static SetFingerprintLockFragment newInstance() {
        SetFingerprintLockFragment fragment = new SetFingerprintLockFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_set_fingerprint_lock, container, false);

        Button btnUse = v.findViewById(R.id.btn_use);
        btnUse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FingerprintAuthBuilder fab = new FingerprintAuthBuilder(getActivity());
                fab.createKey(FingerprintAuthBuilder.DEFAULT_KEY_NAME, true);

                FingerprintDialog dialog = new FingerprintDialog(getActivity());
                dialog.setOnCheckFingerprintListener(new FingerprintDialog.OnCheckFingerprintListener() {
                    @Override
                    public void onChecked() {
                        PreferenceUtil preferenceUtil = new PreferenceUtil(getActivity());
                        preferenceUtil.saveUseFingerprint(true);
                        preferenceUtil.loadPreference();

                        mListener.onFingerBack();
                    }
                });
                dialog.show();
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSetFingerprintLockListener) {
            mListener = (OnSetFingerprintLockListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSetFingerprintLockListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnSetFingerprintLockListener {
        void onFingerBack();
    }
}
