package foundation.icon.sample_iconex_connect;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class InputDialog extends Dialog implements View.OnClickListener {

    private final Context context;
    private final SampleApp.Method method;

    private EditText editVersion, editTo, editFrom, editValue, editStepLimit, editTimestamp, editNid,
            editNonce, editMessage, editScore, editDataType, editData;

    public InputDialog(@NonNull Context context, SampleApp.Method method) {
        super(context);

        this.context = context;
        this.method = method;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_input);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        editVersion = findViewById(R.id.edit_version);
        editFrom = findViewById(R.id.edit_from);
        editTo = findViewById(R.id.edit_to);
        editValue = findViewById(R.id.edit_value);
        editStepLimit = findViewById(R.id.edit_step_limit);
        editTimestamp = findViewById(R.id.edit_timestamp);
        editNid = findViewById(R.id.edit_nid);
        editNonce = findViewById(R.id.edit_nonce);
        editMessage = findViewById(R.id.edit_message);
        editScore = findViewById(R.id.edit_score);
        editScore.setText(SampleApp.score);
        editDataType = findViewById(R.id.edit_data_type);
        editData = findViewById(R.id.edit_data);

        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.btn_confirm).setOnClickListener(this);

        switch (method) {
            case Sign:
                editMessage.setVisibility(View.GONE);
                editScore.setVisibility(View.GONE);
                break;

            case SendIcx:
                editVersion.setVisibility(View.GONE);
                editFrom.setVisibility(View.GONE);
                editStepLimit.setVisibility(View.GONE);
                editTimestamp.setVisibility(View.GONE);
                editNid.setVisibility(View.GONE);
                editNonce.setVisibility(View.GONE);
                editMessage.setVisibility(View.GONE);
                editScore.setVisibility(View.GONE);
                editDataType.setVisibility(View.GONE);
                editData.setVisibility(View.GONE);
                break;

            case SendToken:
                editVersion.setVisibility(View.GONE);
                editFrom.setVisibility(View.GONE);
                editStepLimit.setVisibility(View.GONE);
                editTimestamp.setVisibility(View.GONE);
                editMessage.setVisibility(View.GONE);
                editNid.setVisibility(View.GONE);
                editNonce.setVisibility(View.GONE);
                editDataType.setVisibility(View.GONE);
                editData.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_cancel:
                dismiss();
                break;

            case R.id.btn_confirm:
                RequestData requestData = buildRequestData();
                mListener.onConfirm(requestData);
                dismiss();
                break;
        }
    }

    private RequestData buildRequestData() {
        RequestData requestData;
        switch (method) {
            case Sign:
                return requestData = new RequestData.Builder()
                        .version(editVersion.getText().toString())
                        .from(editFrom.getText().toString())
                        .to(editTo.getText().toString())
                        .value(editValue.getText().toString())
                        .stepLimit(editStepLimit.getText().toString())
                        .timestamp(editTimestamp.getText().toString())
                        .nid(editNid.getText().toString())
                        .nonce(editNonce.getText().toString())
                        .dataType(editDataType.getText().toString())
                        .data(editData.getText().toString())
                        .build();

            case SendIcx:
                return requestData = new RequestData.Builder()
                        .from(SampleApp.from)
                        .to(editTo.getText().toString())
                        .value(editValue.getText().toString())
                        .message(editMessage.getText().toString())
                        .build();

            case SendToken:
                return requestData = new RequestData.Builder()
                        .from(SampleApp.from)
                        .to(editTo.getText().toString())
                        .value(editValue.getText().toString())
                        .score(editScore.getText().toString())
                        .build();
        }

        return null;
    }

    private OnClickListener mListener;

    public void setListener(OnClickListener listener) {
        mListener = listener;
    }

    public interface OnClickListener {
        void onConfirm(RequestData requestData);
    }
}
