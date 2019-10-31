package foundation.icon.iconex.view.ui.wallet;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.menu.WalletInfoViewPager;
import foundation.icon.iconex.wallet.Wallet;

public class ViewWalletInfoActivity extends AppCompatActivity {

    private static final String TAG = ViewWalletInfoActivity.class.getSimpleName();

    private Wallet wallet;
    private String privateKey;

    private Button btnClose;
    private TextView txtTitle;

    private ViewPager viewPager;
    private WalletInfoViewPager adapter;
    private ImageView indicator1, indicator2;

    private boolean wasShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallet_info);

        if (getIntent() != null) {
            wallet = ((Wallet) getIntent().getSerializableExtra("wallet"));
            privateKey = getIntent().getStringExtra("privateKey");
        }

        btnClose = findViewById(R.id.btn_close);
        txtTitle = findViewById(R.id.txt_title);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        txtTitle.setText(wallet.getAlias());

        viewPager = findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    indicator1.setSelected(true);
                    indicator2.setSelected(false);

                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                } else {
                    indicator1.setSelected(false);
                    indicator2.setSelected(true);

                    getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

                    if (!wasShown) {
                        MessageDialog dialog = new MessageDialog(ViewWalletInfoActivity.this);
                        dialog.setMessage(getString(R.string.warningPrivateKey));
                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                wasShown = true;
                            }
                        });
                        dialog.show();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        indicator1 = findViewById(R.id.indicator_1);
        indicator1.setSelected(true);
        indicator2 = findViewById(R.id.indicator_2);
    }

    @Override
    public void onResume() {
        super.onResume();

        adapter = new WalletInfoViewPager(getSupportFragmentManager(), wallet.getAddress(), privateKey);
        viewPager.setAdapter(adapter);

        indicator1.setSelected(true);
        indicator2.setSelected(false);
    }
}
