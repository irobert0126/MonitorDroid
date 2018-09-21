package test.com.accessibility.util.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class bitmap_helper {
    public static String BitMapToString(Bitmap bitmap){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, baos);
            byte[] b = baos.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            return temp;
        } catch (Exception e){
            Log.e("tluo", "bitmap_helper::BitMapToString" + e);
            return null;
        }
    }

    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte= Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static void save_bitmap_to_file(Bitmap bitmap, String file_full_path) {
        try {
            File file = new File(file_full_path);
            Log.d("tluo-plugin", "[key] Save Bitmap To file (" + file + ")");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.d("tluo", "[error] save_bitmap_to_file Exception:" + e);
            if (bitmap != null)
                bitmap.recycle();
            e.printStackTrace();
        }
    }
}
