package foundation.icon.iconex.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import foundation.icon.iconex.R
import foundation.icon.iconex.control.IntroViewPagerAdapter
import foundation.icon.iconex.wallet.load.LoadWalletActivity

class IntroActivity : AppCompatActivity(), View.OnClickListener {

    private var introPager: ViewPager? = null
    private var pagerAdapter: IntroViewPagerAdapter? = null

    private var btnCreate: Button? = null
    private var btnLoad: Button? = null
    private var indicator1: ImageView? = null
    private var indicator2: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        indicator1 = findViewById(R.id.indicator_1)
        indicator1!!.isSelected = true
        indicator2 = findViewById(R.id.indicator_2)

        btnCreate = findViewById(R.id.btn_create_wallet)
        btnCreate!!.setOnClickListener(this)
        btnLoad = findViewById(R.id.btn_load_wallet)
        btnLoad!!.setOnClickListener(this)

        introPager = findViewById(R.id.intro_view_pager)
        pagerAdapter = IntroViewPagerAdapter(supportFragmentManager)
        introPager!!.adapter = pagerAdapter
        introPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        indicator1!!.isSelected = true
                        indicator2!!.isSelected = false
                    }

                    1 -> {
                        indicator1!!.isSelected = false
                        indicator2!!.isSelected = true
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_create_wallet -> startActivity(Intent(this, CreateWalletActivity::class.java))

            R.id.btn_load_wallet -> startActivity(Intent(this, LoadWalletActivity::class.java))
        }
    }

    companion object {

        private val TAG = IntroActivity::class.java.simpleName
    }
}
