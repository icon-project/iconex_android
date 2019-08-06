package foundation.icon.iconex.dialogs2.button

import android.app.Dialog
import android.content.Context
import android.view.View
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs2.comp.DialogButton

class ConfirmDialogButton: DialogButton {

    lateinit var mConfirm: DismissButton

    constructor(context: Context, dialog: Dialog) : super(context) {
        initView(dialog)
    }

    private fun initView(dialog: Dialog) {
        View.inflate(context, R.layout.dlg_confirm_button, this)
        mConfirm = findViewById(R.id.btn_confirm)
        mConfirm.setParentDialog(dialog)
        mConfirm.setText(resources.getText(R.string.confirm))
    }
}