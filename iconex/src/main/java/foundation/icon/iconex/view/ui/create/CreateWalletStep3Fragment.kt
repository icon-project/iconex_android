package foundation.icon.iconex.view.ui.create

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs.Basic2ButtonDialog
import foundation.icon.iconex.dialogs.BasicDialog
import foundation.icon.iconex.util.KeyStoreIO
import foundation.icon.iconex.wallet.create.CreateWalletActivity

class CreateWalletStep3Fragment : Fragment(), View.OnClickListener {

    private var mListener: OnStep3Listener? = null
    private var keyStore: String? = null

    private val STORAGE_PERMISSION_REQUEST = 10001

    private var btnBackUp: Button? = null
    private var btnPrev: Button? = null
    private var btnNext: Button? = null

    private var isAccomplished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            keyStore = arguments!!.getString("KeyStore")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_create_wallet_step3, container, false)

        btnPrev = v.findViewById(R.id.btn_prev)
        btnPrev!!.setOnClickListener(this)
        btnNext = v.findViewById(R.id.btn_next)
        btnNext!!.setOnClickListener(this)

        btnBackUp = v.findViewById(R.id.btn_back_up)
        btnBackUp!!.setOnClickListener(this)
        return v
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStep3Listener) {
            mListener = context
        } else {
            throw RuntimeException("$context must implement OnStep2Listener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    override fun onClick(v: View) {
        val dialog = Basic2ButtonDialog(activity!!)

        when (v.id) {
            R.id.btn_prev -> mListener!!.onStep3Back()

            R.id.btn_next -> mListener!!.onStep3Next()

            R.id.btn_back_up -> checkPermission()
        }
    }

    private fun checkPermission() {
        val dialog = Basic2ButtonDialog(activity!!)
        val permissionCheck = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            dialog.setMessage(getString(R.string.backupKeyStoreFileConfirm))
            dialog.setOnDialogListener(object : Basic2ButtonDialog.OnDialogListener {
                override fun onOk() {
                    if (activity is CreateWalletActivity)
                        isAccomplished = (activity as CreateWalletActivity).backupKeyStoreFile()

                    if (isAccomplished) {
                        val dialog = BasicDialog(activity!!)
                        dialog.setMessage(String.format(getString(R.string.keyStoreDownloadAccomplished), KeyStoreIO.DIR_PATH))
                        dialog.show()
                    }
                }

                override fun onCancel() {

                }
            })
            dialog.show()
        } else {
            ActivityCompat.requestPermissions(activity!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_REQUEST)
        }
    }

    fun setKeyStore(keyStore: String) {
        this.keyStore = keyStore
    }

    interface OnStep3Listener {
        fun onStep3Next()

        fun onStep3Back()
    }

    companion object {

        private val TAG = CreateWalletStep3Fragment::class.java.simpleName

        fun newInstance(keyStore: String): CreateWalletStep3Fragment {
            val fragment = CreateWalletStep3Fragment()
            val args = Bundle()
            args.putString("KeyStore", keyStore)
            //        args.putString(ARG_PARAM2, param2);
            fragment.arguments = args
            return fragment
        }
    }
}// Required empty public constructor
