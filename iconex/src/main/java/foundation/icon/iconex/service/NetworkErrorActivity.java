package foundation.icon.iconex.service;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import foundation.icon.iconex.R;

public class NetworkErrorActivity extends AppCompatActivity {

    private static final String TAG = NetworkErrorActivity.class.getSimpleName();

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
