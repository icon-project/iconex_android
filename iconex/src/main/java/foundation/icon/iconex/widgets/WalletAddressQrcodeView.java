package foundation.icon.iconex.widgets;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletAddressCardView;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.Constants;

public class WalletAddressQrcodeView extends FrameLayout {
    private TextView txtName;
    private ImageView imgQrCode;
    private TextView txtAddress;
    private Button btnCopyAddress;

    private ViewGroup layoutRequestSend;
    private TTextInputLayout editSendAmount;
    private Button btnRequestSend;
    private TextView txtTransSendAmount;

    private ProgressBar qr_loading;

    private String symbol;

    public interface OnDismissListener {
        void onDismiss();
    }
    private WalletAddressCardView.OnDismissListener listener;

    public void setOnDismissListener(WalletAddressCardView.OnDismissListener listener) {
        this.listener = listener;
    }

    public void bind(String title, Wallet wallet, WalletEntry entry) {
        // bind data
        txtName.setText(title);
        symbol = wallet.getWalletEntries().get(0).getSymbol();
        editSendAmount.setText("");

        String address = (wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX) ? "" : "0x") + wallet.getAddress();
        txtAddress.setText(address);
        setQrCode(address, imgQrCode);
        // set only icx
        boolean isICX = wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)
                && entry.getType().equals(MyConstants.TYPE_COIN);
        layoutRequestSend.setVisibility(isICX ? VISIBLE : INVISIBLE);
    }

    public WalletAddressQrcodeView(@NonNull Context context) {
        super(context);
        initView();
    }

    public WalletAddressQrcodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public WalletAddressQrcodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public WalletAddressQrcodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        // inflate ui
        LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_address_qrcode_view, this, true);

        // load ui
        txtName = findViewById(R.id.txt_name);
        imgQrCode = findViewById(R.id.img_qrcode);
        txtAddress = findViewById(R.id.txt_address);
        btnCopyAddress = findViewById(R.id.btn_copy_address);

        layoutRequestSend = findViewById(R.id.layout_request_send);
        editSendAmount = findViewById(R.id.edit_send_amount);
        btnRequestSend = findViewById(R.id.btn_request_send);
        txtTransSendAmount = findViewById(R.id.txt_trans_send_amount);

        qr_loading = findViewById(R.id.qr_loading);

        editSendAmount.getEditView().setTextSize(10);
        editSendAmount.syncTopHeight(findViewById(R.id.con_sync));
        editSendAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        btnRequestSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String address = txtAddress.getText().toString();
                String amount = editSendAmount.getText();
                if (amount.equals("")) {
                    setQrCode(address, imgQrCode);
                } else {
                    JSONObject requestData = new JSONObject();
                    try {
                        requestData.put("address", address);
                        String balanceHexString = ConvertUtil.valueToHexString(amount,18);
                        requestData.put("amount",balanceHexString);
                        String jsonData = requestData.toString();
                        String base64Encoded = Base64.encodeToString(jsonData.getBytes(), Base64.NO_WRAP);
                        String connectString = "iconex://pay?data=" + base64Encoded;
                        setQrCode(connectString, imgQrCode);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        setQrCode(address, imgQrCode);
                    }
                }
            }
        });
        editSendAmount.setOnTextChangedListener(new TTextInputLayout.OnTextChanged() {
            @Override
            public void onChanged(@NotNull CharSequence s) {
                if (s.length() > 0) {
                    if (s.toString().startsWith(".")) {
                        editSendAmount.setText("");
                    } else {
                        if (s.toString().indexOf(".") < 0) {
                            if (s.length() > 10) {
                                editSendAmount.setText(s.subSequence(0, 10).toString());
                                editSendAmount.setSelection(10);
                            }
                        } else {
                            String[] values = s.toString().split("\\.");

                            if (values.length == 2) {
                                String decimal = values[0];
                                String below = values[1];

                                if (decimal.length() > 10) {
                                    decimal = decimal.substring(0, 10);
                                    editSendAmount.setText(decimal + "." + below);
                                    editSendAmount.setSelection(editSendAmount.getText().toString().length());
                                } else if (below.length() > 8) {
                                    below = below.substring(0, 8);
                                    editSendAmount.setText(decimal + "." + below);
                                    editSendAmount.setSelection(editSendAmount.getText().toString().length());
                                }
                            }
                        }

                        String amount = editSendAmount.getText();
                        String strPrice = ICONexApp.EXCHANGE_TABLE.get(symbol.toLowerCase() + "usd");
                        if (strPrice != null) {
                            Double transUSD = Double.parseDouble(amount)
                                    * Double.parseDouble(strPrice);
                            String strTransUSD = String.format("%,.2f", transUSD);

                            txtTransSendAmount.setText(String.format("$ %s", strTransUSD));
                        }
                    }
                } else {
                    txtTransSendAmount.setText("$ 0.00");
                }
            }
        });

        btnCopyAddress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("address", txtAddress.getText().toString());
                clipboard.setPrimaryClip(data);

                CustomToast.makeText(getContext(), getContext().getString(R.string.msgCopyAddress), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setQrCode(String string, ImageView target){
        qr_loading.setVisibility(VISIBLE);
        final Bitmap[] qrCode = {null};
        target.setImageBitmap(null);
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                int size = (int) getResources().getDimension(R.dimen.QRCodeSize);
                BitMatrix matrix = qrCodeWriter.encode(string, BarcodeFormat.QR_CODE, size, size);
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
                        qr_loading.setVisibility(GONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }
}
