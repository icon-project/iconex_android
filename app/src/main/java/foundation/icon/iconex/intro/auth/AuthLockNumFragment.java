package foundation.icon.iconex.intro.auth;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.CryptoUtil;
import foundation.icon.iconex.util.PreferenceUtil;

public class AuthLockNumFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = AuthLockNumFragment.class.getSimpleName();

    private TextView txtGuide;
    private EditText editNum;
    private ImageView num1, num2, num3, num4, num5, num6;

    private boolean isFingerprintInvalidated = false;

    public AuthLockNumFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static AuthLockNumFragment newInstance() {
        AuthLockNumFragment fragment = new AuthLockNumFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_auth_lock_num, container, false);

        txtGuide = v.findViewById(R.id.txt_guide);
        txtGuide.setText(getString(R.string.enterLockNum));

        editNum = v.findViewById(R.id.editNum);
        editNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setNumber(s.length());

                if (s.length() == 6) {
                    if (validateLockNum(editNum.getText().toString())) {
                        if (isFingerprintInvalidated)
                            mListener.onFingerprintInvalidated();
                        else
                            mListener.onLockNumUnlockSuccess();
                    } else {
                        editNum.setText("");
                        txtGuide.setText(getString(R.string.errRetryLockNum));
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        num1 = v.findViewById(R.id.num1);
        num2 = v.findViewById(R.id.num2);
        num3 = v.findViewById(R.id.num3);
        num4 = v.findViewById(R.id.num4);
        num5 = v.findViewById(R.id.num5);
        num6 = v.findViewById(R.id.num6);

        Button btnNum0 = v.findViewById(R.id.btnNum0);
        btnNum0.setOnClickListener(this);
        Button btnNum1 = v.findViewById(R.id.btnNum1);
        btnNum1.setOnClickListener(this);
        Button btnNum2 = v.findViewById(R.id.btnNum2);
        btnNum2.setOnClickListener(this);
        Button btnNum3 = v.findViewById(R.id.btnNum3);
        btnNum3.setOnClickListener(this);
        Button btnNum4 = v.findViewById(R.id.btnNum4);
        btnNum4.setOnClickListener(this);
        Button btnNum5 = v.findViewById(R.id.btnNum5);
        btnNum5.setOnClickListener(this);
        Button btnNum6 = v.findViewById(R.id.btnNum6);
        btnNum6.setOnClickListener(this);
        Button btnNum7 = v.findViewById(R.id.btnNum7);
        btnNum7.setOnClickListener(this);
        Button btnNum8 = v.findViewById(R.id.btnNum8);
        btnNum8.setOnClickListener(this);
        Button btnNum9 = v.findViewById(R.id.btnNum9);
        btnNum9.setOnClickListener(this);

        ImageView btnDel = v.findViewById(R.id.btn_delete);
        btnDel.setOnClickListener(this);

        ViewGroup lockNumLost = v.findViewById(R.id.layout_lost_lock_num);
        lockNumLost.setOnClickListener(this);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnLockNumAuthListener) {
            mListener = (OnLockNumAuthListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFingerprintLockListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        String number;

        switch (v.getId()) {
            case R.id.btnNum0:
                number = editNum.getText().toString();
                editNum.setText(number + "0");
                break;

            case R.id.btnNum1:
                number = editNum.getText().toString();
                editNum.setText(number + "1");
                break;

            case R.id.btnNum2:
                number = editNum.getText().toString();
                editNum.setText(number + "2");
                break;

            case R.id.btnNum3:
                number = editNum.getText().toString();
                editNum.setText(number + "3");
                break;

            case R.id.btnNum4:
                number = editNum.getText().toString();
                editNum.setText(number + "4");
                break;

            case R.id.btnNum5:
                number = editNum.getText().toString();
                editNum.setText(number + "5");
                break;

            case R.id.btnNum6:
                number = editNum.getText().toString();
                editNum.setText(number + "6");
                break;

            case R.id.btnNum7:
                number = editNum.getText().toString();
                editNum.setText(number + "7");
                break;

            case R.id.btnNum8:
                number = editNum.getText().toString();
                editNum.setText(number + "8");
                break;

            case R.id.btnNum9:
                number = editNum.getText().toString();
                editNum.setText(number + "9");
                break;

            case R.id.btn_delete:
                number = editNum.getText().toString();
                if (number.length() != 0)
                    editNum.setText(number.substring(0, number.length() - 1));
                break;

            case R.id.layout_lost_lock_num:
                mListener.onLostLockNum();
                break;
        }
    }

    private void setNumber(int length) {
        switch (length) {
            case 0:
                num1.setSelected(false);
                num2.setSelected(false);
                num3.setSelected(false);
                num4.setSelected(false);
                num5.setSelected(false);
                num6.setSelected(false);
                break;

            case 1:
                num1.setSelected(true);
                num2.setSelected(false);
                num3.setSelected(false);
                num4.setSelected(false);
                num5.setSelected(false);
                num6.setSelected(false);
                break;

            case 2:
                num1.setSelected(true);
                num2.setSelected(true);
                num3.setSelected(false);
                num4.setSelected(false);
                num5.setSelected(false);
                num6.setSelected(false);
                break;

            case 3:
                num1.setSelected(true);
                num2.setSelected(true);
                num3.setSelected(true);
                num4.setSelected(false);
                num5.setSelected(false);
                num6.setSelected(false);
                break;

            case 4:
                num1.setSelected(true);
                num2.setSelected(true);
                num3.setSelected(true);
                num4.setSelected(true);
                num5.setSelected(false);
                num6.setSelected(false);
                break;

            case 5:
                num1.setSelected(true);
                num2.setSelected(true);
                num3.setSelected(true);
                num4.setSelected(true);
                num5.setSelected(true);
                num6.setSelected(false);
                break;

            case 6:
                num1.setSelected(true);
                num2.setSelected(true);
                num3.setSelected(true);
                num4.setSelected(true);
                num5.setSelected(true);
                num6.setSelected(true);
                break;

        }
    }

    private boolean validateLockNum(final String number) {
        PreferenceUtil preferenceUtil = new PreferenceUtil(getActivity());
        String encTxt = preferenceUtil.getLockNum();
        String uuid = preferenceUtil.getUUID();
        String txt;

        try {
            txt = CryptoUtil.decryptText(encTxt, number);
        } catch (Exception e) {
            return false;
        }

        return txt.equals(uuid);
    }

    public void setInvalidated(boolean isInvalidated) {
        isFingerprintInvalidated = isInvalidated;
    }

    private OnLockNumAuthListener mListener;

    public interface OnLockNumAuthListener {
        void onLockNumUnlockSuccess();

        void onFingerprintInvalidated();

        void onLockNumUnlockFailed();

        void onLostLockNum();
    }
}
