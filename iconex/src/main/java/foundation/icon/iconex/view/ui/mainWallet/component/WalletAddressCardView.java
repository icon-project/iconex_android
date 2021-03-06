package foundation.icon.iconex.view.ui.mainWallet.component;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.WalletAddressQrcodeView;


public class WalletAddressCardView extends FrameLayout {

    private WalletAddressQrcodeView walletAddressQrcodeView;

    private ImageButton btnClose;

    public interface OnDismissListener {
        void onDismissStart();
        void onDismissFinish();
    }
    private OnDismissListener listener;

    public void setOnDismissListener(OnDismissListener listener) {
        this.listener = listener;
    }

    public void show(Wallet wallet, WalletEntry entry) {
        // bind data
        walletAddressQrcodeView.bind(wallet.getAlias(), wallet, entry);

        // animation start
        setVisibility(VISIBLE);
        Animator aniShow = AnimatorInflater.loadAnimator(getContext(), R.animator.wallet_address_card_flip_show);
        aniShow.setTarget(this);
        aniShow.start();
    }

    public WalletAddressCardView(@NonNull Context context) {
        super(context);
        initView();
    }

    public WalletAddressCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public WalletAddressCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public WalletAddressCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    public boolean isShow() {
        return getVisibility() == VISIBLE;
    }

    private void initView() {
        // inflate ui
        LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_address_card_view, this, true);

        // load ui
        walletAddressQrcodeView = findViewById(R.id.wallet_address_qrcode_view);
        btnClose = findViewById(R.id.btn_close);

        // init ui
        float scale = getContext().getResources().getDisplayMetrics().density;
        setCameraDistance(scale * 8000);

        btnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClose.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.floating_button_click));
                Animator aniDismiss = AnimatorInflater.loadAnimator(getContext(), R.animator.wallet_address_card_flip_disappear);
                aniDismiss.setTarget(WalletAddressCardView.this);
                aniDismiss.start();
                aniDismiss.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        setVisibility(GONE);
                        if (listener != null)
                            listener.onDismissFinish();
                    }
                });
                if (listener != null)
                    listener.onDismissStart();
            }
        });
    }
}
