package foundation.icon.iconex.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import foundation.icon.iconex.R
import foundation.icon.iconex.view.ui.create.CreateStep1Fragment

class CreateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.TestActivity)

        setContentView(R.layout.create_wallet)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, CreateStep1Fragment.newInstance())
                    .commitNow()
        }
    }

}
