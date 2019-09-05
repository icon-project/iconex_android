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

class TitleBar : RelativeLayout {

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
    private var optionText: String? = null
    private var hint: String? = null

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
        btnOption = v.findViewById(R.id.btn_option)
        btnCancel = v.findViewById(R.id.btn_cancel)

        editSearch = v.findViewById(R.id.edit_search)

        txtTitle = v.findViewById(R.id.txt_title)

        init()
    }

    private fun init() {
        when (type) {
            Type.TITLE -> {
                layoutTitle!!.visibility = View.VISIBLE
                layoutSearch!!.visibility = View.GONE
                btnOption!!.visibility = View.GONE

                txtTitle!!.text = title
            }

            Type.TITLE_OPTION -> {
                layoutTitle!!.visibility = View.VISIBLE
                layoutSearch!!.visibility = View.GONE

                txtTitle!!.text = title
                btnOption!!.text = optionText
            }

            Type.SEARCH -> {
                layoutTitle!!.visibility = View.GONE
                layoutSearch!!.visibility = View.VISIBLE
                editSearch!!.hint = hint
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
            type = Type.fromType(typedArray.getInt(R.styleable.TitleBar_type, -1))

        if (typedArray.hasValue(R.styleable.TitleBar_title))
            title = typedArray.getString(R.styleable.TitleBar_title)

        if (typedArray.hasValue(R.styleable.TitleBar_optionText))
            optionText = typedArray.getString(R.styleable.TitleBar_optionText)

        if (typedArray.hasValue(R.styleable.TitleBar_searchHint))
            hint = typedArray.getString(R.styleable.TitleBar_searchHint)
    }

    private enum class Type constructor(val type: Int) {
        TITLE(0),
        TITLE_OPTION(1),
        SEARCH(2);


        companion object {

            fun fromType(type: Int?): Type? {
                for (t in values()) {
                    if (t.type == type)
                        return t
                }

                return null
            }
        }
    }

    companion object {
        private val TAG = TitleBar::class.java.simpleName
    }
}
