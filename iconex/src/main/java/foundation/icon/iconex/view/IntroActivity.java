package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.IntroViewPagerAdapter;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = IntroActivity.class.getSimpleName();

    private ViewPager introPager;
    private IntroViewPagerAdapter pagerAdapter;

    private Button btnCreate, btnLoad;
    private ImageView indicator1, indicator2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
        setContentView(R.layout.activity_intro);

        indicator1 = findViewById(R.id.indicator_1);
        indicator1.setSelected(true);
        indicator2 = findViewById(R.id.indicator_2);

        btnCreate = findViewById(R.id.btn_create_wallet);
        btnCreate.setOnClickListener(this);
        btnLoad = findViewById(R.id.btn_load_wallet);
        btnLoad.setOnClickListener(this);

        introPager = findViewById(R.id.intro_view_pager);
        pagerAdapter = new IntroViewPagerAdapter(getSupportFragmentManager());
        introPager.setAdapter(pagerAdapter);
        introPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        indicator1.setSelected(true);
                        indicator2.setSelected(false);
                        break;

                    case 1:
                        indicator1.setSelected(false);
                        indicator2.setSelected(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_create_wallet:
                startActivity(new Intent(this, CreateWalletActivity.class));
                break;

            case R.id.btn_load_wallet:
                startActivity(new Intent(this, LoadWalletActivity.class));
                break;
        }
    }
}
