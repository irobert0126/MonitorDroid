package test.com.accessibility.util.screenrecording;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.projection.MediaProjectionManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class screenrecord {
    private static String TAG = "screenrecord(lib)";
    private static final int STORAGE_REQUEST_CODE = 101;
    private static final int REQUEST_MEDIA_RECORD = 201;
    public static int recording_status = 0;
    public static Intent record_token = null;

    private static int DISPLAY_WIDTH, DISPLAY_HEIGHT, DISPLAY_ROTATION, DISPLAY_DPI = 1;
    private static MediaCodec mMediaCodec;

    public static void init(@NonNull Activity activity){
        requestMediaProjPermission(activity);
    }

    public static void setToken(Intent token){
        record_token = token;
    }
    public static void requestMediaProjPermission(@NonNull Activity activity) {
        MediaProjectionManager projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activity.startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_RECORD);
    }

    public static void startRecording(@NonNull Context context, String saveMediaPath, Intent token) {
        recording_status = 1;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        Date curDate = new Date(System.currentTimeMillis());
        String curTime = formatter.format(curDate).replace(" ", "");
        Boolean isVideoSd = false;
        if(isVideoSd)
            saveMediaPath = saveMediaPath + "/SD_" + curTime + ".mp4";
        else
            saveMediaPath = saveMediaPath + "/HD_" + curTime + ".mp4";
        Intent service = new Intent(context, test.com.accessibility.util.screenrecording.ScreenRecordService.class);
        Log.d("tluo", "Buildin Token:[" + record_token + "] Passed Token:["+token + "] saveMediaPath:" + saveMediaPath);
        if (record_token != null) {
            service.putExtra("token", record_token);
        } else {
            service.putExtra("token", token);
        }
        service.putExtra("audio", false);
        service.putExtra("quality", isVideoSd);
        service.putExtra("command", "start");
        service.putExtra("output_dir", saveMediaPath);
        context.startService(service);
    }

    public static void stopRecording(@NonNull Context activity){
        Log.d("tluo", "stopRecording");
        Intent service = new Intent(activity, test.com.accessibility.util.screenrecording.ScreenRecordService.class);
        service.putExtra("command", "stop");
        activity.startService(service);
        recording_status = 0;
    }
}
