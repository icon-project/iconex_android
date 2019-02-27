package foundation.icon;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import foundation.icon.connect.Constants;
import foundation.icon.connect.ErrorCodes;
import foundation.icon.connect.IconexConnect;
import foundation.icon.connect.RequestData;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.PermissionConfirmDialog;
import foundation.icon.iconex.intro.IntroActivity;
import foundation.icon.iconex.intro.auth.AuthActivity;
import foundation.icon.iconex.service.VersionCheck;
import foundation.icon.iconex.util.FingerprintAuthBuilder;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.wallet.main.MainActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private final int PERMISSION_REQUEST = 10001;

    private RequestData request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (ICONexApp.isConnect)
            request = (RequestData) getIntent().getExtras().get("request");

//        if (getIntent() != null) {
//            String action = getIntent().getAction();
//            if (action.equals(ICONexApp.ICONEX_CONNECT)) {
//                ICONexApp.isConnect = true;
//
//                String data = getIntent().getStringExtra("data");
//                String caller = getIntent().getStringExtra("caller");
//                String receiver = getIntent().getStringExtra("receiver");
//
//                request = new RequestData(data, caller, receiver);
//            }
//        }
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
                                if (ICONexApp.isConnect) {
                                    if (ICONexApp.connectMethod == Constants.Method.NONE) {
                                        IconexConnect iconexConnect = new IconexConnect(SplashActivity.this, request);
                                        if (ICONexApp.mWallets.size() > 0)
                                            iconexConnect.startConnectActivity();
                                        else
                                            IconexConnect.sendError(SplashActivity.this, request,
                                                    new ErrorCodes.Error(ErrorCodes.ERR_EMPTY, ErrorCodes.MSG_EMTPY));
                                    } else {
                                        finish();
                                    }
                                } else
                                    checkPermissionConfirm();
                            }
                        });
                versionCheck.execute();
            }
        }, 500);
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
        if (ICONexApp.mWallets.size() > 0) {
            if (ICONexApp.isLocked) {
                StartAuthenticate startAuthenticate = new StartAuthenticate();
                startAuthenticate.execute();
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class)
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
