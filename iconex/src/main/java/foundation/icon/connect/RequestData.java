package foundation.icon.connect;

import java.io.Serializable;

public class RequestData implements Serializable {

    private String data;
    private String caller;
    private String receiver;

    public RequestData(String data, String caller, String receiver) {
        this.data = data;
        this.caller = caller;
        this.receiver = receiver;
    }

    public String getData() {
        return data;
    }

    public String getCaller() {
        return caller;
    }

    public String getReceiver() {
        return receiver;
    }
}
