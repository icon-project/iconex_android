package foundation.icon.iconex.dialogs2

import android.app.Dialog
import android.content.Context
import foundation.icon.iconex.dialogs2.button.ConfirmDialogButton
import foundation.icon.iconex.dialogs2.comp.ProjectDialog
import foundation.icon.iconex.dialogs2.content.TitleSubDialogContent
import foundation.icon.iconex.dialogs2.header.BlankDialogHeader
import foundation.icon.iconex.dialogs2.header.TitleDialogHeader

abstract class HasHeadTitleSubDialog(context: Context) : ProjectDialog<TitleDialogHeader, TitleSubDialogContent, ConfirmDialogButton>(context) {
    override fun getHeader(context: Context): TitleDialogHeader {
        return TitleDialogHeader(context)
    }

    override fun getContent(context: Context): TitleSubDialogContent {
        return TitleSubDialogContent(context)
    }

    override fun getButton(context: Context, dialog: Dialog): ConfirmDialogButton {
        return ConfirmDialogButton(context, dialog)
    }

}