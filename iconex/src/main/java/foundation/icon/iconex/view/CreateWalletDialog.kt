package foundation.icon.iconex.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import foundation.icon.iconex.R

class CreateWalletDialog() : BottomSheetDialogFragment(), View.OnClickListener {
    private val TAG = this@CreateWalletDialog::class.java

    private var btnClose: Button? = null
    private var tvTitle: TextView? = null
    private var container: ViewGroup? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.create_wallet, container, false)
        initView(view)

        return view
    }

    private fun initView(v: View) {
        btnClose = v.findViewById(R.id.btn_close)
        btnClose!!.setOnClickListener(this)

        tvTitle = v.findViewById(R.id.txt_title)
        tvTitle!!.setText(resources.getString(R.string.createWallet))

        container = v.findViewById(R.id.container)

        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val step1 = inflater.inflate(R.layout.layout_create_wallet_step1, container, false)
        container!!.addView(step1)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_close -> {
            }
        }
    }
}
