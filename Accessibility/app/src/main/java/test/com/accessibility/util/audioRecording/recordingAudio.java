package test.com.accessibility.util.audioRecording;

import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class recordingAudio {
    private static MediaRecorder mMediaRecorder;
    public static boolean isRecord = false;
    private final static String AUDIO_RAW_FILENAME = "RawAudio.raw";
    private final static String AUDIO_WAV_FILENAME = "FinalAudio.wav";
    public final static String AUDIO_AMR_FILENAME = "FinalAudio.amr";

    public static void recordingwithTimer(String filename, int timeout){
        Log.d("tluo", "[tluo] Start Recording to [" + filename + "]");
        if(isRecord == false) {
            recordingAudio.startRecordAndFile(filename);
            CountDownTimer countDowntimer = new CountDownTimer(10000, timeout) {
                public void onTick(long millisUntilFinished) {}
                public void onFinish() {
                    recordingAudio.stopRecordAndFile();
                }
            };
            countDowntimer.start();
        }
    }

    public static int startRecordAndFile(String filename) {
        try {
            if (mMediaRecorder == null)
                createMediaRecord(filename);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isRecord = true;
            return 1;
        } catch (IOException ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private static void createMediaRecord(String filename){
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(audioFileUtil.AUDIO_INPUT);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        if(filename == null | filename == ""){
            filename = "FinalAudio.amr";
        }
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
        mMediaRecorder.setOutputFile(filename);
    }

    public static void stopRecordAndFile(){
        if (mMediaRecorder != null && isRecord == true) {
            isRecord = false;
            mMediaRecorder.stop();
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
