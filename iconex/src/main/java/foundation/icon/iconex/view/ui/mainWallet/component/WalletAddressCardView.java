package foundation.icon.iconex.view.ui.mainWallet.component;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ScreenUnit;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.widgets.CustomToast;
import foundation.icon.iconex.widgets.TTextInputLayout;
import foundation.icon.iconex.widgets.WalletAddressQrcodeView;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.Constants;


public class WalletAddressCardView extends FrameLayout {

    private WalletAddressQrcodeView walletAddressQrcodeView;

    private ImageButton btnClose;

    public interface OnDismissListener {
        void onDismiss();
    }
    private OnDismissListener listener;

    public void setOnDismissListener(OnDismissListener listener) {
        this.listener = listener;
    }

    public void show(Wallet wallet) {
        // bind data
        walletAddressQrcodeView.bind(wallet.getAlias(), wallet);

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
                    }
                });
                if (listener != null)
                    listener.onDismiss();
            }
        });
    }
}
