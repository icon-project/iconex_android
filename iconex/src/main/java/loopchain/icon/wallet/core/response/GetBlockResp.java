package loopchain.icon.wallet.core.response;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GetBlockResp {

	private String _id;
	private int _code;
	private BlockData _block;
	
	public GetBlockResp(LCResponse lcResp) {
		_id = lcResp.getID();
		
		JsonElement result = lcResp.getResult();
		if( result instanceof JsonObject) {
			JsonObject resp = (JsonObject)result;
			_code = resp.get("response_code").getAsInt();
			
			JsonElement tmp = resp.get("block");
			if(tmp != null) {
				JsonObject obj = tmp.getAsJsonObject();
				_block = new Gson().fromJson(obj, BlockData.class);
			}
		}
	}
	
	public int getCode() {
		return _code;
	}
	
	public BlockData getBlock() {
		return _block;
	}
	
	public String getID(){
		return _id;
	}
}
