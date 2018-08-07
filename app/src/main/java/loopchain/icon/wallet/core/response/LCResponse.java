package loopchain.icon.wallet.core.response;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

public class LCResponse {

    @SerializedName("id")
    private String id;
    
    @SerializedName("result")
    private JsonElement result;

    public String getID() {
        return id;
    }
    

    public JsonElement getResult() {
    	return result;
    }
   
}
