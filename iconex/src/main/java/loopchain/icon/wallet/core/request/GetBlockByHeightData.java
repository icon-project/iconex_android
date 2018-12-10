package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetBlockByHeightData extends RequestData{

    public GetBlockByHeightData(int id, int height) {
        this.method = Constants.METHOD_GETBLOCKBYHEIGHT;
        this.id = id;
        
        JsonObject params = new JsonObject();
		params.addProperty("height", height);

        this.params = params;
    }
}
