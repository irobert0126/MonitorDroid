package com.example.Util.wechat;


import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Calendar;
import java.util.List;

public class wechatUtil {
    public enum status {
        Unknown, MainPanel, ChatContent, ImageLargeView, LocationInfo, Invalid
    }

    static public status analyze_xml(AccessibilityService accessibilityService, AccessibilityEvent event){
        return analyze_xml_7_0(accessibilityService, event);
    }

    @TargetApi(18)
    static public status analyze_xml_7_0(AccessibilityService accessibilityService, AccessibilityEvent event) {
        AccessibilityNodeInfo accessibilityNodeInfo = accessibilityService.getRootInActiveWindow();
        int eventType = event.getEventType();
        if (accessibilityNodeInfo == null) {
            return status.Invalid;
        }
        AccessibilityNodeInfo root = accessibilityNodeInfo;
        if(event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED){
            return status.Unknown;
        }
        if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            if (root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2").size() > 0) {
                return status.ImageLargeView;
            }
        }
        List<AccessibilityNodeInfo> chatContent_Node = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/z");
        if (chatContent_Node.size() > 0 ) {
            List<AccessibilityNodeInfo> chatName_Node = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/hm");
            if (chatName_Node.size() > 0) {
                //String chatName = chatName_Node.get(0).getText().toString();
            }
            return status.ChatContent;
        }
        List<AccessibilityNodeInfo> mainPanel_Node = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b6");
        if (eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
                chatContent_Node.size() == 0 && mainPanel_Node.size() > 0) {
            return status.MainPanel;
        }
        List<AccessibilityNodeInfo> loc_map_Node = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ckx");
        if (loc_map_Node.size() > 0) {
            return status.ChatContent;
        }

        return status.Unknown;
    }
}