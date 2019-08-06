package foundation.icon.iconex.dialogs2.header

import android.content.Context
import android.util.AttributeSet
import android.view.View
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs2.comp.DialogHeader

class BlankDialogHeader: DialogHeader {
    constructor(context: Context) : super(context) {
        initView()
    }

    private fun initView() {
        View.inflate(context, R.layout.dlg_blank_header, this)
    }
}