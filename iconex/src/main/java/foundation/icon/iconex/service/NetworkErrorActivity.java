package foundation.icon.iconex.service;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.SplashActivity;

public class NetworkErrorActivity extends AppCompatActivity {

    private static final String TAG = NetworkErrorActivity.class.getSimpleName();

    public static String PARAM_TARGET_SPLASH = "param_target_splash";

    private Button btnRetry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_error);

        btnRetry = findViewById(R.id.btn_retry);
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VersionCheck versionCheck = new VersionCheck(NetworkErrorActivity.this,
                        new VersionCheck.VersionCheckCallback() {
                            @Override
                            public void onNeedUpdate() {
                                // Do nothing.
                            }

                            @Override
                            public void onPass() {
                                boolean isTargetSplash = getIntent().getBooleanExtra(PARAM_TARGET_SPLASH, false);
                                if (isTargetSplash)
                                    startActivity(new Intent(NetworkErrorActivity.this, SplashActivity.class));
                                finish();
                            }
                        });
                versionCheck.execute();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Do nothing.
    }
}
