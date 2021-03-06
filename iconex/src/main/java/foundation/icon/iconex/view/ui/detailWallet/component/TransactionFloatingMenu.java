package foundation.icon.iconex.view.ui.detailWallet.component;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.view.DepositActivity;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.view.EtherTransferActivity;
import foundation.icon.iconex.view.IconTransferActivity;
import loopchain.icon.wallet.core.Constants;

public class TransactionFloatingMenu extends FrameLayout implements View.OnClickListener {

    // UI
    private ViewGroup menuModal;
    private ImageButton btnFloating;
    private ViewGroup menu;
    private ViewGroup btnDeposit;
    private ViewGroup btnSend;
    private ViewGroup btnConvert;
    
    private Wallet wallet;
    private WalletEntry entry;

    public TransactionFloatingMenu(@NonNull Context context) {
        super(context);
        viewInit();
    }

    public TransactionFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewInit();
    }

    public TransactionFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewInit();
    }

    private void viewInit() {
        setClickable(false);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_floating_menu, this, true);

        menuModal = findViewById(R.id.menu_modal);
        btnFloating = findViewById(R.id.btn_floating);
        menu = findViewById(R.id.menu);
        btnDeposit = findViewById(R.id.btn_deposit);
        btnSend = findViewById(R.id.btn_send);
        btnConvert = findViewById(R.id.btn_convert);

        menuModal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });

        btnFloating.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });

        btnDeposit.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnConvert.setOnClickListener(this);
    }

    private void toggleMenu() {
        btnFloating.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.floating_button_click));
        boolean isShow = menu.getVisibility() == VISIBLE;
        if (isShow) {
            menuModal.setVisibility(GONE);
            menu.setVisibility(GONE);
            btnFloating.setImageResource(R.drawable.ic_detail_menu);
        } else {
            menuModal.setVisibility(VISIBLE);
            menu.setVisibility(VISIBLE);
            btnFloating.setImageResource(R.drawable.ic_close_menu);
        }
    }
    
    public void setWallet(Wallet wallet, WalletEntry entry) {
        this.wallet = wallet;
        this.entry = entry;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_deposit: {
                getContext().startActivity(new Intent(getContext(), DepositActivity.class)
                .putExtra(DepositActivity.PARAM_WALLET, ((Serializable) wallet))
                .putExtra(DepositActivity.PARAM_ENTRY, (Serializable)entry));
            } break;
            case R.id.btn_send: { // Transfer
                new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                    @Override
                    public void onPass(byte[] bytePrivateKey) {
                        if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                            getContext()
                                    .startActivity(new Intent(getContext(), IconTransferActivity.class)
                                    .putExtra("walletInfo", (Serializable) wallet)
                                    .putExtra("walletEntry", (Serializable) entry)
                                    .putExtra("privateKey", Hex.toHexString(bytePrivateKey)));
                        } else {
                            getContext()
                                    .startActivity(new Intent(getContext(), EtherTransferActivity.class)
                                    .putExtra("walletInfo", (Serializable) wallet)
                                    .putExtra("walletEntry", (Serializable) entry)
                                    .putExtra("privateKey", Hex.toHexString(bytePrivateKey)));
                        }
                    }
                }).show();
            } break;
        }
        toggleMenu();
    }
}
