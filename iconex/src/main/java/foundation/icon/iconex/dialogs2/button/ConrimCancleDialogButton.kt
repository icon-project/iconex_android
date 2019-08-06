package foundation.icon.iconex.dialogs2.button

import android.app.Dialog
import android.content.Context
import android.view.View
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs2.comp.DialogButton

class ConrimCancleDialogButton: DialogButton {

    lateinit var mConfirm: DismissButton
    lateinit var mDismiss: DismissButton

    constructor(context: Context, dialog: Dialog) : super(context) {
        initView(dialog)
    }

    private fun initView(dialog: Dialog) {
        View.inflate(context, R.layout.dlg_confirm_cancle_button, this)
        mConfirm = findViewById(R.id.btn_confirm)
        mDismiss = findViewById(R.id.btn_cancel)

        mConfirm.setParentDialog(dialog)
        mDismiss.setParentDialog(dialog)

        mConfirm.setText(resources.getText(R.string.confirm))
        mDismiss.setText(resources.getText(R.string.cancel))
    }
}