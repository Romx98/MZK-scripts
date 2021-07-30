package cz.mzk.scripts.configuration;

public class ProxySettings{

    public static void activateProxy(String hostName, String hostPort) {
        System.setProperty("http.proxyHost", hostName);
        System.setProperty("http.proxyPort", hostPort);
    }

    public static void deactivationProxy() {
        System.clearProperty("http.proxyHost");
        System.clearProperty("http.proxyPort");
    }
}
