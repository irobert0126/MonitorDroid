package com.example.Util.screenshot;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.SystemClock;
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

public class screenshotHelper {

    public static String pwd;
    public static Intent mIntent;
    private MediaProjection mMediaProjection;
    private static final String SCREENCAP_NAME = "screencap";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    int width, height;
    ImageReader mImageReader;

    @TargetApi(21)
    @UiThread
    public Bitmap takeScreenshot(@NonNull final Context context, boolean bitmap_only, String filename) {
        if (mIntent == null) {
            Log.e("tluo", "[error] Screen Capture Permission Needed.");
            return null;
        }
        final MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mIntent);

        final int density = context.getResources().getDisplayMetrics().densityDpi;
        final Display display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        mImageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1);
        final VirtualDisplay virtualDisplay = mMediaProjection.createVirtualDisplay(
                SCREENCAP_NAME, width, height, density, VIRTUAL_DISPLAY_FLAGS, mImageReader.getSurface(), null, null);
        SystemClock.sleep(1000);
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
            Log.d("tluo", "[error] Exception:" + e);
            if (bitmap != null)
                bitmap.recycle();
            e.printStackTrace();
        }
        if (image != null)
            image.close();
        mImageReader.close();
        mMediaProjection.stop();
        return bitmap;
    }

    public static String BitMapToString(Bitmap bitmap){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
            byte[] b = baos.toByteArray();
            String temp = Base64.encodeToString(b, Base64.DEFAULT);
            return temp;
        }catch (Exception e) {
            return null;
        }
    }

    public static void save_bitmap_to_file(Bitmap bitmap, String file_full_path) {
        try {
            File file = new File(file_full_path);
            Log.d("tluo", "[key] Save Bitmap To file (" + file + ")");
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