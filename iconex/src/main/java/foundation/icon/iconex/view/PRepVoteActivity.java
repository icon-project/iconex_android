package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.tabs.TabLayout;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.prep.vote.PRepVoteFragment;
import foundation.icon.iconex.view.ui.prep.vote.VotePRepListFragment;
import foundation.icon.iconex.view.ui.prep.vote.VoteViewModel;
import foundation.icon.iconex.wallet.Wallet;

public class PRepVoteActivity extends AppCompatActivity implements PRepVoteFragment.OnVoteListener,
        VotePRepListFragment.OnVotePRepListListener {
    private static final String TAG = PRepVoteActivity.class.getSimpleName();

    private VoteViewModel vm;
    private Wallet wallet;

    private TabLayout tabLayout;
    private ImageButton btnSearch;
    private ViewGroup layoutButton;
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

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(PRepVoteActivity.this, PRepSearchActivity.class)
                                .putExtra("preps", (Serializable) vm.getPreps())
                                .putExtra("delegations", (Serializable) vm.getDelegations()),
                        1000);
            }
        });

        layoutButton = findViewById(R.id.layout_button);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, voteFragment)
                                .commit();
                        btnSearch.setVisibility(View.GONE);
                        layoutButton.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.container, prepsFragment)
                                .commit();
                        btnSearch.setVisibility(View.VISIBLE);
                        layoutButton.setVisibility(View.GONE);
                        break;
                }

                ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(tab.getPosition());
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTextAppearance(PRepVoteActivity.this,
                                R.style.TabTextAppearanceS);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(tab.getPosition());
                int tabChildsCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildsCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTextAppearance(PRepVoteActivity.this,
                                R.style.TabTextAppearanceN);
                    }
                }
            }

            @Override

            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        tabLayout.addTab(tabLayout.newTab().setText(R.string.myVotes), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.preps));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1000) {

        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}
