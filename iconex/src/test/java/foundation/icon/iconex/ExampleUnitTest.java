package foundation.icon.iconex;

import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.service.IconService;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.Urls;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void getPrep() {
        BigDecimal iscore = new BigDecimal("523856579194203546917");
        System.out.println("-21=" + iscore.scaleByPowerOfTen(-21).toString());
        System.out.println("15자리=" + iscore.scaleByPowerOfTen(-21).setScale(15, RoundingMode.FLOOR).toString());
        System.out.println("estimatedIcx=" + iscore.divide(new BigDecimal("1000"), RoundingMode.FLOOR).scaleByPowerOfTen(-18).setScale(8, RoundingMode.FLOOR).toString());
        BigDecimal icx = new BigDecimal("523856579194203546");
        System.out.println("-18=" + icx.scaleByPowerOfTen(-18).toString());
        System.out.println("8자리=" + icx.scaleByPowerOfTen(-18).setScale(8, RoundingMode.FLOOR).toString());
    }
}