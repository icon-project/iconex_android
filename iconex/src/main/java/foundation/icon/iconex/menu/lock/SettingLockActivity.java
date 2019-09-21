package foundation.icon.iconex.menu.lock;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.MainWalletActivity;
import foundation.icon.iconex.wallet.main.MainActivity;
import foundation.icon.iconex.widgets.CustomActionBar;

public class SettingLockActivity extends AppCompatActivity implements AppLockManageFragment.OnAppLockManageListener,
        SetFingerprintLockFragment.OnSetFingerprintLockListener, SetLockNumFragment.OnSetLockNumListener {

    private static final String TAG = SettingLockActivity.class.getSimpleName();

    // private Button btnBack;
    // private TextView txtTitle;
    private CustomActionBar appbar;

    private FragmentManager fm;
    private FragmentTransaction ft;

    private AppLockManageFragment alFragment;

    public static String ARG_TYPE = "ARG_TYPE";
    private MyConstants.TypeLock type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_lock);

        if (getIntent() != null)
            type = (MyConstants.TypeLock) getIntent().getSerializableExtra(ARG_TYPE);

        appbar = findViewById(R.id.appbar);
        appbar.setTitle(getString(R.string.titleAppLock));
        appbar.setOnClickStartIcon(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fm.getBackStackEntryCount() > 1) {
                    if (type == MyConstants.TypeLock.LOST) {
                        finish();
                    } else {
                        fm.popBackStackImmediate();
                        appbar.setTitle(getString(R.string.titleAppLock));
                        alFragment.refresh();
                    }
                } else {
                    startActivity(new Intent(SettingLockActivity.this, MainWalletActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                    finish();
                }
            }
        });

        fm = getSupportFragmentManager();
        alFragment = AppLockManageFragment.newInstance();

        if (type == MyConstants.TypeLock.DEFAULT) {
            setFragment(alFragment);
        } else if (type == MyConstants.TypeLock.LOST) {
            setFragment(alFragment);
            appbar.setTitle(getString(R.string.titleSetLockNum));
            setFragment(SetLockNumFragment.newInstance(SetLockNumFragment.TYPE.USE));

        } else if (type == MyConstants.TypeLock.RECOVER) {
            appbar.setTitle(getString(R.string.titleFingerprintLock));
            setFragment(SetFingerprintLockFragment.newInstance());
        }

    }

    private void setFragment(Fragment fragment) {
        ft = fm.beginTransaction();
        ft.addToBackStack(null);
        ft.add(R.id.container, fragment);
        ft.commit();
    }

    public MyConstants.TypeLock getLockType() {
        return type;
    }

    @Override
    public void onSetLockNumUse(boolean isLocked) {
        if (isLocked) {
            appbar.setTitle(getString(R.string.titleSetLockNum));
            setFragment(SetLockNumFragment.newInstance(SetLockNumFragment.TYPE.USE));
        } else {
            setFragment(SetLockNumFragment.newInstance(SetLockNumFragment.TYPE.DISUSE));
        }
    }

    @Override
    public void onLockNumBack() {
        fm.popBackStackImmediate();
        appbar.setTitle(getString(R.string.titleAppLock));
        alFragment.refresh();
    }

    @Override
    public void onResetAppLock() {
        appbar.setTitle(getString(R.string.titleChangeLockNum));
        setFragment(SetLockNumFragment.newInstance(SetLockNumFragment.TYPE.RESET));
    }

    @Override
    public void onFingerBack() {
        if (type == MyConstants.TypeLock.RECOVER)
            finish();
        else {
            fm.popBackStackImmediate();
            appbar.setTitle(getString(R.string.titleAppLock));
            alFragment.refresh();
        }
    }

    @Override
    public void onUnlockFinger() {
        appbar.setTitle(getString(R.string.titleFingerprintLock));
        setFragment(SetFingerprintLockFragment.newInstance());
    }

    @Override
    public void onBackPressed() {
        if (fm.getBackStackEntryCount() > 1) {
            if (type == MyConstants.TypeLock.LOST) {
                finish();
            } else {
                fm.popBackStackImmediate();
                appbar.setTitle(getString(R.string.titleAppLock));
                alFragment.refresh();
            }
        } else {
            startActivity(new Intent(SettingLockActivity.this, MainWalletActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            finish();
        }
    }
}
