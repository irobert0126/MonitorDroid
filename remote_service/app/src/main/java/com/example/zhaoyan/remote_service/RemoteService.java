/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.zhaoyan.remote_service;

import android.app.IntentService;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Vector;


/**
 * This service pulls RSS content from a web site URL contained in the incoming Intent (see
 * onHandleIntent()). As it runs, it broadcasts its status using LocalBroadcastManager; any
 * component that wants to see the status should implement a subclass of BroadcastReceiver and
 * register to receive broadcast Intents with category = CATEGORY_DEFAULT and action
 * Constants.BROADCAST_ACTION.
 *
 */
public class RemoteService extends IntentService {
    // Used to write to the system log from this class.
    public static final String TAG = "PluginService";
    public static final String PARAM_IN_MSG = "imsg";

    public  RemoteService() {
        super("RemoteService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"onCreate called" );
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super. onStart(intent, startId);
        Log.i(TAG,"onStart called" );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"onStartCommand called" );
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy called" );
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"onBind called" );
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log. i(TAG,"onUnbind called" );
        return super.onUnbind(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long id = Thread.currentThread().getId();
        Log.i(TAG, " ----> onHandleIntent() in thread id: " + id);
        String msg = intent.getStringExtra(PARAM_IN_MSG);

        Log.i(TAG, msg);

        try {
            NetworkHelper helper = new NetworkHelper();
//            helper.createTmpFile(this.getBaseContext(), "test.txt");
            helper.receiveCommand(this.getBaseContext());
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Done for Handling.");
    }

    @Override
    public void setIntentRedelivery(boolean enabled) {
        super.setIntentRedelivery(enabled);
        Log.i(TAG, "Intent Redelivered");
    }

}
