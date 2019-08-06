package foundation.icon.iconex.dialogs2.button

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button

class DismissButton: Button {

    private var dialog: Dialog? = null

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    fun setParentDialog(dialog: Dialog) {
        this.dialog = dialog
        setOnClickListener { }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        super.setOnClickListener { v: View? ->
            l?.onClick(v)
            dialog?.dismiss()
        }
    }
}