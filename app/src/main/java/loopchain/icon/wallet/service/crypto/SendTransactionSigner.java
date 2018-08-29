package loopchain.icon.wallet.service.crypto;

import android.util.Log;

import java.math.BigInteger;

import loopchain.icon.wallet.core.Constants;

public class SendTransactionSigner {
    private static final String TAG = SendTransactionSigner.class.getSimpleName();

    private String _method = Constants.METHOD_SENDTRANSACTION;
    private byte[] _tbs;

    public SendTransactionSigner(String version, String from, String to, String value, String stepLimit, String timestamp, String nid, String nonce) {
        _tbs = makeTbs(version, from, to, value, stepLimit, timestamp, nid, nonce);
    }

    private byte[] makeTbs(String version, String from, String to, String value, String stepLimit, String timestamp, String nid, String nonce) {
        String tbs = _method + ".from." + from + ".nid." + nid + ".nonce." + nonce + ".stepLimit." + stepLimit
                + ".timestamp." + timestamp + ".to." + to  + ".value." + value + ".version." + version;
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
