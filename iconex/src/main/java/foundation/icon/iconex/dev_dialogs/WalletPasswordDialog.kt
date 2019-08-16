package foundation.icon.iconex.dev_dialogs

import android.content.Context
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import foundation.icon.iconex.R

class WalletPasswordDialog(context: Context) : MessageDialog(context) {

    private var mLayoutIptPassword: TextInputLayout
    private var mIptPassword: EditText
    private var mLnkForgotPassword: TextView

    init {
        // build dialog

        // set Head
        headText = context.getString(R.string.dialogHeadTextWalletPassword)

        // set Content
        var v = View.inflate(context, R.layout.dlg_content_wallet_password, null)
        content = v

        // load content ui
        mLayoutIptPassword = v.findViewById(R.id.layout_ipt_password)
        mIptPassword = v.findViewById(R.id.ipt_password)
        mLnkForgotPassword = v.findViewById(R.id.lnk_forgot_password)

        // set Button
        isSingleButton = false

        // on focus change -> set box background
        mIptPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!error) { setLayoutColor(hasFocus) }
        }

        mLnkForgotPassword.setOnClickListener {
            // TODO: Need to implement move to Forgot Password
            Toast.makeText(context, "TODO: Need to implement move to Forgot Password", Toast.LENGTH_SHORT).show()
        }

        onConfirmClick = {
            // TODO: Need to implement password validation
            var vaild = false
            error = !vaild
            vaild
        }
    }

    private fun setLayoutColor(hasFocus: Boolean) {
        if (hasFocus) {
            mLayoutIptPassword.boxBackgroundColor = ContextCompat.getColor(context, R.color.primaryF5)
        } else {
            mLayoutIptPassword.boxBackgroundColor = ContextCompat.getColor(context, R.color.darkFA)
        }
    }

    private var error: Boolean
        get() = mLayoutIptPassword.error != null && mLayoutIptPassword.error !=  ""
        set(value) {
            if (value) {
                mLayoutIptPassword.error = "에러 문구"
                mLayoutIptPassword.boxBackgroundColor = ContextCompat.getColor(context, R.color.redF7)
            } else {
                mLayoutIptPassword.error = null
                setLayoutColor(mLayoutIptPassword.hasFocus())
            }
        }
}