package foundation.icon.iconex.dev_detail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import foundation.icon.iconex.R

class WalletDetailHeader: FrameLayout {
    constructor(context: Context) : super(context) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) { initView() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { initView() }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) { initView() }

    fun initView () {
        var view = LayoutInflater.from(context).inflate(R.layout.layout_detail_wallet_header, this, false)
        addView(view)
    }
}