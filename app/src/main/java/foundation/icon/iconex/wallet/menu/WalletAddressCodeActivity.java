package foundation.icon.iconex.wallet.menu;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.R;

public class WalletAddressCodeActivity extends AppCompatActivity {

    private static final String TAG = WalletAddressCodeActivity.class.getSimpleName();

    private String title;
    private String address;

    private ImageView imgQRCode;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet_address_code);

        if (getIntent() != null) {
            title = getIntent().getStringExtra("title");
            address = getIntent().getStringExtra("address");
            if (!address.startsWith("hx"))
                address = MyConstants.PREFIX_ETH + address;
        }

        ((TextView) findViewById(R.id.txt_title)).setText(title);
        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        progress = findViewById(R.id.progress);

        ((TextView) findViewById(R.id.txt_address)).setText(address);
        imgQRCode = findViewById(R.id.img_qrcode);
        findViewById(R.id.btn_copy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("address", ((TextView) findViewById(R.id.txt_address)).getText().toString());
                clipboard.setPrimaryClip(data);

                Toast.makeText(WalletAddressCodeActivity.this, getString(R.string.msgCopyAddress), Toast.LENGTH_SHORT).show();
            }
        });

        GenerateQRCode generateQRCode = new GenerateQRCode();
        generateQRCode.execute(address);
    }

    private Bitmap generateQRcode() {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Bitmap qrCode = null;
        int size = (int) getResources().getDimension(R.dimen.QRCodeSize);
        try {
            qrCode = toBitmap(qrCodeWriter.encode(address, BarcodeFormat.QR_CODE, size, size));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return qrCode;
    }

    private Bitmap toBitmap(BitMatrix matrix) {
        int height = matrix.getHeight();
        int width = matrix.getWidth();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }

    class GenerateQRCode extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            return generateQRcode();
        }

        @Override
        protected void onPostExecute(Bitmap qrCode) {
            progress.setVisibility(View.GONE);
            imgQRCode.setImageBitmap(qrCode);
        }
    }
}
