package foundation.icon.iconex.widgets

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import foundation.icon.iconex.R

class TTextInputLayout : LinearLayout {
    private val TAG = this@TTextInputLayout::class.simpleName

    private lateinit var layout: ViewGroup
    private lateinit var edit: MyEditText
    private lateinit var btnClear: Button
    private lateinit var btnEye: Button
    private lateinit var tvHint: TextView
    private lateinit var tvError: TextView

    private val DEF_STYLEABLE = R.styleable.TTextInputLayout

    private val BG_LAYOUT_N = R.drawable.bg_text_input_layout_n
    private val BG_LAYOUT_F = R.drawable.bg_text_input_layout_f
    private val BG_LAYOUT_E = R.drawable.bg_text_input_layout_e

    private val BG_FLOATING_LABEL_N = R.drawable.bg_floating_label_n
    private val BG_FLOATING_LABEL_F = R.drawable.bg_floating_label_f
    private val BG_FLOATING_LABEL_E = R.drawable.bg_floating_label_e

    private var hint = ""

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

    private fun initView() {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val v = inflater.inflate(R.layout.t_text_input_layout, this, false)
        addView(v)

        layout = v.findViewById(R.id.layout_text_input)
        edit = v.findViewById(R.id.edit)
        btnClear = v.findViewById(R.id.btn_clear_text)
        btnEye = v.findViewById(R.id.btn_eye)
        tvHint = v.findViewById(R.id.tv_hint)
        tvError = v.findViewById(R.id.tv_err)

        edit.onFocusChangeListener = OnFocusChangeListener { _, b ->
            run {
                if (b) {
                    layout.background = resources.getDrawable(BG_LAYOUT_F, null)
                    tvHint.background = resources.getDrawable(BG_FLOATING_LABEL_F, null)
                    tvHint.setTextColor(resources.getColor(R.color.primary00))
                    tvHint.visibility = View.VISIBLE

                    edit.hint = ""

                    mOnFocusChangedListener?.onFocused()
                } else {
                    layout.background = resources.getDrawable(BG_LAYOUT_N, null)
                    tvHint.background = resources.getDrawable(BG_FLOATING_LABEL_N, null)
                    tvHint.setTextColor(resources.getColor(R.color.dark4D))

                    if (edit.text!!.isEmpty())
                        tvHint.visibility = View.GONE

                    mOnFocusChangedListener?.onReleased()
                }
            }
        }

        edit.setOnKeyPreImeListener { mOnKeyPreImeListener?.onDone() }

        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isNotEmpty())
                    btnClear.visibility = View.VISIBLE
                else {
                    btnClear.visibility = View.INVISIBLE

                    if (!edit.isFocused) {
                        tvHint.visibility = View.GONE
                        edit.hint = hint
                    }
                }

                mOnTextChangedListener?.onChanged(s)
            }
        })

        btnClear.setOnClickListener { edit.setText("") }
        btnEye.setOnClickListener {
            when (it.isSelected) {
                true -> {
                    btnEye.isSelected = false
                    edit.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                }

                false -> {
                    btnEye.isSelected = true
                    edit.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                }
            }
        }
    }

    private fun setTypedArray(typedArray: TypedArray) {
        when (typedArray.getInt(R.styleable.TTextInputLayout_inputType, 0)) {
            0 -> edit.inputType = InputType.TYPE_CLASS_TEXT
            1 -> edit.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            2 -> {
                edit.isEnabled = false
                edit.isFocusable = false

                btnClear.visibility = View.GONE
                btnEye.visibility = View.VISIBLE

                tvHint.visibility = View.VISIBLE
            }
        }

        val hint = typedArray.getString(R.styleable.TTextInputLayout_hint)
        hint?.let {
            this.hint = it
            edit.hint = it
            tvHint.text = it
        }
    }

    fun setText(text: String) {
        edit.setText(text)
    }

    fun setError(err: Boolean, msg: String?) {
        when (err) {
            true -> {
                layout.setBackgroundResource(BG_LAYOUT_E)
                tvHint.setTextColor(resources.getColor(R.color.redCoral))
                tvHint.setBackgroundResource(BG_FLOATING_LABEL_E)

                tvError.text = msg
                tvError.visibility = View.VISIBLE
            }

            false -> {
                if (edit.isFocused) {
                    layout.setBackgroundResource(BG_LAYOUT_F)
                    tvHint.setTextColor(resources.getColor(R.color.primary00))
                    tvHint.setBackgroundResource(BG_FLOATING_LABEL_F)
                } else {
                    layout.setBackgroundResource(BG_LAYOUT_N)
                    tvHint.setTextColor(resources.getColor(R.color.dark4D))
                    tvHint.setBackgroundResource(BG_FLOATING_LABEL_N)
                }

                tvError.visibility = View.GONE
            }
        }
    }

    private var mOnFocusChangedListener: OnFocusChanged? = null
    fun setOnFocusChangedListener(onFocusChangedListener: OnFocusChanged) {
        mOnFocusChangedListener = onFocusChangedListener
    }

    interface OnFocusChanged {
        fun onFocused()
        fun onReleased()
    }

    private var mOnKeyPreImeListener: OnKeyPreIme? = null
    fun setOnKeyPreImeListener(onKeyPreImeListener: OnKeyPreIme) {
        mOnKeyPreImeListener = onKeyPreImeListener
    }

    interface OnKeyPreIme {
        fun onDone()
    }

    private var mOnTextChangedListener: OnTextChanged? = null
    fun setOnTextChangedListener(onTextChangedListener: OnTextChanged) {
        mOnTextChangedListener = onTextChangedListener
    }

    interface OnTextChanged {
        fun onChanged(s: CharSequence)
    }
}
