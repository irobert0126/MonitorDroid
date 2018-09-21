package test.com.accessibility.util.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

public class AsyncHelper extends AsyncTask<String[], Void, String> {

    private Context mContext;
    private NetworkHelper mNetworHelper;
    private Boolean mRemoveAfterSent;
    private static String LOG_TAG = "AsyncHelper";

    public AsyncHelper(Context context) {
        mContext = context;
        mRemoveAfterSent = true;
        mNetworHelper = new NetworkHelper();
    }

    public AsyncHelper(Context context, Boolean removeAfterSent) {
        mContext = context;
        mRemoveAfterSent = removeAfterSent;
        mNetworHelper = new NetworkHelper();
    }

    @Override
    protected String doInBackground(String[]... params) {
        Log.v(LOG_TAG, "Sending files in background");
        String[] filesToUpload = params[0];
        for (int i = 0; i < filesToUpload.length; i++) {

            //if (!filesToUpload[i].endsWith("jpg") && !filesToUpload[i].endsWith("png")){
            //    continue;
            //}
            Log.v(LOG_TAG, "--- Sending file: " + filesToUpload[i]);

            //String fileUriString = Uri.fromFile(new File(filesToUpload[i])).toString();
            try {
                mNetworHelper.uploadFile(mContext, filesToUpload[i], true);
            } catch (Exception e){

            }
            if (mRemoveAfterSent) {
                File file = new File(filesToUpload[i]);
                file.delete();
            }
        }
        Log.v(LOG_TAG, "Background task finished");
        return "Done";
    }

    @Override
    protected void onPostExecute(String result) { }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onProgressUpdate(Void... values) {}
}
