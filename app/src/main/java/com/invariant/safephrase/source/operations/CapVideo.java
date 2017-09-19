package com.invariant.safephrase.source.operations;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles taking videos in the background.
 */
public class CapVideo extends Service {


    /* ************************************************************************* *
     *                                                                           *
     * Instance Variables                                                        *
     *                                                                           *
     * ************************************************************************  */

    /** The camera that will be used to record the video */
    Camera camera = null;

    /** The recorder that will record the video */
    MediaRecorder media_recorder = null;

    /** the path to which the video should be saved */
    File video_file_path = null;


    /* ************************************************************************* *
     *                                                                           *
     * Constructors                                                              *
     *                                                                           *
     * ************************************************************************  */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /* Make sure we have permissions to record a video, otherwise, don't bother */
        if(checkPermissions()) {

            /* Video status tells us wether to start recording or stop recording (true = start, false = stop) */
            boolean video_status = intent.getBooleanExtra("video_status", true);

            if (video_status) {
                Log.i("COS", "Starting to record video");
                takeVideo(getApplicationContext());
            } else {
                Log.i("COS", "Stopping currently recording video (if there is one)");
                stopVideo();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    /* ************************************************************************* *
     *                                                                           *
     * Instance Methods                                                          *
     *                                                                           *
     * ************************************************************************  */

    @SuppressWarnings("deprecation")
    public void takeVideo(Context context){

        /* Initialize the surface on which the video will be taken */
        final SurfaceView preview = new SurfaceView(context);
        SurfaceHolder holder = preview.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        /* Display the camera's view (what camera is seeing) in an invisible window */
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, 0, PixelFormat.UNKNOWN);
        wm.addView(preview, params);

        /* Initialize the camera to use the surface created and to take the picture with it */
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override public void surfaceCreated(SurfaceHolder holder) { startVideo(holder); }
            @Override public void surfaceDestroyed(SurfaceHolder holder) {}
            @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
        });
    }

    /**
     * Initializes the camera for video and starts recording.
     *
     * @param holder the surface on which the video will be displayed
     */
    public void startVideo(SurfaceHolder holder){
        try {
            camera = Camera.open();
            camera.unlock();
            media_recorder = new MediaRecorder();
            media_recorder.setCamera(camera);
            media_recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            media_recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            media_recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);

            /* Get the path to save the video to, if it is null, then don't bother recording */
            video_file_path = getOutputMediaFile();
            if(video_file_path == null) return;

            media_recorder.setOutputFile(video_file_path.toString());
            media_recorder.setPreviewDisplay(holder.getSurface());

            media_recorder.prepare();
            SystemClock.sleep(1000);
            media_recorder.start();

        } catch (Exception e) {

            Log.i("COS", "PROBLEM OCCURED IN STARTING VIDEO: " + e.toString());

            if(media_recorder != null) {
                media_recorder.release();
            }
            if (camera != null) {
                camera.release();
            }
        }
    }

    /**
     * Stops the currently recording video if there is one.
     */
    public void stopVideo(){
        if(media_recorder != null) {
            Log.i("COS", "STOPPING VIDEO");
            media_recorder.stop();

            /* Display video in gallery */
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(video_file_path);
            mediaScanIntent.setData(contentUri);
            getApplicationContext().sendBroadcast(mediaScanIntent);

            media_recorder.release();
        }
        if (camera != null) {
            camera.release();
        }
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
     * Create a file to save the video in.
     */
    private static File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "SafePhrase");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("COS", "Failed to create directory for video.");
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return new File(mediaStorageDir.getPath() + File.separator + "VID_"+ timeStamp + ".mp4");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
