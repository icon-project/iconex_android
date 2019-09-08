package foundation.icon.iconex.dev_mainWallet.component;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import foundation.icon.iconex.R;

public class WalletManageMenu extends BottomSheetDialog implements View.OnClickListener {

    private ImageButton btnClose;
    private Button btnRename;
    private Button btnManageToken;
    private Button btnBackupWallet;
    private Button btnChangeWalletPassword;
    private Button btnRemoveWallet;

    private String mTxtName;

    public enum MenuItem{
        Rename,
        ManageToken,
        BackupWallet,
        ChangeWalletPassword,
        RemoveWallet
    }

    public interface OnClickMenuItemListener { void onClickMenuItem(MenuItem menuItem); }
    private OnClickMenuItemListener mOnClickMenuItemListener = null;

    public WalletManageMenu(@NonNull Context context, String txtName, OnClickMenuItemListener listener) {
        super(context, R.style.MyBottomSheetDialog);
        mTxtName = txtName;
        mOnClickMenuItemListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_wallet_manage_menu);

        btnClose = findViewById(R.id.btnClose);
        btnRename = findViewById(R.id.btnRename);
        btnManageToken = findViewById(R.id.btnManageToken);
        btnBackupWallet = findViewById(R.id.btnBackupWallet);
        btnChangeWalletPassword = findViewById(R.id.btnChangeWalletPassword);
        btnRemoveWallet = findViewById(R.id.btnRemoveWallet);

        btnClose.setOnClickListener(this);
        btnRename.setOnClickListener(this);
        btnManageToken.setOnClickListener(this);
        btnBackupWallet.setOnClickListener(this);
        btnChangeWalletPassword.setOnClickListener(this);
        btnRemoveWallet.setOnClickListener(this);

        btnRename.setText(mTxtName);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                break;
            case R.id.btnRename:
                mOnClickMenuItemListener.onClickMenuItem(MenuItem.Rename);
                break;
            case R.id.btnManageToken:
                mOnClickMenuItemListener.onClickMenuItem(MenuItem.ManageToken);
                break;
            case R.id.btnBackupWallet:
                mOnClickMenuItemListener.onClickMenuItem(MenuItem.BackupWallet);
                break;
            case R.id.btnChangeWalletPassword:
                mOnClickMenuItemListener.onClickMenuItem(MenuItem.ChangeWalletPassword);
                break;
            case R.id.btnRemoveWallet:
                mOnClickMenuItemListener.onClickMenuItem(MenuItem.RemoveWallet);
                break;
        }
        this.dismiss();
    }
}
