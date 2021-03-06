package foundation.icon.iconex.dialogs

import android.app.Dialog
import android.content.Context
import android.view.*
import android.widget.*
import foundation.icon.iconex.R

open class MessageDialog: Dialog {

    private var mTextHead: TextView
    private var mHeadLine: View

    private var mContent: FrameLayout
    private var mOtherContent: View? = null
    private var mDefaultContent: View
    private var mTextTitle: TextView
    private var mTextSub: TextView

    private var mWrap2Button: LinearLayout
    private var mConfirmButton: Button
    private var mCancelButton: Button
    private var mProgress: ProgressBar


    private var mSingleButton: Button

    constructor(context: Context) : super(context, R.style.CustomDialog)

    init {
        setContentView(R.layout.dlg_message_dialog)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        setCancelable(false)

        mTextHead = findViewById(R.id.txt_head)
        mHeadLine = findViewById(R.id.headline)

        mContent = findViewById(R.id.content)
        mDefaultContent = findViewById(R.id.default_content)
        mTextTitle = findViewById(R.id.txt_title)
        mTextSub = findViewById(R.id.txt_sub)

        mWrap2Button = findViewById(R.id.wrap_2button)
        mConfirmButton = findViewById(R.id.btn_confirm)
        mCancelButton = findViewById(R.id.btn_cancel)
        mProgress = findViewById(R.id.progress)

        mSingleButton = findViewById(R.id.btn_single_confirm)

        mConfirmButton.setOnClickListener { v ->
            var b = onConfirmClick?.invoke(v)
            if (b == null || b) { this.dismiss() }
        }

        mCancelButton.setOnClickListener { v ->
            var b = onCancelClick?.invoke(v)
            if (b == null || b) { this.dismiss() }
        }

        mSingleButton.setOnClickListener { v ->
            var b = onSingleClick?.invoke(v)
            if (b == null || b) { this.dismiss() }
        }

        title = null
        description = null
        isSingleButton = true
        progressVisible = false
    }

    var onConfirmClick: ((v: View) -> Boolean)? = null
    var onCancelClick: ((v: View) -> Boolean)? = null
    var onSingleClick: ((v: View) -> Boolean)? = null

    var isConfirmEnable: Boolean
        get() = mConfirmButton.isEnabled
        set(b) { mConfirmButton.setEnabled(b) }

    var confirmButtonText: String
        get() = mConfirmButton.text.toString()
        set(s) = mConfirmButton.setText(s)

    var cancelButtonText: String
        get() = mCancelButton.text.toString()
        set(s) = mCancelButton.setText(s)

    var singleButtonText: String
        get() = mSingleButton.text.toString()
        set(s) = mCancelButton.setText(s)

    var isSingleButton: Boolean
        get() = mSingleButton.visibility == View.VISIBLE
        set(b: Boolean) {
            mSingleButton.visibility = if (b) View.VISIBLE else View.GONE
            mWrap2Button.visibility = if (b) View.GONE else View.VISIBLE
        }

    var title: String?
        get() = if (mTextHead.visibility == View.GONE) null else mTextHead.text.toString()
        set(s) {
            mTextHead.visibility = if (s == null) View.GONE else View.VISIBLE
            mHeadLine.visibility = if (s == null) View.GONE else View.VISIBLE
            s.let { mTextHead.text = it }
        }

    var message: String
        get() = mTextTitle.text.toString()
        set(s) = mTextTitle.setText(s)


    var description: String?
        get() = if (mTextSub.visibility == View.GONE) null else mTextSub.text.toString()
        set(s) {
            mTextSub.visibility = if (s == null || s == "") View.GONE else View.VISIBLE
            mTextSub.text = s
        }

    var content: View?
        get() = mOtherContent
        set(v) {
            mDefaultContent.visibility = if (v == null) View.VISIBLE else View.GONE
            if (v != null) {
                if (mOtherContent != null) {
                    mContent.removeView(mOtherContent)
                    mOtherContent = null
                }

                mContent.addView(v,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
                mOtherContent = v
            }
        }

    var progressVisible: Boolean
        get() = mProgress.visibility == View.VISIBLE
        set(v) {
            mProgress.visibility = if(v) View.VISIBLE else View.GONE
        }
}