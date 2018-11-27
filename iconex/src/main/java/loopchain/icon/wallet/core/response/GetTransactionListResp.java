package loopchain.icon.wallet.core.response;

import java.util.Vector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetTransactionListResp {

	private String _id;
	private int _code;
	private int _nextIndex;
	private JsonArray _response;
	
	public GetTransactionListResp(LCResponse lcResp) {
		_id = lcResp.getID();
		
		JsonElement result = lcResp.getResult();
		if( result instanceof JsonObject) {
			JsonObject resp = (JsonObject)result;
			_code = resp.get("response_code").getAsInt();
			
			_nextIndex = resp.get("next_index").getAsInt();
			JsonElement tmp = resp.get("response");
			if(tmp != null) {
				_response = tmp.getAsJsonArray();
			}
		}
	}
	
	public int getCode() {
		return _code;
	}
	
	public int getNexIndex() {
		return _nextIndex;
	}
	
	public Vector<String> getTransactionList() {
		Vector<String> v = new Vector<String>();
		if(_response == null)
			return v;
		
		int size = _response.size();
		for(int i=0; i<size; i++) {
			String tmp = _response.get(i).getAsString();
			v.add(tmp);
		}
		return v;
	}
	
	public String getID(){
		return _id;
	}
}
