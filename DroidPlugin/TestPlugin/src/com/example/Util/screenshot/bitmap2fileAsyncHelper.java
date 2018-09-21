package com.example.Util.screenshot;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class bitmap2fileAsyncHelper extends AsyncTask<String[], Void, String> {
    private static String LOG_TAG = "tluo";

    public Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override
    protected String doInBackground(String[]... params) {
        Log.v(LOG_TAG, "[info] (" + Calendar.getInstance().getTime().toString() + ") Saving task BEGIN in background");
        Bitmap bitmap = StringToBitMap(params[0][0]);
        File file = new File(params[0][1]);
        Log.d("tluo", "[info] Save Bitmap To (" + file + ")");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.d("tluo", "[error] Exception:" + e);
            if (bitmap != null)
                bitmap.recycle();
            e.printStackTrace();
        }
        Log.v(LOG_TAG, "[info] (" + Calendar.getInstance().getTime().toString() + ") Saving task DONE in background");
        return "Done";
    }

    @Override
    protected void onPostExecute(String result) { }
    @Override
    protected void onPreExecute() {}
    @Override
    protected void onProgressUpdate(Void... values) {}

}
