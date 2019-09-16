package foundation.icon.iconex.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import foundation.icon.iconex.R;

public class CustomActionBar extends RelativeLayout implements View.OnClickListener {

    private IconStart mIconStart = IconStart.none;
    private IconEnd mIconEnd = IconEnd.none;
    private Boolean mIsShowIcToggle = false;
    private String mTextButton = null;
    private String mTitle = null;

    private ImageButton mBtnStartIcon;
    private ImageButton mBtnEndIcon;
    private Button mBtnText;
    private TextView mTxtTitle;
    private ImageView mImgToggle;

    public IconStart getIconStart() {
        return mIconStart;
    }

    public void setIconStart(IconStart IconStart) {
        mIconStart = IconStart;
        switch (mIconStart) {
            case menu: mBtnStartIcon.setImageResource(R.drawable.ic_appbar_menu); break;
            case back: mBtnStartIcon.setImageResource(R.drawable.ic_appbar_back); break;
            default: mBtnStartIcon.setVisibility(View.GONE); break;
        }
    }

    public IconEnd getIconEnd() {
        return mIconEnd;
    }

    public void setIconEnd(IconEnd IconEnd) {
        mIconEnd = IconEnd;
        switch (mIconEnd) {
            case info: {
                mBtnText.setVisibility(View.GONE);
                mBtnEndIcon.setImageResource(R.drawable.ic_info);
            } break;
            case more: {
                mBtnText.setVisibility(View.GONE);
                mBtnEndIcon.setImageResource(R.drawable.ic_wallet_more_enabled);
            } break;
            case text: {
                mBtnEndIcon.setVisibility(View.GONE);
                mBtnText.setVisibility(View.VISIBLE);
                mBtnText.setText(mTextButton);
            } break;
            default: {
                mBtnEndIcon.setVisibility(View.GONE);
                mBtnText.setVisibility(View.GONE);
            } break;
        }
    }

    public Boolean getIsShowIcToggle() {
        return mIsShowIcToggle;
    }

    public void setIsShowIcToggle(Boolean IsShowIcToggle) {
        mIsShowIcToggle = IsShowIcToggle;
        mImgToggle.setVisibility(mIsShowIcToggle ? VISIBLE : GONE);
    }

    public String getTextButton() {
        return mTextButton;
    }

    public void setTextButton(String TextButton) {
        mIconEnd = IconEnd.text;
        mTextButton = TextButton;
        mBtnEndIcon.setVisibility(View.GONE);
        mBtnText.setText(mTextButton);
    }

    public void setTextButtonSelected(boolean selected) {
        mBtnText.setSelected(selected);
    }

    public boolean isTextButtonSelected() {
        return mBtnText.isSelected();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String Title) {
        mTitle = Title;
        mTxtTitle.setText(mTitle);
    }

    private View.OnClickListener mOnClickStartIcon = null;
    private View.OnClickListener mOnClickEndIcon = null;
    private View.OnClickListener mOnCLickToggleIcon = null;
    private OnActionClickListener mOnActionClickListener = null;

    public void setOnClickStartIcon(View.OnClickListener listener) {
        mOnClickStartIcon = listener;
    }
    public void setOnClickEndIcon(View.OnClickListener listener) { mOnClickEndIcon = listener; }
    public void setOnCLickToggleIcon(View.OnClickListener listener) { mOnCLickToggleIcon = listener; }
    public void setOnActionClickListener(OnActionClickListener listener) { mOnActionClickListener = listener; }

    public CustomActionBar(Context context) {
        super(context);
        initView();
    }

    public CustomActionBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypedArray(getContext().obtainStyledAttributes(attrs, R.styleable.CustomActionBar));
        initView();
    }

    public CustomActionBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setTypedArray(getContext().obtainStyledAttributes(attrs, R.styleable.CustomActionBar, defStyleAttr,0));
        initView();
    }

    private void setTypedArray(TypedArray typedArray) {
        mIconStart =
            (typedArray.hasValue(R.styleable.CustomActionBar_start_icon)) ?
                IconStart.valueOf(typedArray.getInt(R.styleable.CustomActionBar_start_icon, -1))
            : IconStart.none;
        mIconEnd =
            (typedArray.hasValue(R.styleable.CustomActionBar_end_icon)) ?
                IconEnd.valueOf(typedArray.getInt(R.styleable.CustomActionBar_end_icon, -1))
            : IconEnd.none;
        mIsShowIcToggle =
            (typedArray.hasValue(R.styleable.CustomActionBar_show_ic_toggle)) ?
                typedArray.getBoolean(R.styleable.CustomActionBar_show_ic_toggle, false)
            : false;
        mTitle =
            (typedArray.hasValue(R.styleable.CustomActionBar_text)) ?
                typedArray.getString(R.styleable.CustomActionBar_text)
            : null;
        mTextButton =
            (typedArray.hasValue(R.styleable.CustomActionBar_text_button)) ?
                typedArray.getString(R.styleable.CustomActionBar_text_button)
            : null;
    }

    private void initView () {
        View v = View.inflate(getContext(), R.layout.layout_custom_action_bar, null);

        mBtnStartIcon = v.findViewById(R.id.btn_start_icon);
        mBtnEndIcon = v.findViewById(R.id.btn_end_icon);
        mBtnText = v.findViewById(R.id.btn_text);
        mTxtTitle = v.findViewById(R.id.txt_title);
        mImgToggle = v.findViewById(R.id.img_toggle);

        mBtnStartIcon.setOnClickListener(this);
        mBtnEndIcon.setOnClickListener(this);
        mBtnText.setOnClickListener(this);
        mTxtTitle.setOnClickListener(this);
        mImgToggle.setOnClickListener(this);

        switch (mIconStart) {
            case menu: mBtnStartIcon.setImageResource(R.drawable.ic_appbar_menu); break;
            case back: mBtnStartIcon.setImageResource(R.drawable.ic_appbar_back); break;
            default: mBtnStartIcon.setVisibility(View.GONE); break;
        }

        switch (mIconEnd) {
            case info: {
                mBtnText.setVisibility(View.GONE);
                mBtnEndIcon.setImageResource(R.drawable.ic_info);
            } break;
            case more: {
                mBtnText.setVisibility(View.GONE);
                mBtnEndIcon.setImageResource(R.drawable.ic_wallet_more_enabled);
            } break;
            case text: {
                mBtnEndIcon.setVisibility(View.GONE);
                mBtnText.setText(mTextButton);
            } break;
            default: {
                mBtnEndIcon.setVisibility(View.GONE);
                mBtnText.setVisibility(View.GONE);
            } break;
        }

        mTxtTitle.setText(mTitle);
        mImgToggle.setVisibility(mIsShowIcToggle ? VISIBLE : GONE);

        int dp56 = getResources().getDimensionPixelSize(R.dimen.dp56);
        addView(v, ViewGroup.LayoutParams.MATCH_PARENT, dp56);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_icon: {
                if (mOnActionClickListener != null)
                    mOnActionClickListener.onClickAction(ClickAction.btnStart);

                if (mOnClickStartIcon != null) mOnClickStartIcon.onClick(view);
            } break;
            case R.id.btn_text:
            case R.id.btn_end_icon: {
                if (mOnActionClickListener != null)
                    mOnActionClickListener.onClickAction(ClickAction.btnEnd);

                if (mOnClickEndIcon != null) mOnClickEndIcon.onClick(view);
            } break;
            case R.id.txt_title:
            case R.id.img_toggle: {
                if (mOnActionClickListener != null)
                    mOnActionClickListener.onClickAction(ClickAction.btnToggle);

                if (mOnCLickToggleIcon != null) mOnCLickToggleIcon.onClick(view);
            } break;
        }
    }

    public enum IconStart {
        menu, back, none;

        public static IconStart valueOf(int e) {
            switch (e) {
                case 0: return menu;
                case 1: return back;
                default: return none;
            }
        }
    }
    public enum IconEnd {
        info, more, text, none;

        public static IconEnd valueOf(int e) {
            switch (e) {
                case 0: return info;
                case 1: return more;
                case 2: return text;
                default: return none;
            }
        }
    }

    public enum ClickAction { btnStart, btnEnd, btnToggle }
    public interface OnActionClickListener {
        void onClickAction(ClickAction action);
    }
}
