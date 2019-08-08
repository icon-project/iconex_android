package foundation.icon.iconex.view

import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import foundation.icon.iconex.R
import foundation.icon.iconex.view.ui.create.CreateStep1Fragment

class CreateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.setBackgroundColor(Color.TRANSPARENT)

        setContentView(R.layout.create_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, CreateStep1Fragment.newInstance())
                    .commitNow()
        }
    }

}
