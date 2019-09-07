package foundation.icon.iconex.dev_mainWallet.items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.util.ScreenUnit;

public class TokenWalletItem extends WalletItem{

    public static class TokenColor {
        public static String[] colorCodes = {
            "#4390DE",
            "#735CCC",
            "#B547CC",
            "#40993D",
            "#E66A29",
            "#805339",
            "#E6B000",
            "#E645D3",
            "#4754FF",
            "#71B800",
            "#EE385D",
            "#547345"
        };

        private int color;
        private int index;

        public TokenColor () {
          this(0);
        }

        public TokenColor (int idx) {
            index = idx;
            color = parseColor(idx);
        }

        public void nextColor() {
            index = (index + 1) % 12;
            color = parseColor(index);
        }

        public int getColor() {
            return color;
        }

        private int parseColor(int idx) {
            String colorCode = colorCodes[idx];
            int r = Integer.parseInt(colorCode.substring(1,3), 16);
            int g = Integer.parseInt(colorCode.substring(3,5), 16);
            int b = Integer.parseInt(colorCode.substring(5,7), 16);
            return Color.rgb(r,g,b);
        }
    }
    public ViewGroup layoutWalletItem;

    public ImageView mImgSymbol;
    public TextView mTxtSymbolLetter;
    public TextView mTxtSymbol;
    public TextView mTxtName;
    public TextView mTxtAmount;
    public TextView mTxtExchanged;

    public TokenWalletItem(@NonNull Context context)  {
        super(context);
        initView();
    }

    private void initView () {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.item_wallet_token, this, false);

        layoutWalletItem = v.findViewById(R.id.wallet_item_layout);
        mImgSymbol = v.findViewById(R.id.img_symbol);
        mTxtSymbolLetter = v.findViewById(R.id.txt_symbol_letter);
        mTxtSymbol = v.findViewById(R.id.txt_symbol);
        mTxtName = v.findViewById(R.id.txt_name);
        mTxtAmount = v.findViewById(R.id.txt_amount);
        mTxtExchanged = v.findViewById(R.id.txt_exchanged);

        addView(v);
    }

    public void setLetterSymbol(char letter, int tokenBgColor) {
        mTxtSymbolLetter.setText(letter+"");
        int dp32 = ScreenUnit.dp2px(getContext(), 32);
        Bitmap output = Bitmap.createBitmap(dp32, dp32, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.TRANSPARENT);
        canvas.drawPaint(paint);
        canvas.drawBitmap(output, 0.0f, 0.0f, paint);
        paint.setColor(tokenBgColor);
        int dp16 = dp32 / 2;
        canvas.drawCircle(dp16, dp16, dp16, paint);
        mImgSymbol.setImageBitmap(output);
    }

    public void setSymbolImage(int resId) {
        mTxtSymbolLetter.setText("");
        mImgSymbol.setImageResource(resId);
    }

    @Override
    public void bind(WalletItemViewData data) {
        mTxtSymbol.setText(data.getSymbol());
        mTxtName.setText(data.getName());
        mTxtAmount.setText(data.getTxtAmount());
        mTxtExchanged.setText(data.getTxtExchanged());

        setLetterSymbol(data.getSymbolLetter(), data.getBgSymbolColor());
    }

    @Override
    public void setOnClickWalletItem(OnClickListener listener) {
        layoutWalletItem.setOnClickListener(listener);
    }
}
