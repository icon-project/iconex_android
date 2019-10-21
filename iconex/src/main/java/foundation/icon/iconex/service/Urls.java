package foundation.icon.iconex.service;

import java.math.BigInteger;

public class Urls {

    public static final String endPoint = "/api/v3";

    public enum Network {
        MainNet("https://ctz.solidwallet.io/api/v3",
                "https://ctz.solidwallet.io",
                "https://tracker.icon.foundation", new BigInteger("1")),
        Euljiro("https://test-ctz.solidwallet.io/api/v3",
                "https://test-ctz.solidwallet.io",
                "https://trackerdev.icon.foundation", new BigInteger("2")),
        Yeouido("https://bicon.net.solidwallet.io/api/v3",
                "https://bicon.net.solidwallet.io",
                "https://bicon.tracker.solidwallet.io", new BigInteger("3")),
        Zicon("https://zicon.net.solidwallet.io",
                "https://zicon.net.solidwallet.io",
                "", new BigInteger("3"));

        private String url, noEndPoint, tracker;
        private BigInteger nid;

        public String getUrl() {
            return url;
        }

        public String getUrlNoEndPoint() {
            return noEndPoint;
        }

        public String getTracker() {
            return tracker;
        }

        public BigInteger getNid() {
            return nid;
        }

        Network(String url, String noEndPoint, String tracker, BigInteger nid) {
            this.url = url;
            this.noEndPoint = noEndPoint;
            this.tracker = tracker;
            this.nid = nid;
        }

        public static Network fromNid(int nid) {
            for (Network network : values()) {
                if (nid == network.getNid().intValue())
                    return network;
            }

            return null;
        }
    }
}
