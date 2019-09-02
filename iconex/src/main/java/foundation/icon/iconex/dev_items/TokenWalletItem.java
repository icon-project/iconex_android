package foundation.icon.iconex.dev_items;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.util.zip.Inflater;

import foundation.icon.iconex.R;

public class TokenWalletItem extends FrameLayout {

    public enum TokenColor {
        A("#4390de"),
        B("#73gccc"),
        C("#b547cc"),
        D("#40993d"),
        E("#e66a29"),
        F("#805339"),
        G("#e6b000"),
        H("#e645d3"),
        I("#4754ff"),
        J("#71b800"),
        K("#ee385d"),
        L("#547345");

        public int color;
        TokenColor(String color) {
            this.color = Color.parseColor(color);
        }
    }

    public ImageView mImgSymbol;
    public TextView mTxtSymbolLetter;
    public TextView mTxtSymbol;
    public TextView mTxtName;
    public TextView mTxtAmount;
    public TextView mTxtExchanged;

    public TokenWalletItem(@NonNull Context context) {
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
        mTxtSymbolLetter.setText(letter);
        int dp32 = dp2px(32);
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

    private int dp2px (int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getContext().getResources().getDisplayMetrics());
    }
}
