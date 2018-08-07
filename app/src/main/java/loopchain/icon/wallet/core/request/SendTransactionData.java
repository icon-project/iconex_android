package loopchain.icon.wallet.core.request;

import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.SendTransactionSigner;

public class SendTransactionData extends RequestData {

	private static final String FEE = "0x2386f26fc10000";
	private static final String NONCE = "8367273";

    public SendTransactionData(String id, String timestamp, String from, String to, String value, String fee, String hexPrivateKey) {
        this.method = Constants.METHOD_SENDTRANSACTION;
        this.id = id;
        
//        String timestamp = getTimeStamp();
        
        SendTransactionSigner signer;
        signer = new SendTransactionSigner(FEE, from, timestamp, to, value, NONCE);

        String txHash = signer.getTxHash();
        String signature = signer.getSignature(txHash, hexPrivateKey);
        
        JsonObject params = new JsonObject();
        params.addProperty("from", from);
        params.addProperty("to", to);
        params.addProperty("value", value);
        params.addProperty("fee", fee);
        params.addProperty("timestamp", timestamp);
        params.addProperty("nonce", NONCE);
        params.addProperty("tx_hash", txHash);
        params.addProperty("signature", signature);

        this.params = params;
    }
}
