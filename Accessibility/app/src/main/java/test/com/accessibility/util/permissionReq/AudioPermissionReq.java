package test.com.accessibility.util.permissionReq;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

public class AudioPermissionReq {
    private static String TAG = "tluo";
    // 音频获取源
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    public static int sampleRateInHz = 44100;
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    public static int bufferSizeInBytes = 0;

    public static void makePermissionRequest(final Activity context) {
        /*int REQUEST_CODE_CONTACT = 101;
        if (Build.VERSION.SDK_INT >= 23) {
            context.checkSelfPermission();
            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(Manifest.permission.RECORD_AUDIO, REQUEST_CODE_CONTACT);
            }
        } else {
            checkAudioPermission(context);
        }*/
    }
    public static boolean checkAudioPermission(final Context context) {
        bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        AudioRecord audioRecord = null;
        try {
            audioRecord =  new AudioRecord(audioSource, sampleRateInHz,
                    channelConfig, audioFormat, bufferSizeInBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try{
            audioRecord.startRecording();
        }catch (IllegalStateException e){
            e.printStackTrace();
            Log.e(TAG, Log.getStackTraceString(e));
        }
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING
                && audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_STOPPED) {
            Log.e(TAG, "audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING : " + audioRecord.getRecordingState());
            return false;
        }

        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
            return true;
        }

        byte[] bytes = new byte[1024];
        int readSize = audioRecord.read(bytes, 0, 1024);
        if (readSize == AudioRecord.ERROR_INVALID_OPERATION || readSize <= 0) {
            Log.e(TAG, "readSize illegal : " + readSize);
            return false;
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

        return true;
    }
}
