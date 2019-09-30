package foundation.icon.iconex.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import foundation.icon.iconex.view.ui.mainWallet.MainWalletFragment;

public class DecimalFomatter {

    public static String format(BigDecimal decimal, int scale) {
        String string;
        if (decimal == null) {
            string = "-";
        } else if ((decimal+"").split("\\.")[0].length() > 13) {
            string = String.format("%e", decimal);
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("#,###." + (scale == 2 ? "##" : "####"));
            string = decimalFormat.format(decimal.setScale(scale, BigDecimal.ROUND_FLOOR));
        }
        return string;
    }

    public static String format(BigDecimal decimal) {
        return format(decimal, 4);
    }
}
