package loopchain.icon.wallet.core.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.service.crypto.SendTransactionSigner;

public class Transaction extends RequestData {

    private final String VERSION = "0x3";
    private final Builder builder;

    public Transaction(Builder builder) {
        this.builder = builder;
        this.method = Constants.METHOD_SENDTRANSACTION;
        this.id = builder.id;

        JsonObject params = new JsonObject();
        params.addProperty("version", VERSION);
        params.addProperty("from", builder.from);
        params.addProperty("to", builder.to);

        if (builder.value != null)
            params.addProperty("value", builder.value);

        params.addProperty("stepLimit", builder.stepLimit);
        params.addProperty("timestamp", builder.timestamp);
        params.addProperty("nid", builder.nid);
        params.addProperty("nonce", builder.nonce);

        SendTransactionSigner signer;
        signer = new SendTransactionSigner(this);

        String txHash = signer.getTxHash();
        String signature = signer.getSignature(txHash, builder.hexPrivateKey);

        params.addProperty("signature", signature);

        if (builder.dataType != null)
            params.addProperty("dataType", builder.dataType);

        if (builder.data != null) {
            try {
                JsonObject data = new Gson().fromJson(builder.data, JsonObject.class);
                params.add("data", data);
            } catch (Exception e) {
                params.addProperty("data", builder.data);
            }
        }

        this.params = params;
    }

    public int getId() {
        return builder.id;
    }

    public String getVersion() {
        return VERSION;
    }

    public String getFrom() {
        return builder.from;
    }

    public String getTo() {
        return builder.to;
    }

    public String getValue() {
        return builder.value;
    }

    public String getStepLimit() {
        return builder.stepLimit;
    }

    public String getTimestamp() {
        return builder.timestamp;
    }

    public String getNid() {
        return builder.nid;
    }

    public String getNonce() {
        return builder.nonce;
    }

    public String getDataType() {
        return builder.dataType;
    }

    public String getData() {
        return builder.data;
    }

    public String getDataTo() {
        return builder.dataTo;
    }


    public static class Builder {
        private final int id;
        private final String nid;
        private final String hexPrivateKey;

        private String from;
        private String to;
        private String value;
        private String stepLimit;
        private String timestamp;
        private String nonce;
        private String dataType;
        private String data;
        private String dataTo;

        public Builder(int id, String nid, String hexPrivateKey) {
            this.id = id;
            this.nid = nid;
            this.hexPrivateKey = hexPrivateKey;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder dataTo(String to) {
            this.dataTo = to;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder stepLimit(String stepLimit) {
            this.stepLimit = stepLimit;
            return this;
        }

        public Builder timestamp(String timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder dataType(String dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder data(String data) {
            this.data = data;
            return this;
        }

        public Transaction build() {
            return new Transaction(this);
        }
    }
}
