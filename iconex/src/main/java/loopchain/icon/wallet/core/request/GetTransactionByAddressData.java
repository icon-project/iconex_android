package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetTransactionByAddressData extends RequestData{

    public GetTransactionByAddressData(int id, String address, int index) {
        this.method = Constants.METHOD_GETTRANSACTIONBYADDRESS;
        this.id = id;
        
        JsonObject params = new JsonObject();
		params.addProperty("address", address);
		params.addProperty("index", index);

        this.params = params;
    }
}
