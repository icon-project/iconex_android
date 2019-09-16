package foundation.icon.iconex.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.tabs.TabLayout;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.prep.vote.PRepVoteFragment;
import foundation.icon.iconex.view.ui.prep.vote.VotePRepListFragment;
import foundation.icon.iconex.view.ui.prep.vote.VoteViewModel;
import foundation.icon.iconex.wallet.Wallet;

public class PRepVoteActivity extends AppCompatActivity {
    private static final String TAG = PRepVoteActivity.class.getSimpleName();

    private VoteViewModel vm;
    private Wallet wallet;

    private TabLayout tabLayout;
    private PRepVoteFragment voteFragment;
    private VotePRepListFragment prepsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_vote);

        if (getIntent() != null)
            wallet = (Wallet) getIntent().getSerializableExtra("wallet");

        initData();
        initView();
    }

    private void initData() {
        vm = ViewModelProviders.of(this).get(VoteViewModel.class);
        vm.setWallet(wallet);

        voteFragment = PRepVoteFragment.newInstance();
        prepsFragment = VotePRepListFragment.newInstance();
    }

    private void initView() {
        ((TextView) findViewById(R.id.txt_title)).setText(wallet.getAlias());

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, voteFragment)
                                .commit();
                        break;

                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, prepsFragment)
                                .commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
