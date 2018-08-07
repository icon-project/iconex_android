package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetLastBlockData extends RequestData{

    public GetLastBlockData(String id) {
        this.method = Constants.METHOD_GETLASTBLOCK;
        this.id = id;
        
        JsonObject params = new JsonObject();

        this.params = params;
    }

}
