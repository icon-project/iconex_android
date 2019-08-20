package foundation.icon.iconex.dev_detail

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import foundation.icon.iconex.R

class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    var mData: TransactionItem? = null
    var data: TransactionItem?
    get() = mData
    set(value) {
        mData = value
        txt_date.text = value?.date
        txt_address.text = value?.to
        txt_amount.visibility = View.INVISIBLE
        txt_tint_amount.text = value?.amount
    }

    private var text: TextView
    private var txt_date: TextView
    private var txt_address: TextView
    private var txt_tint_amount: TextView
    private var txt_amount: TextView

    init {
        text = itemView.findViewById(R.id.text)
        txt_date = itemView.findViewById(R.id.txt_date)
        txt_address = itemView.findViewById(R.id.txt_address)
        txt_tint_amount = itemView.findViewById(R.id.txt_tint_amount)
        txt_amount = itemView.findViewById(R.id.txt_amount)
    }
}