package loopchain.icon.wallet.core.request;

import android.util.Log;

import com.google.gson.JsonObject;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;

import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.PKIUtils;
import loopchain.icon.wallet.service.crypto.SendTransactionSigner;

public class SendTransactionData extends RequestData {

    private static final String FEE = "0x2386f26fc10000";
    private static final String VERSION = "0x3";
    private static final String NONCE = "0x1";

    public SendTransactionData(int id, String timestamp, String from, String to, String value, String stepLimit, String hexPrivateKey) {
        this.method = Constants.METHOD_SENDTRANSACTION;
        this.id = id;

//        String timestamp = getTimeStamp();

        SendTransactionSigner signer;
        signer = new SendTransactionSigner(VERSION, from, to, value, stepLimit, timestamp, "0x3", NONCE);

        String txHash = signer.getTxHash();
        String signature = signer.getSignature(txHash, hexPrivateKey);

        JsonObject params = new JsonObject();
        params.addProperty("version", "0x3");
        params.addProperty("from", from);
        params.addProperty("to", to);
        params.addProperty("value", value);
        params.addProperty("stepLimit", stepLimit);
        params.addProperty("timestamp", timestamp);
        params.addProperty("nid", "0x3");
        params.addProperty("nonce", NONCE);
        params.addProperty("signature", signature);

        this.params = params;
    }
}
