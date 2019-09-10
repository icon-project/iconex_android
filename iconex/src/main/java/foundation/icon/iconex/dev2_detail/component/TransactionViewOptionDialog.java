package foundation.icon.iconex.dev2_detail.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.w3c.dom.Text;

import foundation.icon.iconex.R;

public class TransactionViewOptionDialog extends BottomSheetDialog implements View.OnClickListener {

    public ImageButton btnClose;
    public TextView txtTitle;
    public Button btnConfirm;

    public Button btnAll;
    public Button btnSend;
    public Button btnDeposit;

    private SelectType mSelected = null;

    public interface OnSelectListener {
        void onSelect(SelectType selectType);
    }

    private OnSelectListener mOnSelectListener = null;

    public TransactionViewOptionDialog(@NonNull Context context, SelectType selected, OnSelectListener listener) {
        super(context);
        mSelected = selected;
        mOnSelectListener = listener;
        viewInit();
        updateSelected(mSelected);

    }

    private void viewInit() {
        setContentView(R.layout.dialog_view_options);

        btnClose = findViewById(R.id.btn_close);
        txtTitle = findViewById(R.id.txt_title);
        btnConfirm = findViewById(R.id.btn_confirm);

        btnAll = findViewById(R.id.btn_all);
        btnSend = findViewById(R.id.btn_send);
        btnDeposit = findViewById(R.id.btn_deposit);


        btnClose.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnAll.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnDeposit.setOnClickListener(this);
    }

    private void updateSelected(SelectType selected) {
        btnAll.setEnabled(selected != SelectType.All);
        btnSend.setEnabled(selected != SelectType.Send);
        btnDeposit.setEnabled(selected != SelectType.Deposit);
        mSelected = selected;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close: {
                dismiss();
            } break;
            case R.id.btn_confirm: {
                mOnSelectListener.onSelect(mSelected);
                dismiss();
            } break;
            case R.id.btn_all: {
                updateSelected(SelectType.All);
            } break;
            case R.id.btn_send: {
                updateSelected(SelectType.Send);
            } break;
            case R.id.btn_deposit: {
                updateSelected(SelectType.Deposit);
            }
        }
    }
}
