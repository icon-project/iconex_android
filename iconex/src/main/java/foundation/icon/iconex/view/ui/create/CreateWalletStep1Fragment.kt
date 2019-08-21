package foundation.icon.iconex.view.ui.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import foundation.icon.iconex.R

class CreateWalletStep1Fragment : Fragment(), View.OnClickListener {
    private val TAG = this@CreateWalletStep1Fragment::class.java.simpleName

    private lateinit var btnIcx: ViewGroup
    private lateinit var btnEth: ViewGroup
    private lateinit var btnNext: Button

    private val vm: CreateWalletViewModel by lazy {
        ViewModelProviders.of(activity!!).get(CreateWalletViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_create_wallet_step1, container, false)

        initView(v)
        vm.setCoinType(CreateWalletViewModel.CoinType.ICX)

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStep1Listener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnStep1Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_icx -> {
                if (!btnIcx.isSelected) {
                    btnIcx.setBackgroundResource(R.drawable.bg_line_button_coin_s)
                    btnIcx.dispatchSetSelected(true)
                    btnEth.setBackgroundResource(R.drawable.bg_line_button_coin_n)
                    btnEth.dispatchSetSelected(false)

                    vm.setCoinType(CreateWalletViewModel.CoinType.ICX)
                }
            }

            R.id.btn_eth -> {
                if (!btnEth.isSelected) {
                    btnIcx.setBackgroundResource(R.drawable.bg_line_button_coin_n)
                    btnIcx.dispatchSetSelected(false)
                    btnEth.setBackgroundResource(R.drawable.bg_line_button_coin_s)
                    btnEth.dispatchSetSelected(true)

                    vm.setCoinType(CreateWalletViewModel.CoinType.ETH)
                }
            }

            R.id.btn_next -> {
                if (mListener != null)
                    mListener!!.onStep1Done()
            }
        }
    }

    private fun initView(v: View) {
        btnIcx = v.findViewById(R.id.btn_icx)
        btnIcx.setOnClickListener(this)
        btnIcx.setBackgroundResource(R.drawable.bg_line_button_coin_s)
        btnIcx.dispatchSetSelected(true)
        btnEth = v.findViewById(R.id.btn_eth)
        btnEth.setOnClickListener(this)

        btnNext = v.findViewById(R.id.btn_next)
        btnNext.setOnClickListener(this)
    }

    private var mListener: OnStep1Listener? = null

    interface OnStep1Listener {
        fun onStep1Done()
    }

    companion object {

        fun newInstance(): CreateWalletStep1Fragment {

            return CreateWalletStep1Fragment()
        }
    }
}
