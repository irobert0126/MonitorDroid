package test.com.accessibility.Utilz.main;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import test.com.accessibility.util.audioRecording.recordingAudio;

public class mainAsyncHelper extends AsyncTask<Intent, Void, String> {
    private static String LOG_TAG = "tluo";
    private static final String ACTION_Tecent_Screenshot = "test.com.accessibility.action.Tecent_Screenshot";
    private static final String ACTION_Tecent_Screenshot_dst_dir = "test.com.accessibility.extra.dst_dir";

    private static final String ACTION_Screenshot = "test.com.accessibility.action.Screenshot";
    private static final String ACTION_Screenshot_dst_dir = "test.com.accessibility.extra.dst_dir";
    private static final String ACTION_Screenshot_mtype = "test.com.accessibility.extra.mtype";
    private static final String ACTION_Screenshot_status = "test.com.accessibility.extra.status";
    private static final String Action_WhatApp_Accessibility = "x.y.z.WSapp";
    private static final String Param_Accessibility_Root = "x.y.z.paccroot";
    private static final String Action_SMSApp_Accessibility = "x.y.z.SMSapp";
    private static String root_folder;
    private static int counter = 0;

    public long taskID;
    public Context context;
    public mainAsyncHelper(Context context) {
        this.context = context;
        taskID = System.currentTimeMillis();
        root_folder = context.getExternalFilesDir(null).getAbsolutePath();
    }

    @Override
    protected String doInBackground(Intent... params) {
        handleCommand(params[0]);
        return "Done";
    }

    public void handleCommand(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_Tecent_Screenshot.equals(action)) {
                handleAction_Tecent_Screenshot(this.context, intent);
            } else if (Action_WhatApp_Accessibility.equals(action)) {
                //handleAction_WhatsApp_Screenshot(intent);
                handleAction_WhatsApp_Extraction(intent);
            } else if (Action_SMSApp_Accessibility.equals(action)) {
                //handleAction_SMS_Extraction(intent);
            }
        }
    }

    public void handleAction_WhatsApp_Extraction(Intent intent) {
    }

    public void handleAction_Tecent_Screenshot(Context context, Intent intent) {
        String status = intent.getStringExtra(ACTION_Screenshot_status);
        String filename = root_folder + "/" + "wechat_image_" + status + "_" + System.currentTimeMillis() + ".png";

        if(recordingAudio.isRecord == false){
            //recordingAudio.startRecordAndFile(root_folder+"/audio_recording_"+System.currentTimeMillis()+".amr");
        } else {
            counter += 1;
            if (counter == 10){
                //recordingAudio.stopRecordAndFile();
            }
        }
        /*if(screenrecord.recording_status == 0) {
            screenrecord.startRecording(context, root_folder);
        } else {
            counter += 1;
            if (counter == 10){
                screenrecord.stopRecording(context);
            }
        }*/
    // String bitmap = screenshot.capture_core(context);
    // bitmap_helper.save_bitmap_to_file(bitmap_helper.StringToBitMap(bitmap), filename);
    }

    @Override
    protected void onPostExecute(String result) { }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

}