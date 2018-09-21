package test.com.accessibility.Utilz.ScreenLock;


import android.view.accessibility.AccessibilityEvent;

public class Nexus_2 implements ScreenLockUtil {
    public String is_device_locked(AccessibilityEvent event){
        return "None";
    }
    public boolean unlock_pin(AccessibilityEvent event, int[] PIN){
        return false;
    }
}
