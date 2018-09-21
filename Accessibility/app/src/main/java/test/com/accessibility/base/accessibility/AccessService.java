package test.com.accessibility.base.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Calendar;

import test.com.accessibility.core.wechat.wechatUtil;
import test.com.accessibility.base.IntentInterface.intentInterface;

public class AccessService extends AccessibilityService {

    private final AccessibilityServiceInfo info = new AccessibilityServiceInfo();
    private long last_sms_time = 0;

    @Override
    public void onServiceConnected() {
        Log.d("tluo", "GOOD .. onAccessibilityServiceConnected");
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 500;
        info.packageNames = new String[]{"com.google.android.apps.messaging", "com.android.mms", "com.tencent.mm", "com.android.systemui", "com.google.android.apps.nexuslauncher", "com.whatsapp"};
        this.setServiceInfo(info);
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // dumpAccessEvent(event);
        preAccessbilityHook(event);
        prepareIntent(event);
    }


    protected void preAccessbilityHook(AccessibilityEvent event){

    }

    protected Intent prepareIntent(AccessibilityEvent event){
        Intent intent = new Intent();
        AccessibilityNodeInfo root = event.getSource();

        intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, intentInterface.intentServiceName));
        intent.setAction(intentInterface.Action_Accessibility);
        intent.putExtra(intentInterface.AccessEvent_Obj, event);
        //intent.putExtra(intentInterface.AccessEvent_PkgName, event.getPackageName());
        //intent.putExtra(intentInterface.AccessEvent_Type, event.getEventType());
        //intent.putExtra(intentInterface.AccessEvent_Time, event.getEventTime());
        //intent.putExtra(intentInterface.AccessEvent_Action, event.getAction());
        //intent.putExtra(intentInterface.AccessEvent_ContentChangeType, event.getContentChangeTypes());
        intent.putExtra(intentInterface.AccessEvent_RootNode, root);

        intent.putExtra(intentInterface.intentService_Param_RootDir, this.getExternalFilesDir(null).getAbsolutePath());
        startService(intent);

        return intent;
    }

    private void dumpAccessEvent(AccessibilityEvent event){
        String eventType = "";
        switch (event.getEventType()){
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                eventType = "TYPE_VIEW_CLICKED";
                break;
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                eventType = "TYPE_VIEW_FOCUSED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                eventType = "TYPE_VIEW_SCROLLED";
                break;
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                eventType = "TYPE_VIEW_SELECTED";
                break;
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                eventType = "TYPE_VIEW_TEXT_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                eventType = "TYPE_WINDOW_STATE_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                eventType = "TYPE_WINDOWS_CHANGED";
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                eventType = "TYPE_WINDOW_CONTENT_CHANGED";
                break;
            default:
                eventType = "" + event.getEventType();
        }
        Log.d("tluo", "[prepareIntent] Time(" +event.getEventTime()
                +") Package("+ event.getPackageName()
                +") Type("+ eventType
                +") Action("+event.getAction()
                +") ContentChangeTypes("+event.getContentChangeTypes()+")");
    }
}
