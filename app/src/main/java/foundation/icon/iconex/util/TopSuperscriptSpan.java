package foundation.icon.iconex.util;

import android.text.TextPaint;
import android.text.style.SuperscriptSpan;

public class TopSuperscriptSpan extends SuperscriptSpan {

    private int fontScale = 2;
    private  float shiftPercentage = 0;

    public TopSuperscriptSpan() {}

    public TopSuperscriptSpan(float shiftPercentage) {
        if (shiftPercentage > 0.0 && shiftPercentage < 1.0)
            this.shiftPercentage = shiftPercentage;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        float ascent = tp.ascent();

        tp.setTextSize(tp.getTextSize() / fontScale);

        float newAscent = tp.getFontMetrics().ascent;

        tp.baselineShift += (ascent - ascent * shiftPercentage)
                - (newAscent - newAscent * shiftPercentage);
    }

    @Override
    public void updateMeasureState(TextPaint tp) {
        updateDrawState(tp);
    }
}
