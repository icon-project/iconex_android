package foundation.icon.iconex.view.ui.create

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import foundation.icon.iconex.R
import foundation.icon.iconex.realm.RealmUtil
import foundation.icon.iconex.wallet.main.MainActivity
import foundation.icon.iconex.widgets.TTextInputLayout
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CreateWalletStep4Fragment : Fragment() {

    private var mListener: OnStep4Listener? = null
    private lateinit var address: String
    private lateinit var privateKey: String

    private lateinit var btnComplete: Button
    private lateinit var btnBack: Button
    private lateinit var btnCopy: Button
    private lateinit var btnInfo: Button
    private lateinit var inputText: TTextInputLayout

    private val vm: CreateWalletViewModel by lazy {
        ViewModelProviders.of(activity!!).get(CreateWalletViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_create_wallet_step4, container, false)
        initView(v)
        setData()

        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStep4Listener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnStep2Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun initView(v: View) {
        btnCopy = v.findViewById(R.id.btn_copy)
        btnCopy.setOnClickListener {
            val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("priv", privateKey)
            clipboard.primaryClip = data
            Toast.makeText(activity, getString(R.string.msgCopyPrivateKey), Toast.LENGTH_SHORT).show()
        }

        btnInfo = v.findViewById(R.id.btn_view_info)
        btnInfo.setOnClickListener { mListener!!.showWalletInfo() }

        btnComplete = v.findViewById(R.id.btn_complete)
        btnComplete.setOnClickListener { saveWallet() }

        btnBack = v.findViewById(R.id.btn_back)
        btnBack.setOnClickListener { mListener!!.onStep4Back() }

        inputText = v.findViewById(R.id.input_private_key)
    }

    private fun setData() {
        address = vm.getWallet().value!!.address
        privateKey = vm.getPrivateKey().value!!
        inputText.setText(privateKey)
    }

    private fun saveWallet() {
        RealmUtil.addWallet(vm.getWallet().value!!)
        RealmUtil.loadWallet()

        startActivity(Intent(activity!!, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                        or Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    interface OnStep4Listener {
        fun onStep4Back()

        fun showWalletInfo()
    }

    companion object {

        private val TAG = CreateWalletStep4Fragment::class.java.simpleName

        fun newInstance(): CreateWalletStep4Fragment {
            return CreateWalletStep4Fragment()
        }
    }
}// Required empty public constructor
