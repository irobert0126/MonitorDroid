package test.com.accessibility.core.wechat;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;


public class wechatUtil {
    public enum status {
        Unknown, MainPanel, ChatContent, ImageLargeView, LocationInfo, Invalid
    }

    static public boolean filter1(AccessibilityEvent event){
        if(event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
                event.getContentChangeTypes() != AccessibilityEvent.CONTENT_CHANGE_TYPE_TEXT) {
            return true;
        }
        return false;
    }

    static public status analyze_xml(AccessibilityNodeInfo root, int eventType){
        return analyze_xml_7_0(root, eventType);
    }

    static public status analyze_xml_7_0(AccessibilityNodeInfo root, int eventType) {
        if (root == null) {
            return status.Invalid;
        }
        if(eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED){
            return status.Unknown;
        }
        if(eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            if (root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b2").size() > 0) {
                Log.d("wechatUtil", "[info] find large image view - com.tencent.mm:id/b2");
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

    static public int checkVoiceChat(AccessibilityNodeInfo root) {
        List<AccessibilityNodeInfo> presstotalk_Node = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ac9");
        if (presstotalk_Node.size() > 0) {
            List<AccessibilityNodeInfo> switchtotext_Node = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ac6");
            if (switchtotext_Node.size() > 0) {
                return 1;
            }
        }
        return 0;
    }
}
