package com.abhi.offlinemaps.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.search.SKNearbySearchSettings;
import com.skobbler.ngx.search.SKSearchListener;
import com.skobbler.ngx.search.SKSearchManager;
import com.skobbler.ngx.search.SKSearchResult;
import com.skobbler.ngx.search.SKSearchStatus;
import com.abhi.offlinemaps.R;
import java.util.ArrayList;
import java.util.List;


/**
 * Activity in which a nearby search with some user provided parameters is
 * performed
 */
public class NearbySearchResultsActivity extends Activity implements SKSearchListener {

    /**
     * Search manager object
     */
    private SKSearchManager searchManager;

    private ListView listView;

    private ResultsListAdapter adapter;
    //public GpsTracker gpsTracker;
    public SKCoordinate skCoordinate;
    public TextView Subtitle;
    List<SKSearchResult> resultlist = new ArrayList<SKSearchResult>();

    /**
     * List of pairs containing the search results names and categories
     */
    private List<Pair<String, String>> items = new ArrayList<Pair<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        ((TextView) findViewById(R.id.label_operation_in_progress)).setText(getResources()
                .getString(R.string.searching));
        listView = (ListView) findViewById(R.id.list_view);
       // gpsTracker = new GpsTracker(this);
//        Lat = gpsTracker.getLatitude();
//        Lng = gpsTracker.getLongitude();
        skCoordinate = new SKCoordinate();
        //Subtitle = (TextView)findViewById(R.id.subtitle);



        // get the search manager and set the search result listener
        searchManager = new SKSearchManager(this);
        // get a nearby search object
        SKNearbySearchSettings nearbySearchObject = new SKNearbySearchSettings();
        // set the position around which to do the search and the search radius
        nearbySearchObject.setLocation(new SKCoordinate(getIntent().getDoubleExtra("latitude", 0), getIntent()
                .getDoubleExtra("longitude", 0)));
        nearbySearchObject.setRadius(getIntent().getShortExtra("radius", (short)0));
        // set the search topic
        nearbySearchObject.setSearchTerm(getIntent().getStringExtra("searchTopic"));
        // initiate the nearby search
        searchManager.nearbySearch(nearbySearchObject);

        nearbySearchObject.setSearchMode(SKSearchManager.SKSearchMode.OFFLINE);
        // Initiate the nearby search
        SKSearchStatus status =  searchManager.nearbySearch(nearbySearchObject);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent Int = new Intent(NearbySearchResultsActivity.this, MainActivity.class);
                Int.putExtra("latitude",resultlist.get(position).getLocation().getLatitude());
                Int.putExtra("longitude",resultlist.get(position).getLocation().getLongitude());
                Int.putExtra("fromSearch", true);
//                Int.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(100,Int);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
        Intent Int = new Intent(NearbySearchResultsActivity.this, SearchPlaceActivity.class);
        startActivity(Int);

        super.onBackPressed();
    }

    //    private SKOnelineSearchSettings getOneLineSearchSettings(String term, SKCoordinate coordinate) {
//        SKOnelineSearchSettings onelineSearchSettings = new SKOnelineSearchSettings(term, SKSearchManager.SKSearchMode.OFFLINE );
//        onelineSearchSettings.setGpsCoordinates(coordinate);
//
//        TODO add country code
//        onelineSearchSettings.setCountryCode("IN");
//
//        return onelineSearchSettings;
//    }
    @Override
    public void onReceivedSearchResults(final List<SKSearchResult> results) {
        findViewById(R.id.label_operation_in_progress).setVisibility(View.GONE);
        listView.setVisibility(View.VISIBLE);
        resultlist = results;
        // populate the pair list when receiving search results
        for (SKSearchResult result : results) {

            String firstLine;
            if (result.getName() == null || result.getName().equals("")) {
                firstLine = result.getCategory().name();
                firstLine = firstLine.substring(firstLine.lastIndexOf("_") + 1);
            } else {
                firstLine = result.getName();
            }
            items.add(new Pair<String, String>(firstLine, Integer.toString(result.getCategory()
                    .getValue())));
        }
        adapter = new ResultsListAdapter();
        listView.setAdapter(adapter);
    }

    private class ResultsListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            Double latitude = resultlist.get(position).getLocation().getLatitude();
            Double longitude = resultlist.get(position).getLocation().getLongitude();
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.layout_search_list_item, null);
            } else {
                view = convertView;
            }
            ((TextView) view.findViewById(R.id.title)).setText(!items.get(position).first.equals("") ? items
                    .get(position).first : " - ");
            ((TextView)view.findViewById(R.id.subtitle)).setText("Latitude:"+ latitude+","+"Longitude:"+longitude);

            //((TextView) view.findViewById(R.id.subtitle)).setText("type: " + items.get(position).second);
            //((TextView) view.findViewById(R.id.subtitle)).setText("location: " + Lat+Lng);
            return view;



        }


    }

}
