package foundation.icon.iconex.menu.lock;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.UUID;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.CryptoUtil;
import foundation.icon.iconex.util.PreferenceUtil;

public class SetLockNumFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SetLockNumFragment.class.getSimpleName();

    private static final String ARG_TYPE = "ARG_TYPE";

    private TextView txtGuide;
    private EditText editNum;
    private ImageView i1, i2, i3, i4, i5, i6;

    private TYPE mType = null;

    private String step1 = "";
    private boolean isCorrect = false;

    public SetLockNumFragment() {
        // Required empty public constructor
    }

    public static SetLockNumFragment newInstance(TYPE type) {
        SetLockNumFragment fragment = new SetLockNumFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            mType = (TYPE) getArguments().get(ARG_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_set_lock_num, container, false);

        txtGuide = v.findViewById(R.id.txt_lock_num_guide);
        editNum = v.findViewById(R.id.edit_lock_num);
        editNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setCircle(s.length());

                if (s.length() == 6) {
                    switch (mType) {
                        case USE:
                            if (step1.isEmpty()) {
                                step1 = s.toString();
                                txtGuide.setText(getString(R.string.checkLockNumGuide));
                                editNum.setText("");
                            } else {
                                if (checkLockNumber(step1, editNum.getText().toString())) {
                                    saveLockNumber(editNum.getText().toString());
                                    mListener.onLockNumBack();
                                } else {
                                    txtGuide.setText(getString(R.string.errCheckLockNum));
                                    step1 = "";
                                    editNum.setText("");
                                }
                            }
                            break;

                        case RESET:
                            if (isCorrect) {
                                if (step1.isEmpty()) {
                                    if (validateLockNumber(editNum.getText().toString())) {
                                        txtGuide.setText(getString(R.string.errPasscodeSame));
                                        editNum.setText("");
                                    } else {
                                        step1 = s.toString();
                                        txtGuide.setText(getString(R.string.checkLockNumGuide));
                                        editNum.setText("");
                                    }
                                } else {
                                    if (checkLockNumber(step1, editNum.getText().toString())) {
                                        saveLockNumber(editNum.getText().toString());
                                        mListener.onLockNumBack();
                                    } else {
                                        txtGuide.setText(getString(R.string.errCheckLockNum));
                                        step1 = "";
                                        editNum.setText("");
                                    }
                                }
                            } else {
                                if (validateLockNumber(editNum.getText().toString())) {
                                    txtGuide.setText(getString(R.string.newLockNum));
                                    editNum.setText("");
                                    isCorrect = true;
                                } else {
                                    editNum.setText("");
                                    txtGuide.setText(getString(R.string.errCurrentLockNum));
                                }
                            }
                            break;

                        case DISUSE:
                            if (validateLockNumber(editNum.getText().toString())) {
                                disableAppLock();
                                mListener.onLockNumBack();
                            } else {
                                editNum.setText("");
                                txtGuide.setText(getString(R.string.errCurrentLockNum));
                            }
                            break;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        i1 = v.findViewById(R.id.lock_num1);
        i2 = v.findViewById(R.id.lock_num2);
        i3 = v.findViewById(R.id.lock_num3);
        i4 = v.findViewById(R.id.lock_num4);
        i5 = v.findViewById(R.id.lock_num5);
        i6 = v.findViewById(R.id.lock_num6);

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

        ImageView btnDel = v.findViewById(R.id.delete);
        btnDel.setOnClickListener(this);

        switch (mType) {
            case USE:
                txtGuide.setText(getString(R.string.lockNumGuide));
                break;

            case RESET:
            case DISUSE:
                txtGuide.setText(getString(R.string.currentLockNum));
                break;
        }

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSetLockNumListener) {
            mListener = (OnSetLockNumListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSetLockNumListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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

            case R.id.delete:
                number = editNum.getText().toString();
                if (!number.isEmpty())
                    editNum.setText(number.substring(0, number.length() - 1));
                break;
        }
    }

    private void setCircle(int length) {
        switch (length) {
            case 0:
                i1.setSelected(false);
                i2.setSelected(false);
                i3.setSelected(false);
                i4.setSelected(false);
                i5.setSelected(false);
                i6.setSelected(false);
                break;

            case 1:
                i1.setSelected(true);
                i2.setSelected(false);
                i3.setSelected(false);
                i4.setSelected(false);
                i5.setSelected(false);
                i6.setSelected(false);
                break;

            case 2:
                i1.setSelected(true);
                i2.setSelected(true);
                i3.setSelected(false);
                i4.setSelected(false);
                i5.setSelected(false);
                i6.setSelected(false);
                break;

            case 3:
                i1.setSelected(true);
                i2.setSelected(true);
                i3.setSelected(true);
                i4.setSelected(false);
                i5.setSelected(false);
                i6.setSelected(false);
                break;

            case 4:
                i1.setSelected(true);
                i2.setSelected(true);
                i3.setSelected(true);
                i4.setSelected(true);
                i5.setSelected(false);
                i6.setSelected(false);
                break;

            case 5:
                i1.setSelected(true);
                i2.setSelected(true);
                i3.setSelected(true);
                i4.setSelected(true);
                i5.setSelected(true);
                i6.setSelected(false);
                break;

            case 6:
                i1.setSelected(true);
                i2.setSelected(true);
                i3.setSelected(true);
                i4.setSelected(true);
                i5.setSelected(true);
                i6.setSelected(true);
                break;
        }
    }

    private boolean checkLockNumber(String number1, String number2) {
        if (number1.equals(number2))
            return true;
        else
            return false;
    }

    private boolean validateLockNumber(String number) {
        PreferenceUtil preference = new PreferenceUtil(getActivity());
        String encTxt = preference.getLockNum();
        String uuid = preference.getUUID();
        String txt;

        try {
            txt = CryptoUtil.decryptText(encTxt, number);
        } catch (Exception e) {
            return false;
        }

        return txt.equals(uuid);
    }

    private void saveLockNumber(String number) {
        try {
            UUID uuid = UUID.randomUUID();
            String encText = CryptoUtil.encryptText(uuid.toString(), number);

            PreferenceUtil mPreference = new PreferenceUtil(getActivity());
            mPreference.saveAppLock(true);
            mPreference.saveUUID(uuid.toString());
            mPreference.saveLockNum(encText);

            mPreference.loadPreference();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void disableAppLock() {
        try {
            PreferenceUtil mPreference = new PreferenceUtil(getActivity());
            mPreference.saveAppLock(false);
            mPreference.saveUseFingerprint(false);

            mPreference.loadPreference();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OnSetLockNumListener mListener;

    public interface OnSetLockNumListener {
        void onLockNumBack();
    }

    public enum TYPE {
        USE,
        RESET,
        DISUSE
    }
}
