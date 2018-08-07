package loopchain.icon.wallet.core.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;


public class SendTransactionResp {

	private String _id;
	private int _code;
	private String _message;
	private String _txHash;
	
	public SendTransactionResp(LCResponse lcResp) {
		_id = lcResp.getID();
		
		JsonElement result = lcResp.getResult();
		if( result instanceof JsonObject) {
			JsonObject resp = (JsonObject)result;
			_code = resp.get("response_code").getAsInt();
			
			if( _code == Constants.CODE_SUCCESS) {
				_txHash = resp.get("tx_hash").getAsString();
				_message = null;
			} else {
				_message = resp.get("message").getAsString();
				_txHash = null;
			}
		}
	}
	
	public int getCode() {
		return _code;
	}
	
	public String getMessage() {
		return _message;
	}
	
	public String getTxHash() {
		return _txHash;
	}
	
	public String getID(){
		return _id;
	}
}
