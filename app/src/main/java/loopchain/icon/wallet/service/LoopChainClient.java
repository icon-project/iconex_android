package loopchain.icon.wallet.service;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

import loopchain.icon.wallet.core.Constants;
import foundation.icon.iconex.service.RESTClient;
import loopchain.icon.wallet.core.request.GetBalanceData;
import loopchain.icon.wallet.core.request.GetTransactionResultData;
import loopchain.icon.wallet.core.request.SendTransactionData;
import loopchain.icon.wallet.core.response.GetTransactionResultResp;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.core.response.TRResponse;
import loopchain.icon.wallet.service.crypto.KeyStoreUtils;
import loopchain.icon.wallet.service.crypto.PKIUtils;
import retrofit2.Call;


public class LoopChainClient {

	private RESTClient proxyClient;
	private SecureRandom random;
	private static BigInteger decimal = new BigInteger("1000000000000000000");

	public LoopChainClient(String host) throws Exception {
		proxyClient = new RESTClient(host);
		random = new SecureRandom();
	}

	public Call<LCResponse> sendTransaction(String id, String timestamp, String from, String to, String value, String fee, String hexPrivateKey) throws IOException {
		if(!value.startsWith("0x")) {
			value = icxToHexString(value);
		}

		SendTransactionData txData = new SendTransactionData(id, timestamp, from, to, value, fee, hexPrivateKey);
		Call<LCResponse> response = proxyClient.sendRequest(txData);
		return response;
	}

	public Call<LCResponse> getBalance(String id, String address) throws IOException {
		GetBalanceData qData = new GetBalanceData(id, address);
		Call<LCResponse> response = proxyClient.sendRequest(qData);
		return response;
	}

	public Call<TRResponse> getExchangeRates(String reqData) throws IOException {
		Call<TRResponse> response = proxyClient.sendGetExRates(reqData);
		return response;
	}

	public Call<TRResponse> getTxList(String address, int page) throws IOException {
		Call<TRResponse> response = proxyClient.sendGetTxList(address, page);
		return response;
	}

	public GetTransactionResultResp getTransactionResult(String txHash) throws IOException {
		String id = getRandomId();
		GetTransactionResultData qData = new GetTransactionResultData(id, txHash);
//		LCResponse resp = proxyClient.sendRequest(qData);
//		return new GetTransactionResultResp(resp);
		return null;
	}

	public String[] loadKeyStore(String ksFilePath, String password) throws Exception {
		byte[] ksData = readFile(ksFilePath);
		JsonObject jsonKS = new Gson().fromJson(new String(ksData), JsonObject.class);

		String address = jsonKS.get("address").getAsString();

		byte[] decKey = null;
		if(jsonKS.has("coinType")){
			String coinType = jsonKS.get("coinType").getAsString();
			if(coinType.equalsIgnoreCase(Constants.KS_COINTYPE_ICX)) {
				JsonObject crypto = jsonKS.getAsJsonObject("crypto");
				decKey = KeyStoreUtils.decryptPrivateKey(password, address, crypto, Constants.KS_COINTYPE_ICX);
			} else {
				throw new RuntimeException("Unsupported COIN type(" + coinType + ")");
			}
		} else {
			JsonObject crypto = jsonKS.getAsJsonObject("Crypto");
			if(crypto == null)
				crypto = jsonKS.getAsJsonObject("crypto");

			decKey = KeyStoreUtils.decryptPrivateKey(password, address, crypto, Constants.KS_COINTYPE_ETH);
		}
		return new String[]{address, PKIUtils.hexEncode(decKey)};
	}



	public static String printICX(String value) {
		if( value.startsWith("0x"))
			value = value.substring(2);
		else
			return value;

		BigInteger[] total = new BigInteger(value, 16).divideAndRemainder(decimal);

		String icx = total[0].toString();
		String wei = total[1].toString();
		while(wei.length() < 18)
			wei = "0" + wei;

		return icx + "." + wei;
	}

	public static String icxToHexString(String value) {
		int start = value.indexOf(".");
		if(start < 1) {
			BigInteger icx = new BigInteger(value);
			icx = icx.multiply(decimal);
			return "0x" + icx.toString(16);
		} else {
			BigInteger icx = new BigInteger(value.substring(0, start));
			icx = icx.multiply(decimal);

			String tmp = value.substring(start + 1);
			while(tmp.length() < 18)
				tmp = tmp + "0";
			BigInteger dot = new BigInteger(tmp);

			icx = icx.add(dot);
			value = "0x" + icx.toString(16);
			return value;
		}
	}

	public static BigInteger valueToBigInteger(String value) {
		int start = value.indexOf(".");
		if(start < 1) {
			BigInteger eth = new BigInteger(value);
			eth = eth.multiply(decimal);
			return eth;
		} else {
			BigInteger eth = new BigInteger(value.substring(0, start));
			eth = eth.multiply(decimal);

			String tmp = value.substring(start + 1);
			while(tmp.length() < 18)
				tmp = tmp + "0";
			BigInteger dot = new BigInteger(tmp);

			eth = eth.add(dot);
			return eth;
		}
	}

	public static BigInteger stringToBigInt(String value) {
	    if (value.startsWith("0x")) {
	        value = printICX(value);
        }

		int start = value.indexOf(".");
		if(start < 1) {
			BigInteger icx = new BigInteger(value);
			icx = icx.multiply(decimal);
			return icx;
		} else {
			BigInteger icx = new BigInteger(value.substring(0, start));
			icx = icx.multiply(decimal);

			String tmp = value.substring(start + 1);
			while (tmp.length() < 18)
				tmp = tmp + "0";
			BigInteger dot = new BigInteger(tmp);

			icx = icx.add(dot);
			return icx;
		}
	}

	public static String bigIntegerToString(BigInteger value) {
	    BigInteger[] total = value.divideAndRemainder(decimal);

	    String icx = total[0].toString();
	    String wei = total[1].toString();
	    while (wei.length() < 18)
            wei = "0" + wei;

	    return icx + "." + wei;
    }

	private byte[] readFile(String strFilePath) throws IOException {
		FileInputStream fis = new FileInputStream(strFilePath);
		byte[] data = new byte[fis.available()];
		fis.read(data);
		fis.close();
		return data;
	}

	private void writeFile(String strFilePath, byte[] data) throws IOException {
		FileOutputStream fos = new FileOutputStream(strFilePath);
		fos.write(data);
		fos.close();
	}

	private String getRandomId() {
		return PKIUtils.hexEncode(random.generateSeed(3));
	}

//	public String getNonce() {
//		return Integer.toUnsignedString(random.nextInt());
//	}


}
