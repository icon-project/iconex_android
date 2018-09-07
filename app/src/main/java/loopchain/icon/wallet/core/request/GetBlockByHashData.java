package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetBlockByHashData extends RequestData{

    public GetBlockByHashData(int id, String hash) {
        this.method = Constants.METHOD_GETBLOCKBYHASH;
        this.id = id;
        
        JsonObject params = new JsonObject();
		params.addProperty("hash", hash);

        this.params = params;
    }
}
