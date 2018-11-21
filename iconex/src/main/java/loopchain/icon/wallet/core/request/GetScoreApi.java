package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetScoreApi extends RequestData {

    public GetScoreApi(int id, String address) {
        this.id = id;
        this.method = Constants.METHOD_GETSCOREAPI;

        JsonObject params = new JsonObject();
        params.addProperty("address", address);

        this.params = params;
    }
}
