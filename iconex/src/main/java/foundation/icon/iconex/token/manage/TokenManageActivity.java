package foundation.icon.iconex.token.manage;

import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.CustomActionBar;
import loopchain.icon.wallet.core.Constants;

public class TokenManageActivity extends AppCompatActivity implements View.OnClickListener, TokenListFragment.OnTokenListClickListener,
        TokenManageFragment.OnTokenManageListener, IrcListFragment.OnIrcListListener {

    private static final String TAG = TokenManageActivity.class.getSimpleName();

    private Wallet mWallet;

    private CustomActionBar appbar;

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
        appbar.setTitle(getString(R.string.tokenManageTitle));
        appbar.setOnClickStartIcon(this);

        appbar.setOnClickEndIcon(this); // btnEdit, R.id.txt_mod

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
            case R.id.btn_close: // not use
            case R.id.btn_start_icon:
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    if (appbar.isTextButtonSelected()) {
                        Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
                        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                            @Override
                            public void onOk() {
                                fragmentManager.popBackStackImmediate();
                                appbar.setTitle(getString(R.string.tokenManageTitle));

//                                btnEdit.setSelected(false);
//                                btnEdit.setText(getString(R.string.edit));
//                                btnEdit.setVisibility(View.INVISIBLE);

                                appbar.setTextButtonSelected(false);
                                appbar.setTextButton(getString(R.string.edit));
                                appbar.setIconEnd(CustomActionBar.IconEnd.none);

                                listFragment.tokenNotifyDataChanged();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.setMessage(getString(R.string.msgTokenCancelMod));
                        dialog.show();
                    } else if (addFragment != null && !addFragment.isEmpty()) {
                        Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
                        dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                            @Override
                            public void onOk() {
                                fragmentManager.popBackStackImmediate();
                                appbar.setTitle(getString(R.string.tokenManageTitle));

                                appbar.setTextButtonSelected(false);
                                appbar.setTextButton(getString(R.string.edit));
                                appbar.setIconEnd(CustomActionBar.IconEnd.none);

                                listFragment.tokenNotifyDataChanged();
                            }

                            @Override
                            public void onCancel() {

                            }
                        });
                        dialog.setMessage(getString(R.string.msgTokenCancelAdd));
                        dialog.show();
                    } else {
                        fragmentManager.popBackStackImmediate();
                        appbar.setTitle(getString(R.string.tokenManageTitle));

                        appbar.setTextButtonSelected(false);
                        appbar.setTextButton(getString(R.string.edit));
                        appbar.setIconEnd(CustomActionBar.IconEnd.none);

                        listFragment.tokenNotifyDataChanged();
                    }
                } else
                    finish();
                break;

            case R.id.txt_mod:
                if (!appbar.isTextButtonSelected()) {
                    appbar.setTextButtonSelected(true);
                    appbar.setTextButton(getString(R.string.complete));

                    modFragment.setEditable();
                } else {
                    modFragment.onEditDone();
                }
                break;
        }
    }


    @Override
    public void onTokenClick(WalletEntry entry) {

        appbar.setIconEnd(CustomActionBar.IconEnd.text);
        appbar.setTextButtonSelected(false);
        appbar.setTextButton(getString(R.string.edit));

        appbar.setTitle(entry.getUserName());

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
        appbar.setTitle(getString(R.string.tokenManageTitle));

        appbar.setTextButtonSelected(false);
        appbar.setTextButton(getString(R.string.edit));
        appbar.setIconEnd(CustomActionBar.IconEnd.none);

        listFragment.tokenNotifyDataChanged();
    }

    @Override
    public void onDone(String name) {
        appbar.setTitle(name);
        appbar.setTextButtonSelected(false);
        appbar.setTextButton(getString(R.string.edit));
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
        appbar.setTitle(getString(R.string.tokenManageTitle));

        appbar.setTextButtonSelected(false);
        appbar.setTextButton(getString(R.string.edit));
        appbar.setIconEnd(CustomActionBar.IconEnd.none);

        listFragment.tokenNotifyDataChanged();
    }

    @Override
    public void onBackPressed() {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            if (appbar.isTextButtonSelected()) {
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        fragmentManager.popBackStackImmediate();
                        appbar.setTitle(getString(R.string.tokenManageTitle));

                        appbar.setSelected(false);
                        appbar.setTextButton(getString(R.string.edit));
                        appbar.setIconEnd(CustomActionBar.IconEnd.none);

                        listFragment.tokenNotifyDataChanged();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.setMessage(getString(R.string.msgTokenCancelMod));
                dialog.show();
            } else if (addFragment != null && !addFragment.isEmpty()) {
                Basic2ButtonDialog dialog = new Basic2ButtonDialog(this);
                dialog.setOnDialogListener(new Basic2ButtonDialog.OnDialogListener() {
                    @Override
                    public void onOk() {
                        fragmentManager.popBackStackImmediate();
                        appbar.setTitle(getString(R.string.tokenManageTitle));

                        appbar.setTextButtonSelected(false);
                        appbar.setTextButton(getString(R.string.edit));
                        appbar.setIconEnd(CustomActionBar.IconEnd.none);

                        listFragment.tokenNotifyDataChanged();
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                dialog.setMessage(getString(R.string.msgTokenCancelAdd));
                dialog.show();
            } else {
                fragmentManager.popBackStackImmediate();
                appbar.setTitle(getString(R.string.tokenManageTitle));

                appbar.setTextButtonSelected(false);
                appbar.setTextButton(getString(R.string.edit));
                appbar.setIconEnd(CustomActionBar.IconEnd.none);

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
