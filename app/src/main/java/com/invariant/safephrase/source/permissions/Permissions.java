package com.invariant.safephrase.source.permissions;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

/**
 * Provides permissions requests from the android OS.
 */
public class Permissions {


    /* ************************************************************************* *
     *                                                                           *
     * Static Methods                                                            *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Request permissions to draw this app's elements over other apps.
     *
     * @param activity the activity requesting the permission
     */
    public static void requestDrawOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!android.provider.Settings.canDrawOverlays(activity.getApplicationContext())) {
                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, 1705);
            }
        }
    }

    /**
     * Request permissions to record audio.
     *
     * @param activity the activity requesting the permission
     */
    public static void requestRecordAudioPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.RECORD_AUDIO}, 1700);
    }

    /**
     * Request permissions to use the camera.
     *
     * @param activity the activity requesting the permission
     */
    public static void requestCameraPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, 1701);
    }

    /**
     * Request permissions to make calls.
     *
     * @param activity the activity requesting the permission
     */
    public static void requestPhonePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CALL_PHONE}, 1702);
    }

    /**
     * Request permissions to write storage.
     *
     * @param activity the activity requesting the permission
     */
    public static void requestWritePermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1703);
    }

    /**
     * Request all required permissions.
     *
     * @param activity the activity requesting the permission
     */
    public static void requestAllPermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.RECORD_AUDIO
                    }, 1704);
    }


}
