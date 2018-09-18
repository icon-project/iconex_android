package foundation.icon.iconex.wallet.menu.appInfo;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;

public class AppInfoActivity extends AppCompatActivity implements AppInfoFragment.OnAppInfoListener {

    private FragmentManager fragmentManager;

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

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, AppInfoFragment.newInstance());
        transaction.addToBackStack(null);
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
    public void onClickNP() {
        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.networkProvider));
        findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_back);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, NetworkProviderFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
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
