package foundation.icon.iconex.token.manage;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.control.WalletEntry;
import foundation.icon.iconex.control.WalletInfo;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;

public class TokenManageActivity extends AppCompatActivity implements View.OnClickListener, TokenListFragment.OnTokenListClickListener,
        TokenManageFragment.OnTokenManageListener {

    private static final String TAG = TokenManageActivity.class.getSimpleName();

    private WalletInfo mWalletInfo;

    private ViewGroup appbar;
    private TextView txtTitle;
    private Button btnBack;
    private TextView btnEdit;

    private final String TAG_ADD = "TAG_ADD";
    private final String TAG_MOD = "TAG_MOD";

    private FragmentManager fragmentManager;

    private TokenListFragment listFragment;
    private TokenManageFragment addFragment;
    private TokenManageFragment modFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token_manage);

        mWalletInfo = (WalletInfo) getIntent().getSerializableExtra("walletInfo");

        appbar = findViewById(R.id.appbar);
        txtTitle = findViewById(R.id.txt_title);
        txtTitle.setText(getString(R.string.tokenManageTitle));
        btnBack = findViewById(R.id.btn_close);
        btnBack.setBackgroundResource(R.drawable.ic_appbar_back);
        btnBack.setOnClickListener(this);
        btnEdit = findViewById(R.id.btn_mod);
        btnEdit.setOnClickListener(this);

        fragmentManager = getSupportFragmentManager();
        listFragment = TokenListFragment.newInstance(mWalletInfo);
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

            case R.id.btn_mod:
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

        modFragment = TokenManageFragment.newInstance(mWalletInfo.getAddress(), MyConstants.MODE_TOKEN.MOD, entry);
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
        transaction.addToBackStack(TAG_ADD);
        addFragment = TokenManageFragment.newInstance(mWalletInfo.getAddress(), MyConstants.MODE_TOKEN.ADD, null);
        transaction.add(R.id.container, addFragment);
        transaction.commit();
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

                listFragment.tokenNotifyDataChanged();
            }
        } else
            super.onBackPressed();
    }
}
