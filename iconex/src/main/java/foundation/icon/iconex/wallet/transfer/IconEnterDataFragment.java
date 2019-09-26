package foundation.icon.iconex.wallet.transfer;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
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
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.wallet.transfer.data.InputData;
import foundation.icon.iconex.widgets.MyEditText;
import kotlin.jvm.functions.Function1;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IconEnterDataFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = IconEnterDataFragment.class.getSimpleName();

    private TextView txtDataSize;
    private MyEditText editData;

    private ViewGroup layoutDataLimit;
    private ViewGroup layoutComplete;
    private Button btnComplete;

    private TextView btnOption;

    private InputData data;
    private State state;

    private static final String ARG_DATA = "ARG_DATA";
    private static final String ARG_STATE = "ARG_STATE";

    private String beforeStr;
    private int maxSize;

    public IconEnterDataFragment() {
        // Required empty public constructor
    }

    public static IconEnterDataFragment newInstance(InputData data) {
        IconEnterDataFragment fragment = new IconEnterDataFragment();
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

        v.findViewById(R.id.btn_close).setOnClickListener(this);

        btnOption = v.findViewById(R.id.btn_option);
        btnComplete = v.findViewById(R.id.btn_complete);
        layoutComplete = v.findViewById(R.id.layout_complete);
        layoutDataLimit = v.findViewById(R.id.layout_data_limit);

        btnOption.setOnClickListener(this);
        btnComplete.setOnClickListener(this);

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

                    btnComplete.setEnabled(true);
                } else {
                    txtDataSize.setText(String.format(Locale.getDefault(), "%d KB", 0));
                    btnComplete.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (data.getDataType() == DataType.UTF) {
            editData.setHint(R.string.hintUtfData);
            ((TextView) v.findViewById(R.id.txt_type)).setText("UTF-8");
        } else {
            editData.setHint(R.string.hintHexData);
            ((TextView) v.findViewById(R.id.txt_type)).setText("HEX");
        }

        maxSize = 250;
        ((TextView) v.findViewById(R.id.txt_max)).setText(String.format(Locale.getDefault(), getString(R.string.dataMaxSize), maxSize));

        if (data.getData() != null && !data.getData().isEmpty()) {
            // view mode
            state = State.VIEW;

            if (data.getDataType() == DataType.UTF)
                editData.setText(new String(Hex.decode(Utils.remove0x(data.getData()))));
            else
                editData.setText(data.getData());

            editData.setFocusable(false);
            btnOption.setText(getString(R.string.modified));
            layoutComplete.setVisibility(View.GONE);
            layoutDataLimit.setVisibility(View.GONE);

        } // else new data

        btnOption.setEnabled(data.getData() != null && !data.getData().isEmpty());
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

            case R.id.btn_option:
                if (state == State.VIEW) {
                    // btn_option.getText() == Modify
                    btnOption.setText(getString(R.string.delete));
                    editData.setSelection(editData.getText().toString().length());
                    layoutComplete.setVisibility(View.VISIBLE);
                    layoutDataLimit.setVisibility(View.VISIBLE);

                    editData.setFocusableInTouchMode(true);
                    editData.requestFocus();

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editData, InputMethodManager.SHOW_IMPLICIT);

                    state = State.INPUT;
                } else {
                    // btn_option.getText() == Delete
                    MessageDialog messageDialog = new MessageDialog(getActivity());
                    messageDialog.setTitleText(getString(R.string.msgDeleteData));
                    messageDialog.setSingleButton(false);
                    messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                        @Override
                        public Boolean invoke(View view) {
                            if (mListener != null)
                                mListener.onDataDelete();
                            return true;
                        }
                    });
                    messageDialog.show();
                }
                break;

            case R.id.btn_complete:
                if (data.getDataType() == DataType.HEX) {
                    if (!editData.getText().toString().startsWith(MyConstants.PREFIX_HEX)) {
                        MessageDialog messageDialog = new MessageDialog(getActivity());
                        messageDialog.setTitleText(getString(R.string.errInvalidData));
                        messageDialog.show();
                        break;
                    }
                    try {
                        Hex.decode(Utils.remove0x(editData.getText().toString()));
                        data.setData(Utils.checkPrefix(editData.getText().toString()));
                        getStepCost();
                    } catch (Exception e) {
                        MessageDialog messageDialog = new MessageDialog(getActivity());
                        messageDialog.setTitleText(getString(R.string.errInvalidData));
                        messageDialog.show();
                    }
                } else {
                    String dataStr = editData.getText().toString();
                    String hexStr = Hex.toHexString(dataStr.getBytes());
                    data.setData(Utils.checkPrefix(hexStr));
                    getStepCost();
                }
                break;
        }
    }

    public void showCancel() {
        if (data.getData() == null && editData.getText().length() > 0) {
            MessageDialog messageDialog = new MessageDialog(getActivity());
            messageDialog.setSingleButton(false);
            messageDialog.setTitleText(getString(R.string.cancelEnterData));
            messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    if (mListener != null)
                        mListener.onDataCancel(data);
                    return true;
                }
            });
            messageDialog.show();
        } else {
            if (mListener != null)
                mListener.onDataCancel(data);
        }
    }

    private void setDataSize(TextView txtLength, long byteLength) {
        if (byteLength <= 512 * 1024)
            txtLength.setTextColor(getResources().getColor(R.color.colorText));
        else
            txtLength.setTextColor(getResources().getColor(R.color.colorWarning));

        txtLength.setText(String.format(Locale.getDefault(), "%.0f KB",
                Math.floor(byteLength / 1024) + (byteLength % 1024 == 0 ? 0 : 1)));

    }

    private boolean checkBalance(int stepLimit) {
        BigInteger limit = new BigInteger(Integer.toString(stepLimit));
        BigInteger fee = limit.multiply(data.getStepPrice());
        BigInteger total = data.getAmount().add(fee);

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
                        int dataLength = (data.getData() + "\"\"").getBytes().length;
                        Log.d(TAG, "data=" + (data.getData() + "\"\""));
                        int stepLimit = Integer.decode(result.get("default").getAsString()) + input * dataLength;

                        if (checkBalance(stepLimit)) {
                            data.setStepCost(stepLimit);

                            if (mListener != null)
                                mListener.onSetData(data);
                        } else {
                            MessageDialog messageDialog = new MessageDialog(getActivity());
                            messageDialog.setTitleText(getString(R.string.errIcxOwnNotEnough));
                            messageDialog.show();

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
