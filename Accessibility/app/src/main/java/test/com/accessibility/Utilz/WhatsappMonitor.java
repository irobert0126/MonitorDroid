package test.com.accessibility.Utilz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import test.com.accessibility.Utilz.screenshot.ScreenShot_test;

public class WhatsappMonitor {
    public static ScreenShot_test screenShot;
    Context ctx;

    public WhatsappMonitor(Context ctx) {
        this.screenShot = new ScreenShot_test();
        this.ctx = ctx;
    }

    public void processNextEvent(int mEventType) {
        switch(mEventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                //screenShot.takeScreenshot(ctx, false);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                break;
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                //screenShot.takeScreenshot(ctx, false);
                break;
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                //screenShot.takeScreenshot(ctx, false);
                break;
            default:
                break;
        }
    }
    public void sleep(int ms){
        SystemClock.sleep(ms);
    }

    public static Map<String, Object> Extract_Content(AccessibilityNodeInfo root, Context ctx, String path){
        Map<String, Object> extracted = new HashMap<>();
        extracted.put("contact_name", root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_name").get(0).getText().toString());
        //extracted.put("contact_status", root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/conversation_contact_status").get(0).getText().toString());
        List<AccessibilityNodeInfo> messages = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/message_text");
        List<String> text_msg = new ArrayList<String>();
        for (AccessibilityNodeInfo node:messages) {
            text_msg.add(node.getText().toString());
        }

        List<AccessibilityNodeInfo> images = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/image");
        List<AccessibilityNodeInfo> locations = root.findAccessibilityNodeInfosByViewId("com.whatsapp:id/map_frame");
        Bitmap full_image = null;
        if (images.size() + locations.size() > 0) {
            //full_image = screenShot.takeScreenshot(ctx, true, null);
        }
        List<String> image_msg = new ArrayList<String>();
        for (AccessibilityNodeInfo node:images) {
            Rect outBounds = new Rect();
            node.getBoundsInScreen(outBounds);
            Bitmap resizedbitmap1= Bitmap.createBitmap(full_image, outBounds.left, outBounds.top-75, outBounds.width(), outBounds.height());
            String file_path = path +"/" + "whatsapp_image_" + Calendar.getInstance().hashCode() + ".jpg";
            screenShot.save_bitmap_to_file(resizedbitmap1, file_path);
            image_msg.add(file_path);
        }
        List<String> location_msg = new ArrayList<String>();
        for (AccessibilityNodeInfo node:locations) {
            Rect outBounds = new Rect();
            node.getBoundsInScreen(outBounds);
            Bitmap resizedbitmap1= Bitmap.createBitmap(full_image, outBounds.left, outBounds.top-75, outBounds.width(), outBounds.height());
            String file_path =  path +"/" + "whatsapp_location_" + Calendar.getInstance().hashCode() + ".jpg";
            screenShot.save_bitmap_to_file(resizedbitmap1, file_path);
            location_msg.add(file_path);
        }

        extracted.put("text_message", text_msg);
        extracted.put("image_message", image_msg);
        extracted.put("location_message", location_msg);
        JSONObject obj=new JSONObject(extracted);

        Log.d("MINE", "[tluo] MUNING:" + obj.toString());

        String json_file_path = path +"/" + "whatsapp_text_" + Calendar.getInstance().hashCode() + ".json";
        try (PrintStream out = new PrintStream(new FileOutputStream(json_file_path))) {
            out.print(obj.toString());
        }catch (IOException ex){
        }

        return extracted;
    }
}
