package loopchain.icon.wallet.service.crypto;

import java.math.BigInteger;

import loopchain.icon.wallet.core.Constants;

public class SendTransactionSigner {

	private String _method = Constants.METHOD_SENDTRANSACTION;
	private byte[] _tbs;
	
	public SendTransactionSigner(String fee, String from, String timestamp, String to, String value, String nonce) {
		_tbs = makeTbs(fee, from, timestamp, to, value, nonce);
	}
	
	public SendTransactionSigner(String fee, String from, String timestamp, String to, String value) {
		_tbs = makeTbs(fee, from, timestamp, to, value, null);
	}
	
	private byte[] makeTbs(String fee, String from, String timestamp, String to, String value, String nonce)  {
		String tbs = _method + ".fee." + fee + ".from." + from;
		if(nonce != null)
			tbs = tbs + ".nonce." + nonce;
		tbs = tbs + ".timestamp." + timestamp + ".to." + to + ".value." + value;
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
