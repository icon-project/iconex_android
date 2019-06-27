package foundation.icon.sample_iconex_connect;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

public class EnterDataDialog extends Dialog {

    private Context context;
    private Type type;

    private EditText editTo, editValue, editScore,
            editDataType, editData, editJsonRpc;

    public EnterDataDialog(@Nullable Context context, Type type, DialogCallback callback) {
        super(context);

        this.context = context;
        this.type = type;
        mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.dialog_enter_data);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        editTo = findViewById(R.id.edit_to);
        editValue = findViewById(R.id.edit_value);
        editScore = findViewById(R.id.edit_score);
        editDataType = findViewById(R.id.edit_data_type);
        editData = findViewById(R.id.edit_data);
        editJsonRpc = findViewById(R.id.edit_json_rpc);

        findViewById(R.id.btn_cancel).setOnClickListener(view -> dismiss());
        findViewById(R.id.btn_confirm).setOnClickListener(view -> {

            switch (type) {
                case SEND_ICX:
                    mCallback.onSendIcx(editTo.getText().toString(),
                            editValue.getText().toString());
                    break;

                case SEND_MESSAGE:
                    mCallback.onSendMessage(editTo.getText().toString(),
                            editValue.getText().toString(),
                            editData.getText().toString());
                    break;

                case SEND_TOKEN:
                    mCallback.onSendToken(editTo.getText().toString(),
                            editValue.getText().toString(),
                            editScore.getText().toString());
                    break;

                case SEND_CONTRACT:
                    mCallback.onSendContract(editTo.getText().toString(),
                            editDataType.getText().toString(),
                            editData.getText().toString());
                    break;

                case SEND_JSON_RPC:
                    mCallback.onSendJsonRpc(editJsonRpc.getText().toString());
                    break;
            }

            dismiss();
        });

        setView();
    }

    private void setView() {

        switch (type) {
            case SEND_ICX:
                editScore.setVisibility(View.GONE);
                editDataType.setVisibility(View.GONE);
                editData.setVisibility(View.GONE);
                editJsonRpc.setVisibility(View.GONE);
                break;

            case SEND_MESSAGE:
                editScore.setVisibility(View.GONE);
                editDataType.setVisibility(View.GONE);
                editJsonRpc.setVisibility(View.GONE);
                break;

            case SEND_TOKEN:
                editDataType.setVisibility(View.GONE);
                editData.setVisibility(View.GONE);
                editJsonRpc.setVisibility(View.GONE);
                break;

            case SEND_CONTRACT:
                editValue.setVisibility(View.GONE);
                editScore.setVisibility(View.GONE);
                editJsonRpc.setVisibility(View.GONE);
                break;

            case SEND_JSON_RPC:
                editTo.setVisibility(View.GONE);
                editValue.setVisibility(View.GONE);
                editScore.setVisibility(View.GONE);
                editDataType.setVisibility(View.GONE);
                editData.setVisibility(View.GONE);
                break;
        }
    }

    private DialogCallback mCallback;

    public interface DialogCallback {
        void onSendIcx(String to, String value);

        void onSendMessage(String to, String value, String data);

        void onSendToken(String to, String value, String score);

        void onSendContract(String to, String dataType, String data);

        void onSendJsonRpc(String jsonRpc);
    }

    public enum Type {
        SEND_ICX,
        SEND_MESSAGE,
        SEND_TOKEN,
        SEND_CONTRACT,
        SEND_JSON_RPC
    }
}
