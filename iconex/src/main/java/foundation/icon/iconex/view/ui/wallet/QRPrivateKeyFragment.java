package foundation.icon.iconex.view.ui.wallet;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import foundation.icon.iconex.R;

public class QRPrivateKeyFragment extends Fragment {

    private static final String ARG_PRIV_KEY = "ARG_PRIVATE_KEY";

    private String mPrivateKey;

    private TextView txtPrivateKey;
    private ImageView imgQR;
    private ProgressBar progressBar;
    private Button btnCopy;

    public QRPrivateKeyFragment() {
        // Required empty public constructor
    }

    public static QRPrivateKeyFragment newInstance(String privateKey) {
        QRPrivateKeyFragment fragment = new QRPrivateKeyFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRIV_KEY, privateKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPrivateKey = getArguments().getString(ARG_PRIV_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_qrprivate_key, container, false);

        txtPrivateKey = v.findViewById(R.id.txt_private_key);
        txtPrivateKey.setText(mPrivateKey);

        imgQR = v.findViewById(R.id.img_qrcode);
        progressBar = v.findViewById(R.id.progress);

        btnCopy = v.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("privateKey", mPrivateKey);
                clipboard.setPrimaryClip(data);

                Toast.makeText(getActivity(), getString(R.string.msgCopyPrivateKey), Toast.LENGTH_SHORT).show();
            }
        });

        GenerateQRCode generateQRCode = new GenerateQRCode();
        generateQRCode.execute(mPrivateKey);

        return v;
    }

    private Bitmap generateQRcode() {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Bitmap qrCode = null;
        int size = (int) getResources().getDimension(R.dimen.QRCodeSize);
        try {
            qrCode = toBitmap(qrCodeWriter.encode(mPrivateKey, BarcodeFormat.QR_CODE, size, size));
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
            progressBar.setVisibility(View.GONE);
            imgQR.setImageBitmap(qrCode);
        }
    }
}
