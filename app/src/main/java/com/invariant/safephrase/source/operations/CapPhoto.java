package com.invariant.safephrase.source.operations;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.invariant.safephrase.GUI.home.Home;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * HAndles taking pictures in the background.
 */
public class CapPhoto extends Service{


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** Whether to use the back or the front of the camera */
    private boolean use_back;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        /* Make sure we have permissions to take a picture, otherwise, don't bother */
        if(checkPermissions()){
            use_back = intent.getBooleanExtra("use_back", true);

            /* Take the picture */
            takePicture(this);
        }

        return super.onStartCommand(intent, flags, startId);
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    /**
     * Take a picture of whatever the camera is currently seeing.
     *
     * @param context the context of this service
     */
    @SuppressWarnings("deprecation")
    private void takePicture(final Context context) {

        /* Initialize the surface on which the picture will be taken */
        final SurfaceView preview = new SurfaceView(context);
        SurfaceHolder holder = preview.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /* Display the camera's view (what camera is seeing) in an invisible window */
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.UNKNOWN);
        wm.addView(preview, params);

        /* Initialize the camera to use the surface created and to take the picture with it */
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override public void surfaceCreated(SurfaceHolder holder) { initializeCamera(holder); }
            @Override public void surfaceDestroyed(SurfaceHolder holder) {}
            @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        });
    }

    /**
     * Initializes the camera using the given SurfaceHolder and then takes a picture.
     *
     * @param holder the holder the camera will use to display what it is seeing
     */
    private void initializeCamera(SurfaceHolder holder){

        Camera camera = null;

        try {

            /* Set the camera to the back or front camera based on what the user requested */
            camera = use_back ? Camera.open() :  Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);

            camera.setPreviewDisplay(holder);

            camera.startPreview();
            SystemClock.sleep(1000);

            /* Take the picture with the given initialized camera */
            camera.takePicture(null, null, this::finalizePicture);
        } catch (Exception e) {
            if (camera != null) camera.release();
        }
    }

    /**
     * Takes the picture with the given camera and then saves the data.
     *
     * @param data the data to save once the picture is taken
     * @param camera the camera to take the picture with
     */
    private void finalizePicture(byte[] data, Camera camera){
        Log.i("COS", "PICTURE TAKEN");
        FileOutputStream outStream;
        try{
            File image_file_path = getOutputMediaFile();

            outStream = new FileOutputStream(image_file_path.toString());
            Bitmap bitmap = BitmapFactory.decodeByteArray(data , 0, data.length);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();


            /* Display the picture in the gallery */
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(image_file_path);
            mediaScanIntent.setData(contentUri);
            getApplicationContext().sendBroadcast(mediaScanIntent);

            Log.i("COS", "PICTURE SAVED");
        } catch (Exception e) { Log.d("COS", e.getMessage()); }

        camera.release();
        stopSelf();
    }

    /**
     * Request audio permissions if there aren't any.
     */
    private boolean checkPermissions(){
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Intent i = new Intent(this, Home.class);
            i.putExtra("PERMISSION", "CAMERA");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(i);
            stopSelf();
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)){
                Intent i = new Intent(this, Home.class);
                Log.i("COS", "NO PERMISSION");
                i.putExtra("PERMISSION", "SYSTEM_ALERT_WINDOW");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(i);
                stopSelf();
                return false;
            }
        }

        return true;
    }


    /**
     * Create a file to save the picture in.
     */
    private static File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SafePhrase");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                Log.d("COS", "failed to create directory");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
    }

    @Override public IBinder onBind(Intent intent) { return null; }
}
