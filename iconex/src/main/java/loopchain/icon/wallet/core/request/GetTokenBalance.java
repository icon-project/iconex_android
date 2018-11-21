package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetTokenBalance extends RequestData {

    public GetTokenBalance(int id, String address, String score) {
        this.method = Constants.METHOD_ICXCALL;
        this.id = id;

        JsonObject params = new JsonObject();
        params.addProperty("from", address);
        params.addProperty("to", score);
        params.addProperty("dataType", "call");

        JsonObject data = new JsonObject();
        data.addProperty("method", Constants.METHOD_GETTOKENBALANCE);
        JsonObject dataParams = new JsonObject();
        dataParams.addProperty("_owner", address);

        data.add("params", dataParams);
        params.add("data", data);

        this.params = params;
    }
}
