package foundation.icon.iconex.dialogs2.content

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs2.comp.DialogContent

class TitleSubDialogContent: DialogContent {

    lateinit var mTitle: TextView
    lateinit var mSub: TextView

    constructor(context: Context) : super(context) {
        initView()
    }

    private fun initView() {
        View.inflate(context, R.layout.dlg_title_sub_content, this)
        mTitle = findViewById(R.id.txt_title)
        mSub = findViewById(R.id.txt_sub)
    }
}