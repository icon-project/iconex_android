package loopchain.icon.wallet.core.request;

import com.google.gson.JsonElement;

public abstract class RequestData {

    private String jsonrpc = "2.0";
    protected String method;
    protected int id;
    protected JsonElement params;

    protected String getTimeStamp() {
        long time = System.currentTimeMillis() * 1000;
        return Long.toString(time);
    }

    public String toString() {
        String req = "{" +
                "\"jsonrpc\":\"" + jsonrpc + "\"" +
                ",\"method\":\"" + method + "\"" +
                ",\"id\":\"" + id + "\"" +
                ",\"params\":" + params.toString() +
                "}";
        System.out.println("Request Data  : " + req);
        return req;
    }
}
