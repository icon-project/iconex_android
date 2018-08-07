package loopchain.icon.wallet.core.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;


public class GetTransactionResultResp {
	
	private String _id;
	private int _code;
	private String _message;
	private int _resultCode;
	
	public GetTransactionResultResp(LCResponse lcResp) {
		_id = lcResp.getID();
		
		JsonElement result = lcResp.getResult();
		if( result instanceof JsonObject) {
			JsonObject resp = (JsonObject)result;
			_code = resp.get("response_code").getAsInt();
			
			if( _code != Constants.CODE_SUCCESS) {
				_message = resp.get("message").getAsString();
			}
			
			JsonElement tmp = resp.get("response");
			if( (tmp != null) && (tmp instanceof JsonObject) ) {
				_resultCode = ((JsonObject)tmp).get("code").getAsInt();
			}
		}
	}
	
	public int getCode() {
		return _code;
	}
	
	public String getMessage() {
		return _message;
	}
	
	public int getResultCode() {
		return _resultCode;
	}
	
	public String getID(){
		return _id;
	}
}
