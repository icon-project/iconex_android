package foundation.icon.iconex.wallet.main;

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

public class AddressFragment extends Fragment {

    private static final String ARG_ALIAS = "ARG_ALIAS";
    private static final String ARG_ADDRESS = "ARG_ADDRESS";

    private String mAlias;
    private String mAddress;

    private ProgressBar progress;
    private ImageView imgQRCode;
    private TextView txtAddress;
    private Button btnCopy;
    private Button btnReturn;

    private GenerateQRCode genQR;

    public AddressFragment() {
        // Required empty public constructor
    }

    public static AddressFragment newInstance(String alias, String address) {
        AddressFragment fragment = new AddressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ALIAS, alias);
        args.putString(ARG_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAlias = getArguments().getString(ARG_ALIAS);
            mAddress = getArguments().getString(ARG_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_address, container, false);

        ((TextView) v.findViewById(R.id.txt_alias)).setText(mAlias);

        imgQRCode = v.findViewById(R.id.img_qrcode);
        progress = v.findViewById(R.id.progress);

        txtAddress = v.findViewById(R.id.txt_address);
        txtAddress.setText(mAddress);

        btnCopy = v.findViewById(R.id.btn_copy);
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData data = ClipData.newPlainText("address", txtAddress.getText().toString());
                clipboard.setPrimaryClip(data);

                Toast.makeText(getActivity(), getString(R.string.msgCopyAddress), Toast.LENGTH_SHORT).show();
            }
        });

        btnReturn = v.findViewById(R.id.btn_return);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).hideWalletAddress();
            }
        });

        genQR = new GenerateQRCode();
        genQR.execute(mAddress);

        return v;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        genQR.cancel(true);
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
            progress.setVisibility(View.GONE);
            imgQRCode.setImageBitmap(qrCode);
        }
    }
}
