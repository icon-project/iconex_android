package loopchain.icon.wallet.core.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by js on 2018. 2. 13..
 */

public class TransactionData {

    @SerializedName("method")
    private String _method;
    @SerializedName("from")
    private String _from;
    @SerializedName("to")
    private String _to;
    @SerializedName("value")
    private String _value;
    @SerializedName("fee")
    private String _fee;
    @SerializedName("timestamp")
    private String _timeStamp;
    @SerializedName("tx_hash")
    private String _txHash;
    @SerializedName("signature")
    private String _signature;

    public String getMethod() {
        return _method;
    }

    public String getFrom() {
        return _from;
    }

    public String getTo() {
        return _to;
    }

    public String getValue() {
        return _value;
    }

    public String getFee() {
        return _fee;
    }

    public String getTimeStamp() {
        return _timeStamp;
    }

    public String getTxHash() {
        return _txHash;
    }

    public String getSignature() {
        return _signature;
    }
}
