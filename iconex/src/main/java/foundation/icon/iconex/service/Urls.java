package foundation.icon.iconex.service;

public class Urls {

    public static final String endPoint = "/api/v3";

    public enum Network {
        MainNet("https://ctz.solidwallet.io/api/v3",
                "https://tracker.icon.foundation", 1),
        Euljiro("https://test-ctz.solidwallet.io/api/v3",
                "https://trackerdev.icon.foundation", 2),
        Yeouido("https://bicon.net.solidwallet.io/api/v3",
                "https://bicon.tracker.solidwallet.io", 3);

        private String url, tracker;
        private int nid;

        public String getUrl() {
            return url;
        }

        public String getTracker() {
            return tracker;
        }

        public int getNid() {
            return nid;
        }

        Network(String url, String tracker, int nid) {
            this.url = url;
            this.tracker = tracker;
            this.nid = nid;
        }
    }
}
