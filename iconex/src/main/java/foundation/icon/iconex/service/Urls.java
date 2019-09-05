package foundation.icon.iconex.service;

public class Urls {

    public static final String endPoint = "/api/v3";

    public enum MainNet {
        Node("https://ctz.solidwallet.io"),
        Tracker("https://tracker.icon.foundation");

        private String url;
        private int nid = 1;

        public String getUrl() {
            return url;
        }

        public int getNid() {
            return nid;
        }

        MainNet(String url) {
            this.url = url;
        }
    }

    public enum Euljiro {
        Node("https://test-ctz.solidwallet.io/api/v3"),
        Tracker("https://trackerdev.icon.foundation");

        private String url;
        private static int nid = 2;

        public String getUrl() {
            return url;
        }

        public static int getNid() {
            return nid;
        }

        Euljiro(String url) {
            this.url = url;
        }
    }

    public enum Yeouido {
        Node("https://bicon.net.solidwallet.io"),
        Tracker("https://bicon.tracker.solidwallet.io");

        private String url;
        private int nid = 3;

        public String getUrl() {
            return url;
        }

        public int getNid() {
            return nid;
        }

        Yeouido(String url) {
            this.url = url;
        }
    }
}
