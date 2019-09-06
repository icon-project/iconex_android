package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.MainWalletActivity;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.view.ui.auth.AuthFingerprintFragment;
import foundation.icon.iconex.view.ui.auth.AuthLockNumFragment;
import foundation.icon.iconex.view.ui.auth.UserVerificationFragment;
import foundation.icon.iconex.wallet.main.MainActivity;
import foundation.icon.iconex.menu.lock.SettingLockActivity;

public class AuthActivity extends AppCompatActivity implements AuthFingerprintFragment.OnFingerprintLockListener,
        AuthLockNumFragment.OnLockNumAuthListener, UserVerificationFragment.OnVerificationListener {

    private static final String TAG = AuthActivity.class.getSimpleName();

    private FragmentManager fm;
    private AuthLockNumFragment frgLockNum;
    private AuthFingerprintFragment frgFinger;

    public static final String EXTRA_INVALIDATED = "EXTRA_INVALIDATED";
    public static final String ARG_APP_STATUS = "ARG_APP_STATUS";
    private ICONexApp.AppStatus mAppStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (getIntent().getSerializableExtra(ARG_APP_STATUS) != null)
            mAppStatus = (ICONexApp.AppStatus) getIntent().getSerializableExtra(ARG_APP_STATUS);

        fm = getSupportFragmentManager();
        frgLockNum = AuthLockNumFragment.newInstance();
        frgFinger = AuthFingerprintFragment.newInstance();

        if (ICONexApp.useFingerprint)
            setFragment(frgFinger, true);
        else
            setFragment(frgLockNum, true);
    }

    private void setFragment(Fragment fragment, boolean replace) {
        FragmentTransaction ft = fm.beginTransaction();
        if (replace) {
            ft.replace(R.id.container, fragment);
        } else {
            ft.addToBackStack(null);
            ft.add(R.id.container, fragment);
        }
        ft.commit();
    }

    @Override
    public void onLockNumUnlockSuccess() {
        if (mAppStatus == ICONexApp.AppStatus.RETURNED_TO_FOREGROUND) {
            finish();
        } else {
            PreferenceUtil preferenceUtil = new PreferenceUtil(this);
            preferenceUtil.saveBeingLock(false);

            startActivity(new Intent(this, MainWalletActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }

    @Override
    public void onFingerprintInvalidated() {
        PreferenceUtil preferenceUtil = new PreferenceUtil(this);
        preferenceUtil.saveUseFingerprint(false);
        preferenceUtil.loadPreference();

        if (mAppStatus == ICONexApp.AppStatus.RETURNED_TO_FOREGROUND) {
            finish();
        } else {
            preferenceUtil.saveBeingLock(false);

            startActivity(new Intent(this, MainWalletActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra(EXTRA_INVALIDATED, true));
            finish();
        }
    }

    @Override
    public void onLockNumUnlockFailed() {

    }

    @Override
    public void onFingerprintSuccess() {
        if (mAppStatus == ICONexApp.AppStatus.RETURNED_TO_FOREGROUND) {
            finish();
        } else {
            PreferenceUtil preferenceUtil = new PreferenceUtil(this);
            preferenceUtil.saveBeingLock(false);

            startActivity(new Intent(this, MainWalletActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }

    @Override
    public void onLostLockNum() {
        setFragment(UserVerificationFragment.newInstance(), false);
    }

    @Override
    public void onFingerprintFailed() {

    }

    @Override
    public void onByLockNum(MyConstants.FingerprintState state) {
        if (state == MyConstants.FingerprintState.DISABLED || state == MyConstants.FingerprintState.NO_ENROLLED) {
            PreferenceUtil preferenceUtil = new PreferenceUtil(this);
            preferenceUtil.saveUseFingerprint(false);
            preferenceUtil.loadPreference();
        }

        frgLockNum.setInvalidated(state);
        setFragment(frgLockNum, true);
    }

    @Override
    public void onVerification() {
        fm.popBackStackImmediate();
        startActivity(new Intent(this, SettingLockActivity.class)
                .putExtra(SettingLockActivity.ARG_TYPE, MyConstants.TypeLock.LOST));
    }

    @Override
    public void onVerificationBack() {
        fm.popBackStackImmediate();
    }

    @Override
    public void onBackPressed() {
        if (fm.getBackStackEntryCount() > 0)
            fm.popBackStackImmediate();
        else {
            if (mAppStatus == ICONexApp.AppStatus.RETURNED_TO_FOREGROUND)
                finishAffinity();
            else
                finish();
        }
    }
}
