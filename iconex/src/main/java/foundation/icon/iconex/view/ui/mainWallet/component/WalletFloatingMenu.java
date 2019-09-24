package foundation.icon.iconex.view.ui.mainWallet.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.mainWallet.MainWalletFragment;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.widgets.ToolTip;

public class WalletFloatingMenu extends FrameLayout {

    private ImageButton btnAction;
    private ViewGroup bubbleMenuModal;
    private ViewGroup bubbleMenu;
    private ViewGroup iconVotingMenu;
    private Button btnPReps;
    private Button btnStake;
    private Button btnVote;
    private Button btnIScore;

    public WalletFloatingMenu(@NonNull Context context) {
        super(context);
        viewInit();
    }

    public WalletFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewInit();
    }

    public WalletFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewInit();
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
        btnAction.setVisibility(enable ? VISIBLE : GONE);
    }

    private void setBubbleMenuShow(boolean isShow) {
        if (!isShow) {
            bubbleMenuModal.setVisibility(View.GONE);
            bubbleMenu.setVisibility(View.GONE);
            btnAction.setImageResource(R.drawable.ic_vote_menu);
        } else {
            bubbleMenuModal.setVisibility(View.VISIBLE);
            bubbleMenu.setVisibility(View.VISIBLE);
            btnAction.setImageResource(R.drawable.ic_close_menu);
            iconVotingMenu.setVisibility(GONE);
        }
    }
    
    public void setOnCilckMenuItemListener (View.OnClickListener listener) {
        View.OnClickListener onClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v);
                setBubbleMenuShow(false);
            }
        };
        btnPReps.setOnClickListener(onClickListener);
        btnStake.setOnClickListener(onClickListener);
        btnVote.setOnClickListener(onClickListener);
        btnIScore.setOnClickListener(onClickListener);
    }
}
