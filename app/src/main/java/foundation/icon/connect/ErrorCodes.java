package foundation.icon.connect;

public class ErrorCodes {

    // =========== Codes ===========
    public static final int ERR_USER_CANCEL = -1000;
    public static final int ERR_PARSE = -1001;
    public static final int ERR_INVALID_RQ = -1002;
    public static final int ERR_INVALID_METHOD = -1003;
    public static final int ERR_NOT_FOUND_CALLER = -1004;

    public static final int ERR_EMPTY = -2001;
    public static final int ERR_NOT_FOUND = -2002;

    public static final int ERR_NO_WALLET = -3001;
    public static final int ERR_SAME_ADDRESS = -3002;
    public static final int ERR_INSUFFICIENT_BALANCE = -3003;
    public static final int ERR_INSUFFICIENT_BALANCE_FOR_FEE = -3004;
    public static final int ERR_INVALID_PARAMETER = -3005;

    public static final int ERR_SIGN_FAILED = -4001;

    public static final int ERR_NETWORK = -9999;

    // =========== Codes ===========
    public static final String MSG_USR_CANCEL = "Operation canceled by user.";
    public static final String MSG_PARSE = "Parse error. (Invalid JSON type)";
    public static final String MSG_INVALID_RQ = "Invalid request.";
    public static final String MSG_INVALID_M = "Invalid method.";
    public static final String MSG_NOT_FOUND_CALLER = "Could not find caller.";

    public static final String MSG_EMTPY = "ICONex has no ICX wallet.";
    public static final String MSG_NOT_FOUND = "Not found parameter. (%s)";

    public static final String MSG_NO_WALLET = "Could not find matched wallet. (%s)";
    public static final String MSG_SAME_ADDRESS = "Sending and receiving address are same.";
    public static final String MSG_INSUFFICIENT = "Insufficient balance.";
    public static final String MSG_INSUFFICIENT_FEE = "Insufficient balance for fee";
    public static final String MSG_INVALID_PARAM = "Invalid parameter. (%s)";

    public static final String MSG_SIGN_FAILED = "Failed to sign.";

    public static final String MSG_NETWORK = "Somethings wrong with network. (%s)";


    public static class Error extends Exception {
        private int code;
        private String result;

        public Error(int code, String result) {
            this.code = code;
            this.result = result;
        }

        public int getCode() {
            return code;
        }

        public String getResult() {
            return result;
        }
    }
}
