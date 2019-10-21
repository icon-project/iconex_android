package foundation.icon.iconex.view.ui.mainWallet.component;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.spongycastle.util.encoders.Hex;

import java.io.Serializable;
import java.math.BigInteger;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.dialogs.WalletPasswordDialog;
import foundation.icon.iconex.view.PRepIScoreActivity;
import foundation.icon.iconex.view.PRepListActivity;
import foundation.icon.iconex.view.PRepStakeActivity;
import foundation.icon.iconex.view.PRepVoteActivity;
import foundation.icon.iconex.wallet.Wallet;

public class FloatingRRepsMenu extends FrameLayout implements View.OnClickListener {

    private ImageButton btnAction;
    private ViewGroup bubbleMenuModal;
    private ViewGroup bubbleMenu;
    private ViewGroup iconVotingMenu;
    private Button btnPReps;
    private Button btnStake;
    private Button btnVote;
    private Button btnIScore;

    private Wallet wallet;

    public FloatingRRepsMenu(@NonNull Context context) {
        super(context);
        viewInit();
    }

    public FloatingRRepsMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewInit();
    }

    public FloatingRRepsMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewInit();
    }

    public void bindWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    private void viewInit() {
        setClickable(false);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_floating_menu, this, true);

        btnAction = findViewById(R.id.btn_action);
        bubbleMenuModal = findViewById(R.id.bubble_menu_modal);
        bubbleMenu = findViewById(R.id.bubble_menu);
        iconVotingMenu = findViewById(R.id.menu_icon_voting);
        btnPReps = findViewById(R.id.btn_preps);
        btnStake = findViewById(R.id.btn_stake);
        btnVote = findViewById(R.id.btn_vote);
        btnIScore = findViewById(R.id.btn_iscore);


        btnPReps.setOnClickListener(this);
        btnStake.setOnClickListener(this);
        btnVote.setOnClickListener(this);
        btnIScore.setOnClickListener(this);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int visibility = bubbleMenuModal.getVisibility();
                setBubbleMenuShow(visibility != ViewGroup.VISIBLE);
            }
        });

        bubbleMenuModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBubbleMenuShow(false);
            }
        });

        setBubbleMenuShow(false);
        iconVotingMenu.setVisibility(GONE);
    }

    public void showIconVoting() {
        iconVotingMenu.setVisibility(VISIBLE);
        iconVotingMenu.postDelayed(new Runnable() {
            @Override
            public void run() {
                iconVotingMenu.setVisibility(GONE);
            }
        }, 5000);
    }

    public void setEnableFloatingButton(boolean enable) {
        if (enable == btnAction.isEnabled()) return;
        btnAction.setEnabled(enable);
        btnAction.clearAnimation();
        if (enable) {
            Animation aniBtnShow = AnimationUtils.loadAnimation(getContext(), R.anim.floating_button_show);
            btnAction.setVisibility(VISIBLE);
            btnAction.startAnimation(aniBtnShow);
        } else {
            Animation aniBtnDisappear = AnimationUtils.loadAnimation(getContext(), R.anim.floating_button_disappear);
            btnAction.startAnimation(aniBtnDisappear);
            aniBtnDisappear.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (!btnAction.isEnabled())
                        btnAction.setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        }
    }

    private void setBubbleMenuShow(boolean isShow) {
        Animation aniBtnClick = AnimationUtils.loadAnimation(getContext(), R.anim.floating_button_click);
        btnAction.startAnimation(aniBtnClick);

        bubbleMenu.clearAnimation();

        if (!isShow) {
            bubbleMenuModal.setVisibility(View.GONE);
            btnAction.setImageResource(R.drawable.ic_vote_menu);
            Animation aniMenuDisappear = AnimationUtils.loadAnimation(getContext(), R.anim.floating_menu_disappear);
            bubbleMenu.startAnimation(aniMenuDisappear);
            aniMenuDisappear.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    bubbleMenu.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
        } else {
            bubbleMenuModal.setVisibility(View.VISIBLE);
            bubbleMenu.setVisibility(View.VISIBLE);
            Animation aniMenuShow = AnimationUtils.loadAnimation(getContext(), R.anim.floating_menu_show);
            bubbleMenu.startAnimation(aniMenuShow);
            btnAction.setImageResource(R.drawable.ic_close_menu);
            iconVotingMenu.setVisibility(GONE);
        }
    }

    public void bind(Wallet wallet) {
        this.wallet = wallet;
    }

    // ========================= P-Peps Menu, (from main wallet activity)
    private MessageDialog messageDialog;
    private WalletPasswordDialog passwordDialog;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_preps: {
                getContext().startActivity(new Intent(getContext(), PRepListActivity.class));
            }
            break;
            case R.id.btn_stake: {
                passwordDialog = new WalletPasswordDialog(getContext(), wallet,
                        new WalletPasswordDialog.OnPassListener() {
                            @Override
                            public void onPass(byte[] bytePrivateKey) {
                                getContext().startActivity(new Intent(getContext(), PRepStakeActivity.class)
                                        .putExtra("wallet", (Serializable) wallet)
                                        .putExtra("privateKey", Hex.toHexString(bytePrivateKey)));
                            }
                        });

                try {
                    BigInteger balance = new BigInteger(wallet.getWalletEntries().get(0).getBalance());
                    if (balance.compareTo(new BigInteger("5")) < 0) {
                        messageDialog = new MessageDialog(getContext());
                        messageDialog.setTitleText(getContext().getString(R.string.notEnoughForStaking));
                        messageDialog.show();
                    } else {
                        passwordDialog.show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Balance loading", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.btn_vote: {
                passwordDialog = new WalletPasswordDialog(getContext(), wallet,
                        new WalletPasswordDialog.OnPassListener() {
                            @Override
                            public void onPass(byte[] bytePrivateKey) {
                                getContext().startActivity(new Intent(getContext(), PRepVoteActivity.class)
                                        .putExtra("wallet", (Serializable) wallet)
                                        .putExtra("privateKey", Hex.toHexString(bytePrivateKey)));
                            }
                        });
                try {
                    BigInteger voted = wallet.getStaked();
                    if (voted.compareTo(BigInteger.ZERO) == 0) {
                        messageDialog = new MessageDialog(getContext());
                        messageDialog.setTitleText(getContext().getString(R.string.hasNoVotingPower));
                        messageDialog.show();
                    } else {
                        passwordDialog.show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Voting power loading", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            case R.id.btn_iscore: {
                passwordDialog = new WalletPasswordDialog(getContext(), wallet,
                        new WalletPasswordDialog.OnPassListener() {
                            @Override
                            public void onPass(byte[] bytePrivateKey) {
                                getContext().startActivity(new Intent(getContext(), PRepIScoreActivity.class)
                                        .putExtra("wallet", (Serializable) wallet)
                                        .putExtra("privateKey", Hex.toHexString(bytePrivateKey)));
                            }
                        });

                passwordDialog.show();
            }
            break;
        }

        setBubbleMenuShow(false);
    }
}
