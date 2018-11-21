package loopchain.icon.wallet.core;

public class Constants {

    public static final int CODE_SUCCESS = 0;

    public static final String METHOD_SENDTRANSACTION = "icx_sendTransaction";
    public static final String METHOD_GETBALANCE = "icx_getBalance";
    public static final String METHOD_GETTRANSACTIONRESULT = "icx_getTransactionResult";
    public static final String METHOD_GETLASTBLOCK = "icx_getLastBlock";
    public static final String METHOD_GETBLOCKBYHASH = "icx_getBlockByHash";
    public static final String METHOD_GETBLOCKBYHEIGHT = "icx_getBlockByHeight";
    public static final String METHOD_GETTRANSACTIONBYADDRESS = "icx_getTransactionByAddress";
    public static final String METHOD_GETTOTALSUPPLY = "icx_getTotalSupply";
    public static final String METHOD_ICXCALL = "icx_call";
    public static final String METHOD_GETSCOREAPI = "icx_getScoreApi";

    public static final String METHOD_GETSTEPPRICE = "getStepPrice";
    public static final String METHOD_GETSTEPCOSTS = "getStepCosts";
    public static final String METHOD_GETMAXSTEPLIMIT = "getMaxStepLimit";
    public static final String METHOD_GETTOKENBALANCE = "balanceOf";

    public static final String DATA_CALL = "call";
    public static final String DATA_MESSAGE = "message";

    public static final String ADDRESS_GOVERNANCE = "cx0000000000000000000000000000000000000001";

    public static final int KS_VERSION = 3;

    public static final String KS_COINTYPE_ICX = "ICX";
    public static final String KS_COINTYPE_ETH = "ETH";

    public static final String KDF_PBKDF2 = "pbkdf2";
    public static final String KDF_SCRYPT = "scrypt";
}
