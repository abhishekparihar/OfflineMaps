package com.abhi.offlinemaps.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKBoundingBox;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapFragment;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapSettings;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.map.realreach.SKRealReachListener;
import com.skobbler.ngx.navigation.SKAdvisorSettings;
import com.skobbler.ngx.navigation.SKNavigationListener;
import com.skobbler.ngx.navigation.SKNavigationManager;
import com.skobbler.ngx.navigation.SKNavigationSettings;
import com.skobbler.ngx.navigation.SKNavigationState;
import com.skobbler.ngx.poitracker.SKDetectedPOI;
import com.skobbler.ngx.poitracker.SKPOITrackerListener;
import com.skobbler.ngx.poitracker.SKTrackablePOIType;
import com.skobbler.ngx.positioner.SKCurrentPositionListener;
import com.skobbler.ngx.positioner.SKCurrentPositionProvider;
import com.skobbler.ngx.positioner.SKPosition;
import com.skobbler.ngx.positioner.SKPositionerManager;
import com.skobbler.ngx.reversegeocode.SKReverseGeocoderManager;
import com.skobbler.ngx.routing.SKRouteInfo;
import com.skobbler.ngx.routing.SKRouteJsonAnswer;
import com.skobbler.ngx.routing.SKRouteListener;
import com.skobbler.ngx.routing.SKRouteManager;
import com.skobbler.ngx.routing.SKRouteSettings;
import com.skobbler.ngx.routing.SKViaPoint;
import com.skobbler.ngx.search.SKSearchListener;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.ngx.util.SKLogging;
import com.skobbler.ngx.versioning.SKMapVersioningListener;
import com.abhi.offlinemaps.R;
import com.abhi.offlinemaps.application.MyApplication;
import com.abhi.offlinemaps.sdktools.SKToolsNavigationListener;
import com.abhi.offlinemaps.sdktools.SKToolsNavigationManager;
import com.abhi.offlinemaps.sdktools.SKToolsNavigationUIManager;
import com.abhi.offlinemaps.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Created by nishchay_s on 28-12-2016.
 */
public class MainActivity extends Activity implements SKMapSurfaceListener,SKRouteListener,SKNavigationListener, SKRealReachListener, SKPOITrackerListener, SKCurrentPositionListener, SensorEventListener,
        SKMapVersioningListener, SKToolsNavigationListener,SKSearchListener {

    private static final String TAG = "MainActivity";
    private int AnimationDuration=1000;
    private float Accuracy=1;
    private boolean center = true;
    public boolean navigationInProgress;
    private boolean reRoutingInProgress = false;
    private boolean fromSearch = false;
    private long currentPositionTime;
    Button btn_showRoute,btn_search;
    private static final byte GREEN_PIN_ICON_ID = 0;
    private ViewGroup compassViewPanel;
    private static final byte RED_PIN_ICON_ID = 1;
    public static final byte VIA_POINT_ICON_ID = 4;
    Button btn_currentLocation;
    public SKToolsNavigationManager navigationManager;
    private ProgressDialog pDialog;
    private boolean isStartPointBtnPressed = false, isEndPointBtnPressed = false, isViaPointSelected = false;
    private DrawerLayout drawerLayout;
    TextView textView;
    private Toolbar toolbar;
    String string;

    public TextView voice_text;

//    @Override
//    public void onClick(View v) {
//        switch (compassStates) {
//            case PEDESTRIAN_COMPASS:
//                compassStates = CompassStates.NORTH_ORIENTED;
//                Toast.makeText(this, "The map will not turn. It will always stay northbound.", Toast.LENGTH_SHORT).show();
//                compassPanelImageView.setBackgroundResource(com.skobbler.ngx.R.drawable.icon_north_oriented);
//                mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROUTE);
//                break;
//            case NORTH_ORIENTED:
//                compassStates = CompassStates.HISTORICAL_POSITIONS;
//                Toast.makeText(this, "Map will Rotate in direction od Driving", Toast.LENGTH_LONG).show();
//                compassPanelImageView.setBackgroundResource(com.skobbler.ngx.R.drawable.icon_compass);
//                mapView.getMapSettings().setCompassShown(true);
//                mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.HISTORIC_POSITIONS);
//                mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROUTE);
//                startOrientationSensor();
//                break;
//        }
//
//    }

    private enum MapAdvices {
        TEXT_TO_SPEECH
    }
    private ViewGroup backButtonPanel;
    private ViewGroup searchingForGPSPanel;
    private boolean skToolsNavigationInProgress;
    private boolean skToolsRouteCalculated;
    private byte numberOfConsecutiveBadPositionReceivedDuringNavi;
    public static final int progress_bar_type = 0;
    private enum CompassStates {
        HISTORICAL_POSITIONS, PEDESTRIAN_COMPASS, NORTH_ORIENTED
    }
    private CompassStates compassStates = CompassStates.PEDESTRIAN_COMPASS;
    private SKToolsNavigationUIManager skToolsNavigationUIManager;
    public Integer cachedRouteId;
    Button btn_navigate;
    // GpsTracker gpsTracker;
    StringBuilder stringBuilder = new StringBuilder();
    SKMapSettings skMapSettings ;
    //    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;
    protected PowerManager.WakeLock mWakeLock;
    private boolean headingOn;
    double latitude, longitude;
    public SKCoordinate startPoint;
    public ImageView compassPanelImageView;
    public SKCoordinate destinationPoint;
    private SKViaPoint viaPoint;
    public MyApplication app;
    //    double longitude;
    public TextToSpeech textToSpeechEngine;
    public SKMapSurfaceView mapView;
    public SKPosition currentPosition;
    private float[] orientationValues;
    public AlertDialog.Builder alert;
    public int ZoomLevel=15;
    double Lat;
    double Lng;
    String Place_Name;
    private AlertDialog.Builder builder;
    public SKCurrentPositionProvider currentPositionProvider;
    private List<Integer> routeIds = new ArrayList<Integer>();

    @Override
    public void onReceivedSearchResults(List<SKSearchResult> list) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        currentPositionProvider = new SKCurrentPositionProvider(MainActivity.this);
        btn_search = (Button) findViewById(R.id.buttonSearch);
        btn_currentLocation = (Button) findViewById(R.id.currentLocation);
        voice_text = (TextView) findViewById(R.id.voice_textview);
        //compassPanelImageView  = (ImageView) findViewById(R.id.pedestrian_compass_panel_image_view);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        btn_navigate = (Button) findViewById(R.id.downloadPlace);
        btn_navigate.setTag(1);
        btn_navigate.setText("Start Navigation");
        app = (MyApplication) getApplication();
        builder = new AlertDialog.Builder(this);
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean enable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enable) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("GPS Settings");
            builder.setMessage("GPS is not enabled. Please enable GPS");
            builder.setInverseBackgroundForced(true);
            builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            AlertDialog alert = builder.create();
            alert.setCanceledOnTouchOutside(false);
            alert.show();
        }
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };


        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        final SKMapFragment mapFragment = (SKMapFragment) getFragmentManager().findFragmentById(R.id.mapfragment);
        mapFragment.initialise();
        mapFragment.setMapSurfaceListener(this);
        // compassPanelImageView.setOnClickListener( MainActivity.this);
        // Navigation Option with Text To Speech...
        btn_navigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if(((Button)v).getText().equals("Start Navigation")){
                    builder.setTitle("Start Navigation!!");
                    builder.setMessage("Do you want to Start Navigation?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Button)v).setText("Stop Navigation");
                            //btn_showRoute.setVisibility(View.GONE);
                            btn_search.setVisibility(View.GONE);
                            btn_currentLocation.setVisibility(View.GONE);
                            if (textToSpeechEngine == null){
                                textToSpeechEngine = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
                                    @Override
                                    public void onInit(int status) {
                                        if (status == TextToSpeech.SUCCESS) {
                                            int result = textToSpeechEngine.setLanguage(Locale.US);
                                            if (result == TextToSpeech.LANG_MISSING_DATA || result ==
                                                    TextToSpeech.LANG_NOT_SUPPORTED) {
                                                Toast.makeText(MainActivity.this,
                                                        "This Language is not supported",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        } else {
                                            Toast.makeText(MainActivity.this, getString(R.string.text_to_speech_engine_not_initialized),
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                        StartVoiceNavigation(MapAdvices.TEXT_TO_SPEECH);
                                    }
                                });
                            }
                            launchNavigation();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mapView.getMapSettings().setCompassShown(false);
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                else {
                    builder.setTitle("Stop Navigation!!");
                    builder.setMessage("Do you want to exit Navigation?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            stopNavigation();
                            textToSpeechEngine.stop();
                            SKRouteManager.getInstance().clearCurrentRoute();
                            ((Button)v).setText("Start Navigation");
                            btn_currentLocation.setVisibility(View.VISIBLE);
                            btn_search.setVisibility(View.VISIBLE);

                            //btn_showRoute.setVisibility(View.VISIBLE);
                            voice_text.setText(null);
                            stringBuilder = new StringBuilder();
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    clearRouteFromCache();
                    AlertDialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            }
        });
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchPlaceActivity.class);
                startActivityForResult(intent, 100);
                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
            }
        });
        btn_currentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (gpsTracker.canGetLocation()) {
//                    latitude = gpsTracker.getLatitude();
//                    longitude = gpsTracker.getLongitude();
//                    mapView.centerOnCurrentPosition(17, true, 500);
//                }
                if (mapView != null && currentPosition != null) {
                    //setHeading(false);

                    //mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.NONE);
                    mapView.centerOnCurrentPosition(17, true, 500);
                    mapView.getMapSettings().setOrientationIndicatorType(
                            SKMapSurfaceView.SKOrientationIndicatorType.DEFAULT);
                    mapView.getMapSettings()
                            .setHeadingMode(SKMapSettings.SKHeadingMode.NONE);
                }
                else {
                    Toast.makeText(MainActivity.this, "Current position not avaliable ", Toast.LENGTH_SHORT)
                            .show();
                }

                //mapView.animateToLocation(new SKCoordinate(latitude,longitude),1000);
//                mapView.getMapSettings().setCurrentPositionShown(true);
//                mapView.getMapSettings().setMapZoomingEnabled(true);
//                mapView.animateToZoomLevel((float)18.999001);
//                SKAnnotation annotationWithTextureId = new SKAnnotation(10);
//                annotationWithTextureId.setLocation(new SKCoordinate(latitude,longitude));
//                annotationWithTextureId.setMininumZoomLevel(10);
//                //annotationWithTextureId.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
//                mapView.centerOnCurrentPosition(17, true, 500);
//                mapView.addAnnotation(annotationWithTextureId, SKAnimationSettings.ANIMATION_NONE);
                currentPositionProvider.setCurrentPositionListener(MainActivity.this);
                currentPositionProvider.requestLocationUpdates(Utils.hasGpsModule(MainActivity.this),
                        Utils.hasNetworkModule(MainActivity.this), false);


            }
        });
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            Lat = bundle.getDouble("latitude");
            Lng = bundle.getDouble("longitude");
            Place_Name = bundle.getString("placeName");
            if (bundle.getBoolean("fromSearch", false)) {
                fromSearch = true;
            }
        }
        if (fromSearch) {
            setSearchedLocation();
            fromSearch = false;
        }
    }

    private void StartVoiceNavigation(MapAdvices currentMapAdvices) {
        final SKAdvisorSettings advisorSettings = new SKAdvisorSettings();
        advisorSettings.setLanguage(SKAdvisorSettings.SKAdvisorLanguage.LANGUAGE_EN);
        advisorSettings.setAdvisorConfigPath(app.getMapResourcesDirPath() + "/Advisor");
        advisorSettings.setResourcePath(app.getMapResourcesDirPath() + "/Advisor/Languages");
        advisorSettings.setAdvisorVoice("en");
        switch (currentMapAdvices){
            case TEXT_TO_SPEECH:
                advisorSettings.setAdvisorType(SKAdvisorSettings.SKAdvisorType.TEXT_TO_SPEECH);
                break;
        }
        SKRouteManager.getInstance().setAdvisorSettings(advisorSettings);
        launchNavigation();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(data != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                Lat = bundle.getDouble("latitude");
                Lng = bundle.getDouble("longitude");
                Place_Name = bundle.getString("placeName");
                SKRouteManager.getInstance().clearCurrentRoute();
                if (bundle.getBoolean("fromSearch", false)) {
                    fromSearch = true;
                }
            }
            if(fromSearch){
                setSearchedLocation();
                fromSearch = false;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void stopNavigation() {
        navigationInProgress = false;
        routeIds.clear();
        if (textToSpeechEngine != null && !textToSpeechEngine.isSpeaking()) {
            textToSpeechEngine.stop();
        }
        if ( TrackElementsActivity.selectedTrackElement !=
                null) {
            SKRouteManager.getInstance().clearCurrentRoute();
            mapView.drawTrackElement(TrackElementsActivity.selectedTrackElement);
            mapView.fitTrackElementInView(TrackElementsActivity.selectedTrackElement, false);

            SKRouteManager.getInstance().setRouteListener(this);
            SKRouteManager.getInstance().createRouteFromTrackElement(
                    TrackElementsActivity.selectedTrackElement, SKRouteSettings.SKRouteMode.BUS_FASTEST, true, true,
                    false);
        }

        SKNavigationManager.getInstance().stopNavigation();
    }
    // To clear route,Stop Navigation & to Terminate App....
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle("Really quit? ");
        alert.setMessage("Do you really want to exit the app?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
        alert.setNegativeButton("Cancel", null);
        Dialog dialog = alert.show();
        dialog.setCanceledOnTouchOutside(false);
    }


    public void clearRouteFromCache() {
        SKRouteManager.getInstance().clearAllRoutesFromCache();
        cachedRouteId = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mapView!=null)
            mapView.onPause();
    }


    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    public class DownloadFileFromURL extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... down_file) {
            int count;
            try {
                URL url = new URL(down_file[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                int lenghtOfFile = conection.getContentLength();
                //InputStream input = new BufferedInputStream(url.openStream(),8192);
                InputStream input = getAssets().open("/SKMaps/PreinstalledMaps/v1/20160426/package/INCITY08.skm");
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().toString()+ "/INCITY08.skm");
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);}
        @Override
        protected void onPostExecute(String file_url) {
            dismissDialog(progress_bar_type);
        }
    }
    private void showViewIfNotVisible(ViewGroup target) {
        if (target != null && target.getVisibility() == View.GONE) {
            target.setVisibility(View.VISIBLE);
            target.bringToFront();
        }
    }
//    public void showSearchingForGPSPanel() {
//        showViewIfNotVisible(searchingForGPSPanel);
//        if(backButtonPanel.isClickable() == false)
//        {
//            backButtonPanel.bringToFront();
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) backButtonPanel.getLayoutParams();
//            params.addRule(RelativeLayout.BELOW, com.skobbler.ngx.R.id.navigation_searching_for_gps_panel);
//        }
//        else {
//        }
//    }

    private void SetMapLocation(){
        SKCoordinate coordinates = new SKCoordinate(latitude,longitude);
        SKPosition lastSKPosition  = new SKPosition (coordinates);
        SKPositionerManager.getInstance ().reportNewGPSPosition (lastSKPosition);
        mapView.centerOnCurrentPosition (ZoomLevel, true, AnimationDuration);
        mapView.setPositionAsCurrent (lastSKPosition.getCoordinate (), Accuracy, center);
        mapView.animateToLocation(startPoint,1000);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.animateToZoomLevel((float)18.999001);
        SKAnnotation annotationWithTextureId = new SKAnnotation(10);
        annotationWithTextureId.setLocation(new SKCoordinate(latitude,longitude));
        annotationWithTextureId.setMininumZoomLevel(10);
        mapView.centerOnCurrentPosition(17, true, 500);
        mapView.getMapSettings().setOrientationIndicatorType(SKMapSurfaceView.SKOrientationIndicatorType.NONE);
        mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROTATING_MAP);
        //annotationWithTextureId.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
        mapView.addAnnotation(annotationWithTextureId, SKAnimationSettings.ANIMATION_NONE);
    }
    private void setSearchedLocation(){
        mapView.animateToLocation(new SKCoordinate(latitude,longitude),1000);
        mapView.animateToLocation(new SKCoordinate(Lat,Lng),1000);
//        mapView.getMapSettings().setMapZoomingEnabled(true);
//        mapView.animateToZoomLevel((float)18.999001);
        SKAnnotation annotationWithTextureId = new SKAnnotation(10);
        // Set annotation location
        annotationWithTextureId.setLocation(new SKCoordinate(latitude,longitude));
        annotationWithTextureId.setLocation(new SKCoordinate(Lat,Lng));
        // Set minimum zoom level at which the annotation should be visible
        annotationWithTextureId.setMininumZoomLevel(10);
        // Set the annotation's type
        //annotationWithTextureId.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_RED);
        annotationWithTextureId.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_GREEN);
        mapView.addAnnotation(annotationWithTextureId, SKAnimationSettings.ANIMATION_NONE);
        ShowRoute();
        btn_currentLocation.setVisibility(View.GONE);
        stringBuilder = new StringBuilder();
    }
    private void setHeading(boolean enabled) {
        if (enabled) {
            headingOn = true;
            mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROTATING_MAP);
            startOrientationSensor();
        } else {
            headingOn = false;
            mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROTATING_HEADING);
            stopOrientationSensor();
        }
    }
    private void stopOrientationSensor() {
        orientationValues = null;
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    public void ShowRoute() {
        SKRouteManager.getInstance().clearAllRoutesFromCache();
        startPoint = currentPosition.getCoordinate();
        SKRouteSettings route = new SKRouteSettings();
        //route.setStartCoordinate(new SKCoordinate(latitude,longitude));
        route.setStartCoordinate(startPoint);
        //route.setDestinationCoordinate(destinationPoint);
        route.setDestinationCoordinate(new SKCoordinate(Lat,Lng));
//        SKToolsNavigationConfiguration configuration = new SKToolsNavigationConfiguration();
//        configuration.setRouteType(SKRouteSettings.SKRouteMode.CAR_FASTEST);
        route.setRouteMode(SKRouteSettings.SKRouteMode.CAR_FASTEST);
        route.setMaximumReturnedRoutes(1);
        List <SKViaPoint> viaPointList = new ArrayList<SKViaPoint>();
        if (viaPoint != null){
            viaPointList.add(viaPoint);
            route.setViaPoints(viaPointList);
        }
        route.setRouteExposed(true);
        SKRouteManager.getInstance().setRouteListener(this);
        SKRouteManager.getInstance().calculateRoute(route);
    }
    public void launchNavigation() {
        if (TrackElementsActivity.selectedTrackElement != null) {
            mapView.clearTrackElement(TrackElementsActivity.selectedTrackElement);
        }
        SKNavigationSettings navigationSettings = new SKNavigationSettings();
        navigationSettings.setNavigationType(SKNavigationSettings.SKNavigationType.REAL);
        navigationSettings.setNavigationMode(SKNavigationSettings.SKNavigationMode.CAR);
        navigationSettings.setPositionerVerticalAlignment(-0.25f);
        navigationSettings.setShowStreetNamesPopusOnRoute(true);
        navigationSettings.setDistanceUnit(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_MILES_FEET);
        navigationSettings.getSpeedWarningThresholdInCity();
        SKNavigationManager sknavigation = SKNavigationManager.getInstance();
        sknavigation.getNavigationMode();
        sknavigation.setMapView(mapView);
        sknavigation.setNavigationListener(this);
        sknavigation.startNavigation(navigationSettings);
        navigationInProgress = true;
        mapView.centerOnCurrentPosition(17, true, 500);
        mapView.animateToZoomLevel((float)13.999001);
        mapView.getMapSettings().setOrientationIndicatorType(SKMapSurfaceView.SKOrientationIndicatorType.NONE);
        mapView.getMapSettings().setFollowPositions(true);
        mapView.getMapSettings().setCompassPosition(new SKScreenPoint(10, 70));
        mapView.getMapSettings().setCompassShown(true);
        mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.HISTORIC_POSITIONS);
        mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROUTE);
        //mapView.animateToBearing(1.0f,true,200);
        startOrientationSensor();
//        switch (compassStates) {
//            case PEDESTRIAN_COMPASS:
//                compassStates = CompassStates.NORTH_ORIENTED;
//                Toast.makeText(this, "The map will not turn. It will always stay northbound.", Toast.LENGTH_SHORT).show();
//                compassPanelImageView.setBackgroundResource(com.skobbler.ngx.R.drawable.icon_north_oriented);
//                mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROUTE);
//                break;
//            case NORTH_ORIENTED:
//                compassStates = CompassStates.HISTORICAL_POSITIONS;
//                Toast.makeText(this, "Map will Rotate in direction od Driving", Toast.LENGTH_LONG).show();
//                compassPanelImageView.setBackgroundResource(com.skobbler.ngx.R.drawable.icon_compass);
//                mapView.getMapSettings().setCompassShown(true);
//                mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.HISTORIC_POSITIONS);
//                mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.ROUTE);
//                //mapView.animateToBearing(1.0f,true,200);
//                startOrientationSensor();
//                break;
//        }
    }


    private void startOrientationSensor() {
        orientationValues = new float[3];
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        sensorManager.registerListener(this, orientationSensor, SensorManager.SENSOR_DELAY_UI);
    }
    private void onGPSSignalLost() {
        navigationManager.showSearchingForGPSPanel();
    }

    @Override
    public void onAllRoutesCompleted() {
        //launchNavigation();
    }

    @Override
    protected void onResume() {
        if(mapView !=null)
            mapView.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        SKMaps.getInstance().destroySKMaps();
        if (textToSpeechEngine != null) {
            textToSpeechEngine.stop();
            textToSpeechEngine.shutdown();
        }
        try{
            deleteCache(MainActivity.this);
        }catch (Exception e){

            e.printStackTrace();
        }
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {

        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()){
            String[] directory = dir.list();
            for (int i = 0; i< directory.length;i++){
                boolean success = deleteDir(new File(dir,directory[i]));
                if (!success){
                    return false;
                }
            }
            return dir.delete();
        }
        else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onActionPan() {

    }

    @Override
    public void onActionZoom() {

    }

    @Override
    public void onSurfaceCreated(SKMapViewHolder skMapViewHolder) {
        mapView = skMapViewHolder.getMapSurfaceView();
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.animateToZoomLevel((float)18.999001);
        mapView.getMapSettings().setCompassPosition(new SKScreenPoint(10, 70));
        mapView.getMapSettings().setCompassShown(true);
        mapView.animateToZoomLevel((float)13.999001);
        mapView.setZoom((float)13.999001);
        mapView.centerOnCurrentPosition(17,true,400);
        if (currentPosition != null){
            mapView.animateToZoomLevel((float)13.999001);
            mapView.setZoom((float)13.999001);
            mapView.centerOnCurrentPosition(17,true,400);
            mapView.getMapSettings().setHeadingMode(SKMapSettings.SKHeadingMode.NONE);
        }
        SetMapLocation();
        if(fromSearch){
            setSearchedLocation();
            fromSearch = false;}
//        if (currentPosition != null) {
//            mapView.animateToLocation(startPoint,1000);
//            SKPositionerManager.getInstance().reportNewGPSPosition(currentPosition);
//        }

    }

    @Override
    public void onMapRegionChanged(SKCoordinateRegion skCoordinateRegion) {
    }

    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion skCoordinateRegion) {
    }

    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion skCoordinateRegion) {
    }

    @Override
    public void onDoubleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onSingleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onRotateMap() {
    }

    @Override
    public void onLongPress(SKScreenPoint skScreenPoint) {

        SKCoordinate skCoordinate = mapView.pointToCoordinate(skScreenPoint);
        final SKSearchResult skSearchResult = SKReverseGeocoderManager.getInstance().reverseGeocodePosition(skCoordinate);
        startPoint = skSearchResult.getLocation();
        SKAnnotation skAnnotation = new SKAnnotation(VIA_POINT_ICON_ID);
        skAnnotation.setLocation(startPoint);
        skAnnotation.setMininumZoomLevel(10);
        skAnnotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_BLUE);
        mapView.animateToLocation(startPoint,500);
        viaPoint = new SKViaPoint(VIA_POINT_ICON_ID,startPoint);
        mapView.addAnnotation(skAnnotation,SKAnimationSettings.ANIMATION_NONE);
    }

    @Override
    public void onInternetConnectionNeeded() {

    }

    @Override
    public void onMapActionDown(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onMapActionUp(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onPOIClusterSelected(SKPOICluster skpoiCluster) {

    }

    @Override
    public void onMapPOISelected(SKMapPOI skMapPOI) {

    }

    @Override
    public void onAnnotationSelected(SKAnnotation skAnnotation) {
        mapView.animateToLocation(startPoint,1000);
        mapView.getMapSettings().setMapZoomingEnabled(true);
        mapView.animateToZoomLevel((float)18.999001);

    }

    @Override
    public void onCustomPOISelected(SKMapCustomPOI skMapCustomPOI) {

    }

    @Override
    public void onCompassSelected() {

    }

    @Override
    public void onCurrentPositionSelected() {

    }

    @Override
    public void onObjectSelected(int i) {

    }

    @Override
    public void onInternationalisationCalled(int i) {

    }

    @Override
    public void onBoundingBoxImageRendered(int i) {

    }

    @Override
    public void onGLInitializationError(String s) {

    }

    @Override
    public void onScreenshotReady(Bitmap bitmap) {

    }

    @Override
    public void onRealReachCalculationCompleted(SKBoundingBox skBoundingBox) {

    }

    @Override
    public void onDestinationReached() {
        SKRouteManager.getInstance().clearRouteAlternatives();
        Toast.makeText(MainActivity.this, "You have Reached "+Place_Name, Toast.LENGTH_SHORT).show();
    }


    //Voice Navigation Process (Text To Speech) Starts Here.....
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onSignalNewAdviceWithInstruction(String instruction) {
        SKLogging.writeLog(TAG, " onSignalNewAdviceWithInstruction " + instruction, Log.DEBUG);
        textToSpeechEngine.speak(instruction, TextToSpeech.QUEUE_ADD,null);
        //textToSpeechEngine.speak(instruction,TextToSpeech.QUEUE_ADD,null,null);
        string = instruction;
        stringBuilder.append("\t"+" - - "+"  "+string+"\n"+"\n");
        voice_text.setText(stringBuilder);
    }


    @Override
    public void onSignalNewAdviceWithAudioFiles(String[] audioFiles, boolean b) {
//        SKLogging.writeLog(TAG, " onSignalNewAdviceWithAudioFiles " + Arrays.asList(audioFiles), Log.DEBUG);
//        SKToolsAdvicePlayer.getInstance().playAdvice(audioFiles, SKToolsAdvicePlayer.PRIORITY_NAVIGATION);
//        string = Arrays.toString(audioFiles);
//        string = string.replace("_open","");
//        string = string.replace("_close","");
//        string = string.replace("_"," ");
//        string = string.replace(","," ");
//        string = string.replace("meter","yard");
//        string = string.replace("meters","yards");
//        String regex = "\\[|\\]";
//        string = string.replaceAll(regex,"   ");
//        stringBuilder.append(string+"\n"+"\n");
//        voice_text.setText(stringBuilder);

    }

    @Override
    public void onSpeedExceededWithAudioFiles(String[] strings, boolean b) {

    }

    @Override
    public void onSpeedExceededWithInstruction(String s, boolean b) {

    }

    @Override
    public void onUpdateNavigationState(SKNavigationState skNavigationState) {

    }

    @Override
    public void onReRoutingStarted() {

    }

    @Override
    public void onFreeDriveUpdated(String s, String s1, String s2, SKNavigationState.SKStreetType skStreetType, double v, double v1) {

    }

    @Override
    public void onViaPointReached(int i) {

    }

    @Override
    public void onVisualAdviceChanged(boolean b, boolean b1, SKNavigationState skNavigationState) {

    }

    @Override
    public void onTunnelEvent(boolean b) {

    }

    @Override
    public void onUpdatePOIsInRadius(double v, double v1, int i) {

    }

    @Override
    public void onReceivedPOIs(SKTrackablePOIType skTrackablePOIType, List<SKDetectedPOI> list) {

    }

    @Override
    public void onCurrentPositionUpdate(SKPosition currentPosition) {
        MainActivity.this.currentPositionTime = System.currentTimeMillis();
        MainActivity.this.currentPosition = currentPosition;
        SKPositionerManager.getInstance().reportNewGPSPosition(this.currentPosition);
        if (currentPosition != null){
            mapView.centerOnCurrentPosition(17,true,500);
            setHeading(false);
        }

        if (skToolsNavigationInProgress) {
            if (this.currentPosition.getHorizontalAccuracy() >= 150) {
                numberOfConsecutiveBadPositionReceivedDuringNavi++;
                if (numberOfConsecutiveBadPositionReceivedDuringNavi >= 3) {
                    numberOfConsecutiveBadPositionReceivedDuringNavi = 0;
                    onGPSSignalLost();
                }
            } else {
                numberOfConsecutiveBadPositionReceivedDuringNavi = 0;
                onGPSSignalRecovered();
            }
        }
        currentPositionProvider.requestLocationUpdates(true,true,false);
    }

    private void onGPSSignalRecovered() {
        navigationManager.hideSearchingForGPSPanel();
    }
//    public void updateLocation(Location location){
//        if (location != null) {
//            latitude = location.getLatitude();
//            longitude = location.getLongitude();
//            SetMapLocation();
//        }
//
//    }



    @Override
    public void onRouteCalculationCompleted(SKRouteInfo skRouteInfo) {
        SKRouteManager.getInstance().getAdviceListForRouteByUniqueId(skRouteInfo.getRouteID(),
                SKMaps.SKDistanceUnitType.DISTANCE_UNIT_MILES_FEET);

    }

    private void selectAlternativeRoute(int i) {
    }

    @Override
    public void onRouteCalculationFailed(SKRoutingErrorCode skRoutingErrorCode) {

    }



    @Override
    public void onServerLikeRouteCalculationCompleted(SKRouteJsonAnswer json) {

    }

    @Override
    public void onOnlineRouteComputationHanging(int i) {

    }

    @Override
    public void onNavigationStarted() {

    }

    @Override
    public void onNavigationEnded() {

    }

    @Override
    public void onRouteCalculationStarted() {

    }

    @Override
    public void onRouteCalculationCompleted() {

    }

    @Override
    public void onRouteCalculationCanceled() {

    }

    @Override
    public void onNewVersionDetected(int i) {

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
