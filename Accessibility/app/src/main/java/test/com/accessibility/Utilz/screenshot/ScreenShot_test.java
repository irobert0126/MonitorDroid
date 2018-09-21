package test.com.accessibility.Utilz.screenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class ScreenShot_test {
    public static String pwd;
    public static Intent mIntent;
    private static MediaProjection mMediaProjection = null;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static int width, height, density;
    private static ImageReader mImageReader = null;

    @UiThread
    public String takeScreenshot(@NonNull final Context context, boolean bitmap_only, String filename) {
        if (mIntent == null) {
            Log.e("tluo-plugin", "[error] Screen Capture Permission Needed.");
            return null;
        }
        if (mMediaProjection== null) {
            final MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mMediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mIntent);
        }
        if (mImageReader == null) {
            density = context.getResources().getDisplayMetrics().densityDpi;
            final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            final Point size = new Point();
            display.getSize(size);
            width = size.x;
            height = size.y;
            mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        }
        final VirtualDisplay virtualDisplay = mMediaProjection.createVirtualDisplay(
               SCREENCAP_NAME + System.currentTimeMillis(), width, height,
                density, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, null);
        Image image = null;
        Bitmap bitmap = null;
        try {
            image = mImageReader.acquireLatestImage();
            if (image != null) {
                Image.Plane[] planes = image.getPlanes();
                ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride(), rowStride = planes[0].getRowStride(), rowPadding = rowStride - pixelStride * width;
                bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                if(bitmap_only == false) {
                    save_bitmap_to_file(bitmap, filename);
                }
            }
        } catch (Exception e) {
            Log.d("tluo-plugin", "[error] Exception:" + e);
            if (bitmap != null)
                bitmap.recycle();
            e.printStackTrace();
        }
        String bp_str= BitMapToString(bitmap);
        if (image != null)
            image.close();
        //mImageReader.close();
        //mMediaProjection.stop();
        return bp_str;
    }

    public static String BitMapToString(Bitmap bitmap){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
            byte[] b = baos.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            return temp;
        } catch (Exception e){
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
