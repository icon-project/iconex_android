package foundation.icon.connect;

public class Response {

    private int code;
    private String message;
    private String result;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getResult() {
        return result;
    }

    private Response(Builder builder) {

        code = builder.code;
        message = builder.message;
        result = builder.result;
    }

    public static class Builder {

        private int code;
        private String message;
        private String result;

        public Builder code(int code) {
            this.code = code;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder result(String result) {
            this.result = result;
            return this;
        }

        public Response build() {
            return new Response(this);
        }
    }
}
