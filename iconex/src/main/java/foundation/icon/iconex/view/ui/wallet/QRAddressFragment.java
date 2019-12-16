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

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;

public class QRAddressFragment extends Fragment {

    private static final String ARG_ADDR = "ARG_ADDRESS";

    private String mAddress;

    private TextView txtAddress;
    private ImageView imgQR;
    private ProgressBar progressBar;
    private Button btnCopy;

    public QRAddressFragment() {
        // Required empty public constructor
    }

    public static QRAddressFragment newInstance(String address) {
        QRAddressFragment fragment = new QRAddressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ADDR, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAddress = getArguments().getString(ARG_ADDR);
            if (!mAddress.startsWith("hx"))
                mAddress = MyConstants.PREFIX_HEX + mAddress;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_qraddress, container, false);

        txtAddress = v.findViewById(R.id.txt_address);
        txtAddress.setText(mAddress);

        imgQR = v.findViewById(R.id.img_qrcode);
        progressBar = v.findViewById(R.id.progress);

        btnCopy = v.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("address", mAddress);
                clipboard.setPrimaryClip(data);

                Toast.makeText(getActivity(), getString(R.string.msgCopyAddress), Toast.LENGTH_SHORT).show();
            }
        });

        GenerateQRCode generateQRCode = new GenerateQRCode();
        generateQRCode.execute(mAddress);

        return v;
    }

    private Bitmap generateQRcode() {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Bitmap qrCode = null;
        int size = (int) getResources().getDimension(R.dimen.QRCodeSize);
        try {
            qrCode = toBitmap(qrCodeWriter.encode(mAddress, BarcodeFormat.QR_CODE, size, size));
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
