package foundation.icon.sample_iconex_connect;

import android.app.Application;

public class SampleApp extends Application {

    public static String version = "0x3";
    public static String from = "hx80e3543e010dff7bce9a34f91d1fa17e1f15281f";
    public static String to = "hx1aef4e4c6ebf429436986d2f974a0d9bdecef496";
    public static String value = "0xde0b6b3a7640000";
    public static String stepLimit = "0x12345";
    public static String timestamp = "0x563a6cf330136";
    public static String nid = "0x3";
    public static String nonce = "0x1";

    public static String score = "cx4ae65c058d35b5bb8cef668be5113354448c0264";

    public static final String ACTION_DEVELOPER = "DEVELOPER";

    public static final String LOCAL_ACTION = "Update";

    public enum Method {
        Sign,
        SendIcx,
        SendToken
    }
}
