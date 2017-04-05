package com.abhi.offlinemaps.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.abhi.offlinemaps.R;
import com.abhi.offlinemaps.util.GpsTracker;

public class SearchPlaceActivity extends Activity {
    GpsTracker gps_tracker;



    double latitude = 12.983480 ;
    double longitude = 77.585512;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_place);
    }
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_button:
                if (validateCoordinates()) {
                    try {
                        //short radius = Short.parseShort(((TextView) findViewById(R.id.radius_field)).getText().toString());
                        short radius = 20000;
                        Intent intent = new Intent(this, NearbySearchResultsActivity.class);
                        intent.putExtra("radius", radius);
                        intent.putExtra("latitude",
                                Double.parseDouble(String.valueOf(latitude)));
                        intent.putExtra("longitude",
                                Double.parseDouble(String.valueOf(longitude)));
                        intent.putExtra("searchTopic", ((TextView) findViewById(R.id.search_topic_field)).getText()
                                .toString());
                        startActivityForResult(intent,100);
//                        finish();
                    }
                    catch (NumberFormatException ex){
                        Toast.makeText(this, "Provide a short value, maximum value is 32 767.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Invalid latitude or longitude was provided", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == 100){
            setResult(100,data);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean validateCoordinates() {
        try {
            String latString = (String.valueOf(latitude));
            String longString = (String.valueOf(longitude));
            double latitude = Double.parseDouble(latString);
            double longitude = Double.parseDouble(longString);
            if (latitude > 90 || latitude < -90) {
                return false;
            }
            if (longitude > 180 || longitude < -180) {
                return false;
            }
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    @Override
    public void onBackPressed() {
//        finish();
//        Intent Int = new Intent(this, MainActivity.class);
////                Int.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        startActivity(Int);
        super.onBackPressed();
    }
}
