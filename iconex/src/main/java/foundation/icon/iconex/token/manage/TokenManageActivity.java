package foundation.icon.iconex.token.manage;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import loopchain.icon.wallet.core.Constants;

public class TokenManageActivity extends AppCompatActivity implements View.OnClickListener, TokenListFragment.OnTokenListClickListener,
        TokenManageFragment.OnTokenManageListener, IrcListFragment.OnIrcListListener {

    private static final String TAG = TokenManageActivity.class.getSimpleName();

    private Wallet mWallet;

    private ViewGroup appbar;
    private TextView txtTitle;
    private Button btnBack;
    private TextView btnEdit;

    private final String TAG_ADD = "TAG_ADD";
    private final String TAG_MOD = "TAG_MOD";
    private final String TAG_IRC = "TAG_IRC";

    private FragmentManager fragmentManager;

    private TokenListFragment listFragment;
    private TokenManageFragment addFragment;
    private TokenManageFragment modFragment;
    private IrcListFragment ircFragment;

    private TOKEN_TYPE tokenType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_manage);

        if (getIntent().getExtras() != null)
            tokenType = (TOKEN_TYPE) getIntent().getExtras().get("type");

        mWallet = (Wallet) getIntent().getSerializableExtra("walletInfo");

        appbar = findViewById(R.id.appbar);
        txtTitle = findViewById(R.id.txt_title);
        txtTitle.setText(getString(R.string.tokenManageTitle));
        btnBack = findViewById(R.id.btn_close);
        btnBack.setBackgroundResource(R.drawable.ic_appbar_back);
        btnBack.setOnClickListener(this);
        btnEdit = findViewById(R.id.txt_mod);
        btnEdit.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();
        listFragment = TokenListFragment.newInstance(mWallet);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.container, listFragment);
        transaction.commit();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    if (btnEdit.isSelected()) {
                        Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
                        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                            @Override
                            public void onOk() {
                                fragmentManager.popBackStackImmediate();
                                txtTitle.setText(getString(R.string.tokenManageTitle));

                                btnEdit.setSelected(false);
                                btnEdit.setText(getString(R.string.edit));
                                btnEdit.setVisibility(View.INVISIBLE);

                                listFragment.tokenNotifyDataChanged();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.setMessage(getString(R.string.msgTokenCancelMod));
                        dialog.show();
                    } else {
                        fragmentManager.popBackStackImmediate();
                        txtTitle.setText(getString(R.string.tokenManageTitle));

                        btnEdit.setSelected(false);
                        btnEdit.setText(getString(R.string.edit));
                        btnEdit.setVisibility(View.INVISIBLE);

                        listFragment.tokenNotifyDataChanged();
                    }
                } else
                    finish();
                break;

            case R.id.txt_mod:
                if (!btnEdit.isSelected()) {
                    btnEdit.setSelected(true);
                    btnEdit.setText(getString(R.string.complete));

                    modFragment.setEditable();
                } else {
                    modFragment.onEditDone();
                }
                break;
        }
    }


    @Override
    public void onTokenClick(WalletEntry entry) {

        btnEdit.setVisibility(View.VISIBLE);
        btnEdit.setSelected(false);
        btnEdit.setText(getString(R.string.edit));
        txtTitle.setText(entry.getUserName());

        TOKEN_TYPE tokenType;
        if (mWallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
            tokenType = TOKEN_TYPE.IRC;
        else
            tokenType = TOKEN_TYPE.ERC;

        modFragment = TokenManageFragment.newInstance(mWallet.getAddress(), MyConstants.MODE_TOKEN.MOD, tokenType, entry);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(TAG_MOD);
        transaction.add(R.id.container, modFragment);
        transaction.commit();
    }

    @Override
    public void onClose() {
        fragmentManager.popBackStackImmediate();
        txtTitle.setText(getString(R.string.tokenManageTitle));

        btnEdit.setSelected(false);
        btnEdit.setText(getString(R.string.edit));
        btnEdit.setVisibility(View.INVISIBLE);

        listFragment.tokenNotifyDataChanged();
    }

    @Override
    public void onDone(String name) {
        txtTitle.setText(name);
        btnEdit.setSelected(false);
        btnEdit.setText(getString(R.string.edit));
    }

    @Override
    public void onTokenAdd() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (tokenType == TOKEN_TYPE.IRC) {
            ircFragment = IrcListFragment.newInstance(mWallet.getAddress());
            transaction.add(R.id.container, ircFragment);
            transaction.addToBackStack(TAG_IRC);
        } else {
            addFragment = TokenManageFragment.newInstance(mWallet.getAddress(), MyConstants.MODE_TOKEN.ADD,
                    TOKEN_TYPE.ERC, null);
            transaction.add(R.id.container, addFragment);
            transaction.addToBackStack(TAG_ADD);
        }

        transaction.commit();
    }

    @Override
    public void enterInfo() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStackImmediate();
        addFragment = TokenManageFragment.newInstance(mWallet.getAddress(), MyConstants.MODE_TOKEN.ADD,
                TOKEN_TYPE.IRC, null);
        transaction.add(R.id.container, addFragment);
        transaction.addToBackStack(TAG_ADD);

        transaction.commit();
    }

    @Override
    public void onListClose() {
        fragmentManager.popBackStackImmediate();
        txtTitle.setText(getString(R.string.tokenManageTitle));

        btnEdit.setSelected(false);
        btnEdit.setText(getString(R.string.edit));
        btnEdit.setVisibility(View.INVISIBLE);

        listFragment.tokenNotifyDataChanged();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            if (btnEdit.isSelected()) {
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        fragmentManager.popBackStackImmediate();
                        txtTitle.setText(getString(R.string.tokenManageTitle));

                        btnEdit.setSelected(false);
                        btnEdit.setText(getString(R.string.edit));
                        btnEdit.setVisibility(View.INVISIBLE);

                        listFragment.tokenNotifyDataChanged();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.setMessage(getString(R.string.msgTokenCancelMod));
                dialog.show();
            } else {
                fragmentManager.popBackStackImmediate();
                txtTitle.setText(getString(R.string.tokenManageTitle));

                btnEdit.setSelected(false);
                btnEdit.setText(getString(R.string.edit));
                btnEdit.setVisibility(View.INVISIBLE);

                listFragment.tokenNotifyDataChanged();
            }
        } else
            super.onBackPressed();
    }

    public enum TOKEN_TYPE {
        IRC,
        ERC
    }

}
