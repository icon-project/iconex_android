package foundation.icon.sample_icon_connect;

import android.app.Application;

public class SampleApp extends Application {

    public static String from;
    public static String to = "hx3c06b6dae3c7f50f768aa7a0e18c46e8d4b97b4e";
    public static String stepLimit = "1000000";
    public static String score = "cx4ae65c058d35b5bb8cef668be5113354448c0264";

    public static final String ACTION_CONNECT = "ICONEX_CONNECT";
    public static final String ACTION_DEVELOPER = "DEVELOPER";

    public static final String LOCAL_ACTION = "Update";

    public enum Method {
        Sign,
        SendIcx,
        SendToken
    }
}
