package foundation.icon.iconex;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.Urls;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.icx.transport.jsonrpc.RpcArray;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;

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
        try {
            PRepService prepService = new PRepService(Urls.Euljiro.Node.getUrl());
            RpcItem prepsResult = prepService.getPreps();

            RpcObject prepsObject = prepsResult.asObject();
            RpcArray prepArray = prepsObject.getItem("preps").asArray();
            List<PRep> pRepList = new ArrayList<>();
            for (RpcItem i : prepArray.asList()) {
                PRep prep = PRep.valueOf(i.asObject());
                System.out.println(prep);

                pRepList.add(prep);
            }

            for (PRep p : pRepList) {
                prepService.getPrep(p.getAddress());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}