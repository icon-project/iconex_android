package foundation.icon.iconex.dialogs2.content

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs2.comp.DialogContent

class TextDialogContent: DialogContent {

    lateinit var mText: TextView

    constructor(context: Context) : super(context) {
        initView()
    }

    private fun initView() {
        var view = View.inflate(context, R.layout.dlg_text_content, this)
        mText = view.findViewById(R.id.txt_text)
    }
}