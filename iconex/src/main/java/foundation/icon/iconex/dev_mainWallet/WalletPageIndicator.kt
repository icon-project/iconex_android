package foundation.icon.iconex.dev_mainWallet

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.LinearLayout

class WalletPageIndicator: LinearLayout {

    private var isReady: Boolean = false
    private var size: Int = 1
    private var index: Int = 0

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    private fun viewInit () {
        isReady = true
    }

    private fun updateSize () {
        removeAllViews()

        for (i in 1..size) {
            var img = ImageView(context)
            addView(img)
        }

        updateIndex()
    }

    private fun updateIndex () {

    }
}