package foundation.icon.iconex.wallet.transfer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.transfer.data.InputData;
import foundation.icon.iconex.widgets.MyEditText;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnterDataFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = EnterDataFragment.class.getSimpleName();

    private TextView txtDataSize, txtMod;
    private MyEditText editData;

    private ViewGroup layoutDelete;
    private Button btnDelete;

    private InputData data;
    private State state;

    private static final String ARG_DATA = "ARG_DATA";
    private static final String ARG_STATE = "ARG_STATE";

    private String beforeStr;
    private int maxSize;

    public EnterDataFragment() {
        // Required empty public constructor
    }

    public static EnterDataFragment newInstance(InputData data) {
        EnterDataFragment fragment = new EnterDataFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
            data = (InputData) getArguments().get(ARG_DATA);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        View v = inflater.inflate(R.layout.fragment_enter_data, container, false);

        ((TextView) v.findViewById(R.id.txt_title)).setText(R.string.data);
        ((TextView) v.findViewById(R.id.txt_mod)).setText(R.string.complete);
        v.findViewById(R.id.btn_close).setOnClickListener(this);

        layoutDelete = v.findViewById(R.id.layout_delete);

        txtMod = v.findViewById(R.id.txt_mod);
        txtMod.setVisibility(View.VISIBLE);
        txtMod.setOnClickListener(this);

        txtDataSize = v.findViewById(R.id.txt_limit);

        editData = v.findViewById(R.id.edit_data);
        editData.setOnKeyPreImeListener(new OnKeyPreImeListener() {
            @Override
            public void onBackPressed() {
                // Do nothing
            }
        });
        editData.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    String dataStr = s.toString();
                    BasicDialog dialog = new BasicDialog(getActivity());
                    dialog.setMessage(String.format(Locale.getDefault(), getString(R.string.errOverByteLimit), maxSize));
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            if (beforeStr != null) {
                                editData.setText(beforeStr);
                                editData.setSelection(editData.getText().toString().length());
                            } else {
                                editData.setText("");
                            }
                        }
                    });

                    long compareLength;
                    if (data.getDataType() == DataType.UTF)
                        compareLength = dataStr.getBytes().length;
                    else
                        compareLength = dataStr.getBytes().length / 2;

                    if (compareLength > maxSize * 1024) {
                        dialog.show();

                        if (beforeStr == null)
                            setDataSize(txtDataSize, 0);
                        else
                            setDataSize(txtDataSize, compareLength);
                    } else {
                        setDataSize(txtDataSize, compareLength);
                        beforeStr = dataStr;
                    }


                    txtMod.setTextColor(getResources().getColor(R.color.colorWhite));
                    txtMod.setEnabled(true);
                } else {
                    txtDataSize.setText(String.format(Locale.getDefault(), "%d KB", 0));
                    txtMod.setTextColor(getResources().getColor(R.color.buttonTextDisabled));
                    txtMod.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        btnDelete = v.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(this);

        if (data.getDataType() == DataType.UTF) {
            editData.setHint(R.string.hintUtfData);
            ((TextView) v.findViewById(R.id.txt_type)).setText("UTF-8");
        } else {
            editData.setHint(R.string.hintHexData);
            ((TextView) v.findViewById(R.id.txt_type)).setText("HEX");
        }

        maxSize = 250;
        ((TextView) v.findViewById(R.id.txt_max)).setText(String.format(Locale.getDefault(), getString(R.string.dataMaxSize), maxSize));

        if (data.getData() != null) {
            if (data.getData().isEmpty()) {
                txtMod.setTextColor(getResources().getColor(R.color.buttonTextDisabled));
                txtMod.setEnabled(false);
            } else {
                state = State.VIEW;

                if (data.getDataType() == DataType.UTF)
                    editData.setText(new String(Hex.decode(Utils.remove0x(data.getData()))));
                else
                    editData.setText(data.getData());

                layoutDelete.setVisibility(View.VISIBLE);
                editData.setFocusable(false);

                txtMod.setText(R.string.modified);
            }
        } else {
            txtMod.setTextColor(getResources().getColor(R.color.buttonTextDisabled));
            txtMod.setEnabled(false);
        }

        editData.requestFocus();

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnEnterDataLisnter) {
            mListener = (OnEnterDataLisnter) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEnterDataLisnter");
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
                showCancel();
                break;

            case R.id.txt_mod:
                if (state == State.VIEW) {
                    layoutDelete.setVisibility(View.GONE);
                    editData.setSelection(editData.getText().toString().length());

                    txtMod.setText(getString(R.string.complete));

                    editData.setFocusableInTouchMode(true);
                    editData.requestFocus();

                    state = State.INPUT;
                } else {
                    if (data.getDataType() == DataType.HEX) {
                        if (!editData.getText().toString().startsWith(MyConstants.PREFIX_HEX)) {
                            BasicDialog err = new BasicDialog(getActivity());
                            err.setMessage(getString(R.string.errInvalidData));
                            err.show();

                            break;
                        }

                        try {
                            Hex.decode(Utils.remove0x(editData.getText().toString()));
                            data.setData(Utils.checkPrefix(editData.getText().toString()));
                            getStepCost();
                        } catch (Exception e) {
                            BasicDialog err = new BasicDialog(getActivity());
                            err.setMessage(getString(R.string.errInvalidData));
                            err.show();
                        }
                    } else {
                        String dataStr = editData.getText().toString();
                        Log.d(TAG, "Hex.toHexString Start");
                        String hexStr = Hex.toHexString(dataStr.getBytes());
                        Log.d(TAG, "Hex.toHexString End");
                        data.setData(Utils.checkPrefix(hexStr));
                        getStepCost();
                    }
                }
                break;

            case R.id.btn_delete:
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());
                dialog.setMessage(getString(R.string.msgDeleteData));
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        if (mListener != null)
                            mListener.onDataDelete();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.show();
                break;
        }
    }

    private void showCancel() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(getActivity());
        dialog.setMessage(getString(R.string.cancelEnterData));
        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
            @Override
            public void onOk() {
                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                if (mListener != null)
                    mListener.onDataCancel(data);
            }

            @Override
            public void onCancel() {

            }
        });
        dialog.show();
    }

    private boolean isHexCharSet(char c) {
        if (c >= 48 && c <= 57) {
            return true;
        } else if (c >= 65 && c <= 90) {
            return true;
        } else if (c >= 97 && c <= 122) {
            return true;
        } else {
            return false;
        }
    }

    private void setDataSize(TextView txtLength, long byteLength) {
        if (byteLength <= 512 * 1024)
            txtLength.setTextColor(getResources().getColor(R.color.colorText));
        else
            txtLength.setTextColor(getResources().getColor(R.color.colorWarning));

        if (byteLength < 1024) {
            txtLength.setText(String.format(Locale.getDefault(), "%s", Long.toString(byteLength) + " B"));
            return;
        } else {
            txtLength.setText(String.format(Locale.getDefault(), "%s", Long.toString(byteLength / 1024) + " KB"));
            return;
        }
    }

    private boolean checkBalance(int stepLimit) {
        BigInteger limit = new BigInteger(Integer.toString(stepLimit));
        BigInteger fee = limit.multiply(data.getStepPrice());
        BigInteger total = data.getAmount().add(fee);

        Log.d(TAG, "Balance=" + data.getBalance().toString());
        Log.d(TAG, "totla=" + total.toString());

        if (data.getBalance().compareTo(total) < 0)
            return false;
        else
            return true;
    }

    private void getStepCost() {
        String url;
        if (ICONexApp.network == MyConstants.NETWORK_MAIN)
            url = ServiceConstants.TRUSTED_HOST_MAIN;
        else if (ICONexApp.network == MyConstants.NETWORK_TEST)
            url = ServiceConstants.TRUSTED_HOST_TEST;
        else
            url = ServiceConstants.DEV_HOST;

        try {
            LoopChainClient client = new LoopChainClient(url);
            Call<LCResponse> response = client.getStepCost(1234, data.getAddress());
            response.enqueue(new Callback<LCResponse>() {
                @Override
                public void onResponse(Call<LCResponse> call, Response<LCResponse> response) {
                    if (response.isSuccessful()) {
                        JsonObject result = response.body().getResult().getAsJsonObject();
                        int input = Integer.decode(result.get("input").getAsString());
                        int dataLength = editData.getText().toString().getBytes().length;
                        int stepLimit = Integer.decode(result.get("default").getAsString()) + input * dataLength;

                        if (checkBalance(stepLimit)) {
                            data.setStepCost(stepLimit);

                            if (mListener != null)
                                mListener.onSetData(data);
                        } else {
                            BasicDialog dialog = new BasicDialog(getActivity());
                            dialog.setMessage(getString(R.string.errIcxOwnNotEnough));
                            dialog.show();

                            data.setData(null);
                        }
                    } else {

                    }
                }

                @Override
                public void onFailure(Call<LCResponse> call, Throwable t) {

                }
            });
        } catch (Exception e) {

        }
    }

    public enum DataType {
        UTF,
        HEX
    }

    public enum State {
        INPUT,
        VIEW
    }

    private OnEnterDataLisnter mListener;

    public interface OnEnterDataLisnter {
        void onSetData(InputData data);

        void onDataCancel(InputData data);

        void onDataDelete();
    }
}
