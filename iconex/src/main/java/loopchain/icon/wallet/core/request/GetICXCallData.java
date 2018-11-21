package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetICXCallData extends RequestData {

    public GetICXCallData(int id, String address, String score, JsonObject data) {
        this.method = Constants.METHOD_ICXCALL;
        this.id = id;

        JsonObject params = new JsonObject();
        params.addProperty("from", address);
        params.addProperty("to", score);
        params.addProperty("dataType", "call");
        params.add("data", data);

        this.params = params;
    }
}
