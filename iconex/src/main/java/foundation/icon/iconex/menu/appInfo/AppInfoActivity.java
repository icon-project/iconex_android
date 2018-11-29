package foundation.icon.iconex.menu.appInfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;

public class AppInfoActivity extends AppCompatActivity implements AppInfoFragment.OnAppInfoListener, DeveloperFragment.DeveloperOnclick {
    private static final String TAG = AppInfoActivity.class.getSimpleName();

    private FragmentManager fragmentManager;
    private AppInfoFragment infoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.appInfo));
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });

        infoFragment = AppInfoFragment.newInstance();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, infoFragment);
        transaction.addToBackStack("info");
        transaction.commit();
    }

    private void setFragment() {

    }

    private void onBack() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.appInfo));
            findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_close);
            fragmentManager.popBackStackImmediate();
        } else {
            finish();
        }
    }

    @Override
    public void onUpdate() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ServiceConstants.URL_STORE)));
        finishAffinity();
    }

    @Override
    public void onClickOSS() {
        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.openSource));
        findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_back);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, OSSFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClickDeveloper() {
        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.modeDeveloper));
        findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_back);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, DeveloperFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onOff() {
        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.appInfo));
        findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_close);
        fragmentManager.popBackStackImmediate();

        infoFragment.versionCheck();
        infoFragment.setDeveloperMode();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.appInfo));
            findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_close);
            fragmentManager.popBackStackImmediate();
        } else {
            finish();
        }
    }
}
