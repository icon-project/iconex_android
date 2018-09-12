package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetStepCosts extends RequestData {

    public GetStepCosts(int id, String address) {
        this.method = Constants.METHOD_ICXCALL;
        this.id = id;

        JsonObject params = new JsonObject();
        params.addProperty("from", address);
        params.addProperty("to", Constants.ADDRESS_GOVERNANCE);
        params.addProperty("dataType", "call");
        JsonObject data = new JsonObject();
        data.addProperty("method", Constants.METHOD_GETSTEPCOSTS);
        params.add("data", data);

        this.params = params;
    }
}
