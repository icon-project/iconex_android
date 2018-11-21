package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;

public class GetTransactionResultData extends RequestData {

    public GetTransactionResultData(int id, String txHash) {
        this.method = Constants.METHOD_GETTRANSACTIONRESULT;
        this.id = id;
        
        JsonObject params = new JsonObject();
		params.addProperty("tx_hash", txHash);

        this.params = params;
    }
}
