package test.com.accessibility.util.screenshot;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.nio.ByteBuffer;


public class screenshot {
    public static final int REQUEST_MEDIA_PROJECTION = 18;
    public static Intent project_token = null;
    private static MediaProjection projection_media = null;
    private static ImageReader mImageReader;
    private static int maxImageinReader = 25;
    private static int imageCounter = 0;

    public static void init(@NonNull Activity activity){
        requestMediaProjPermission(activity);
    }

    public static void setToken(int resultCode, Intent token, @NonNull Activity activity){
        project_token = token;
    }

    public static void requestMediaProjPermission(@NonNull Activity activity) {
        MediaProjectionManager projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        activity.startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
    }

    @UiThread
    public static String capture_core(@NonNull Context activity, Intent token) {
        final int density = activity.getResources().getDisplayMetrics().densityDpi;
        Point size = getScreenSize(activity);
        String SCREENCAP_NAME = "MainScreen";// + System.currentTimeMillis();
        if (projection_media == null) {
            MediaProjectionManager projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            projection_media = projectionManager.getMediaProjection(Activity.RESULT_OK, token);
        }
        if(imageCounter == maxImageinReader - 1) {
            Log.d("tluo", "Close Image.close ... ");
            mImageReader.close();
            mImageReader = null;
            imageCounter = 0;
        }
        if (mImageReader == null) {
            mImageReader = ImageReader.newInstance(size.x, size.y, PixelFormat.RGBA_8888, maxImageinReader);
            projection_media.stop();
            MediaProjectionManager projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            projection_media = projectionManager.getMediaProjection(Activity.RESULT_OK, token);
            projection_media.createVirtualDisplay(
                    SCREENCAP_NAME, size.x, size.y, density,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR | DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                    mImageReader.getSurface(), null, null);
        }
        Image image = null;
        Bitmap bitmap = null;
        try {
            while (image == null) {
                image = mImageReader.acquireNextImage();
            }
            imageCounter += 1;

            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride(), rowStride = planes[0].getRowStride(), rowPadding = rowStride - pixelStride * size.x;
            bitmap = Bitmap.createBitmap(size.x + rowPadding / pixelStride, size.y, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
        } catch (Exception e) {
            Log.d("tluo", "[error-screen-shot] Exception:" + e);
            if (bitmap != null)
                bitmap.recycle();
            mImageReader.close();
            mImageReader = null;
            e.printStackTrace();
        }
        return bitmap_helper.BitMapToString(bitmap);
    }

    public static Point getScreenSize(@NonNull Context activity) {
        final Display display = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);
        return size;
    }
}
