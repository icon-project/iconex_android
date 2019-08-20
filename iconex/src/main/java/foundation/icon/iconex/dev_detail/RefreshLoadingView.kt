package foundation.icon.iconex.dev_detail

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import foundation.icon.iconex.R
import foundation.icon.iconex.widgets.RefreshLayout.LoadingHeaderListener

class RefreshLoadingView: FrameLayout, LoadingHeaderListener {
    constructor(context: Context) : super(context) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView()
    }

    private fun initView () {
        var view = LayoutInflater.from(context).inflate(R.layout.layout_pull_to_refreash, this, true)
    }

    override fun onRefreshBefore(scrollY: Int, headerHeight: Int) {

    }

    override fun onRefreshAfter(scrollY: Int, headerHeight: Int) {

    }

    override fun onRefreshReady(scrollY: Int, headerHeight: Int) {

    }

    override fun onRefreshing(scrollY: Int, headerHeight: Int) {

    }

    override fun onRefreshComplete(scrollY: Int, headerHeight: Int, isRefreshSuccess: Boolean) {

    }

    override fun onRefreshCancel(scrollY: Int, headerHeight: Int) {

    }

    override fun getRefreshHeight(): Int {
        return 0
    }
}