package test.com.accessibility.Utilz.ScreenLock;

import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class HTC_ONE_7_0 implements ScreenLockUtil {
    public String is_device_locked(AccessibilityEvent event) {
        AccessibilityNodeInfo root = event.getSource();
        List<AccessibilityNodeInfo> Enter_PIN_Logo = root.findAccessibilityNodeInfosByText("Enter PIN to unlock");
        if(Enter_PIN_Logo.size() > 0) {
            Log.d("WeChatLog", "Current Window is a PIN Panel.");
            return "PIN";
        }
        return "None";
    }
    public boolean unlock_pin(AccessibilityEvent event, int[] PINs){
        /*
        int[] key_mapping = {10,0,1,2,3,4,5,6,7,8};
        AccessibilityNodeInfo root = event.getSource();
        List<AccessibilityNodeInfo> PIN_buttons = new ArrayList<AccessibilityNodeInfo>();
        List<AccessibilityNodeInfo> allnodes = MonitorService.getAllChildrenBFS(root);
        for (AccessibilityNodeInfo node : allnodes) {
            if (node != null) {
                if(node.getClassName().equals("android.widget.Button")) {
                    CharSequence content = node.getContentDescription();
                    if(content != null && content.equals("Dot.")) {
                        PIN_buttons.add(node);
                    }
                }
            }
        }
        if(PIN_buttons.size() < 10) {
            return false;
        }
        for(int pin : PINs){
            Log.d("WeChatLog", "PINs:"+pin);
            if(PIN_buttons.get(key_mapping[pin]) != null) {
                PIN_buttons.get(key_mapping[pin]).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }*/
        return true;
    }
}
