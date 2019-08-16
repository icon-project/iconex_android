package foundation.icon.iconex.view.ui.create

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.textfield.TextInputLayout
import foundation.icon.ICONexApp
import foundation.icon.iconex.R
import foundation.icon.iconex.control.OnKeyPreImeListener
import foundation.icon.iconex.util.PasswordValidator
import foundation.icon.iconex.util.PasswordValidator.checkPasswordMatch
import foundation.icon.iconex.util.Utils
import foundation.icon.iconex.widgets.MyEditText

class CreateWalletStep2Fragment : Fragment(), View.OnClickListener {

    private var mListener: OnStep2Listener? = null

    private lateinit var editAlias: MyEditText
    private var editPwd: MyEditText? = null
    private var editCheck: MyEditText? = null
    private var lineAlias: View? = null
    private var linePwd: View? = null
    private var lineCheck: View? = null
    private var txtAliasWarning: TextView? = null
    private var txtPwdWarning: TextView? = null
    private var txtCheckWarning: TextView? = null
    private var btnAliasDel: Button? = null
    private var btnPwdDel: Button? = null
    private var btnCheckDel: Button? = null

    private var btnPrev: Button? = null
    private lateinit var btnNext: Button
    private var progress: ProgressBar? = null

    private var mImm: InputMethodManager? = null
    private var mOnKeyPreImeListener: OnKeyPreImeListener? = null

    private var beforeStr: String? = null
    private var beforePwd: String? = null
    private var beforeCheck: String? = null

    private val OK = 0
    private val ALIAS_DUP = 1
    private val ALIAS_EMPTY = 2

    private val vm: CreateWalletViewModel by lazy {
        ViewModelProviders.of(activity!!).get(CreateWalletViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mImm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mOnKeyPreImeListener = OnKeyPreImeListener {
            setNextEnable(editAlias!!.text!!.toString(),
                    editPwd!!.text!!.toString(), editCheck!!.text!!.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_create_wallet_step2, container, false)
        btnPrev = view!!.findViewById(R.id.btn_prev)
        btnPrev!!.setOnClickListener {
            clearEdit()
            mListener!!.onStep2Back()
        }
        btnNext = view!!.findViewById(R.id.btn_next)
        btnNext!!.setOnClickListener {
            mListener!!.onStep2Done("", "")
        }

        progress = view!!.findViewById(R.id.progress)

//        val textLayout = view.findViewById<TextInputLayout>(R.id.text_input_layout)
//        Log.d(TAG, "childCount=" + textLayout.childCount)

//        editAlias = view!!.findViewById(R.id.edit_alias)
//        //        editAlias.setFilters(new InputFilter[]{new ByteLengthFilter()});
//        editAlias!!.setOnKeyPreImeListener(mOnKeyPreImeListener)
//        editAlias!!.setOnEditTouchListener { showInputMode(editAlias!!) }
//        editAlias!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                lineAlias!!.setBackgroundColor(resources.getColor(R.color.editActivated))
//                // TODO: 2018. 3. 22. check password
//            } else {
//                lineAlias!!.setBackgroundColor(resources.getColor(R.color.editNormal))
//
//                val aliasValidate = checkAlias(editAlias!!.text!!.toString())
//                when (aliasValidate) {
//                    ALIAS_DUP -> showWarning(lineAlias!!, txtAliasWarning!!, getString(R.string.duplicateWalletAlias))
//                    else -> hideWarning(editAlias!!, lineAlias, txtAliasWarning!!)
//                }
//            }
//        }
//        editAlias!!.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                if (s.length > 0) {
//                    if (s.toString().trim { it <= ' ' }.isEmpty()) {
//                        editAlias!!.setText("")
//                    } else if (s[0] == ' ') {
//                        editAlias!!.setText(beforeStr)
//                        editAlias!!.setSelection(editAlias!!.text!!.toString().length)
//                    } else {
//                        btnAliasDel!!.visibility = View.VISIBLE
//                        if (Utils.checkByteLength(s.toString()) > 16) {
//                            editAlias!!.setText(beforeStr)
//                            editAlias!!.setSelection(editAlias!!.text!!.toString().length)
//                        } else {
//                            beforeStr = s.toString()
//                        }
//                    }
//                } else {
//                    btnAliasDel!!.visibility = View.INVISIBLE
//                    txtAliasWarning!!.visibility = View.GONE
//
//                    if (editAlias!!.isFocused)
//                        lineAlias!!.setBackgroundColor(resources.getColor(R.color.editActivated))
//                    else
//                        lineAlias!!.setBackgroundColor(resources.getColor(R.color.editNormal))
//
//                    btnNext!!.isEnabled = false
//                }
//            }
//
//            override fun afterTextChanged(s: Editable) {
//
//            }
//        })
//        editAlias!!.setOnEditorActionListener { v, actionId, event ->
//            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
//                hideInputMode()
//                setNextEnable(editAlias!!.text!!.toString(), editPwd!!.text!!.toString(), editCheck!!.text!!.toString())
//            }
//            false
//        }
//
//        editPwd = view!!.findViewById(R.id.edit_pwd)
//        editPwd!!.setOnKeyPreImeListener(mOnKeyPreImeListener)
//        editPwd!!.setOnEditTouchListener { showInputMode(editPwd!!) }
//        editPwd!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                linePwd!!.setBackgroundColor(resources.getColor(R.color.editActivated))
//            } else {
//                linePwd!!.setBackgroundColor(resources.getColor(R.color.editNormal))
//
//                val result = PasswordValidator.validatePassword(editPwd!!.text!!.toString())
//                when (result) {
//
//                    PasswordValidator.LEAST_8 -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errAtLeast))
//
//                    PasswordValidator.NOT_MATCH_PATTERN -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errPasswordPatternMatch))
//
//                    PasswordValidator.HAS_WHITE_SPACE -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errWhiteSpace))
//
//                    PasswordValidator.SERIAL_CHAR -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errSerialChar))
//
//                    else -> hideWarning(editPwd!!, linePwd, txtPwdWarning!!)
//                }
//            }
//        }
//        editPwd!!.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                if (s.length > 0) {
//                    btnPwdDel!!.visibility = View.VISIBLE
//                    if (s[s.length - 1] == ' ') {
//                        editPwd!!.setText(s.subSequence(0, s.length - 1))
//                        if (editPwd!!.text!!.toString().length > 0)
//                            editPwd!!.setSelection(editPwd!!.text!!.toString().length)
//                    } else if (s.toString().contains(" ")) {
//                        editPwd!!.setText(beforePwd)
//                        editPwd!!.setSelection(beforePwd!!.length)
//                    } else {
//                        beforePwd = s.toString()
//                    }
//                } else {
//                    btnPwdDel!!.visibility = View.INVISIBLE
//                    txtPwdWarning!!.visibility = View.GONE
//
//                    if (editPwd!!.isFocused)
//                        linePwd!!.setBackgroundColor(resources.getColor(R.color.editActivated))
//                    else
//                        linePwd!!.setBackgroundColor(resources.getColor(R.color.editNormal))
//
//                    btnNext!!.isEnabled = false
//                }
//            }
//
//            override fun afterTextChanged(s: Editable) {
//
//            }
//        })
//        editPwd!!.setOnEditorActionListener { v, actionId, event ->
//            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
//                hideInputMode()
//                setNextEnable(editAlias!!.text!!.toString(), editPwd!!.text!!.toString(), editCheck!!.text!!.toString())
//            }
//            false
//        }
//
//        editCheck = view!!.findViewById(R.id.edit_check)
//        editCheck!!.setOnKeyPreImeListener(mOnKeyPreImeListener)
//        editCheck!!.setOnEditTouchListener { showInputMode(editCheck!!) }
//        editCheck!!.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
//            if (hasFocus) {
//                lineCheck!!.setBackgroundColor(resources.getColor(R.color.editActivated))
//            } else {
//                lineCheck!!.setBackgroundColor(resources.getColor(R.color.editNormal))
//
//                if (editCheck!!.text!!.toString().isEmpty()) {
//                    btnNext!!.isEnabled = false
//                } else {
//                    if (editPwd!!.text!!.toString().isEmpty()) {
//                        showWarning(lineCheck!!, txtCheckWarning!!, getString(R.string.errPasswordNotMatched))
//                    } else {
//                        val result = PasswordValidator.checkPasswordMatch(editPwd!!.text!!.toString(), editCheck!!.text!!.toString())
//                        if (!result) {
//                            showWarning(lineCheck!!, txtCheckWarning!!, getString(R.string.errPasswordNotMatched))
//                        } else {
//                            hideWarning(editCheck!!, lineCheck, txtCheckWarning!!)
//                        }
//                    }
//                }
//            }
//        }
//        editCheck!!.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
//
//            }
//
//            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
//                if (s.length > 0) {
//                    btnCheckDel!!.visibility = View.VISIBLE
//                    if (s[s.length - 1] == ' ') {
//                        editCheck!!.setText(s.subSequence(0, s.length - 1))
//                        if (editCheck!!.text!!.toString().length > 0)
//                            editCheck!!.setSelection(editCheck!!.text!!.toString().length)
//                    } else if (s.toString().contains(" ")) {
//                        editCheck!!.setText(beforeCheck)
//                        editCheck!!.setSelection(beforeCheck!!.length)
//                    } else {
//                        beforeCheck = s.toString()
//                    }
//                } else {
//                    btnCheckDel!!.visibility = View.INVISIBLE
//                    txtCheckWarning!!.visibility = View.GONE
//                    if (editCheck!!.isFocused)
//                        lineCheck!!.setBackgroundColor(resources.getColor(R.color.editActivated))
//                    else
//                        lineCheck!!.setBackgroundColor(resources.getColor(R.color.editNormal))
//
//                    btnNext!!.isEnabled = false
//                }
//            }
//
//            override fun afterTextChanged(s: Editable) {
//
//            }
//        })
//        editCheck!!.setOnEditorActionListener { v, actionId, event ->
//            if (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER || actionId == EditorInfo.IME_ACTION_DONE) {
//                hideInputMode()
//                setNextEnable(editAlias!!.text!!.toString(), editPwd!!.text!!.toString(), editCheck!!.text!!.toString())
//            }
//            false
//        }
//
//        txtAliasWarning = view!!.findViewById(R.id.txt_alias_warning)
//        txtPwdWarning = view!!.findViewById(R.id.txt_pwd_warning)
//        txtCheckWarning = view!!.findViewById(R.id.txt_check_warning)
//
//        lineAlias = view!!.findViewById(R.id.line_alias)
//        linePwd = view!!.findViewById(R.id.line_pwd)
//        lineCheck = view!!.findViewById(R.id.line_check)
//
//        btnAliasDel = view!!.findViewById(R.id.btn_alias_delete)
//        btnAliasDel!!.setOnClickListener(this)
//        btnPwdDel = view!!.findViewById(R.id.btn_pwd_delete)
//        btnPwdDel!!.setOnClickListener(this)
//        btnCheckDel = view!!.findViewById(R.id.btn_check_delete)
//        btnCheckDel!!.setOnClickListener(this)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStep2Listener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnStep2Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_alias_delete -> editAlias!!.setText("")

            R.id.btn_pwd_delete -> editPwd!!.setText("")

            R.id.btn_check_delete -> editCheck!!.setText("")
        }
    }

//    private fun showInputMode(view: View) {
//        view.requestFocus()
//        mImm!!.showSoftInput(view, 0)
//        val layoutInputArea = this.view!!.findViewById<ViewGroup>(R.id.layout_input_area)
//        val layoutParams = layoutInputArea.layoutParams as RelativeLayout.LayoutParams
//        layoutParams.removeRule(RelativeLayout.BELOW)
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
//        layoutInputArea.layoutParams = layoutParams
//        mListener!!.onShowInputMode()
//    }
//
//    private fun hideInputMode() {
//        val layoutInputArea = this.view!!.findViewById<ViewGroup>(R.id.layout_input_area)
//        val layoutParams = layoutInputArea.layoutParams as RelativeLayout.LayoutParams
//        layoutParams.removeRule(RelativeLayout.ALIGN_PARENT_TOP)
//        layoutParams.addRule(RelativeLayout.BELOW, R.id.layout_header)
//        layoutParams.addRule(RelativeLayout.ABOVE, R.id.layout_buttons)
//        layoutInputArea.layoutParams = layoutParams
//        mListener!!.onHideInputMode()
//    }

    private fun showWarning(line: View, txtView: TextView, msg: String) {
        line.setBackgroundColor(resources.getColor(R.color.colorWarning))
        txtView.visibility = View.VISIBLE
        txtView.text = msg
    }

    private fun hideWarning(edit: View, line: View?, txtView: View) {
        txtView.visibility = View.GONE
        if (edit.isFocused)
            line!!.setBackgroundColor(resources.getColor(R.color.editActivated))
        else
            line!!.setBackgroundColor(resources.getColor(R.color.editNormal))
    }

    private fun checkAlias(target: String): Int {
        val alias = Utils.strip(target)
        if (alias.isEmpty())
            return ALIAS_EMPTY

        for (info in ICONexApp.mWallets) {
            if (info.alias == alias) {
                return ALIAS_DUP
            }
        }

        return OK
    }

    private fun setNextEnable(alias: String, pwd: String, checkPwd: String) {
        var aliasValidate = 0
        var pwdValidate = 0
        var matched = true

        if (!editAlias!!.text!!.toString().isEmpty()) {
            aliasValidate = checkAlias(alias)
            when (aliasValidate) {
                ALIAS_EMPTY -> showWarning(lineAlias!!, txtAliasWarning!!, getString(R.string.errAliasEmpty))

                ALIAS_DUP -> showWarning(lineAlias!!, txtAliasWarning!!, getString(R.string.duplicateWalletAlias))
                else -> hideWarning(editAlias!!, lineAlias, txtAliasWarning!!)
            }
        } else {
            showWarning(lineAlias!!, txtAliasWarning!!, getString(R.string.errAliasEmpty))
            btnNext!!.isEnabled = false
            return
        }

        if (!editPwd!!.text!!.toString().isEmpty()) {
            pwdValidate = PasswordValidator.validatePassword(pwd)
            if (pwdValidate != 0) {
                when (pwdValidate) {
                    PasswordValidator.EMPTY -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errPwdEmpty))

                    PasswordValidator.LEAST_8 -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errAtLeast))
                    PasswordValidator.NOT_MATCH_PATTERN -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errPasswordPatternMatch))

                    PasswordValidator.HAS_WHITE_SPACE -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errWhiteSpace))

                    PasswordValidator.SERIAL_CHAR -> showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errSerialChar))
                    else -> hideWarning(editPwd!!, linePwd, txtPwdWarning!!)
                }
            }
        } else {
            showWarning(linePwd!!, txtPwdWarning!!, getString(R.string.errPwdEmpty))
            btnNext!!.isEnabled = false
            return
        }

        if (!editCheck!!.text!!.toString().isEmpty()) {
            matched = checkPasswordMatch(pwd, checkPwd)
            if (!matched) {
                showWarning(lineCheck!!, txtCheckWarning!!, getString(R.string.errPasswordNotMatched))
            } else {
                hideWarning(editCheck!!, lineCheck, txtCheckWarning!!)
            }
        } else {
            showWarning(lineCheck!!, txtCheckWarning!!, getString(R.string.errCheckEmpty))
            btnNext!!.isEnabled = false
            return
        }

        if (aliasValidate == OK && matched && pwdValidate == PasswordValidator.OK)
            btnNext!!.isEnabled = true
        else
            btnNext!!.isEnabled = false
    }

    fun clearEdit() {
        editAlias!!.requestFocus()

        editAlias!!.setText("")
        editPwd!!.setText("")
        editCheck!!.setText("")

        lineAlias!!.setBackgroundColor(resources.getColor(R.color.editActivated))
        linePwd!!.setBackgroundColor(resources.getColor(R.color.editNormal))
        lineCheck!!.setBackgroundColor(resources.getColor(R.color.editNormal))

        txtAliasWarning!!.visibility = View.GONE
        txtPwdWarning!!.visibility = View.GONE
        txtCheckWarning!!.visibility = View.GONE

        progress!!.visibility = View.GONE
        btnNext!!.isEnabled = false
    }

    interface OnStep2Listener {
        fun onStep2Done(name: String, pwd: String)

        fun onStep2Back()

        fun onShowInputMode()

        fun onHideInputMode()
    }

    companion object {

        private val TAG = CreateWalletStep2Fragment::class.java.simpleName

        fun newInstance(): CreateWalletStep2Fragment {
            return CreateWalletStep2Fragment()
        }
    }
}// Required empty public constructor
