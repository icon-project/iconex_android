package foundation.icon.iconex.dev_detail

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonArray
import foundation.icon.MyConstants
import foundation.icon.iconex.R
import foundation.icon.iconex.service.NetworkService
import foundation.icon.iconex.service.ServiceConstants
import foundation.icon.iconex.wallet.detail.TransactionListAdapter
import foundation.icon.iconex.wallet.detail.TxItem
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

    private var transactions: MutableList<TransactionItem> = ArrayList()
    private var adpater = object : RecyclerView.Adapter<TransactionViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
            var v = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
            return TransactionViewHolder(v)
        }

        override fun getItemCount(): Int {
            return transactions.size
        }

        override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
            holder.data = transactions[position]
        }

    }
    private var currentPage: Int = 1


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
                loadTransactions()
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
        list_transaction.adapter = adpater
    }

    private fun loadTransactions() {
        mService?.run {
            this.requestICONTxList("hxb8d4800dbe6c902a4ab9540c4391d43468a468a0", currentPage)
        }
    }

    override fun onStart() {
        super.onStart()
        bindService(Intent(this, NetworkService::class.java), mConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()
        unbindService(mConnection)
    }

    // service
    private var mService: NetworkService? = null
    private var mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as NetworkService.NetworkServiceBinder
            mService = binder.service
            mService?.registerTxListCallback(mTxCallback)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mService = null
        }
    }

    private val mTxCallback = object : NetworkService.TxListCallback {
        override fun onReceiveTransactionList(totalData: Int, txList: JsonArray) {
            for (raw in txList) {
                var tx = raw.asJsonObject
                var txItem = TransactionItem(
                        tx["txHash"].asString,
                        tx["createDate"].asString,
                        tx["fromAddr"].asString,
                        tx["toAddr"].asString,
                        tx["amount"].asString,
                        tx["fee"].asString,
                        tx["state"].asString.toInt()
                )
                transactions.add(txItem)
            }
            adpater.notifyDataSetChanged()
            refresh.stopRefresh(true)
        }

        override fun onReceiveError(resCode: String) {
            Toast.makeText(this@WalletDetailActivity, resCode, Toast.LENGTH_SHORT).show()
            refresh.stopRefresh(false)
        }

        override fun onReceiveException(t: Throwable) {
            Toast.makeText(this@WalletDetailActivity, t.message, Toast.LENGTH_SHORT).show()
            refresh.stopRefresh(false)
        }
    }
}