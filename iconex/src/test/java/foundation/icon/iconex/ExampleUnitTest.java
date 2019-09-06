package foundation.icon.iconex;

import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
        IconService iconService = new IconService(Urls.Euljiro.Node.getUrl());

        try {
//            RpcItem result = iconService.getStepPrice();
//            RpcValue value = result.asValue();
//            System.out.println(value);
//            System.out.println(iconService.estimateStep("hx8f21e5c54f006b6a5d5fe65486908592151a7c57"));

            BigInteger step = iconService.estimateStep("hx8f21e5c54f006b6a5d5fe65486908592151a7c57");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}