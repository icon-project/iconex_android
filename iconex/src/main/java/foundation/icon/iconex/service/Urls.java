package foundation.icon.iconex.service;

import java.math.BigInteger;

public class Urls {

    public static final String endPoint = "/api/v3";

    public enum Network {
        MainNet("https://ctz.solidwallet.io/api/v3",
                "https://tracker.icon.foundation", new BigInteger("1")),
        Euljiro("https://test-ctz.solidwallet.io/api/v3",
                "https://trackerdev.icon.foundation", new BigInteger("2")),
        Yeouido("https://bicon.net.solidwallet.io/api/v3",
                "https://bicon.tracker.solidwallet.io", new BigInteger("3"));

        private String url, tracker;
        private BigInteger nid;

        public String getUrl() {
            return url;
        }

        public String getTracker() {
            return tracker;
        }

        public BigInteger getNid() {
            return nid;
        }

        Network(String url, String tracker, BigInteger nid) {
            this.url = url;
            this.tracker = tracker;
            this.nid = nid;
        }
    }
}
