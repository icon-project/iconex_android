package foundation.icon.iconex.wallet.load;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.MyEditText;

public class LoadInputWalletNameFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = LoadInputWalletNameFragment.class.getSimpleName();

    private MyEditText editAlias;
    private Button btnNameDelete;
    private View lineName;
    private TextView txtNameWarning;

    private Button btnDone, btnBack;

    private String mCoinType;
    private String mKeyStore;

    private String beforeStr;

    private final int OK = 0;
    private final int ALIAS_DUP = 1;
    private final int ALIAS_EMPTY = 2;

    private OnInputWalletNameCallback mListener;

    public LoadInputWalletNameFragment() {
        // Required empty public constructor
    }

    public static LoadInputWalletNameFragment newInstance() {
        LoadInputWalletNameFragment fragment = new LoadInputWalletNameFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_input_wallet_name, container, false);

        editAlias = v.findViewById(R.id.edit_name);
        editAlias.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });
        editAlias.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (Utils.checkByteLength(s.toString()) > 16) {
                        editAlias.setText(beforeStr);
                        editAlias.setSelection(editAlias.getText().toString().length());
                    } else {
                        beforeStr = s.toString();
                    }

                    btnNameDelete.setVisibility(View.VISIBLE);
                } else {
                    if (editAlias.isFocused())
                        lineName.setBackgroundColor(getActivity().getResources().getColor(R.color.editActivated));
                    else
                        lineName.setBackgroundColor(getActivity().getResources().getColor(R.color.editNormal));
                    txtNameWarning.setVisibility(View.INVISIBLE);
                    btnNameDelete.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        editAlias.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                int aliasValidate = checkAlias(editAlias.getText().toString());
                switch (aliasValidate) {
                    case ALIAS_DUP:
                        lineName.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                        txtNameWarning.setVisibility(View.VISIBLE);
                        txtNameWarning.setText(getString(R.string.duplicateWalletAlias));
                        btnDone.setEnabled(false);
                        break;

                    case ALIAS_EMPTY:
                        lineName.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                        txtNameWarning.setVisibility(View.VISIBLE);
                        txtNameWarning.setText(getString(R.string.errAliasEmpty));
                        btnDone.setEnabled(false);
                        break;

                    default:
                        if (editAlias.hasFocus())
                            lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
                        else
                            lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));

                        btnDone.setEnabled(true);
                        txtNameWarning.setVisibility(View.INVISIBLE);
                        break;
                }
            }
        });
        editAlias.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {

                    int aliasValidate = checkAlias(editAlias.getText().toString());
                    switch (aliasValidate) {
                        case ALIAS_DUP:
                            lineName.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                            txtNameWarning.setVisibility(View.VISIBLE);
                            txtNameWarning.setText(getString(R.string.duplicateWalletAlias));
                            btnDone.setEnabled(false);
                            break;

                        case ALIAS_EMPTY:
                            lineName.setBackgroundColor(getResources().getColor(R.color.colorWarning));
                            txtNameWarning.setVisibility(View.VISIBLE);
                            txtNameWarning.setText(getString(R.string.errAliasEmpty));
                            btnDone.setEnabled(false);
                            break;

                        default:
                            if (editAlias.hasFocus())
                                lineName.setBackgroundColor(getResources().getColor(R.color.editActivated));
                            else
                                lineName.setBackgroundColor(getResources().getColor(R.color.editNormal));

                            btnDone.setEnabled(true);
                            txtNameWarning.setVisibility(View.INVISIBLE);
                            break;
                    }
                }
                return false;
            }
        });

        lineName = v.findViewById(R.id.line_name);
        txtNameWarning = v.findViewById(R.id.txt_name_warning);
        btnNameDelete = v.findViewById(R.id.btn_name_delete);
        btnNameDelete.setOnClickListener(this);

        btnDone = v.findViewById(R.id.btn_done);
        btnDone.setOnClickListener(this);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_name_delete:
                editAlias.setText("");
                break;

            case R.id.btn_done:
                mListener.onDoneLoadWalletByKeyStore(Utils.strip(editAlias.getText().toString()));
                break;

            case R.id.btn_back:
                clear();
                mListener.onNameBack();
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnInputWalletNameCallback) {
            mListener = (OnInputWalletNameCallback) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnInputWalletNameCallback");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private int checkAlias(String target) {
        String alias = Utils.strip(target);
        if (alias.isEmpty())
            return ALIAS_EMPTY;

        for (Wallet info : ICONexApp.mWallets) {
            if (info.getAlias().equals(alias)) {
                return ALIAS_DUP;
            }
        }

        return OK;
    }

    public void setKeyStore(String coinType, String keyStore) {
        mCoinType = coinType;
        mKeyStore = keyStore;
    }

    private void clear() {
        editAlias.setText("");
    }

    public interface OnInputWalletNameCallback {
        void onDoneLoadWalletByKeyStore(String name);

        void onNameBack();
    }
}
