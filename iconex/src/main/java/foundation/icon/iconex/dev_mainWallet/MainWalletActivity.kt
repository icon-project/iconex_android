package foundation.icon.iconex.dev_mainWallet

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import foundation.icon.iconex.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager


class MainWalletActivity : AppCompatActivity() {

    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_wallet_frgment)

        refresh = findViewById(R.id.refresh)
        refresh.setOnRefreshListener {
            refresh.isRefreshing = false
        }


        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = object: RecyclerView.Adapter<ItemViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
                val context = parent.context
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view =
                        if (viewType != 0)
                            inflater.inflate(R.layout.item_wallet_card_borderless, parent, false)
                        else
                            inflater.inflate(R.layout.item_wallet_card, parent, false)
                return ItemViewHolder(view)
            }

            override fun getItemCount(): Int {
                return 30
            }

            override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {

            }

            override fun getItemViewType(position: Int): Int {
                return if (position == 0) 0 else 1
            }
        }
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
