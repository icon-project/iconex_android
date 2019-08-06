package foundation.icon.iconex.dialogs2

import android.app.Dialog
import android.content.Context
import foundation.icon.iconex.dialogs2.button.ConfirmDialogButton
import foundation.icon.iconex.dialogs2.comp.ProjectDialog
import foundation.icon.iconex.dialogs2.content.TitleSubDialogContent
import foundation.icon.iconex.dialogs2.header.BlankDialogHeader

abstract class TitleSubDialog(context: Context) : ProjectDialog<BlankDialogHeader, TitleSubDialogContent, ConfirmDialogButton>(context) {
    override fun getHeader(context: Context): BlankDialogHeader {
        return BlankDialogHeader(context)
    }

    override fun getContent(context: Context): TitleSubDialogContent {
        return TitleSubDialogContent(context)
    }

    override fun getButton(context: Context, dialog: Dialog): ConfirmDialogButton {
        return ConfirmDialogButton(context, dialog)
    }

}