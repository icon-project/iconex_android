package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 3. 28..
 */

public class SearchConditionDialog extends BottomSheetDialog implements View.OnClickListener {

    private Context mContext;

    private Button btnClose;
    private TextView btnConfirm;

    private Button btnStateDone, btnStatePending;
    private Button btnWholeness, btnRem, btnDep;

    private MyConstants.TxState mState;
    private MyConstants.TxType mType;

    public SearchConditionDialog(@NonNull Context context, @NonNull MyConstants.TxState state, @NonNull MyConstants.TxType type) {
        super(context, R.style.MyBottomSheetDialog);

        mContext = context;
        mState = state;
        mType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_search_condition);

        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);
        btnConfirm = findViewById(R.id.txt_confirm);
        btnConfirm.setOnClickListener(this);

        btnStateDone = findViewById(R.id.btn_state_done);
        btnStateDone.setOnClickListener(this);
        btnStatePending = findViewById(R.id.btn_state_pending);
        btnStatePending.setOnClickListener(this);

        btnWholeness = findViewById(R.id.btn_type_wholeness);
        btnWholeness.setOnClickListener(this);
        btnRem = findViewById(R.id.btn_type_rem);
        btnRem.setOnClickListener(this);
        btnDep = findViewById(R.id.btn_type_dep);
        btnDep.setOnClickListener(this);

        setState(mState);
        setType(mType);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                dismiss();
                break;

            case R.id.txt_confirm:
                mListener.onSelected(mState, mType);
                dismiss();
                break;

            case R.id.btn_state_done:
                setState(MyConstants.TxState.DONE);
                break;

            case R.id.btn_state_pending:
                setState(MyConstants.TxState.PENDING);
                break;

            case R.id.btn_type_wholeness:
                setType(MyConstants.TxType.WHOLENESS);
                break;

            case R.id.btn_type_rem:
                setType(MyConstants.TxType.REMITTANCE);
                break;

            case R.id.btn_type_dep:
                setType(MyConstants.TxType.DEPOSIT);
                break;
        }
    }

    private void setState(MyConstants.TxState state) {
        mState = state;

        if (mState == MyConstants.TxState.DONE) {
            btnStateDone.setSelected(true);
            btnStatePending.setSelected(false);
        } else {
            btnStateDone.setSelected(false);
            btnStatePending.setSelected(true);
        }
    }

    private void setType(MyConstants.TxType type) {
        mType = type;

        if (mType == MyConstants.TxType.WHOLENESS) {
            btnWholeness.setSelected(true);
            btnRem.setSelected(false);
            btnDep.setSelected(false);
        } else if (mType == MyConstants.TxType.REMITTANCE) {
            btnWholeness.setSelected(false);
            btnRem.setSelected(true);
            btnDep.setSelected(false);
        } else {
            btnWholeness.setSelected(false);
            btnRem.setSelected(false);
            btnDep.setSelected(true);
        }
    }

    public OnSearchConListener mListener = null;

    public void setOnSearchConListener(OnSearchConListener listener) {
        mListener = listener;
    }

    public interface OnSearchConListener {
        void onSelected(MyConstants.TxState state, MyConstants.TxType type);
    }
}
