package foundation.icon.iconex.view

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import foundation.icon.ICONexApp
import foundation.icon.connect.RequestData
import foundation.icon.iconex.R
import foundation.icon.iconex.dialogs.PermissionConfirmDialog
import foundation.icon.iconex.intro.auth.AuthActivity
import foundation.icon.iconex.util.FingerprintAuthBuilder
import foundation.icon.iconex.util.PreferenceUtil
import foundation.icon.iconex.wallet.main.MainActivity

class SplashActivity : AppCompatActivity() {
    val TAG = this@SplashActivity::class.simpleName

    private val PERMISSION_REQUEST = 10001

    private val request: RequestData? = null

    private var isIconConnect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (intent != null)
            isIconConnect = intent.getBooleanExtra("icon_connect", false)
    }

    public override fun onResume() {
        super.onResume()

//        val localHandler = Handler()
//        localHandler.postDelayed({
//            val versionCheck = VersionCheck(this@SplashActivity,
//                    object : VersionCheck.VersionCheckCallback {
//                        override fun onNeedUpdate() {
//                            // Do nothing.
//                        }
//
//                        override fun onPass() {
//                            if (isIconConnect) {
//                                finish()
//                            } else
//                                checkPermissionConfirm()
//                        }
//                    })
//            versionCheck.execute()
//        }, 500)

        startActivity(Intent(this@SplashActivity, CreateActivity::class.java))
    }

    private fun checkPermissionConfirm() {
        val dialog = PermissionConfirmDialog(this, R.style.AppTheme)
        dialog.setOnDismissListener {
            val preferenceUtil = PreferenceUtil(this@SplashActivity)
            preferenceUtil.setPermissionConfirm(true)

            startActivity()
        }

        if (!ICONexApp.permissionConfirm)
            dialog.show()
        else
            startActivity()
    }

    private fun startActivity() {
        if (ICONexApp.mWallets.size > 0) {
            if (ICONexApp.isLocked) {
                val startAuthenticate = StartAuthenticate()
                startAuthenticate.execute()
            } else {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
            }
        } else {
            startActivity(Intent(this@SplashActivity, IntroActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))

            finish()
        }
    }

    internal inner class StartAuthenticate : AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg voids: Void): Void? {
            if (ICONexApp.useFingerprint) {
                val builder = FingerprintAuthBuilder(this@SplashActivity)

                try {
                    val hasKey = builder.hasKey()

                    if (!hasKey)
                        builder.createKey(FingerprintAuthBuilder.DEFAULT_KEY_NAME, true)
                } catch (e: Exception) {
                    builder.createKey(FingerprintAuthBuilder.DEFAULT_KEY_NAME, true)
                }

            }

            return null
        }

        override fun onPostExecute(aVoid: Void) {
            super.onPostExecute(aVoid)

            startActivity(Intent(this@SplashActivity, AuthActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    }
}
