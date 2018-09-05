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

    public static final int KS_VERSION = 3;

    public static final String KS_COINTYPE_ICX = "ICX";
    public static final String KS_COINTYPE_ETH = "ETH";

    public static final String KDF_PBKDF2 = "pbkdf2";
    public static final String KDF_SCRYPT = "scrypt";
}
