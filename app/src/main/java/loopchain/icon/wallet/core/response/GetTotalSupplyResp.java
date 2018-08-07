package loopchain.icon.wallet.core.response;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetTotalSupplyResp {

	private String _id;
	private int _code;
	private String _response;
	
	public GetTotalSupplyResp(LCResponse lcResp) {
		_id = lcResp.getID();
		
		JsonElement result = lcResp.getResult();
		if( result instanceof JsonObject) {
			JsonObject resp = (JsonObject)result;
			_code = resp.get("response_code").getAsInt();
			
			JsonElement tmp = resp.get("response");
			if(tmp != null)
				_response = tmp.getAsString();
		}
	}
	
	public int getCode() {
		return _code;
	}
	
	public String getTotalSupply() {
		return _response;
	}
	
	public String getID(){
		return _id;
	}
}
