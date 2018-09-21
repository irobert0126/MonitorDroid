package test.com.accessibility.core.audio;

import android.util.Log;

import test.com.accessibility.util.audioRecording.recordingAudio;

public class audioRecord {

    public static boolean isRecording(){
        return recordingAudio.isRecord;
    }
    public static void startRecording(String root_folder){
        if(recordingAudio.isRecord == false) {
            Log.d("audioRecord", "[tluo] Start Audio Recording and Save to ["+root_folder+"]");
            recordingAudio.startRecordAndFile(root_folder + "/audio_recording_" + System.currentTimeMillis() + ".amr");
        }
    }
    public static void stopRecording(){
        if(recordingAudio.isRecord == true) {
            recordingAudio.stopRecordAndFile();
        }
    }

    public static void test(String root_folder){
        if(recordingAudio.isRecord == false){
            recordingAudio.startRecordAndFile(root_folder+"/audio_recording_"+System.currentTimeMillis()+".amr");
        } else {
            recordingAudio.stopRecordAndFile();
        }
    }
}
