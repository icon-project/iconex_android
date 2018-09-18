package loopchain.icon.wallet.core.response;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * Created by js on 2018. 3. 13..
 */

public class TRResponse {

    @SerializedName("result")
    private String result;

    @SerializedName("description")
    private String decription;

    @SerializedName("listSize")
    private int listSize;

    @SerializedName("data")
    private JsonElement data;

    public String getResult() {
        return result;
    }

    public String getDecription() {
        return decription;
    }

    public int getListSize() {
        return listSize;
    }

    public JsonElement getData() {
        return data;
    }
}
