package foundation.icon.iconex.wallet.contacts;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import loopchain.icon.wallet.core.Constants;

public class ContactsActivity extends AppCompatActivity implements ContactsFragment.OnContactsClickListener, MyContactsFragment.OnContactListener {

    private static final String TAG = ContactsActivity.class.getSimpleName();

    private Button btnClose;
    private TextView btnMod;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ContactsViewPagerAdapter viewPagerAdapter;

    private String mCoinType;
    private String mAddress;

    public static final int CODE_REQUEST = 30000;
    public static final int CODE_RESULT = 30001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.contactsTitle));

        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnMod = findViewById(R.id.txt_mod);
        btnMod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnMod.isSelected()) {
                    btnMod.setSelected(false);
                    btnMod.setText(getString(R.string.edit));
                    viewPagerAdapter.setEditable(false);
                } else {
                    btnMod.setSelected(true);
                    btnMod.setText(getString(R.string.complete));
                    viewPagerAdapter.setEditable(true);
                }
            }
        });

        mCoinType = getIntent().getStringExtra("coinType");
        mAddress = getIntent().getStringExtra("address");

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.viewpager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    btnMod.setVisibility(View.VISIBLE);
                    if (mCoinType.equals(Constants.KS_COINTYPE_ICX)) {
                        if (ICONexApp.ICXContacts.size() == 0)
                            btnMod.setVisibility(View.INVISIBLE);
                    } else {
                        if (ICONexApp.ETHContacts.size() == 0)
                            btnMod.setVisibility(View.INVISIBLE);
                    }

                    btnMod.setSelected(false);
                    btnMod.setText(getString(R.string.edit));
                    viewPagerAdapter.setEditable(false);
                } else
                    btnMod.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        viewPagerAdapter = new ContactsViewPagerAdapter(this, getSupportFragmentManager(), mAddress, mCoinType, false);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);

        btnMod.setSelected(false);
        btnMod.setText(getString(R.string.edit));
        btnMod.setVisibility(View.VISIBLE);

        if (mCoinType.equals(Constants.KS_COINTYPE_ICX)) {
            if (ICONexApp.ICXContacts.size() == 0)
                btnMod.setVisibility(View.INVISIBLE);
        } else {
            if (ICONexApp.ETHContacts.size() == 0)
                btnMod.setVisibility(View.INVISIBLE);
        }

        viewPagerAdapter.setEditable(false);
    }

    @Override
    public void onClick(String address) {
        setResult(CODE_RESULT, new Intent().putExtra("address", address));
        finish();
    }

    public void setBtnModVisibility(int visible) {
        btnMod.setVisibility(visible);
    }
}
