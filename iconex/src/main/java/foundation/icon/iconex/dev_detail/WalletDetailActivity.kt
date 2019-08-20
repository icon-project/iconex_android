package foundation.icon.iconex.dev_detail

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import foundation.icon.iconex.R
import foundation.icon.iconex.widgets.CustomActionBar
import foundation.icon.iconex.widgets.RefreshLayout.LoadingHeaderView
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout

class WalletDetailActivity: AppCompatActivity() {
    private companion object {
        val TAG = WalletDetailActivity::class.java.simpleName
    }

    private lateinit var actionbar: CustomActionBar
    private lateinit var refresh: RefreshLayout
    private lateinit var scroll : NestedScrollView
    private lateinit var info_wallet: WalletInfoView
    private lateinit var list_header: WalletDetailHeader
    private lateinit var list_header_fixed: WalletDetailHeader
    private lateinit var list_transaction: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet_detail_activity)
        initView()
    }

    private fun initView () {
        // load ui
        actionbar = findViewById(R.id.actionbar)
        refresh = findViewById(R.id.refresh)
        scroll  = findViewById(R.id.scroll )
        info_wallet = findViewById(R.id.info_wallet)
        list_header = findViewById(R.id.list_header)
        list_header_fixed = findViewById(R.id.list_header_fixed)
        list_transaction = findViewById(R.id.list_transaction)

        refresh.setOnRefreshListener(object : OnRefreshListener {
            override fun onRefresh() {
                refresh.postDelayed({
                    refresh.stopRefresh(true)
                }, 1000)
            }

            override fun onLoadMore() {
                Log.d(TAG, "on Loadmore!")
            }

        })
        refresh.setRefreshEnable(true)
        refresh.addHeader(RefreshLoadingView(this))

        scroll.setOnScrollChangeListener {
            v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            list_header_fixed.visibility = if (scrollY >= info_wallet.height) View.VISIBLE else View.GONE
        }

        list_transaction.layoutManager = LinearLayoutManager(this)
        list_transaction.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                var v = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
                return object: RecyclerView.ViewHolder(v) { }
            }

            override fun getItemCount(): Int {
                return 30
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

            }

        }
    }
}