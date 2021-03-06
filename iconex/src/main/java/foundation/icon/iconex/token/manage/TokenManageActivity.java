package foundation.icon.iconex.token.manage;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.CustomActionBar;
import kotlin.jvm.functions.Function1;
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
                onBackPressed();
                break;

            case R.id.txt_mod: // not use
            case R.id.btn_text:
                if (!appbar.isTextButtonSelected()) {
                    appbar.setTextButtonSelected(true);
                    appbar.setTextButton(getString(R.string.delete));

                    modFragment.setEditable();
                } else {
                    modFragment.deleteToken();
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
        int resString = fragmentManager.getBackStackEntryCount() > 0 ?
                R.string.addToken : R.string.tokenManageTitle;
        appbar.setTitle(getString(resString));
        appbar.setIconEnd(CustomActionBar.IconEnd.none);
        listFragment.tokenNotifyDataChanged();
    }

    @Override
    public void onDoneEditToken(String name) {
        appbar.setTitle(name);
        appbar.setTextButtonSelected(false);
        appbar.setTextButton(getString(R.string.edit));
    }

    @Override
    public void onDoneAddToken() {
        fragmentManager.popBackStackImmediate();
        if (fragmentManager.getBackStackEntryCount() > 0)
            fragmentManager.popBackStackImmediate();
        appbar.setTitle(getString(R.string.tokenManageTitle));
        appbar.setIconEnd(CustomActionBar.IconEnd.none);
        listFragment.tokenNotifyDataChanged();
    }

    @Override
    public void onTokenAdd() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (tokenType == TOKEN_TYPE.IRC) {
            ircFragment = IrcListFragment.newInstance(mWallet.getAddress());
            transaction.add(R.id.container, ircFragment);
            transaction.addToBackStack(TAG_IRC);
            appbar.setTitle(getString(R.string.addToken));
        } else {
            addFragment = TokenManageFragment.newInstance(mWallet.getAddress(), MyConstants.MODE_TOKEN.ADD,
                    TOKEN_TYPE.ERC, null);
            transaction.add(R.id.container, addFragment);
            transaction.addToBackStack(TAG_ADD);
            appbar.setTitle(getString(R.string.enterTokenInfo));
        }

        transaction.commit();
    }

    @Override
    public void enterInfo() {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        addFragment = TokenManageFragment.newInstance(mWallet.getAddress(), MyConstants.MODE_TOKEN.ADD,
                TOKEN_TYPE.IRC, null);
        transaction.add(R.id.container, addFragment);
        transaction.addToBackStack(TAG_ADD);
        appbar.setTitle(getString(R.string.enterTokenInfo));
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
            MessageDialog messageDialog = new MessageDialog(this);
            messageDialog.setSingleButton(false);
            messageDialog.setOnConfirmClick(new Function1<View, Boolean>() {
                @Override
                public Boolean invoke(View view) {
                    onClose();
                    return true;
                }
            });

            if (modFragment != null && modFragment.isEdited()) {
                messageDialog.setMessage(getString(R.string.msgTokenCancelMod));
                messageDialog.show();
            } else if (addFragment != null && !addFragment.isEmpty()) {
                if (tokenType == TOKEN_TYPE.IRC && fragmentManager.getBackStackEntryCount() == 1) {
                    onClose();
                    return;
                }
                messageDialog.setMessage(getString(R.string.msgTokenCancelAdd));
                messageDialog.show();
            } else {
                onClose();
            }
        } else finish();
    }

    public enum TOKEN_TYPE {
        IRC,
        ERC
    }

}
