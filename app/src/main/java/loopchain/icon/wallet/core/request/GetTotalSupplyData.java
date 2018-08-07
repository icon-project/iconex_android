package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetTotalSupplyData extends RequestData{

    public GetTotalSupplyData(String id) {
        this.method = Constants.METHOD_GETTOTALSUPPLY;
        this.id = id;
        
        JsonObject params = new JsonObject();

        this.params = params;
    }

}
