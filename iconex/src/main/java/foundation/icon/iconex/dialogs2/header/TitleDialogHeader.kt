package foundation.icon.iconex.dialogs2.header

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs2.comp.DialogHeader

class TitleDialogHeader: DialogHeader {

    lateinit var mTitle: TextView

    constructor(context: Context) : super(context) {
        initView()
    }

    private fun initView() {
        View.inflate(context, R.layout.dlg_title_header, this)
        mTitle = findViewById(R.id.txt_title)
    }
}