package foundation.icon.iconex.dialogs

import android.content.Context
import android.view.View
import android.widget.TextView
import foundation.icon.iconex.R

class StakeDialog(context: Context) : MessageDialog(context) {

    private var mTxtTimeRequired: TextView
    private var mTxtStepLimit: TextView
    private var mTxtEstimatedMaxFee: TextView
    private var mTxtExchangedFee: TextView

    init {
        // set Content
        var v = View.inflate(context, R.layout.dlg_content_set_stake, null)
        content = v

        // load Content Ui
        mTxtTimeRequired = findViewById(R.id.txt_time_required)
        mTxtStepLimit = findViewById(R.id.txt_step_limit)
        mTxtEstimatedMaxFee = findViewById(R.id.txt_estimated_max_fee)
        mTxtExchangedFee = findViewById(R.id.txt_exchanged_fee)

        isSingleButton = false
    }

    var timeRequired: String
        get() = mTxtTimeRequired.text.toString()
        set(value) { mTxtTimeRequired.text = value }

    var stepLimit: String
        get() = mTxtStepLimit.text.toString()
        set(value) { mTxtStepLimit.text = value }

    var estimatedMaxFee: String
        get() = mTxtEstimatedMaxFee.text.toString()
        set(value) { mTxtEstimatedMaxFee.text = value }

    var exchangedFee: String
        get() = mTxtExchangedFee.text.toString()
        set(value) { mTxtExchangedFee.text = value }
}