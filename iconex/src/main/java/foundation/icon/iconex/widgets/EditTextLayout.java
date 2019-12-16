package foundation.icon.iconex.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.OnKeyPreImeListener;

/**
 * Created by js on 2018. 2. 22..
 */

public class EditTextLayout extends RelativeLayout {

    private LayoutInflater mInflater;
    private Context mContext;

    private MyEditText editText;
    private Button btnVisibility;
    private View noticeLine;
    private TextView txtWarning;

    private boolean attrIsPassword;
    private String attrHint = null;
    private String attrWarning = null;

    public EditTextLayout(Context context) {
        super(context);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        init();
    }

    public EditTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        TypedArray attr = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.EditTextLayout, 0, 0);

        attrIsPassword = attr.getBoolean(R.styleable.EditTextLayout_isPassword, false);
        attrHint = attr.getString(R.styleable.EditTextLayout_setHint);
        attrWarning = attr.getString(R.styleable.EditTextLayout_setWarning);
        init();
    }

    public EditTextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mInflater = LayoutInflater.from(context);
        init();
    }

    private void init() {
        View v = mInflater.inflate(R.layout.layout_edit_text, this, true);
        editText = v.findViewById(R.id.edit_text);
        if (attrIsPassword) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnVisibility = v.findViewById(R.id.btn_eye);
            btnVisibility.setVisibility(View.VISIBLE);
            btnVisibility.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (btnVisibility.isSelected()) {
                        btnVisibility.setSelected(false);
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    } else {
                        btnVisibility.setSelected(true);
                        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    }
                }
            });
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
        }

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    noticeLine.setBackgroundColor(getResources().getColor(R.color.editActivated));
                } else {
                    noticeLine.setBackgroundColor(getResources().getColor(R.color.editNormal));
                }
            }
        });

        if (attrHint != null) {
            editText.setHint(attrHint);
        }

        noticeLine = v.findViewById(R.id.line_old_pwd);
        txtWarning = v.findViewById(R.id.txt_file_warning);
        if (attrWarning != null) {
            txtWarning.setText(attrWarning);
        }
    }

    public String getText() {
        return editText.getText().toString();
    }

    public void setWarningMessage(String msg) {
        txtWarning.setText(msg);
        txtWarning.setVisibility(View.VISIBLE);
    }

    public void setNoticeWarning() {
        noticeLine.setBackgroundColor(getResources().getColor(R.color.colorWarning));
    }

    public void setOnFocusListenter(OnFocusChangeListener listenter) {
        editText.setOnFocusChangeListener(listenter);
    }

    public void setOnEditTextTouchListener(OnTouchListener listener) {
        editText.setOnTouchListener(listener);
    }

    public void setOnKeyPreImeListener(OnKeyPreImeListener listener) {
        ((MyEditText) editText).setOnKeyPreImeListener(listener);
    }
}
