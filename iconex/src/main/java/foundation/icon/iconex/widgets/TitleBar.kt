package foundation.icon.iconex.widgets

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView

import foundation.icon.iconex.R

class TitleBar : RelativeLayout, View.OnClickListener {

    private var layoutTitle: ViewGroup? = null
    private var layoutSearch: ViewGroup? = null
    private var btnClose: Button? = null
    private var btnOption: TextView? = null
    private var btnCancel: TextView? = null
    private var btnClear: Button? = null
    private var txtTitle: TextView? = null
    private var editSearch: MyEditText? = null

    private var type: Type? = null
    private var title: String? = null

    private var mListener: OnTitleBarListener? = null

    constructor(context: Context) : super(context) {

        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

        getAttrs(attrs)
        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        getAttrs(attrs, defStyleAttr)
        initView()
    }

    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.layout_title_bar, this, false)

        addView(v)

        layoutTitle = v.findViewById(R.id.layout_title)
        layoutSearch = v.findViewById(R.id.layout_search)

        btnClose = v.findViewById(R.id.btn_close)
        btnClose!!.setOnClickListener(this)
        btnOption = v.findViewById(R.id.btn_option)
        btnOption!!.setOnClickListener(this)
        btnCancel = v.findViewById(R.id.btn_cancel)
        btnCancel!!.setOnClickListener(this)
        btnClear = v.findViewById(R.id.btn_clear)
        btnClose!!.setOnClickListener(this)

        txtTitle = v.findViewById(R.id.txt_title)
        editSearch = v.findViewById(R.id.edit_search)

        init()
    }

    private fun init() {
        when (type) {
            Type.TITLE, Type.TITLE_OPTION -> {
                layoutTitle!!.visibility = View.VISIBLE
                layoutSearch!!.visibility = View.GONE

                txtTitle!!.text = title
            }

            Type.SEARCH -> {
                layoutTitle!!.visibility = View.GONE
                layoutSearch!!.visibility = View.VISIBLE
            }
        }
    }

    private fun getAttrs(attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar)
        setTypedArray(typedArray)
    }

    private fun getAttrs(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.TitleBar, defStyleAttr, 0)
        setTypedArray(typedArray)
    }

    private fun setTypedArray(typedArray: TypedArray) {
        if (typedArray.hasValue(R.styleable.TitleBar_type))
            type = Type.fromType(typedArray.getString(R.styleable.TitleBar_type))

        if (typedArray.hasValue(R.styleable.TitleBar_title))
            title = typedArray.getString(R.styleable.TitleBar_title)
    }

    override fun onClick(view: View) {

        when (view.id) {
            R.id.btn_close -> {
            }

            R.id.btn_option -> {
            }

            R.id.btn_cancel -> {
            }

            R.id.btn_clear -> {
            }
        }
    }

    private enum class Type constructor(val type: String) {
        TITLE("title"),
        TITLE_OPTION("title_option"),
        SEARCH("search");


        companion object {

            fun fromType(type: String?): Type? {
                for (t in values()) {
                    if (t.type == type)
                        return t
                }

                return null
            }
        }
    }

    fun setListener(listener: OnTitleBarListener) {
        mListener = listener
    }

    interface OnTitleBarListener {
        fun onClose()

        fun onOption()

        fun onCancel()
    }

    companion object {
        private val TAG = TitleBar::class.java.simpleName
    }
}
