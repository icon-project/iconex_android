package loopchain.icon.wallet.service.crypto;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.math.BigInteger;

import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.request.Transaction;

public class SendTransactionSigner {
    private static final String TAG = SendTransactionSigner.class.getSimpleName();

    private String _method = Constants.METHOD_SENDTRANSACTION;
    private byte[] _tbs;

    public SendTransactionSigner(Transaction tx) {
        _tbs = makeTbs(tx);
    }

    private byte[] makeTbs(Transaction tx) {

        String tbs;
        if (tx.getDataType() == null)
            tbs = _method + ".from." + tx.getFrom() + ".nid." + tx.getNid() + ".nonce." + tx.getNonce() + ".stepLimit." + tx.getStepLimit()
                    + ".timestamp." + tx.getTimestamp() + ".to." + tx.getTo() + ".value." + tx.getValue() + ".version." + tx.getVersion();
        else {
            String value = new Gson().fromJson(tx.getData(), JsonObject.class).get("params").getAsJsonObject().get("_value").getAsString();
            tbs = _method + ".data.{method.transfer.params.{_to." + tx.getDataTo() + "._value." + value + "}}.dataType.call.from." + tx.getFrom()
                    + ".nid." + tx.getNid() + ".nonce." + tx.getNonce() + ".stepLimit." + tx.getStepLimit()
                    + ".timestamp." + tx.getTimestamp() + ".to." + tx.getTo() + ".version." + tx.getVersion();
        }

        Log.d(TAG, "tbs=" + tbs);

        return tbs.getBytes();
    }

    public String getTxHash() {
        try {
            byte[] hash = PKIUtils.hash(_tbs, PKIUtils.ALGORITHM_HASH);
            return PKIUtils.hexEncode(hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getSignature(String txHash, String hexPrivateKey) {
        byte[] privateKey = PKIUtils.hexDecode(hexPrivateKey);
        byte[] hash = PKIUtils.hexDecode(txHash);
        BigInteger[] sign = PKIUtils.sign(hash, privateKey);
        byte[] publicKey = PKIUtils.getPublicKeyFromPrivateKey(privateKey, true);

        byte recoveryId = PKIUtils.getRecoveryId(sign, hash, publicKey);
        byte[] signData = PKIUtils.getSignature(sign[0], sign[1], new byte[]{recoveryId});
        return PKIUtils.b64Encode(signData);
    }
}
