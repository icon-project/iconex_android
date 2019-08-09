package foundation.icon.iconex.widgets

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.*
import foundation.icon.iconex.R

class CustomActionBar: RelativeLayout {

    private var mIconStart: IconStart = IconStart.none
    private var mIconEnd: IconEnd = IconEnd.none
    private var mIsShowIcToggle: Boolean = false
    private var mTextButton: String? = null
    private var mTitle: String? = null

    private lateinit var mBtnStartIcon: ImageButton
    private lateinit var mBtnEndIcon: ImageButton
    private lateinit var mBtnText: Button
    private lateinit var mTxtTitle: TextView
    private lateinit var mImgToggle: ImageView

    constructor(context: Context) : super(context) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(context.obtainStyledAttributes(attrs, R.styleable.CustomActionBar))
        initView()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setTypedArray(context.obtainStyledAttributes(
                attrs, R.styleable.CustomActionBar, defStyleAttr,0))
        initView()
    }

    private fun setTypedArray(typedArray: TypedArray) {
        mIconStart =
                if (typedArray.hasValue(R.styleable.CustomActionBar_start_icon))
                    IconStart.valueOf(typedArray.getInt(R.styleable.CustomActionBar_start_icon, -1))
                else IconStart.none
        mIconEnd =
                if (typedArray.hasValue(R.styleable.CustomActionBar_end_icon))
                    IconEnd.valueOf(typedArray.getInt(R.styleable.CustomActionBar_end_icon, -1))
                else IconEnd.none
        mIsShowIcToggle =
                if (typedArray.hasValue(R.styleable.CustomActionBar_show_ic_toggle))
                    typedArray.getBoolean(R.styleable.CustomActionBar_show_ic_toggle, false)
                else false
        mTitle =
                if (typedArray.hasValue(R.styleable.CustomActionBar_text))
                    typedArray.getString(R.styleable.CustomActionBar_text)
                else null

        mTextButton =
                if (typedArray.hasValue(R.styleable.CustomActionBar_text_button))
                    typedArray.getString(R.styleable.CustomActionBar_text_button)
                else null
    }

    private fun initView () {
        var v = View.inflate(context, R.layout.layout_custom_action_bar, null)

        mBtnStartIcon = v.findViewById(R.id.btn_start_icon)
        mBtnEndIcon = v.findViewById(R.id.btn_end_icon)
        mBtnText = v.findViewById(R.id.btn_text)
        mTxtTitle = v.findViewById(R.id.txt_title)
        mImgToggle = v.findViewById(R.id.img_toggle)

        when (mIconStart) {
            IconStart.menu -> mBtnStartIcon.setImageResource(R.drawable.ic_appbar_menu)
            IconStart.back -> mBtnStartIcon.setImageResource(R.drawable.ic_appbar_back)
            else -> mBtnStartIcon.visibility = View.GONE
        }

        when (mIconEnd) {
            IconEnd.info -> {
                mBtnText.visibility = View.GONE
                mBtnEndIcon.setImageResource(R.drawable.ic_info)
            }
            IconEnd.more -> {
                mBtnText.visibility = View.GONE
                mBtnEndIcon.setImageResource(R.drawable.ic_more_vert_darkgray)
            }
            IconEnd.text -> {
                mBtnEndIcon.visibility = View.GONE
                mBtnText.setText(mTextButton)
            }
            else -> {
                mBtnEndIcon.visibility = View.GONE
                mBtnText.visibility = View.GONE
            }
        }

        mTxtTitle.setText(mTitle)
        mImgToggle.visibility = if (mIsShowIcToggle) View.VISIBLE else View.GONE

        addView(v,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
    }

    enum class IconStart {
        menu, back, none;

        companion object {
            fun valueOf(e: Int): IconStart {
                return when (e) {
                    0 -> menu
                    1 -> back
                    else -> none
                }
            }
        }
    }
    enum class IconEnd {
        info, more, text, none;

        companion object {
            fun valueOf(e: Int): IconEnd {
                return when (e) {
                    0 -> info
                    1 -> more
                    2 -> text
                    else -> none
                }
            }
        }
    }
}