package admin.ui.connector.utills;

import javax.net.ssl.*;
import java.security.*;
import java.security.cert.X509Certificate;

public class SSLUtil {
    public static void disableSSLVerification() {
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }}, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            System.out.println("SSL Verification Disabled Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
