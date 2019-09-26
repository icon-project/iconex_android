package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.widget.Button;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.transfer.IconEnterDataFragment;

public class DataTypeDialog extends BottomSheetDialog implements View.OnClickListener {

    private Button btnUtf, btnHex;

    private IconEnterDataFragment.DataType type;

    public DataTypeDialog(@NonNull Context context) {
        super(context, R.style.MyBottomSheetDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_data_type);

        findViewById(R.id.btn_close).setOnClickListener(this);
        findViewById(R.id.txt_confirm).setOnClickListener(this);

        btnUtf = findViewById(R.id.btn_utf);
        btnUtf.setOnClickListener(this);
        btnHex = findViewById(R.id.btn_hex);
        btnHex.setOnClickListener(this);

        btnUtf.setSelected(true);
        type = IconEnterDataFragment.DataType.UTF;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                dismiss();
                break;

            case R.id.btn_utf:
                btnUtf.setSelected(true);
                btnHex.setSelected(false);

                type = IconEnterDataFragment.DataType.UTF;
                break;

            case R.id.btn_hex:
                btnUtf.setSelected(false);
                btnHex.setSelected(true);

                type = IconEnterDataFragment.DataType.HEX;
                break;

            case R.id.txt_confirm:
                if (mListener != null)
                    mListener.onSelect(type);
                break;
        }
    }

    public void setOnTypeListener(OnTypeListener listener) {
        mListener = listener;
    }

    private OnTypeListener mListener;

    public interface OnTypeListener {
        void onSelect(IconEnterDataFragment.DataType type);
    }
}
