package foundation.icon.iconex.service.response;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * Created by js on 2018. 5. 29..
 */

public class VSResponse {
    @SerializedName("result")
    private String result;

    @SerializedName("data")
    private JsonObject data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public JsonObject getData() {
        return data;
    }

    public void setData(JsonObject data) {
        this.data = data;
    }
}
