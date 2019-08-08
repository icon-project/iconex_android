package foundation.icon.iconex.dev_dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import foundation.icon.iconex.R
import kotlinx.android.synthetic.main.dlg_txhash_content.view.*

class TxHashDialog(context: Context) : MessageDialog(context) {

    private var mTxtTxHash: TextView
    private var mLnkTracker: TextView

    init {
        // build dialog

        // set Head
        headText = context.resources.getString(R.string.dialogHeadTextTxHash)

        // set Content
        var v = View.inflate(context, R.layout.dlg_txhash_content, null)
        content = v

        // load content ui
        mTxtTxHash = v.findViewById(R.id.txt_tx_hash)
        mLnkTracker = v.findViewById(R.id.lnk_tracker)

        // set button
        isSingleButton = false
        confirmButtonText = context.resources.getString(R.string.dialogTxHashCopyTxHash)

        // add button event
        mLnkTracker.setOnClickListener {
            // TODO: Need to implement move to tracker
            Toast.makeText(context, "TODO: Need to implement move to tracker", Toast.LENGTH_SHORT).show()
        }

        onConfirmClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.primaryClip = ClipData.newPlainText("TxHash", txHash)
            Toast.makeText(context, "TODO: Copied TxHash", Toast.LENGTH_SHORT).show()

            true
        }
    }

    var txHash: String
        get() = mTxtTxHash.text.toString()
        set(s) { mTxtTxHash.text = s }
}