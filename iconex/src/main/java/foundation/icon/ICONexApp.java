package foundation.icon;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import foundation.icon.connect.Constants;
import foundation.icon.iconex.control.Contacts;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.intro.auth.AuthActivity;
import foundation.icon.iconex.realm.MyMigration;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.VersionCheck;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.wallet.Wallet;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by js on 2018. 1. 30..
 */

public class ICONexApp extends Application {

    private static final String TAG = ICONexApp.class.getSimpleName();

    public static ArrayList<Wallet> mWallets = new ArrayList<>();

    // ========== Exchange Rate ================
    public static List<String> EXCHANGES = new ArrayList<>();
    public static HashMap<String, String> EXCHANGE_TABLE = new HashMap<>();

    // ========== Contacts ================
    public static List<RecentSendInfo> ICXSendInfo = new ArrayList<>();
    public static List<Contacts> ICXContacts = new ArrayList<>();

    public static List<RecentSendInfo> ETHSendInfo = new ArrayList<>();
    public static List<Contacts> ETHContacts = new ArrayList<>();

    // ========== App Lock ================
    public static boolean isLocked = false;
    public static boolean useFingerprint = false;
    public static String language = "";

    private Handler lockTimeLimiter = new Handler();

    // ========== Preference ================
    public static int network = 0;

    // ========== Preference ================
    public static String version = "";

    // ======== ICON Connect ========
    public static final String ICONEX_CONNECT = "ICONEX_CONNECT";
    public static boolean isConnect = false;
    public static Constants.Method connectMethod = Constants.Method.NONE;

    // ======== Developer Mode ========
    public static final String DEVELOPER = "DEVELOPER";
    public static boolean isDeveloper = false;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        init();
        initRealm();
    }

    private void init() {
        PreferenceUtil preferenceUtil = new PreferenceUtil(getApplicationContext());
        preferenceUtil.saveBeingLock(false);

        loadPreferences();

        setLanguage();

        registerActivityLifecycleCallbacks(new MyActivityLifecycleCallbacks());
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
//                .deleteRealmIfMigrationNeeded()
                .schemaVersion(MyConstants.VERSION_REALM_SCHEMA)
                .migration(new MyMigration())
                .build();
        Realm.setDefaultConfiguration(config);
        try {
            RealmUtil.loadWallet();
            RealmUtil.loadContacts();
            RealmUtil.loadRecents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadPreferences() {
        PreferenceUtil mPreference = new PreferenceUtil(getApplicationContext());
        mPreference.loadPreference();
    }

    private void setLanguage() {
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

    public static AppStatus mAppStatus = AppStatus.BACKGROUND;

    // Get app is foreground
    public AppStatus getAppStatus() {
        return mAppStatus;
    }

    // check if app is return foreground
    public boolean isBackground() {
        return mAppStatus.ordinal() == AppStatus.BACKGROUND.ordinal();
    }

    public static boolean isBackgroundStatic() {
        return mAppStatus.ordinal() == AppStatus.BACKGROUND.ordinal();
    }

    public enum AppStatus {
        BACKGROUND, // app is background
        RETURNED_TO_FOREGROUND, // app returned to foreground(or first launch)
        FOREGROUND // app is foreground
    }

    public class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        // running activity count
        private int running = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityStarted(Activity activity) {
            if (++running == 1) {
                // running activity is 1,
                // app must be returned from background just now (or first launch)
                mAppStatus = AppStatus.RETURNED_TO_FOREGROUND;

                PreferenceUtil preferenceUtil = new PreferenceUtil(getApplicationContext());
                boolean beingLock = preferenceUtil.getBeingLock();
                Log.d(TAG, "Foreground beingLock=" + beingLock);
                Log.d(TAG, "activity=" + activity.getLocalClassName());

                if (!activity.getLocalClassName().equals("SplashActivity")) {
                    if (activity.getLocalClassName().equals("wallet.transfer.ICONTransferActivity")
                            || activity.getLocalClassName().equals("wallet.transfer.EtherTransferActivity")) {
                        VersionCheck versionCheck = new VersionCheck(activity);
                        versionCheck.execute();
                    }

                    if (isLocked && beingLock) {
                        if (!activity.getLocalClassName().equals("intro.auth.AuthActivity")) {
                            startActivity(new Intent(getApplicationContext(), AuthActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .putExtra(AuthActivity.ARG_APP_STATUS, AppStatus.RETURNED_TO_FOREGROUND));
                        } else {
                            Log.d(TAG, "remove time callback");
                            lockTimeLimiter.removeCallbacks(mLockTimeLimitTask);
                        }
                    } else {
                        Log.d(TAG, "remove time callback");
                        lockTimeLimiter.removeCallbacks(mLockTimeLimitTask);
                    }

                    preferenceUtil.saveBeingLock(false);
                }
            } else if (running > 1) {
                // 2 or more running activities,
                // should be foreground already.
                mAppStatus = AppStatus.FOREGROUND;
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
        }

        @Override
        public void onActivityPaused(Activity activity) {
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (--running == 0) {
                // no active activity
                // app goes to background
                mAppStatus = AppStatus.BACKGROUND;

                PreferenceUtil preferenceUtil = new PreferenceUtil(getApplicationContext());
                boolean beingLock = preferenceUtil.getBeingLock();
                if (beingLock)
                    preferenceUtil.saveBeingLock(false);

                lockTimeLimiter.postDelayed(mLockTimeLimitTask, MyConstants.LOCK_TIME_LIMIT);
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    }

    private Runnable mLockTimeLimitTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "*** Time Lock Task Executed!!");
            PreferenceUtil preferenceUtil = new PreferenceUtil(getApplicationContext());
            preferenceUtil.saveBeingLock(true);
        }
    };
}
