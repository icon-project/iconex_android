package foundation.icon.iconex.view.ui.create

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

import foundation.icon.iconex.R
import foundation.icon.iconex.widgets.TTextInputLayout

class CreateWalletStep4Fragment : Fragment() {

    private var mListener: OnStep4Listener? = null
    private var address: String? = null
    private var privKey: String? = null

    private var txtPrivateKey: TextView? = null
    private var btnVisibility: Button? = null
    private var btnDone: Button? = null
    private var btnCopy: Button? = null
    private var btnInfo: Button? = null

    private val layoutTwoButton: ViewGroup? = null

    private var isTwoBtn = false

    private lateinit var inputText: TTextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            address = arguments!!.getString("address")
            privKey = arguments!!.getString("privateKey")
            isTwoBtn = arguments!!.getBoolean("isTwoButton")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_create_wallet_step4, container, false)

//        txtPrivateKey = v.findViewById(R.id.txt_private_key)
//        txtPrivateKey!!.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
//        btnVisibility = v.findViewById(R.id.btn_visibility)
//        btnVisibility!!.setOnClickListener {
//            if (btnVisibility!!.isSelected) {
//                btnVisibility!!.isSelected = false
//                txtPrivateKey!!.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                        or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
//            } else {
//                btnVisibility!!.isSelected = true
//                txtPrivateKey!!.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
//                        or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
//            }
//        }

        btnCopy = v.findViewById(R.id.btn_copy)
        btnCopy!!.setOnClickListener {
            val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val data = ClipData.newPlainText("priv", privKey)
            clipboard.primaryClip = data
            Toast.makeText(activity, getString(R.string.msgCopyPrivateKey), Toast.LENGTH_SHORT).show()
        }

        btnInfo = v.findViewById(R.id.btn_view_info)
        btnInfo!!.setOnClickListener { mListener!!.showWalletInfo(privKey) }

        inputText = v.findViewById(R.id.input_private_key)
        inputText.setText("Test Private Key")


//        btnDone = v.findViewById(R.id.btn_done)
//        btnDone!!.setOnClickListener { mListener!!.onStep4Done() }
//
//        //        layoutTwoButton = v.findViewById(R.id.layout_two);
//        val btnBack = v.findViewById<Button>(R.id.btn_back)
//        btnBack.setOnClickListener { mListener!!.onStep4Back() }
//        val btnNext = v.findViewById<Button>(R.id.btn_next)
//        btnNext.setOnClickListener { mListener!!.onStep4Next() }
//
//        if (isTwoBtn) {
//            btnDone!!.visibility = View.GONE
//            layoutTwoButton!!.visibility = View.VISIBLE
//        }

        return v
    }

    override fun onResume() {
        super.onResume()

//        btnVisibility!!.isSelected = false
//        txtPrivateKey!!.inputType = (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
//                or InputType.TYPE_TEXT_FLAG_MULTI_LINE)
//        txtPrivateKey!!.text = privKey
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

    interface OnStep4Listener {
        fun onStep4Done()

        fun onStep4Back()

        fun onStep4Next()

        fun showWalletInfo(privateKey: String?)
    }

    companion object {

        private val TAG = CreateWalletStep4Fragment::class.java.simpleName

        fun newInstance(address: String, privKey: String, isTwoBtn: Boolean): CreateWalletStep4Fragment {
            val fragment = CreateWalletStep4Fragment()
            val args = Bundle()
            args.putString("address", address)
            args.putString("privateKey", privKey)
            args.putBoolean("isTwoButton", isTwoBtn)
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
