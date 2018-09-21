package test.com.accessibility.core.wechat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import test.com.accessibility.core.audio.audioRecord;
import test.com.accessibility.util.screenrecording.screenrecord;
import test.com.accessibility.util.screenshot.bitmap_helper;
import test.com.accessibility.util.screenshot.screenshot;

public class wechat_main {
    public static int counter = 0;
    public static SharedPreferences settings = null;
    public static String setting_wechat_action = "wechat_action";
    public static String action_screenshot = "takescreenshot";
    public static String action_videoshot = "takevideoshot";
    public static String setting_wechat_parse_xml = "wechat_parse_xml";

    public void init(Context contex) {
        settings = contex.getSharedPreferences("setting", 0);
        setActionScreenshot();
        setActionParseNodeXML(false);
    }

    public void setActionScreenshot() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(setting_wechat_action, action_screenshot);
        editor.commit();
    }

    public void setActionVideoshot() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(setting_wechat_action, action_videoshot);
        editor.commit();
    }

    public void setActionParseNodeXML(boolean action) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(setting_wechat_parse_xml, action);
        editor.commit();
    }

    /* TODO: */
    public void processAccessEvent(Context context, String root_dir, AccessibilityNodeInfo root, int event_type, Intent token) {
        counter += 1;
        String current_action = settings.getString("wechat_action", action_screenshot);
        Log.d("tluo", "WeChat Counter = (" + counter + ")" + " [" + System.currentTimeMillis() + "] current_action:" + current_action);

        if (current_action.equals(action_screenshot)) {
            wechatUtil.status xml_status = wechatUtil.status.Unknown;
            if (audioRecord.isRecording() && wechatUtil.checkVoiceChat(root) > 0) {
                audioRecord.stopRecording();
            }
            if(settings.getBoolean(setting_wechat_parse_xml, false)) {
                xml_status = wechatUtil.analyze_xml(root, event_type);
            }
            takeScreenShot(context, root_dir, xml_status.toString(), token);
        } else if (current_action.equals(action_videoshot)) {
            takeVideo(context, root_dir, token);
        }
    }

    public void takeScreenShot(Context context, String root_dir, String status, Intent token) {
        String filename = root_dir + "/" + "wechat_image_" + status + "_" + System.currentTimeMillis() + ".png";
        String bitmap = screenshot.capture_core(context, token);
        bitmap_helper.save_bitmap_to_file(bitmap_helper.StringToBitMap(bitmap), filename);
    }

    public void takeVideo(Context context, String root_dir, Intent token) {
        if (screenrecord.recording_status == 0) {
            screenrecord.startRecording(context, root_dir, token);
        } else {
            if (counter % 40 == 0) {
                screenrecord.stopRecording(context);
            }
        }
    }
}
