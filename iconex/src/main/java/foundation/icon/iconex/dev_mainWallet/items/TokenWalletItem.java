package foundation.icon.iconex.dev_mainWallet.items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.util.ScreenUnit;

public class TokenWalletItem extends FrameLayout implements WalletItem{

    public enum TokenColor {
        A("#4390DE"),
        B("#735CCC"),
        C("#B547CC"),
        D("#40993D"),
        E("#E66A29"),
        F("#805339"),
        G("#E6B000"),
        H("#E645D3"),
        I("#4754FF"),
        J("#71B800"),
        K("#EE385D"),
        L("#547345");

        public int color;
        TokenColor(String color) {
            int r = Integer.parseInt(color.substring(1,3), 16);
            int g = Integer.parseInt(color.substring(3,5), 16);
            int b = Integer.parseInt(color.substring(5,7), 16);
            this.color = Color.rgb(r,g,b);
        }
    }

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
        mTxtAmount.setText(data.getAmount());
        mTxtExchanged.setText(data.getExchanged());

        setLetterSymbol(data.getSymbolLetter(), data.getBgSymbolColor());
    }
}
