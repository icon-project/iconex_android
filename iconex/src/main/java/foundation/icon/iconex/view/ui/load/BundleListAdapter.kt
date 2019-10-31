package foundation.icon.iconex.view.ui.load

import android.content.Context
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.icon.MyConstants
import foundation.icon.iconex.R
import foundation.icon.iconex.util.ConvertUtil
import java.math.BigInteger
import java.util.*

/**
 * Created by js on 2018. 5. 5..
 */

class BundleListAdapter(private val mContext: Context, private var mData: List<BundleItem>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val mInflater: LayoutInflater

    private val TYPE_HEADER = 1
    private val TYPE_ITEM = 2

    init {
        mInflater = LayoutInflater.from(mContext)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_HEADER) {
            val v = mInflater.inflate(R.layout.layout_load_bundle_header, parent, false)
            return HeaderHolder(v)
        } else {
            val v = mInflater.inflate(R.layout.item_wallet_with_address, parent, false)
            return ItemHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderHolder) {
            setWalletCount(holder.txtRegistered)
        } else {
            val itemHolder = holder as ItemHolder
            val item = mData!![position - 1]
            itemHolder.txtAlias.text = item.getAlias()

            if (!item.getBalance().isEmpty()) {
                if (item.getBalance() == MyConstants.NO_BALANCE) {
                    itemHolder.txtBalance.text = String.format(Locale.getDefault(), "%s %s",
                            MyConstants.NO_BALANCE, item.getSymbol())
                } else {
                    try {
                        val value = ConvertUtil.getValue(BigInteger(item.getBalance()), 18)
                        val doubValue = java.lang.Double.parseDouble(value)
                        itemHolder.txtBalance.text = String.format(Locale.getDefault(),
                                "%,.4f %s",
                                doubValue, item.getSymbol())
                    } catch (e: Exception) {
                        // Do nothing.
                    }

                }
            }

            if (item.isRegistered) {
                itemHolder.txtAlias.setTextColor(mContext.resources.getColor(R.color.darkB3))
                itemHolder.txtBalance.setTextColor(mContext.resources.getColor(R.color.darkB3))
                itemHolder.txtAddress.setTextColor(mContext.resources.getColor(R.color.primary00))
                itemHolder.txtAddress.text = String.format(Locale.getDefault(),
                        mContext.getString(R.string.registeredWallet),
                        item.address)
            } else {
                itemHolder.txtAlias.setTextColor(mContext.resources.getColor(R.color.dark4D))
                itemHolder.txtBalance.setTextColor(mContext.resources.getColor(R.color.dark4D))
                itemHolder.txtAddress.setTextColor(mContext.resources.getColor(R.color.darkB3))
                itemHolder.txtAddress.text = item.address
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM

    }

    override fun getItemCount(): Int {
        return mData!!.size + 1
    }

    inner class HeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var txtRegistered: TextView = itemView.findViewById(R.id.registered)

    }

    inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var txtAlias: TextView = itemView.findViewById(R.id.alias)
        internal var txtBalance: TextView = itemView.findViewById(R.id.balance)
        internal var txtAddress: TextView = itemView.findViewById(R.id.address)
    }

    private fun getColoredSpanned(text: String, color: String): String {

        return "<font color=$color>$text</font>"
    }

    private fun setWalletCount(view: TextView) {
        val total = mData!!.size
        var registered = 0

        for (item in mData!!) {
            if (item.isRegistered)
                registered++
        }

        val strTotal = getColoredSpanned(mContext.getString(R.string.loadBundleTotal, total), "#262626")
        val strRegisterd = getColoredSpanned(mContext.getString(R.string.loadBundleRegistered, registered), "#1aaaba")

        if (Locale.getDefault().language == MyConstants.LOCALE_KO)
            view.text = Html.fromHtml("$strTotal $strRegisterd")
        else
            view.text = Html.fromHtml("$strRegisterd $strTotal")

    }

    fun setData(data: List<BundleItem>) {
        mData = data
    }

    companion object {

        private val TAG = BundleListAdapter::class.java.simpleName
    }
}
