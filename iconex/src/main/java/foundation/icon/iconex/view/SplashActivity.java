package foundation.icon.iconex.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.MainWalletActivity;
import foundation.icon.iconex.dialogs.PermissionConfirmDialog;
import foundation.icon.iconex.service.VersionCheck;
import foundation.icon.iconex.util.FingerprintAuthBuilder;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.wallet.main.MainActivity;
import loopchain.icon.wallet.core.request.RequestData;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();

    private final int PERMISSION_REQUEST = 10001;

    private RequestData request;

    private boolean isIconConnect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.primary00));
        setContentView(R.layout.activity_splash);

        if (getIntent() != null)
            isIconConnect = getIntent().getBooleanExtra("icon_connect", false);
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
                                if (isIconConnect) {
//                                    if (ICONexApp.connectMethod == Constants.Method.NONE) {
//                                        IconexConnect iconexConnect = new IconexConnect(SplashActivity.this, request);
//                                        if (ICONexApp.wallets.size() > 0)
//                                            iconexConnect.startConnectActivity();
//                                        else
//                                            IconexConnect.sendError(SplashActivity.this, request,
//                                                    new ErrorCodes.Error(ErrorCodes.ERR_EMPTY, ErrorCodes.MSG_EMTPY));
//                                    } else {
//                                        finish();
//                                    }
                                    finish();
                                } else
                                    checkPermissionConfirm();
                            }
                        });
                versionCheck.execute();
            }
        }, 500);
//        startActivity(new Intent(this, IntroActivity.class));
    }

    private void checkPermissionConfirm() {
        PermissionConfirmDialog dialog = new PermissionConfirmDialog(this, R.style.AppTheme);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                PreferenceUtil preferenceUtil = new PreferenceUtil(SplashActivity.this);
                preferenceUtil.setPermissionConfirm(true);

                startActivity();
            }
        });

        if (!ICONexApp.permissionConfirm)
            dialog.show();
        else
            startActivity();
    }

    private void startActivity() {
        if (ICONexApp.wallets.size() > 0) {
            if (ICONexApp.isLocked) {
                StartAuthenticate startAuthenticate = new StartAuthenticate();
                startAuthenticate.execute();
            } else {
                startActivity(new Intent(SplashActivity.this, MainWalletActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
            }
        } else {
            startActivity(new Intent(SplashActivity.this, IntroActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));

            finish();
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

            startActivity(new Intent(SplashActivity.this, AuthActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    }
}
