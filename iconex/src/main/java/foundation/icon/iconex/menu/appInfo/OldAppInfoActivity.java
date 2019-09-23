package foundation.icon.iconex.menu.appInfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;

public class OldAppInfoActivity extends AppCompatActivity implements OldAppInfoFragment.OnAppInfoListener, OldDeveloperFragment.DeveloperOnclick {
    private static final String TAG = OldAppInfoActivity.class.getSimpleName();

    private FragmentManager fragmentManager;
    private OldAppInfoFragment infoFragment;

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

        infoFragment = OldAppInfoFragment.newInstance();
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, infoFragment);
        transaction.addToBackStack("info");
        transaction.commit();
    }

    private void onBack() {
        if (fragmentManager.getBackStackEntryCount() > 1) {
            ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.appInfo));
            findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_close);
            fragmentManager.popBackStackImmediate();

            infoFragment.versionCheck();
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
        transaction.add(R.id.container, OldOSSFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClickDeveloper() {
        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.modeDeveloper));
        findViewById(R.id.btn_close).setBackgroundResource(R.drawable.ic_appbar_back);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, OldDeveloperFragment.newInstance());
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

            infoFragment.versionCheck();
        } else {
            finish();
        }
    }
}
