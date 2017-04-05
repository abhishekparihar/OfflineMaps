package com.abhi.offlinemaps.activity;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.skobbler.ngx.SKDeveloperKeyException;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.SKMapsInitSettings;
import com.skobbler.ngx.SKMapsInitializationListener;
import com.skobbler.ngx.SKStorageManager;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapVersioningListener;
import com.skobbler.ngx.versioning.SKVersioningManager;
import com.abhi.offlinemaps.R;
import com.abhi.offlinemaps.application.ApplicationPreferences;
import com.abhi.offlinemaps.application.MyApplication;
import com.abhi.offlinemaps.util.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
/**
 * Created by nishchay_s on 27-12-2016.
 */
/**
 * Activity that installs required resources (from assets/MapResources.zip) to
 * the device
 */
public class SplashActivity extends Activity implements SKMapsInitializationListener, SKMapVersioningListener {

    private static final String TAG = "SplashActivity";
    public static int newMapVersionDetected = 0;
    SKStorageManager skStorageManager;
    private boolean update = false;
    private long startLibInitTime;
    private File filePath;
    /**
     * flag that shows whether the debug kit is enabled or not
     */
    private boolean debugKitEnabled;

  // File to check required folder in the directory...

    String sourcePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/com.abhi.offlinemaps";
    File SourceFile = new File(sourcePath);
    String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/data/com.abhi.offlinemaps";
    File destinationFile = new File(destinationPath);
    String checkfile1 = Environment.getExternalStorageDirectory().getAbsolutePath()+"/com.abhi.offlinemaps/files/SKMaps/.Common";
    String checkfile2 = Environment.getExternalStorageDirectory().getAbsolutePath()+"/com.abhi.offlinemaps/files/SKMaps/.Routing";
    String checkfile3 = Environment.getExternalStorageDirectory().getAbsolutePath()+"/com.abhi.offlinemaps/files/SKMaps/.Shaders";
    File check1 = new File(checkfile1);
    File check2 = new File(checkfile2);
    File check3 = new File(checkfile3);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        filePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                +"/Android/data/com.abhi.offlinemaps/files/SKMaps/PreinstalledMaps/");
        if (destinationFile.exists()){
            skStorageManager = new SKStorageManager();
            SKLogging.enableLogs(true);
            boolean multipleMapSupport = false;
            try {
                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
                Bundle bundle = applicationInfo.metaData;
                multipleMapSupport = bundle.getBoolean("provideMultipleMapSupport");
               // debugKitEnabled = bundle.getBoolean(DebugKitConfig.ENABLE_DEBUG_KIT_KEY);
            } catch (PackageManager.NameNotFoundException e) {
              //  debugKitEnabled = false;
                e.printStackTrace();
            }
            if (multipleMapSupport) {
                SKMapSurfaceView.preserveGLContext = false;
                Utils.isMultipleMapSupportEnabled = true;
            }
            initializeMap();
        }
        else {
            new MyAsynk().execute();

        }
    }


    public class MyAsynk extends AsyncTask<File,Long,Boolean>{
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(SplashActivity.this);
            pDialog.setTitle("Please Wait");
            pDialog.setMessage(" Map is Initializing...");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(File... params) {
            checkFile();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Toast.makeText(SplashActivity.this, "Map has Initialized", Toast.LENGTH_SHORT).show();
            pDialog.dismiss();
            initializeMap();
            super.onPostExecute(aBoolean);
        }

    }

// Check and Copy logic

    private void checkFile() {
        if (!check1.exists()){
            Toast.makeText(getApplicationContext(),".Common File not found",Toast.LENGTH_LONG).show();
            finish();
        }
        else if (!check2.exists()){
            Toast.makeText(getApplicationContext(),".Routing File not found",Toast.LENGTH_LONG).show();
            finish();
        }
        else if (!check3.exists()){
            Toast.makeText(getApplicationContext(),".Shaders File not found",Toast.LENGTH_LONG).show();
            finish();
        }
        else {
            try {
                copyDirectoryToFolder(SourceFile, destinationFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initializeMap(){
        try {
            SKLogging.writeLog(TAG, "Initialize SKMaps", SKLogging.LOG_DEBUG);
            startLibInitTime = System.currentTimeMillis();
            checkForSDKUpdate();
            SKMapsInitSettings mapsInitSettings = new SKMapsInitSettings();
            mapsInitSettings.setMapResourcesPath(getExternalFilesDir(null)+"/SKMaps/");
            mapsInitSettings.setConnectivityMode(SKMaps.CONNECTIVITY_MODE_OFFLINE);
            mapsInitSettings.setPreinstalledMapsPath(filePath.toString());
            SKMaps.getInstance().initializeSKMaps(getApplication(), SplashActivity.this, mapsInitSettings);
        } catch (SKDeveloperKeyException exception) {
            exception.printStackTrace();
            Utils.showApiKeyErrorDialog(SplashActivity.this);
        }
    }

    @Override
    public void onLibraryInitialized(boolean isSuccessful) {
        SKLogging.writeLog(TAG, " SKMaps library initialized isSuccessful= " + isSuccessful + " time= " + (System.currentTimeMillis() - startLibInitTime), SKLogging.LOG_DEBUG);
        if (isSuccessful) {
            final MyApplication app = (MyApplication) getApplication();
            app.setMapCreatorFilePath(SKMaps.getInstance().getMapInitSettings().getMapResourcesPath() + "MapCreator/mapcreatorFile.json");
            app.setMapResourcesDirPath(SKMaps.getInstance().getMapInitSettings().getMapResourcesPath());
            copyOtherResources();
            prepareMapCreatorFile();
            SKVersioningManager.getInstance().setMapUpdateListener(this);

            goToMap();
        } else {
            //map was not initialized successfully
            finish();
        }
    }
    private void goToMap() {
        finish();
        //if (!debugKitEnabled) {
            startActivity(new Intent(this, MainActivity.class));
//        } else {
//            Intent intent = new Intent(this, DebugMapActivity.class);
//            intent.putExtra("mapResourcesPath", SKMaps.getInstance().getMapInitSettings().getMapResourcesPath());
//            startActivity(intent);
//        }
    }

    public void copyDirectoryToFolder(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }
            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectoryToFolder(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }
            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }
    /**
     * Copy some additional resources from assets
     */
    private void copyOtherResources() {
        final String mapResourcesDirPath = SKMaps.getInstance().getMapInitSettings().getMapResourcesPath();
        new Thread() {

            public void run() {
                try {
                    boolean resAlreadyExist;

                    String tracksPath = mapResourcesDirPath + "GPXTracks";
                    File tracksDir = new File(tracksPath);
                    resAlreadyExist = tracksDir.exists();
                    if (!resAlreadyExist || update) {
                        if (!resAlreadyExist) {
                            tracksDir.mkdirs();
                        }
                        Utils.copyAssetsToFolder(getAssets(), "GPXTracks", mapResourcesDirPath + "GPXTracks");
                    }

                    String imagesPath = mapResourcesDirPath + "images";
                    File imagesDir = new File(imagesPath);
                    resAlreadyExist = imagesDir.exists();
                    if (!resAlreadyExist || update) {
                        if (!resAlreadyExist) {
                            imagesDir.mkdirs();
                        }
                        Utils.copyAssetsToFolder(getAssets(), "images", mapResourcesDirPath + "images");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /**
     * Copies the map creator file and logFile from assets to a storage.
     */
    private void prepareMapCreatorFile() {
        final String mapResourcesDirPath = SKMaps.getInstance().getMapInitSettings().getMapResourcesPath();
        final MyApplication app = (MyApplication) getApplication();
        final Thread prepareGPXFileThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

                    boolean resAlreadyExist;

                    final String mapCreatorFolderPath = mapResourcesDirPath + "MapCreator";
                    // create the folder where you want to copy the json file
                    final File mapCreatorFolder = new File(mapCreatorFolderPath);

                    resAlreadyExist = mapCreatorFolder.exists();
                    if (!resAlreadyExist || update) {
                        if (!resAlreadyExist) {
                            mapCreatorFolder.mkdirs();
                        }
                        app.setMapCreatorFilePath(mapCreatorFolderPath + "/mapcreatorFile.json");
                        Utils.copyAsset(getAssets(), "MapCreator", mapCreatorFolderPath, "mapcreatorFile.json");
                    }

                    // Copies the log file from assets to a storage.
                    final String logFolderPath = mapResourcesDirPath + "logFile";
                    final File logFolder = new File(logFolderPath);
                    resAlreadyExist = logFolder.exists();
                    if (!resAlreadyExist || update) {
                        if (!resAlreadyExist) {
                            logFolder.mkdirs();
                        }
                        Utils.copyAsset(getAssets(), "logFile", logFolderPath, "Seattle.log");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        prepareGPXFileThread.start();
    }


    /**
     * Checks if the current version code is grater than the previous and performs an SDK update.
     */
    public void checkForSDKUpdate() {
        MyApplication appContext = (MyApplication) getApplication();
        int currentVersionCode = appContext.getAppPrefs().getIntPreference(ApplicationPreferences.CURRENT_VERSION_CODE);
        int versionCode = getVersionCode();
        if (currentVersionCode == 0) {
            appContext.getAppPrefs().setCurrentVersionCode(versionCode);
        }

        if (0 < currentVersionCode && currentVersionCode < versionCode) {
           SKMaps.getInstance().updateToLatestSDKVersion = true;
            appContext.getAppPrefs().setCurrentVersionCode(versionCode);
        }
    }

    /**
     * Returns the current version code
     *
     * @return
     */
    public int getVersionCode() {
        int v = 0;
        try {
            v = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return v;
    }

    @Override
    public void onNewVersionDetected(int i) {
        Log.e(""," New version = " + i);
        newMapVersionDetected = i;

    }

    @Override
    public void onMapVersionSet(int i) {

    }

    @Override
    public void onVersionFileDownloadTimeout() {

    }

    @Override
    public void onNoNewVersionDetected() {

    }
}
