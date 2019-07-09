package foundation.icon.iconex.menu;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.BasicDialog;

public class ViewWalletInfoActivity extends AppCompatActivity {

    private static final String TAG = ViewWalletInfoActivity.class.getSimpleName();

    private String walletAlias;
    private String mCoinName;
    private String mAddress;
    private String mPrivKey;
    private String mDate;

    private Button btnBack;
    private TextView txtCoinName, txtDate;

    private ViewPager viewPager;
    private WalletInfoViewPager adapter;
    private ImageView indicator1, indicator2;

    private boolean wasShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_wallet_info);

        if (getIntent() != null) {
            walletAlias = getIntent().getStringExtra("alias");
            mCoinName = getIntent().getStringExtra("coinName");
            mAddress = getIntent().getStringExtra("address");
            mPrivKey = getIntent().getStringExtra("privateKey");
            mDate = getIntent().getStringExtra("date");
        }

        ((TextView) findViewById(R.id.txt_title)).setText(walletAlias);

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtCoinName = findViewById(R.id.txt_name);
        txtCoinName.setText(mCoinName);

        txtDate = findViewById(R.id.txt_date);
        txtDate.setText(getDate());

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
                        BasicDialog dialog = new BasicDialog(ViewWalletInfoActivity.this);
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

        adapter = new WalletInfoViewPager(getSupportFragmentManager(), mAddress, mPrivKey);
        viewPager.setAdapter(adapter);

        indicator1.setSelected(true);
        indicator2.setSelected(false);
    }

    private String getDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(Long.parseLong(mDate)));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(calendar.getTime());
    }
}
