package foundation.icon.iconex.view.ui.create

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import foundation.icon.ICONexApp
import foundation.icon.MyConstants
import foundation.icon.iconex.R
import foundation.icon.iconex.util.PasswordValidator
import foundation.icon.iconex.util.PasswordValidator.checkPasswordMatch
import foundation.icon.iconex.util.Utils
import foundation.icon.iconex.wallet.Wallet
import foundation.icon.iconex.wallet.WalletEntry
import foundation.icon.iconex.widgets.TTextInputLayout
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import loopchain.icon.wallet.core.Constants
import loopchain.icon.wallet.service.crypto.KeyStoreUtils
import java.util.*

class CreateWalletStep2Fragment : Fragment(), View.OnClickListener {

    private var mListener: OnStep2Listener? = null

    private lateinit var inputAlias: TTextInputLayout
    private lateinit var inputPwd: TTextInputLayout
    private lateinit var inputCheck: TTextInputLayout

    private lateinit var btnBack: Button
    private lateinit var btnNext: Button
    private lateinit var progress: ProgressBar

    private var beforeStr: String = ""
    private var beforePwd: String = ""
    private var beforeCheck: String = ""

    private val OK = 0
    private val ALIAS_DUP = 1
    private val ALIAS_EMPTY = 2

    private val vm: CreateWalletViewModel by lazy {
        ViewModelProviders.of(activity!!).get(CreateWalletViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.layout_create_wallet_step2, container, false)
        initView(view)

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnStep2Listener) {
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
        inputAlias = v.findViewById(R.id.input_alias)
        inputAlias.setOnFocusChangedListener(object : TTextInputLayout.OnFocusReleased {
            override fun onReleased() {
                when (checkAlias(inputAlias.getText())) {
                    ALIAS_DUP -> inputAlias.setError(true, getString(R.string.duplicateWalletAlias))
                    else -> inputAlias.setError(false, null)
                }
            }
        })
        inputAlias.setOnTextChangedListener(object : TTextInputLayout.OnTextChanged {
            override fun onChanged(s: CharSequence) {
                if (s.isNotEmpty()) {
                    if (s.toString().trim { it <= ' ' }.isEmpty()) {
                        inputAlias.setText("")
                    } else if (s[0] == ' ') {
                        inputAlias.setText(beforeStr)
                    } else {
                        if (Utils.checkByteLength(s.toString()) > 16) {
                            inputAlias.setText(beforeStr)
                        } else {
                            beforeStr = s.toString()
                        }
                    }
                } else {
                    btnNext.isEnabled = false
                }
            }
        })
        inputAlias.setOnKeyPreImeListener(onKeyPreIme)

        inputPwd = v.findViewById(R.id.input_pwd)
        inputPwd.setOnFocusChangedListener(object : TTextInputLayout.OnFocusReleased {
            override fun onReleased() {
                val result = PasswordValidator.validatePassword(inputPwd.getText())
                when (result) {

                    PasswordValidator.LEAST_8 -> inputPwd.setError(true, getString(R.string.errAtLeast))

                    PasswordValidator.NOT_MATCH_PATTERN -> inputPwd.setError(true, getString(R.string.errPasswordPatternMatch))

                    PasswordValidator.HAS_WHITE_SPACE -> inputPwd.setError(true, getString(R.string.errWhiteSpace))

                    PasswordValidator.SERIAL_CHAR -> inputPwd.setError(true, getString(R.string.errSerialChar))

                    else -> inputPwd.setError(false, null)
                }
            }
        })
        inputPwd.setOnTextChangedListener(object : TTextInputLayout.OnTextChanged {
            override fun onChanged(s: CharSequence) {
                if (s.isNotEmpty()) {
                    if (s[s.length - 1] == ' ') {
                        inputPwd.setText(s.subSequence(0, s.length - 1).toString())

                    } else if (s.toString().contains(" ")) {
                        inputPwd.setText(beforePwd)
                    } else {
                        beforePwd = s.toString()
                    }
                } else {
                    btnNext.isEnabled = false
                }
            }
        })
        inputPwd.setOnKeyPreImeListener(onKeyPreIme)

        inputCheck = v.findViewById(R.id.input_pwd_check)
        inputCheck.setOnTextChangedListener(object : TTextInputLayout.OnTextChanged {
            override fun onChanged(s: CharSequence) {
                if (s.isNotEmpty()) {
                    if (s[s.length - 1] == ' ') {
                        inputCheck.setText(s.subSequence(0, s.length - 1).toString())
                    } else if (s.toString().contains(" ")) {
                        inputCheck.setText(beforeCheck)
                    } else {
                        beforeCheck = s.toString()
                    }
                } else {
                    btnNext.isEnabled = false
                }
            }
        })
        inputCheck.setOnKeyPreImeListener(onKeyPreIme)
        inputCheck.setOnEditorActionListener(object : TTextInputLayout.OnEditorAction {
            override fun onDone() {
                setNextEnable(inputAlias.getText(), inputPwd.getText(), inputCheck.getText())
            }
        })

        btnBack = v.findViewById(R.id.btn_back)
        btnBack.setOnClickListener(this)
        btnNext = v.findViewById(R.id.btn_next)
        btnNext.setOnClickListener(this)

        progress = v.findViewById(R.id.progress)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_next -> {
                btnNext.isEnabled = false
                progress.visibility = View.VISIBLE

                Observable.just(0)
                        .map { createWallet() }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(object : Observer<Array<String>> {
                            override fun onComplete() {
                                progress.visibility = View.GONE
                            }

                            override fun onSubscribe(d: Disposable) {
                            }

                            override fun onNext(t: Array<String>) {
                                val wallet = Wallet()
                                wallet.alias = inputAlias.getText()
                                wallet.address = t[0]
                                vm.setPrivateKey(t[1])
                                wallet.keyStore = t[2]
                                wallet.coinType = vm.getCoinType().value!!.type

                                val entries = ArrayList<WalletEntry>()
                                val coin = WalletEntry()
                                coin.type = MyConstants.TYPE_COIN
                                coin.address = t[0]

                                if (wallet.coinType == Constants.KS_COINTYPE_ICX) {
                                    coin.name = MyConstants.NAME_ICX
                                    coin.symbol = Constants.KS_COINTYPE_ICX
                                } else {
                                    coin.name = MyConstants.NAME_ETH
                                    coin.symbol = Constants.KS_COINTYPE_ETH
                                }

                                entries.add(coin)
                                wallet.walletEntries = entries

                                vm.setWallet(wallet)

                                mListener!!.onStep2Done()
                            }

                            override fun onError(e: Throwable) {
                                e.printStackTrace()
                            }
                        })
            }

            R.id.btn_back -> mListener!!.onStep2Back()
        }
    }

    private var onKeyPreIme = object : TTextInputLayout.OnKeyPreIme {
        override fun onDone() {
            setNextEnable(inputAlias.getText(),
                    inputPwd.getText(),
                    inputCheck.getText())
        }
    }

    private fun checkAlias(target: String): Int {
        val alias = Utils.strip(target)
        if (alias.isEmpty())
            return ALIAS_EMPTY

        for (info in ICONexApp.mWallets) {
            if (info.alias == alias) {
                return ALIAS_DUP
            }
        }

        return OK
    }

    private fun setNextEnable(alias: String, pwd: String, checkPwd: String) {
        val aliasValidate: Int
        val pwdValidate: Int
        val matched: Boolean

        if (inputAlias.getText().isNotEmpty()) {
            aliasValidate = checkAlias(alias)
            when (aliasValidate) {
                ALIAS_EMPTY -> inputAlias.setError(true, getString(R.string.errAliasEmpty))

                ALIAS_DUP -> inputAlias.setError(true, getString(R.string.duplicateWalletAlias))
                else -> inputAlias.setError(false, null)
            }
        } else {
            inputAlias.setError(true, getString(R.string.errAliasEmpty))
            btnNext.isEnabled = false
            return
        }

        if (inputPwd.getText().isNotEmpty()) {
            pwdValidate = PasswordValidator.validatePassword(pwd)
            if (pwdValidate != 0) {
                when (pwdValidate) {
                    PasswordValidator.LEAST_8 -> inputPwd.setError(true, getString(R.string.errAtLeast))

                    PasswordValidator.NOT_MATCH_PATTERN -> inputPwd.setError(true, getString(R.string.errPasswordPatternMatch))

                    PasswordValidator.HAS_WHITE_SPACE -> inputPwd.setError(true, getString(R.string.errWhiteSpace))

                    PasswordValidator.SERIAL_CHAR -> inputPwd.setError(true, getString(R.string.errSerialChar))

                    else -> inputPwd.setError(false, null)
                }
            }
        } else {
            inputPwd.setError(true, getString(R.string.errPwdEmpty))
            btnNext.isEnabled = false
            return
        }

        if (inputCheck.getText().isNotEmpty()) {
            matched = checkPasswordMatch(pwd, checkPwd)
            if (!matched) {
                inputCheck.setError(true, getString(R.string.errPasswordNotMatched))
            } else {
                inputCheck.setError(false, null)
            }
        } else {
            inputCheck.setError(true, getString(R.string.errCheckEmpty))
            btnNext.isEnabled = false
            return
        }

        if (aliasValidate == 0 && matched && pwdValidate == 0)
            btnNext.isEnabled = true
        else
            btnNext.isEnabled = false
    }

    private fun createWallet(): Array<String> {
        if (vm.getCoinType().value == CreateWalletViewModel.CoinType.ICX)
            return KeyStoreUtils.generateICXKeystore(inputPwd.getText())
        else
            return KeyStoreUtils.generateEtherKeystore(inputPwd.getText())
    }

    interface OnStep2Listener {
        fun onStep2Done()

        fun onStep2Back()
    }

    companion object {

        private val TAG = CreateWalletStep2Fragment::class.java.simpleName

        fun newInstance(): CreateWalletStep2Fragment {
            return CreateWalletStep2Fragment()
        }
    }
}// Required empty public constructor
