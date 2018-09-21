package test.com.accessibility.Utilz.ScreenLock;

import android.view.accessibility.AccessibilityEvent;


public interface ScreenLockUtil {
    public String is_device_locked(AccessibilityEvent event);
    public boolean unlock_pin(AccessibilityEvent event, int[] PIN);
}

