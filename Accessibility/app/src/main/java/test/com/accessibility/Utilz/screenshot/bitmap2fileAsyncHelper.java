package test.com.accessibility.Utilz.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;


public class bitmap2fileAsyncHelper extends AsyncTask<String[], Void, String> {
    private static String LOG_TAG = "tluo";

    public bitmap2fileAsyncHelper() {
    }

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
        Bitmap bitmap = StringToBitMap(params[0][0]);
        File file = new File(params[0][1]);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 10, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.d("tluo", "[error] Exception:" + e);
            if (bitmap != null)
                bitmap.recycle();
            e.printStackTrace();
        }
        Log.d("tluo-plugin", "[time] (" + System.currentTimeMillis() + ") DONE Save to File (Async)");
        return "Done";
    }

    @Override
    protected void onPostExecute(String result) { }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}

}
