package foundation.icon.iconex.wallet.transfer;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.Locale;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;
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

public class EnterDataActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = EnterDataActivity.class.getSimpleName();

    private TextView txtDataLimit, txtMod;
    private MyEditText editData;

    private InputData data;

    public static final String ARG_DATA = "ARG_DATA";
    public static final int RES_DATA = 1111;
    public static final int RES_CANCEL = 2222;

    private static String beforeStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_data);

        if (getIntent() != null)
            data = (InputData) getIntent().getSerializableExtra(ARG_DATA);

        ((TextView) findViewById(R.id.txt_title)).setText(R.string.data);
        ((TextView) findViewById(R.id.txt_mod)).setText(R.string.complete);
        findViewById(R.id.btn_close).setOnClickListener(this);

        txtMod = findViewById(R.id.txt_mod);
        txtMod.setVisibility(View.VISIBLE);
        txtMod.setOnClickListener(this);

        txtDataLimit = findViewById(R.id.txt_limit);

        editData = findViewById(R.id.edit_data);
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
                    if (dataStr.getBytes().length > 512 * 1024) {
                        BasicDialog dialog = new BasicDialog(EnterDataActivity.this);
                        dialog.setMessage(getString(R.string.errOverByteLimit));
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                editData.setText(beforeStr);
                                editData.setSelection(editData.getText().toString().length());
                            }
                        });
                        dialog.show();
                        setDataLength(txtDataLimit, beforeStr.getBytes().length);
                    } else {
                        setDataLength(txtDataLimit, dataStr.getBytes().length);
                        beforeStr = dataStr;
                    }

                    txtMod.setTextColor(getResources().getColor(R.color.colorWhite));
                    txtMod.setEnabled(true);
                } else {
                    txtDataLimit.setText(String.format(Locale.getDefault(), "%d KB", 0));
                    txtMod.setTextColor(getResources().getColor(R.color.buttonTextDisabled));
                    txtMod.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        if (data.getDataType() == DataType.UTF) {
            editData.setHint(R.string.hintUtfData);
            ((TextView) findViewById(R.id.txt_type)).setText("UTF-8");
        } else {
            editData.setHint(R.string.hintHexData);
            ((TextView) findViewById(R.id.txt_type)).setText("HEX");
        }

        if (data.getData() != null) {
            if (data.getData().isEmpty()) {
                txtMod.setTextColor(getResources().getColor(R.color.buttonTextDisabled));
                txtMod.setEnabled(false);
            } else {
                if (data.getDataType() == DataType.UTF)
                    editData.setText(new String(Hex.decode(Utils.remove0x(data.getData()))));
                else
                    editData.setText(data.getData());

                editData.setSelection(editData.getText().toString().length());
            }
        } else {
            txtMod.setTextColor(getResources().getColor(R.color.buttonTextDisabled));
            txtMod.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                showCancel();
                break;

            case R.id.txt_mod:
                if (data.getDataType() == DataType.HEX) {
                    try {
                        Hex.decode(Utils.remove0x(editData.getText().toString()));
                        data.setData(Utils.checkPrefix(editData.getText().toString()));
                        getStepCost();
                    } catch (Exception e) {
                        BasicDialog err = new BasicDialog(this);
                        err.setMessage(getString(R.string.errInvalidData));
                        err.show();
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

    @Override
    public void onBackPressed() {
        showCancel();
    }

    private void showCancel() {
        Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
        dialog.setMessage(getString(R.string.cancelEnterData));
        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
            @Override
            public void onOk() {
                setResult(RES_CANCEL);
                finish();
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

    private void setDataLength(TextView txtLength, long byteLength) {
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

//        if (byteLength % 1024 > 0) {
//
//            return;
//        } else {
//            txtLength.setText(String.format(Locale.getDefault(), "%s", Long.toString(byteLength / 1024) + " KB"));
//            return;
//        }
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
                        int dataLength = editData.getText().toString().getBytes().length;
                        int stepLimit = Integer.decode(result.get("default").getAsString()) + input * dataLength;

                        if (checkBalance(stepLimit)) {
                            data.setStepCost(stepLimit);

                            Intent dataIntent = new Intent();
                            dataIntent.putExtra(ARG_DATA, data);
                            setResult(RES_DATA, dataIntent);
                            finish();
                        } else {
                            BasicDialog dialog = new BasicDialog(EnterDataActivity.this);
                            dialog.setMessage(getString(R.string.errIcxOwnNotEnough));
                            dialog.show();
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
}
