package foundation.icon.iconex.util;

import android.util.Log;

import java.math.BigInteger;

/**
 * Created by js on 2018. 4. 30..
 */

public class ConvertUtil {

    private static final String TAG = ConvertUtil.class.getSimpleName();

    public static String valueToHexString(String value, int dec) {
        BigInteger decimals = getDecimals(dec);
        int start = value.indexOf(".");
        if (start < 1) {
            BigInteger icx = new BigInteger(value);
            icx = icx.multiply(decimals);
            return "0x" + icx.toString(16);
        } else {
            BigInteger icx = new BigInteger(value.substring(0, start));
            icx = icx.multiply(decimals);

            String tmp = value.substring(start + 1);
            while (tmp.length() < dec)
                tmp = tmp + "0";
            BigInteger dot = new BigInteger(tmp);

            icx = icx.add(dot);
            value = "0x" + icx.toString(16);
            return value;
        }
    }

    public static BigInteger valueToBigInteger(String value, int dec) {
        BigInteger decimals = getDecimals(dec);
        int start = value.indexOf(".");
        if (start < 1) {
            BigInteger eth = new BigInteger(value);
            eth = eth.multiply(decimals);
            return eth;
        } else {
            BigInteger eth = new BigInteger(value.substring(0, start));
            eth = eth.multiply(decimals);

            String tmp = value.substring(start + 1);
            while (tmp.length() < dec)
                tmp = tmp + "0";


            BigInteger dot = new BigInteger(tmp);

            eth = eth.add(dot);
            return eth;
        }
    }

    public static BigInteger hexStringToBigInt(String value, int dec) throws Exception{
        BigInteger decimals = getDecimals(dec);
        if (value.startsWith("0x")) {
            value = hexToFloat(value, dec, decimals);
        }

        int start = value.indexOf(".");
        if (start < 1) {
            BigInteger icx = new BigInteger(value);
            icx = icx.multiply(decimals);
            return icx;
        } else {
            BigInteger icx = new BigInteger(value.substring(0, start));
            icx = icx.multiply(decimals);

            String tmp = value.substring(start + 1);
            while (tmp.length() < 18)
                tmp = tmp + "0";
            BigInteger dot = new BigInteger(tmp);

            icx = icx.add(dot);
            return icx;
        }
    }

    public static String getValue(BigInteger balance, int dec) {
        BigInteger decimals = getDecimals(dec);
        BigInteger[] total = balance.divideAndRemainder(decimals);


        String integer = total[0].toString();
        String floating = total[1].toString();
        while (floating.length() < dec)
            floating = "0" + floating;

        return integer + "." + floating;
    }

    private static String hexToFloat(String value, int dec, BigInteger decimals) {
        if (value.startsWith("0x"))
            value = value.substring(2);
        else
            return value;

        BigInteger[] total = new BigInteger(value, 16).divideAndRemainder(decimals);

        String icx = total[0].toString();
        String wei = total[1].toString();
        while (wei.length() < dec)
            wei = "0" + wei;

        return icx + "." + wei;
    }

    private static BigInteger getDecimals(int dec) {
        if (dec == 0) {
            return BigInteger.ONE;
        } else {
            String theOne = "1";
            String decimals = "";
            while (decimals.length() < dec)
                decimals += "0";
            return new BigInteger(theOne + decimals);
        }
    }
}
