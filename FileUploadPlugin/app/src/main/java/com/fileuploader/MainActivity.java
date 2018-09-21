package com.fileuploader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    private static String LOG_TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        test_wechat_files();

    }



    private void test_random_files(){
        File dataDir = new File(getExternalFilesDir(null).getAbsolutePath());
        for (int i = 0; i < 10; i++) {
            try {
                File file = File.createTempFile("test-", ".txt", dataDir);
                Log.v(LOG_TAG, "Created temp file: " + file.getAbsolutePath().toString());

                try {
                    FileWriter out = new FileWriter(file);
                    out.write("this is a test file");
                    out.close();
                } catch (IOException e) {
                }

            } catch (IOException e) {
                // Error while creating file
            }
        }

        startFileUploaderService();
    }


    private void test_wechat_files() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("img");
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed to get asset file list.", e);
        }

        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                Log.e(LOG_TAG, "img/" + filename);
                Log.e(LOG_TAG,  getExternalFilesDir(null).getAbsolutePath()+"/" + filename);
                in = assetManager.open("img/" + filename);
                //File outFile = new File(this.getApplicationInfo().dataDir +"/", filename);
                File outFile = new File(getExternalFilesDir(null),  filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }

        startFileUploaderService();
    }

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private void startFileUploaderService(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.fileuploader", "com.fileuploader.FileUploadService"));
        startService(intent);
    }

    private void stopFileUploaderService(){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.fileuploader", "com.fileuploader.FileUploadService"));
        stopService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
