package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetMaxStepLimit extends RequestData {

    public GetMaxStepLimit(int id, String address) {
        this.method = Constants.METHOD_ICXCALL;
        this.id = id;

        JsonObject params = new JsonObject();
        params.addProperty("from", address);
        params.addProperty("to", Constants.ADDRESS_GOVERNANCE);
        params.addProperty("dataType", "call");
        JsonObject data = new JsonObject();
        data.addProperty("method", Constants.METHOD_GETMAXSTEPLIMIT);

        JsonObject dataParams = new JsonObject();
        dataParams.addProperty("context_type", "invoke");
        data.add("params", dataParams);

        params.add("data", data);

        this.params = params;
    }
}
