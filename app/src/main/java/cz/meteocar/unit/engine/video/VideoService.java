package cz.meteocar.unit.engine.video;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.VideoView;

import net.engio.mbassy.listener.Handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.meteocar.unit.engine.ServiceManager;
import cz.meteocar.unit.engine.clock.ClockService;
import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.FileSystem;

/**
 * Created by Toms, 2014.
 */
public class VideoService extends Thread {

    // view pro náhled videa
    private VideoView videoView;
    private Activity videoActivity;

    // je povolen start threadu?
    private boolean isEnabled = true;
    private boolean isRunning = false;

    // runtime proměnné
    private boolean canStartCapture = false;
    private boolean canStopCapture = false;
    private boolean isCapturingNow = false;
    private boolean shouldBeCapturingNow = false;
    private boolean noSleepingThisCycle = false;

    public VideoService(){

        // přihlášení k odběru eventů z event busu
        ServiceManager.getInstance().eventBus.subscribe(this);
    }

    /**
     * Povolí spuštění služby při incializaci View
     */
    public void enableService(){
        isEnabled = true;
    }

    // ---------- Nastavení VideView a aktivity  -------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Inicializuje view a spuští službu, pokud je povolena
     * - pokud není povolena, skryje View
     * @param vidView
     */
    public void startInView(VideoView vidView, Activity act){
        videoView = vidView;
        videoActivity = act;

        // povolit či skrýt
        if(isEnabled){
            start();
        }else{
            videoView.setVisibility(View.GONE);
        }
    }

    /**
     * Updatuje service při návratu na hlavní obrazovku
     * @param vidView
     * @param act
     */
    public void updateVideoView(VideoView vidView, Activity act){
        AppLog.i(AppLog.LOG_TAG_VIDEO, " >>> init video view");
        videoView = vidView;
        videoActivity = act;
        //restart();
    }

    // ---------- Video nahrávání a další --------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    public synchronized void saveAndContinue(){
        mMediaRecorder.stop();
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());
        try {
            mMediaRecorder.prepare();
        }catch(Exception e){}
        mMediaRecorder.start();

    }

    private int photoCount = 1;

    public void capture() {
        Camera.PictureCallback pictureCB = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, Camera cam) {
                //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                //       Environment.DIRECTORY_PICTURES), "MyCameraApp");

                File mediaStorageDir = new File(FileSystem.getPhotoDir());
                File picFile = new File(mediaStorageDir.getPath() + File.separator + "photo_" + (photoCount++) + ".jpg");

                try {
                    FileOutputStream fos = new FileOutputStream(picFile);
                    fos.write(data);
                    fos.close();
                    System.out.println("PHOTO SAVED: "+picFile.getName());
                } catch (FileNotFoundException e) {
                    //
                    e.printStackTrace();
                } catch (IOException e) {
                    //
                    e.printStackTrace();
                    e.printStackTrace();
                }
            }
        };
        mCamera.takePicture(null, null, pictureCB);
    }


    public synchronized void startVideoCapture(){
        if(!isCapturingNow) {
            canStartCapture = true;
            this.notifyAll();
            //MyActivity.setText("Nahrávej!");
            Log.i(null, "Nahrávej!");
        }else{
            canStopCapture = true;
            this.notifyAll();
            //MyActivity.setText("Zastavuji!");
            Log.i(null, "Zastav!");
        }
    }

    private synchronized void goStartCapture(){
        Log.i(null, "Chci začít nahrávat");

        //
        if (prepareVideoRecorder()) {
            Log.i(null, "Prepare ok!");
            AppLog.i(AppLog.LOG_TAG_VIDEO, "Prepare ok!");
            // Camera is available and unlocked, MediaRecorder is prepared,
            // now you can start recording
            try {
                Thread.sleep(200);} catch (InterruptedException e) {e.printStackTrace();}
            try{mMediaRecorder.start();}catch(Exception e){
                e.printStackTrace();
            }

            // inform the user that recording has started
            Log.i(null, "Nahrávám!");
            AppLog.i(AppLog.LOG_TAG_VIDEO, "Nahrávám!");
            //MyActivity.setButtonText("Stop!!!!!");
            isCapturingNow = true;
            //canStartCapture = false;
        } else {
            Log.i(null, "Nešlo připravit kameru :(");
            AppLog.i(AppLog.LOG_TAG_VIDEO, "Nešlo připravit kameru :(");
            releaseMediaRecorder();


            if(shouldBeCapturingNow){
                canStopCapture = true;
                canStartCapture = true;
                noSleepingThisCycle = true;
            }
        }
    }

    private synchronized void goStopCapture(){

        // stop recording and release camera
        mMediaRecorder.stop();  // stop the recording
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock();         // take camera access back from MediaRecorder

        // inform the user that recording has stopped
        AppLog.i(AppLog.LOG_TAG_VIDEO, "Zastaveno");
        //MyActivity.setButtonText("Nahrávej!");
        isCapturingNow = false;
    }

    private Camera mCamera;
    private MediaRecorder mMediaRecorder;

    private boolean prepareVideoRecorder(){

        // detailní log
        boolean log = false;

        if(log){ Log.i(null, "Prepare L0"); }
        int cameraID = findFrontFacingCamera();
        if(log){ Log.i(null, "Camera to use: " + cameraID); }
        //if(true) return false;

        // ------------------------------ Příprava kamery
        if(mCamera == null) {
            if (log) {
                Log.i(null, "Prepare L1");
            }
            mCamera = null;
            try {
                if (log) {
                    Log.i(null, "Prepare L2");
                }
                mCamera = Camera.open(cameraID); // attempt to get a Camera instance
            } catch (Exception e) {
                if (log) {
                    Log.i(null, "Prepare L3");
                }
                e.printStackTrace();
                AppLog.i(AppLog.LOG_TAG_VIDEO, "Nemám kameru :(");
                return false;
            }

            if (mCamera == null) {
                if (log) {
                    Log.i(null, "Prepare L3A - camera null!");
                }
                return false;
            }
        }

        // změníme rychle orientaci než se začne něco dít
        setCameraDisplayOrientation(videoActivity, cameraID, mCamera);

        if(log){ Log.i(null, "Prepare L4"); }
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        if(log){ Log.i(null, "Prepare L5"); }
        try {
            mCamera.unlock();
        }catch(Exception e){
            if(log){ Log.i(null, "Prepare L5 exception unlock"); }
            e.printStackTrace();
        }
        if(log){ Log.i(null, "Prepare L5A"); }
        mMediaRecorder.setCamera(mCamera);
        if(log){ Log.i(null, "Prepare L5B"); }

        // Step 2: Set sources
        if(log){ Log.i(null, "Prepare L6"); }
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        if(log){ Log.i(null, "Prepare L7"); }
        mMediaRecorder.setProfile(CamcorderProfile.get(cameraID, CamcorderProfile.QUALITY_HIGH));

        // Step 4: Set output file
        if(log){ Log.i(null, "Prepare L8"); }
        mMediaRecorder.setOutputFile(getOutputMediaFile().toString());

        //mMediaRecorder.setVideoFrameRate(20); // set to 20
        //mMediaRecorder.setVideoSize(400, 300);

        // Step 5: Set the preview output
        //mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());
        if(log){ Log.i(null, "Prepare L9"); }
        //mMediaRecorder.setPreviewDisplay(MyActivity.videoView.getHolder().getSurface());
        try {
            mMediaRecorder.setPreviewDisplay(videoView.getHolder().getSurface());
        }catch(Exception e){
            if(log){ Log.i(null, "Error while setting video view: "); }
            e.printStackTrace();
        }

        // stišíme pípnutí na začátku
        // - aby nepípalo každou minutu při spuštění nahrávání
        AudioManager am = (AudioManager) videoActivity.getSystemService(Context.AUDIO_SERVICE);
        am.setStreamMute(AudioManager.STREAM_MUSIC, true);
        am.setStreamMute(AudioManager.STREAM_SYSTEM, true);

        // omezíme délku na minutu (60s = 60000ms)
        //mMediaRecorder.setMaxDuration(60000);

        // pořešíme otočení
        //
        /*WindowManager windowManager =
                (WindowManager) MyActivity.myActivity.getBaseContext().getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        if(rotation == Surface.ROTATION_0){rotation = 0;}
        if(rotation == Surface.ROTATION_90){rotation = 90;}
        if(rotation == Surface.ROTATION_180){rotation = 180;}
        if(rotation == Surface.ROTATION_270){rotation = 270;}
        Log.i(null, "Prepare display rotation: "+rotation);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, cameraInfo);
        Log.i(null, "Prepare camera rotation: "+cameraInfo.orientation);

        int newOrientation = (cameraInfo.orientation - rotation)%360;
        //int newOrientation = 180;
        Log.i(null, "Prepare new rotation: "+newOrientation);
        mMediaRecorder.setOrientationHint(newOrientation);
        try {
            mCamera.stopPreview();
            mCamera.setDisplayOrientation(newOrientation);
            mCamera.startPreview();
        }catch (Exception e){
            Log.i(null, "Chyba při otáčení dipleje");
            e.printStackTrace();
            return false;
        }
        mMediaRecorder.setPreviewDisplay(MyActivity.videoView.getHolder().getSurface());*/

        // Step 6: Prepare configured MediaRecorder
        if(log){ Log.i(null, "Prepare L10"); }
        try {
            if(log){ Log.i(null, "Prepare L11"); }
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            if(log){ Log.i(null, "Prepare L12"); }
            if(log){ Log.d(null, "XE - IllegalStateException preparing MediaRecorder: " + e.getMessage()); }
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            if(log){ Log.i(null, "Prepare L13"); }
            if(log){ Log.d(null, "XE - IOException preparing MediaRecorder: " + e.getMessage()); }
            releaseMediaRecorder();
            return false;
        }

        if(log){ Log.i(null, "Prepare L14"); }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private int findFrontFacingCamera() {
        Log.i(null, "C1");
        int cameraId = -1;
        Log.i(null, "C2");
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        Log.i(null, "C3");
        Log.i(null, "Number of cameras: " + numberOfCameras);
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            Log.i(null, "Camera info: " + info.toString());
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.i(null, "Camera found");
                cameraId = i;
                break;
            }
        }
        Log.i(null, "CX");
        return cameraId;
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera){

        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    // ---------- Soubory - ----------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    private static final int MAX_FILES = 30;

    private static void cleanupVideoDir(){

        // adresář s videi
        File mediaStorageDir = new File(FileSystem.getVideoDir());

        // načteme soubory
        File[] files = mediaStorageDir.listFiles();

        // pokud máme méně videí než limit, nic neděláme
        if(files.length < MAX_FILES){ return; }

        // máme moc, videí najdeme nejstarší
        File oldest = null;

        //
        for (File file : files) {
            if (!file.isDirectory()) {

                // inic nejstarší
                if(oldest == null){
                    oldest = file; continue;
                }

                // update nejstaršího
                if(file.lastModified() < oldest.lastModified()){
                    oldest = file;
                }
            }
        }

        // smažeme nejstarší
        oldest.delete();
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(){

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
        //        Environment.DIRECTORY_PICTURES), "MyCameraApp");

        File mediaStorageDir = new File(FileSystem.getVideoDir());

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_"+ timeStamp + ".mp4");
       /* if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }*/

        // promažeme adresář
        cleanupVideoDir();

        return mediaFile;
    }

    // ---------- Časování  ----------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    private int time = 0;
    private static final int TIME_PER_FILE = 60;

    /**
     * Zaznamená příchozí čas
     * @param evt
     */
    @Handler
    public void handleClockEvent(ClockService.TimeEvent evt){
        time++;
        //AppLog.i(AppLog.LOG_TAG_VIDEO, "video time: "+time);
        if(time >= TIME_PER_FILE){
            restart();
        }
    }

    public synchronized  void restart(){
        if(isRunning) {
            time = 0;
            canStopCapture = true;
            canStartCapture = true;
            this.notifyAll();
        }
    }

    // ---------- Start a loop -------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    @Override
    public synchronized void start() {

        //
        if(!isEnabled){return;}

        //
        if(shouldBeCapturingNow){return;}
        shouldBeCapturingNow = true;

        AppLog.i(AppLog.LOG_TAG_VIDEO, "Video start REQ ");
        if(!isRunning){
            AppLog.i(AppLog.LOG_TAG_VIDEO, "Video START <<<<<<<<<<<<<<<");
            isRunning = true;
            time = 0;
            super.start();
        }  else{

            // povolíme nahrávání a vzbudíme
            canStartCapture = true;
            canStopCapture = false;
            this.notifyAll();
        }
    }

    public synchronized void stopVideo(){

        if(!shouldBeCapturingNow){return;}

        // povolíme nahrávání a vzbudíme
        shouldBeCapturingNow = false;
        canStartCapture = false;
        canStopCapture = true;
        this.notifyAll();
    }

    @Override
    public synchronized void run(){

        AppLog.i(AppLog.LOG_TAG_VIDEO, "Entering loop!");

        // připravíme službu k nahrávání videa - flagy jsou jiné než při restartu
        canStopCapture = false;
        canStartCapture = true;


        try {
            while (true) {
                Log.i(null, "Začíná cyklus");
                noSleepingThisCycle = false;

                // zastaví
                if(canStopCapture){
                    Log.i(null, "Jdu zastavovat");
                    goStopCapture();
                    canStopCapture = false;
                    //continue;
                }

                // začne nahrávat
                if(canStartCapture){
                    Log.i(null, "Jdu nahrávat");
                    goStartCapture();
                    canStartCapture = false;
                    //continue;
                }

                // zákaz spánku
                if(noSleepingThisCycle){
                    continue;
                }

                // nikdo nic nechtěl, jde se spát
                Log.i(null, "Jdu spát");
                this.wait();

                Log.i(null, "Končí cyklus");
            }
        }catch(Exception ex){
            //
        }
    }

}
