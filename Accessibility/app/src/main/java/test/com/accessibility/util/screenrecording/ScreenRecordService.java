package test.com.accessibility.util.screenrecording;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScreenRecordService extends Service {

    private static final String TAG = "ScreenRecordingService";

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenDensity;
    private int mResultCode;
    public static Intent iToken;
    private boolean isVideoSd;
    private boolean isAudio;

    private MediaProjection mMediaProjection;
    private MediaRecorder mMediaRecorder;
    private VirtualDisplay mVirtualDisplay;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Log.i(TAG, "Service onCreate() is called");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = intent.getStringExtra("command");
        Log.i(TAG, "[tluo] onStartCommand() is called with command = " + command);
        if(command.equals("stop")) {
            if (mMediaRecorder == null) {
                Log.e(TAG, "[tluo] When Stopping the MediaRecorder, its gone already !!!");
            } else {
                mMediaProjection.stop();
                mMediaRecorder.reset();
                mVirtualDisplay.release();
            }
        } else {
            mScreenDensity = intent.getIntExtra("density", 1);
            isVideoSd = intent.getBooleanExtra("isVideoSd", false);
            isAudio = intent.getBooleanExtra("audio", true);

            getScreenSize(this);
            iToken = intent.getExtras().getParcelable("token");
            mMediaProjection = createMediaProjection(iToken);
            String output = intent.getStringExtra("output_dir");
            mMediaRecorder = createMediaRecorder(output);
            mVirtualDisplay = createVirtualDisplay();
            mMediaRecorder.start();
        }
        return Service.START_NOT_STICKY;
    }

    public Point getScreenSize(@NonNull Context activity) {
        final Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        mScreenWidth = size.x;
        mScreenHeight = size.y;
        mScreenDensity = activity.getResources().getDisplayMetrics().densityDpi;
        return size;
    }

    private MediaProjection createMediaProjection(Intent token) {
        return ((MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE))
                .getMediaProjection(Activity.RESULT_OK, token);
    }

    private MediaRecorder createMediaRecorder(String output) {
        MediaRecorder mediaRecorder = new MediaRecorder();
        if(isAudio)
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(output);
        mediaRecorder.setVideoSize(mScreenWidth, mScreenHeight);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        if(isAudio) mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int bitRate;
        if(isVideoSd) {
            mediaRecorder.setVideoEncodingBitRate(mScreenWidth * mScreenHeight);
            mediaRecorder.setVideoFrameRate(30);
            bitRate = mScreenWidth * mScreenHeight / 1000;
        } else {
            mediaRecorder.setVideoEncodingBitRate(5 * mScreenWidth * mScreenHeight);
            mediaRecorder.setVideoFrameRate(60);
            bitRate = 5 * mScreenWidth * mScreenHeight / 1000;
        }
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Log.i(TAG, "ERROR:" + e.toString());
            e.printStackTrace();
        }
        return mediaRecorder;
    }

    private VirtualDisplay createVirtualDisplay() {
        return mMediaProjection.createVirtualDisplay(TAG, mScreenWidth, mScreenHeight, mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service onDestroy");
        if(mVirtualDisplay != null) {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }
        if(mMediaRecorder != null) {
            mMediaRecorder.setOnErrorListener(null);
            mMediaProjection.stop();
            mMediaRecorder.reset();
        }
        if(mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}