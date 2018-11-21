package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetBalanceData extends RequestData {

    public GetBalanceData(int id, String address) {

        this.method = Constants.METHOD_GETBALANCE;
        this.id = id;

        JsonObject params = new JsonObject();
        params.addProperty("address", address);

        this.params = params;
    }
}
