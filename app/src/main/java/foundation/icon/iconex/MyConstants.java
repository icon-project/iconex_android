package foundation.icon.iconex;

/**
 * Created by js on 2018. 3. 7..
 */

public class MyConstants {

    public static final String TYPE_COIN = "COIN";
    public static final String TYPE_TOKEN = "TOKEN";

    public static final String NAME_ICX = "ICON";
    public static final String NAME_ETH = "Ethereum";

    public static final String SYMBOL_ICON = "ICX";
    public static final String SYMBOL_ETH = "ETH";

    public static final String EXCHANGE_USD = "USD";
    public static final String EXCHANGE_BTC = "BTC";
    public static final String EXCHANGE_ETH = "ETH";

    public static final String NO_EXCHANGE = "0";

    public static final int CODE_OK = 0;
    public static final String RESULT_OK = "200";

    public static final String PREFIX_HEX = "0x";
    public static final String PREFIX_IRC = "cx";

    // ======== Realm schema version ========
    public static final int VERSION_REALM_SCHEMA = 0;

    // ======== Balance ========
    public static final String NO_BALANCE = "-";

    // ======== Tx State ========
    public static final int TX_DONE = 0;
    public static final int TX_PENDING = 1;

    // ======== Wallet Management Menu Tags ========
    public static final String TAG_MENU_ALIAS = "ALIAS";
    public static final String TAG_MENU_TOKEN = "TOKEN";
    public static final String TAG_MENU_BACKUP = "BACKUP";
    public static final String TAG_MENU_PWD = "PWD";
    public static final String TAG_MENU_REMOVE = "REMOVE";

    // ======== Language ========
    public static final String LOCALE_KO = "ko";
    public static final String LOCALE_EN = "en";

    // ======== Eth Tokens ========
    public static final String ETH_TOKEN_FILE = "ethTokens.json";
    public static final String ETH_INCINERATION = "0x0000000000000000000000000000000000000000";

    // ======== ICX Tokens ========
    public static final String IRC_TOKENS_FILE = "IrcTokens.json";
    public static final String M_ERC_ICX_ADDR = "0xb5A5F22694352C15B00323844aD545ABb2B11028";
    public static final String T_ERC_ICX_ADDR = "0x55116b9cf269e3f7e9183d35d65d6c310fcacf05";

    public static final int ICX_DEC = 18;
    public static final String ICX_SYM = "ICX";

    // ======== App Lock ========
    public static final int LOCK_TIME_LIMIT = 5 * 60 * 1000;

    // ======== Network ========
    public static final int NETWORK_MAIN = 0;
    public static final int NETWORK_TEST = 1;
    public static final int NETWORK_DEV = 2;

    public static final String PATTERN_PASSWORD = "^(?=.*\\d)(?=.*[a-zA-Z])(?=.*[?!:\\.,%+-/*<>{}()\\[\\]`\"'~_^\\|@#$&]).{8,}$";

    public enum CoinType {
        ICX,
        ETH
    }

    public enum TxState {
        DONE,
        PENDING
    }

    public enum TxType {
        WHOLENESS,
        REMITTANCE,
        DEPOSIT
    }

    public enum MODE_TOKEN {
        ADD,
        MOD
    }

    public enum TypeLock {
        DEFAULT,
        LOST,
        RECOVER
    }

    public enum FingerprintState {
        INVALID,
        DISABLED,
        NO_ENROLLED,
        PASSCODE
    }
}
