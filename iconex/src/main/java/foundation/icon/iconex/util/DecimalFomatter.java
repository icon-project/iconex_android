package foundation.icon.iconex.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public class DecimalFomatter {

    public static String format(BigDecimal decimal, int scale) {
        String string;
        if (decimal == null) {
            string = "-";
        } else if ((decimal+"").split("\\.")[0].length() > 13) {
            string = String.format("%e", decimal);
        } else {
            String repeated0 = new String(new char[scale]).replace("\0", "0");
            DecimalFormat decimalFormat = new DecimalFormat("#,##0"+ (scale == 0 ? "" : ".") + repeated0);
            string = decimalFormat.format(decimal.setScale(scale, BigDecimal.ROUND_FLOOR));
        }
        return string;
    }

    public static String format(BigDecimal decimal) {
        return format(decimal, 4);
    }
}
