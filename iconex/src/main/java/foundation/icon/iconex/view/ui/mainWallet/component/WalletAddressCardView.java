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
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.Constants;

// TODO: implement request amount qrcode
public class WalletAddressCardView extends FrameLayout {

    private TextView txtName;
    private ImageView imgQrCode;
    private TextView txtAddress;
    private Button btnCopyAddress;

    private ViewGroup layoutRequestSend;
    private TTextInputLayout editSendAmount;
    private Button btnRequestSend;
    private TextView txtTransSendAmount;

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
        txtName.setText(wallet.getAlias());
        txtAddress.setText(wallet.getAddress());
        setQrCode(wallet.getAddress(), imgQrCode);

        // set only icx
        boolean isICX = wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX);
        layoutRequestSend.setVisibility(isICX ? VISIBLE : INVISIBLE);

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
        txtName = findViewById(R.id.txt_name);
        imgQrCode = findViewById(R.id.img_qrcode);
        txtAddress = findViewById(R.id.txt_address);
        btnCopyAddress = findViewById(R.id.btn_copy_address);

        layoutRequestSend = findViewById(R.id.layout_request_send);
        editSendAmount = findViewById(R.id.edit_send_amount);
        btnRequestSend = findViewById(R.id.btn_request_send);
        txtTransSendAmount = findViewById(R.id.txt_trans_send_amount);

        btnClose = findViewById(R.id.btn_close);

        // init ui
        float scale = getContext().getResources().getDisplayMetrics().density;
        setCameraDistance(scale * 8000);

        btnCopyAddress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("address", txtAddress.getText().toString());
                clipboard.setPrimaryClip(data);

                CustomToast.makeText(getContext(), getContext().getString(R.string.msgCopyAddress), Toast.LENGTH_SHORT).show();
            }
        });

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

    private void setQrCode(String address, ImageView target){
        final Bitmap[] qrCode = {null};
        target.setImageBitmap(null);
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                int size = (int) getResources().getDimension(R.dimen.QRCodeSize);
                BitMatrix matrix = qrCodeWriter.encode(address, BarcodeFormat.QR_CODE, size, size);
                int height = matrix.getHeight();
                int width = matrix.getWidth();
                qrCode[0] = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        qrCode[0].setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
                    }
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        target.setImageBitmap(qrCode[0]);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }
}
