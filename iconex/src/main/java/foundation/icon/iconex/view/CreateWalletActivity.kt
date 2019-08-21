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

    override fun onStep1Done() {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, CreateWalletStep2Fragment())
                .addToBackStack("step2")
                .commit()
    }

    override fun onStep2Done() {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, CreateWalletStep3Fragment())
                .addToBackStack("step3")
                .commit()
    }

    override fun onStep2Back() {
        supportFragmentManager.popBackStack()
    }

    override fun onStep3Next() {
        supportFragmentManager.beginTransaction()
                .add(R.id.container, CreateWalletStep4Fragment())
                .addToBackStack("step4")
                .commit()
    }

    override fun onStep3Back() {
        supportFragmentManager.popBackStack()
    }

    override fun onStep4Back() {
        supportFragmentManager.popBackStack()
    }

    override fun showWalletInfo() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
