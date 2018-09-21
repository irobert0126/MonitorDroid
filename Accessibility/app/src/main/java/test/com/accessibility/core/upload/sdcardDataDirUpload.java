package test.com.accessibility.core.upload;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import test.com.accessibility.base.IntentInterface.intentInterface;
import test.com.accessibility.util.network.AsyncHelper;
import test.com.accessibility.util.fileListMonitor.fileListMonitor;

public class sdcardDataDirUpload {
    // For wechat screenshots on SDCard, firsTime is always False, i.e., we upload all images from the very beginning.
    private static boolean firstTime = false;
    private static String targetDir = null;
    //private static AsyncHelper uploadCallback;

    public static void init(Context context, String dir){

        targetDir = dir;
    }

    public static void Uploading(Context context, String dir){
        if (targetDir == null) {
            init(context, dir);
        }
        AsyncHelper uploadCallback = new AsyncHelper(context, true);
        Log.d("tluo", "sdcardDataDirUpload::Uploading");
        fileListMonitor imageObserver = new fileListMonitor(context, targetDir, uploadCallback);
        imageObserver.startWatching(firstTime);
    }

    public static void sendUploadIntent(Context context){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(intentInterface.UploadService_Action_PkgName, intentInterface.UploadService_Action_ClsName));
        intent.setAction(intentInterface.UploadService_Action_DataDir);
        //intent.putExtra(intentInterface.UploadService_Action_DataDir_Param_RootDir, context.getExternalFilesDir(null).getAbsolutePath());
        //Log.d("tluo", context.getPackageName());
        intent.putExtra(intentInterface.UploadService_Action_DataDir_Param_RootDir, "/storage/emulated/0/Android/data/com.example.TestPlugin/files/");
        context.startService(intent);
    }
}
