package foundation.icon.iconex;

import android.app.Activity;
import android.app.Application;
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

import foundation.icon.iconex.control.Contacts;
import foundation.icon.iconex.control.RecentSendInfo;
import foundation.icon.iconex.control.WalletInfo;
import foundation.icon.iconex.intro.auth.AuthActivity;
import foundation.icon.iconex.realm.MyMigration;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.service.VersionCheck;
import foundation.icon.iconex.util.PreferenceUtil;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by js on 2018. 1. 30..
 */

public class ICONexApp extends Application {

    private static final String TAG = ICONexApp.class.getSimpleName();

    public static ArrayList<WalletInfo> mWallets = new ArrayList<>();

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
    public static final boolean isMain = true;

    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initRealm();
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
                Log.d(TAG, "beingLock=" + beingLock);

                if (!activity.getLocalClassName().equals("SplashActivity")) {
                    VersionCheck versionCheck = new VersionCheck(activity);
                    versionCheck.execute();

                    if (isLocked && beingLock) {
                        startActivity(new Intent(getApplicationContext(), AuthActivity.class)
                                .putExtra(AuthActivity.ARG_APP_STATUS, AppStatus.RETURNED_TO_FOREGROUND));
                    } else {
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
            PreferenceUtil preferenceUtil = new PreferenceUtil(getApplicationContext());
            preferenceUtil.saveBeingLock(true);
        }
    };
}
