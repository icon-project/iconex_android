package foundation.icon.iconex.view.ui.create

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.gson.Gson
import com.google.gson.JsonObject
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs.Basic2ButtonDialog
import foundation.icon.iconex.dialogs.BasicDialog
import foundation.icon.iconex.util.KeyStoreIO
import io.reactivex.Completable
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CreateWalletStep3Fragment : Fragment(), View.OnClickListener {

    private var mListener: OnStep3Listener? = null
    private var keyStore: String? = null

    private val STORAGE_PERMISSION_REQUEST = 10001

    private var btnBackUp: Button? = null
    private var btnPrev: Button? = null
    private var btnNext: Button? = null

    private var isAccomplished = false

    private val vm: CreateWalletViewModel by lazy {
        ViewModelProviders.of(activity!!).get(CreateWalletViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_create_wallet_step3, container, false)

        btnPrev = v.findViewById(R.id.btn_back)
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
            R.id.btn_back -> mListener!!.onStep3Back()

            R.id.btn_next -> {
                dialog.setOnDialogListener(object : Basic2ButtonDialog.OnDialogListener {
                    override fun onOk() {
                        mListener!!.onStep3Next()
                    }

                    override fun onCancel() {

                    }
                })

                if (isAccomplished) {
                    mListener!!.onStep3Next()
                } else {
                    dialog.setMessage(getString(R.string.noBackupKeyStoreFileConfirm))
                    dialog.show()
                }
            }

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
                    Completable.fromAction { backupKeyStoreFile() }
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(object : CompletableObserver {
                                override fun onComplete() {
                                    isAccomplished = true

                                    val dialog = BasicDialog(activity!!)
                                    dialog.setMessage(String.format(getString(R.string.keyStoreDownloadAccomplished), KeyStoreIO.DIR_PATH))
                                    dialog.show()
                                }

                                override fun onSubscribe(d: Disposable) {
                                }

                                override fun onError(e: Throwable) {
                                }
                            })
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

    fun backupKeyStoreFile(): Boolean {
        try {
            val keyStore = Gson().fromJson(vm.getWallet().value!!.keyStore, JsonObject::class.java)
            KeyStoreIO.exportKeyStore(keyStore, vm.getCoinType().value!!.type)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
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

        fun newInstance(): CreateWalletStep3Fragment {
            return CreateWalletStep3Fragment()
        }
    }
}// Required empty public constructor
