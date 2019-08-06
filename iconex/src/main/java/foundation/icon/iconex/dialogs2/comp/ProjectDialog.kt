package foundation.icon.iconex.dialogs2.comp

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import foundation.icon.iconex.R

abstract class ProjectDialog<H: DialogHeader,C: DialogContent,B: DialogButton>(context: Context)
    : Dialog(context, R.style.ProjectDialog) {

    protected abstract fun getHeader(context: Context): H
    protected abstract fun getContent(context: Context): C
    protected abstract fun getButton(context: Context, dialog: Dialog): B

    protected abstract fun initView(header: H, content: C, button: B)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dlg_project_dialog)

        window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )

        var header = getHeader(context!!)
        var content = getContent(context!!)
        var button = getButton(context!!, this)

        findViewById<FrameLayout>(R.id.header).addView(header)
        findViewById<FrameLayout>(R.id.content).addView(content)
        findViewById<FrameLayout>(R.id.button).addView(button)

        initView(header, content, button)
        findViewById<ConstraintLayout>(R.id.layout).setOnClickListener { cancel() }
    }
}