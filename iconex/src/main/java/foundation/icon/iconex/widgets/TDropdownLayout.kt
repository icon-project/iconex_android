package foundation.icon.iconex.widgets

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import foundation.icon.iconex.R

class TDropdownLayout : LinearLayout {

    private val DEF_STYLEABLE = R.styleable.TDropdownLayout

    private lateinit var dropDown: ViewGroup
    private lateinit var text: TextView
    private lateinit var arrow: Button
    private lateinit var helper: TextView

    private var helperMsg: String? = null

    constructor(context: Context) : super(context) {
        initView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()

        val typedArray = getContext().obtainStyledAttributes(attrs, DEF_STYLEABLE)
        setTypedArray(typedArray)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()

        val typedArray = getContext().obtainStyledAttributes(attrs, DEF_STYLEABLE, defStyleAttr, 0)
        setTypedArray(typedArray)
    }

    fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.t_drop_down_layout, this, false)

        dropDown = v.findViewById(R.id.drop_down)
        dropDown.setOnClickListener {
            dropDown.setBackgroundResource(R.drawable.bg_drop_down_f)
            arrow.isActivated = true
            mOnClickListener?.onClick()
        }

        text = v.findViewById(R.id.text)
        arrow = v.findViewById(R.id.arrow)
        helper = v.findViewById(R.id.helper)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        helperMsg = typedArray.getString(R.styleable.TDropdownLayout_helper)

        this.text.text = helperMsg
        helper.text = helperMsg
    }

    fun setText(text: String) {
        dropDown.setBackgroundResource(R.drawable.bg_drop_down_n)
        arrow.isActivated = false
        helper.visibility = View.VISIBLE

        this.text.text = text
    }

    fun deactivate() {
        dropDown.setBackgroundResource(R.drawable.bg_drop_down_n)
        arrow.isActivated = false
    }

    var mOnClickListener: OnDropDownClickListener? = null
    fun setOnClickListener(listener: OnDropDownClickListener) {
        mOnClickListener = listener
    }

    interface OnDropDownClickListener {
        fun onClick()
    }
}