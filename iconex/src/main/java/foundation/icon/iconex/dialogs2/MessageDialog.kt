package foundation.icon.iconex.dialogs2

import android.app.Dialog
import android.content.Context
import foundation.icon.iconex.dialogs2.button.ConfirmDialogButton
import foundation.icon.iconex.dialogs2.comp.*
import foundation.icon.iconex.dialogs2.content.TextDialogContent
import foundation.icon.iconex.dialogs2.header.BlankDialogHeader

abstract class MessageDialog(context: Context) : ProjectDialog<BlankDialogHeader, TextDialogContent, ConfirmDialogButton>(context) {

    private lateinit var header: BlankDialogHeader
    private lateinit var content: TextDialogContent
    private lateinit var button: ConfirmDialogButton

    override fun getHeader(context: Context): BlankDialogHeader{
        header = BlankDialogHeader(context)
        return header
    }

    override fun getContent(context: Context): TextDialogContent {
        content = TextDialogContent(context)
        return content
    }

    override fun getButton(context: Context, dialog: Dialog): ConfirmDialogButton {
        button = ConfirmDialogButton(context, dialog)
        return button
    }
}