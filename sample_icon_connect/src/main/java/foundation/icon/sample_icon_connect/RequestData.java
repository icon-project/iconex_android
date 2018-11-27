package foundation.icon.sample_icon_connect;

public class RequestData {
    private String version;
    private String from;
    private String to;
    private String value;
    private String stepLimit;
    private String timestamp;
    private String nid;
    private String nonce;
    private String message;
    private String contractAddress;
    private String dataType;
    private String data;

    public RequestData(Builder builder) {
        this.version = builder.version;
        this.from = builder.from;
        this.to = builder.to;
        this.value = builder.value;
        this.stepLimit = builder.stepLimit;
        this.timestamp = builder.timestamp;
        this.nid = builder.nid;
        this.nonce = builder.nonce;
        this.message = builder.message;
        this.contractAddress = builder.contractAddress;
        this.dataType = builder.dataType;
        this.data = builder.data;
    }

    public String getVersion() {
        return version;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getValue() {
        return value;
    }

    public String getStepLimit() {
        return stepLimit;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getNid() {
        return nid;
    }

    public String getNonce() {
        return nonce;
    }

    public String getMessage() {
        return message;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public String getDataType() {
        return dataType;
    }

    public String getData() {
        return data;
    }

    public static class Builder {
        private String version;
        private String from;
        private String to;
        private String value;
        private String stepLimit;
        private String timestamp;
        private String nid;
        private String nonce;
        private String message;
        private String contractAddress;
        private String dataType;
        private String data;

        public Builder version(String version) {
            this.version = version;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
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

        public Builder nid(String nid) {
            this.nid = nid;
            return this;
        }

        public Builder nonce(String nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder score(String score) {
            this.contractAddress = score;
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

        public RequestData build() {
            return new RequestData(this);
        }
    }
}
