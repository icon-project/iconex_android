package foundation.icon.iconex.widgets

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import foundation.icon.iconex.R

class TTextInputLayout : LinearLayout {
    private val TAG = this@TTextInputLayout::class.simpleName

    private lateinit var layout: ViewGroup

    private lateinit var layoutInput: ViewGroup
    private lateinit var edit: MyEditText
    private lateinit var btnClear: Button
    private lateinit var btnEye: Button
    private lateinit var txtAppend: TextView
    private lateinit var tvHint: TextView
    private lateinit var tvError: TextView

    private lateinit var layoutFile: ViewGroup
    private lateinit var imgFile: ImageView
    private lateinit var tvFileName: TextView

    private val DEF_STYLEABLE = R.styleable.TTextInputLayout

    private val BG_LAYOUT_N = R.drawable.bg_text_input_layout_n
    private val BG_LAYOUT_F = R.drawable.bg_text_input_layout_f
    private val BG_LAYOUT_E = R.drawable.bg_text_input_layout_e

    private val BG_FLOATING_LABEL_N = R.drawable.bg_floating_label_n
    private val BG_FLOATING_LABEL_F = R.drawable.bg_floating_label_f
    private val BG_FLOATING_LABEL_E = R.drawable.bg_floating_label_e

    private var hint = ""
    private var isError = false

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

        layout = v.findViewById(R.id.layout)

        layoutInput = v.findViewById(R.id.layout_input)
        edit = v.findViewById(R.id.edit)
        btnClear = v.findViewById(R.id.btn_clear_text)
        btnEye = v.findViewById(R.id.btn_eye)
        txtAppend = v.findViewById(R.id.txt_append)
        tvHint = v.findViewById(R.id.tv_hint)
        tvError = v.findViewById(R.id.tv_err)

        layoutFile = v.findViewById(R.id.layout_file)
        imgFile = v.findViewById(R.id.img_file)
        tvFileName = v.findViewById(R.id.txt_file_name)

        edit.onFocusChangeListener = OnFocusChangeListener { _, b ->
            run {
                if (b) {
                    layout.background = resources.getDrawable(BG_LAYOUT_F, null)
                    tvHint.background = resources.getDrawable(BG_FLOATING_LABEL_F, null)
                    tvHint.setTextColor(resources.getColor(R.color.primary00))
                    tvHint.visibility = View.VISIBLE

                    if (tvError.visibility == View.VISIBLE)
                        tvError.visibility = View.INVISIBLE

                    edit.hint = ""
                } else {
                    layout.background = resources.getDrawable(BG_LAYOUT_N, null)
                    tvHint.background = resources.getDrawable(BG_FLOATING_LABEL_N, null)
                    tvHint.setTextColor(resources.getColor(R.color.dark4D))

                    if (edit.text!!.isEmpty()) {
                        tvHint.visibility = View.INVISIBLE
                        edit.hint = hint
                    }

                    mOnFocusReleasedListener?.onReleased()
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
                if (s.isNotEmpty()) {
                    if (edit.isEnabled)
                        btnClear.visibility = View.VISIBLE
                } else {
                    btnClear.visibility = View.INVISIBLE

                    if (!edit.isFocused) {
                        tvHint.visibility = View.INVISIBLE
                        edit.hint = hint
                    }
                }

                mOnTextChangedListener?.onChanged(s)
            }
        })

        edit.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
                    mOnEditorActionListener?.onDone()
                }

                return false
            }
        })

        btnClear.setOnClickListener { edit.setText("") }
        btnEye.setOnClickListener {
            when (it.isSelected) {
                true -> {
                    btnEye.isSelected = false
                    edit.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                            or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                }

                false -> {
                    btnEye.isSelected = true
                    edit.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                            or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                }
            }
        }
    }

    private fun setTypedArray(typedArray: TypedArray) {
        when (typedArray.getInt(R.styleable.TTextInputLayout_inputType, 0)) {
            0 -> { // text
                layoutInput.visibility = View.VISIBLE
                edit.inputType = InputType.TYPE_CLASS_TEXT
            }
            1 -> { // password
                layoutInput.visibility = View.VISIBLE
                edit.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            2 -> { // readOnly
                layoutInput.visibility = View.VISIBLE
                edit.isEnabled = false
                edit.isFocusable = false
                edit.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        or InputType.TYPE_TEXT_FLAG_MULTI_LINE)

                btnEye.visibility = View.VISIBLE
                tvHint.visibility = View.VISIBLE
            }
            3 -> { // file
                layoutInput.visibility = View.GONE
                layoutFile.visibility = View.VISIBLE
            }
        }

        val hint = typedArray.getString(R.styleable.TTextInputLayout_hint)
        hint?.let {
            this.hint = it
            edit.hint = it
            tvHint.text = it
            tvFileName.text = it
        }

        setAppendText(typedArray.getString(R.styleable.TTextInputLayout_appendText))
    }

    fun getText(): String {
        return edit.text.toString()
    }

    fun setText(text: String) {
        edit.setText(text)
        if (text.isNotEmpty())
            edit.setSelection(text.length)
        tvHint.visibility = if (text.isNotEmpty()) View.VISIBLE else View.INVISIBLE
    }

    fun setAppendText(text: String?) {
        txtAppend.visibility = if (text == null || text == "") View.GONE else View.VISIBLE
        txtAppend.text = text
    }

    fun setError(err: Boolean, msg: String?) {
        isError = err

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

                tvError.visibility = View.INVISIBLE
            }
        }
    }

    fun isError(): Boolean {
        return isError
    }

    fun setFile(fileName: String) {
        isError = false

        layout.setBackgroundResource(BG_LAYOUT_F)
        tvHint.setBackgroundResource(BG_FLOATING_LABEL_F)
        tvHint.setTextColor(resources.getColor(R.color.primary00))
        tvHint.visibility = View.VISIBLE

        imgFile.setBackgroundResource(R.drawable.ic_keystorefile_load)
        imgFile.visibility = View.VISIBLE
        tvFileName.setTextColor(resources.getColor(R.color.primary00))
        tvFileName.text = fileName

        tvError.visibility = View.INVISIBLE
    }

    fun setHint(hint: String) {
        this.hint = hint
        edit.hint = hint
        tvHint.text = hint
        tvFileName.text = hint
    }

    fun setFileError(fileName: String, msg: String) {
        isError = true

        layout.setBackgroundResource(BG_LAYOUT_E)
        tvHint.setBackgroundResource(BG_FLOATING_LABEL_E)
        tvHint.setTextColor(resources.getColor(R.color.redCoral))
        tvHint.visibility = View.VISIBLE

        imgFile.setBackgroundResource(R.drawable.ic_keystorefile_error)
        tvFileName.setTextColor(resources.getColor(R.color.redCoral))
        tvFileName.text = fileName

        tvError.text = msg
        tvError.visibility = View.VISIBLE
    }

    fun setInputEnabled(enabled: Boolean) {
        if (enabled) {
            edit.isFocusableInTouchMode = enabled
        } else {
            btnClear.visibility = View.INVISIBLE
        }

        edit.isEnabled = enabled
        edit.isFocusable = enabled
    }

    fun setSelection(index: Int) {
        edit.setSelection(index)
    }

    fun setInputType(type: Int) {
        edit.inputType = type
    }

    private var mOnFocusReleasedListener: OnFocusReleased? = null
    fun setOnFocusChangedListener(onFocusReleasedListener: OnFocusReleased) {
        mOnFocusReleasedListener = onFocusReleasedListener
    }

    interface OnFocusReleased {
        fun onReleased()
    }

    private var mOnKeyPreImeListener: OnKeyPreIme? = null
    fun setOnKeyPreImeListener(onKeyPreImeListener: OnKeyPreIme) {
        mOnKeyPreImeListener = onKeyPreImeListener
    }

    interface OnKeyPreIme {
        fun onDone()
    }

    private var mOnEditorActionListener: OnEditorAction? = null
    fun setOnEditorActionListener(listener: OnEditorAction) {
        mOnEditorActionListener = listener
    }

    interface OnEditorAction {
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
