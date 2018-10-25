package foundation.icon.iconex;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import java.util.Locale;

import foundation.icon.iconex.intro.IntroActivity;
import foundation.icon.iconex.intro.auth.AuthActivity;
import foundation.icon.iconex.service.VersionCheck;
import foundation.icon.iconex.util.FingerprintAuthBuilder;
import foundation.icon.iconex.wallet.main.MainActivity;

import static foundation.icon.iconex.ICONexApp.language;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private final int PERMISSION_REQUEST = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    public void onResume() {
        super.onResume();

        Handler localHandler = new Handler();
        localHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                VersionCheck versionCheck = new VersionCheck(SplashActivity.this,
                        new VersionCheck.VersionCheckCallback() {
                            @Override
                            public void onNeedUpdate() {
                                // Do nothing.
                            }

                            @Override
                            public void onPass() {
                                checkPermission();
                            }
                        });
                versionCheck.execute();
            }
        }, 500);

        Locale locale;

        if (language.isEmpty()) {
            if (Locale.getDefault().getLanguage().equals(MyConstants.LOCALE_KO))
                language = MyConstants.LOCALE_KO;
            else
                language = MyConstants.LOCALE_EN;
        }

        locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

    }

    private void checkPermission() {
        if (ICONexApp.mWallets.size() > 0) {
                if (ICONexApp.isLocked) {
                    StartAuthenticate startAuthenticate = new StartAuthenticate();
                    startAuthenticate.execute();
                } else {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                }
        } else {
            startActivity(new Intent(SplashActivity.this, IntroActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {
                if (ICONexApp.mWallets.size() > 0) {
                    if (ICONexApp.isLocked) {
                        StartAuthenticate startAuthenticate = new StartAuthenticate();
                        startAuthenticate.execute();
                    } else {
                        startActivity(new Intent(SplashActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
                        finish();
                    }
                } else {
                    startActivity(new Intent(SplashActivity.this, IntroActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

                    finish();
                }
                return;
            }
        }
    }

    class StartAuthenticate extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (ICONexApp.useFingerprint) {
                FingerprintAuthBuilder builder = new FingerprintAuthBuilder(SplashActivity.this);

                try {
                    boolean hasKey = builder.hasKey();

                    if (!hasKey)
                        builder.createKey(FingerprintAuthBuilder.DEFAULT_KEY_NAME, true);
                } catch (Exception e) {
                    builder.createKey(FingerprintAuthBuilder.DEFAULT_KEY_NAME, true);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            finish();
        }
    }
}
