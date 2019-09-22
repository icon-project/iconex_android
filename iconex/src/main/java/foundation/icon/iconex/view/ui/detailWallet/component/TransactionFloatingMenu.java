package foundation.icon.iconex.view.ui.detailWallet.component;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.transfer.EtherTransferActivity;
import foundation.icon.iconex.wallet.transfer.ICONTransferActivity;
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
                Toast.makeText(getContext(), "not implement btn_deposit", Toast.LENGTH_SHORT).show();
            } break;
            case R.id.btn_send: { // Transfer
                if (entry.getBalance().equals(MyConstants.NO_BALANCE)
                        || new BigInteger(entry.getBalance()).compareTo(BigInteger.ZERO) == 0) {
                    MessageDialog messageDialog = new MessageDialog(getContext());
                    messageDialog.setTitleText(getContext().getString(R.string.errCantWithdraw));
                    messageDialog.show();
                    return;
                }

                if (entry.getType().equals(MyConstants.TYPE_COIN)) {
                    if (new BigInteger(entry.getBalance()).equals(BigInteger.ZERO)) {
                        MessageDialog messageDialog = new MessageDialog(getContext());
                        messageDialog.setTitleText(getContext().getString(R.string.errCantWithdraw));
                        messageDialog.show();
                        return;
                    }
                } else {
                    if (new BigInteger(wallet.getWalletEntries().get(0).getBalance()).equals(BigInteger.ZERO)) {
                        MessageDialog messageDialog = new MessageDialog(getContext());
                        if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX))
                            messageDialog.setTitleText(getContext().getString(R.string.errIcxOwnNotEnough));
                        else
                            messageDialog.setTitleText(getContext().getString(R.string.errEthOwnNotEnough));
                        messageDialog.show();
                        return;
                    }
                }

                new WalletPasswordDialog(getContext(), wallet, new WalletPasswordDialog.OnPassListener() {
                    @Override
                    public void onPass(byte[] bytePrivateKey) {
                        if (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                            getContext()
                                    .startActivity(new Intent(getContext(), ICONTransferActivity.class)
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
            case R.id.btn_convert: {
                Toast.makeText(getContext(), "not implement btn_convert", Toast.LENGTH_SHORT).show();
            } break;
        }
        toggleMenu();
    }
}
