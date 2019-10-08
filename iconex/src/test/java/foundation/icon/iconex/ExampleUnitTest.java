package foundation.icon.iconex;

import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
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
        PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());

        try {
            RpcItem result = pRepService.getDelegation("hx19ab90c4d767d6f5e80080fdf842f63acf4b1acc");
            RpcObject o = result.asObject();
            System.out.println(o.getItem("votingPower").asInteger());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}