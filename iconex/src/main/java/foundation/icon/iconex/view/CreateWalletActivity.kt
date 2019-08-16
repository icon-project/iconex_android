package foundation.icon.iconex.view

import android.os.Bundle
import android.widget.Button
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import foundation.icon.iconex.R
import foundation.icon.iconex.view.ui.create.*

class CreateWalletActivity : FragmentActivity(), CreateWalletStep1Fragment.OnStep1Listener,
        CreateWalletStep2Fragment.OnStep2Listener, CreateWalletStep3Fragment.OnStep3Listener,
        CreateWalletStep4Fragment.OnStep4Listener {

    private val vm: CreateWalletViewModel by lazy {
        ViewModelProviders.of(this).get(CreateWalletViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_wallet)

        initView()

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, CreateWalletStep1Fragment())
                .commitNow()
    }

    private fun initView() {
        val btnClose = findViewById<Button>(R.id.btn_close)
        btnClose.setOnClickListener {
            finish()
        }
    }

    override fun onStep1Done(coinType: String) {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, CreateWalletStep2Fragment())
                .commit()
    }

    override fun onStep2Done(name: String, pwd: String) {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, CreateWalletStep3Fragment())
                .commit()
    }

    override fun onStep2Back() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onShowInputMode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onHideInputMode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStep3Next() {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, CreateWalletStep4Fragment())
                .commit()
    }

    override fun onStep3Back() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStep4Done() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStep4Back() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStep4Next() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun showWalletInfo(privateKey: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
