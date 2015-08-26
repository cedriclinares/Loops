package com.apres.gerber.loops;

import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.*;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends AppCompatActivity {

    private EditText mEditDistance;
    private Button mButton;
    private Button mSubmit;
    private Button mNext;
    private Button mPrev;
    private GoogleMap mGoogleMap;
    private String provider;
    private LocationManager locationManager;
    private Location location;
    private GoogleApiClient mGoogleApiClient;
    private LocationListener mLocationListener;
    PolylineOptions rectLine;
    private Polyline mPolyline;
    private LatLngBounds mLatLngBounds;
    private CameraPosition mCameraPosition;
    private Marker mMarker;

    private final float radOfEarth = 3959;
    private final float constant = (float) Math.sqrt(2) / 2;
    private double lat;
    private double lng;
    private float changeInLat;
    private float changeInLng;
    int clicks = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        mEditDistance = (EditText) findViewById(R.id.edt_text);
        mSubmit = (Button) findViewById(R.id.Submit);
        mButton = (Button) findViewById(R.id.loop_button);
        mNext = (Button) findViewById(R.id.next);
        mPrev = (Button) findViewById(R.id.prev);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        //mapFragment.getMapAsync(this);
        mGoogleMap = mapFragment.getMap();
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        // Define the criteria how to select the location in provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

        location = locationManager.getLastKnownLocation(provider);

        LatLng camera = new LatLng(location.getLatitude(),location.getLongitude());
        mCameraPosition = new CameraPosition.Builder().target(camera)
                .zoom(15.5f)
                .bearing(0)
                .tilt(25)
                .build();
        mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        addMarker(camera,15);

        mEditDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditDistance.getText().clear();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location = locationManager.getLastKnownLocation(provider);

                try {
                    Double.parseDouble(mEditDistance.getText().toString());
                    // Initialize the location fields
                        if (location != null)
                            calcLoop();
                        else
                            Toast.makeText(MapsActivity.this, "Cannot Find Location", Toast.LENGTH_LONG).show();
                    }
                 catch (NumberFormatException e) {
                    Log.i("Check EditText","Input is not a number");
                    Toast.makeText(MapsActivity.this, "Must input a number", Toast.LENGTH_LONG).show();
                }
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Submit clicked", Toast.LENGTH_LONG).show();
                location = locationManager.getLastKnownLocation(provider);
                mLocationListener.onLocationChanged(location);
            }
        });
    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {

        rectLine = new PolylineOptions().width(10).color(Color.RED);

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }

        mPolyline = this.mGoogleMap.addPolyline(rectLine);

    }

    public void findDirections(ArrayList <LatLng> circle, String mode)
    {
        Map<String, String> map = new HashMap<String, String>();

        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(circle.get(0).latitude));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(circle.get(0).longitude));
        map.put(GetDirectionsAsyncTask.Waypoint1_Lat, String.valueOf(circle.get(1).latitude));
        map.put(GetDirectionsAsyncTask.Waypoint1_Long, String.valueOf(circle.get(1).longitude));
        map.put(GetDirectionsAsyncTask.Waypoint2_Lat, String.valueOf(circle.get(2).latitude));
        map.put(GetDirectionsAsyncTask.Waypoint2_Long, String.valueOf(circle.get(2).longitude));
        map.put(GetDirectionsAsyncTask.Waypoint3_Lat, String.valueOf(circle.get(3).latitude));
        map.put(GetDirectionsAsyncTask.Waypoint3_Long, String.valueOf(circle.get(3).longitude));
        map.put(GetDirectionsAsyncTask.Waypoint4_Lat, String.valueOf(circle.get(4).latitude));
        map.put(GetDirectionsAsyncTask.Waypoint4_Long, String.valueOf(circle.get(4).longitude));
        /*map.put(GetDirectionsAsyncTask.Waypoint5_Lat, String.valueOf(circle.get(5).latitude));
        map.put(GetDirectionsAsyncTask.Waypoint5_Long, String.valueOf(circle.get(5).longitude));
        map.put(GetDirectionsAsyncTask.Waypoint6_Lat, String.valueOf(circle.get(6).latitude));
        map.put(GetDirectionsAsyncTask.Waypoint6_Long, String.valueOf(circle.get(6).longitude));
        map.put(GetDirectionsAsyncTask.Waypoint7_Lat, String.valueOf(circle.get(7).latitude));
        map.put(GetDirectionsAsyncTask.Waypoint7_Long, String.valueOf(circle.get(7).longitude));*/
        map.put(GetDirectionsAsyncTask.DIRECTIONS_MODE, mode);

        GetDirectionsAsyncTask asyncTask = new GetDirectionsAsyncTask(this);
        asyncTask.execute(map);
    }

    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation)
    {
        if (firstLocation != null && secondLocation != null)
        {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.option1:
                //TODO add what to do
                break;

            case R.id.option2:
                //TODO add what to do
                break;

            case R.id.option3:
                //TODO add what to do
                break;
        }

        return true;
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }


    private void addMarker(LatLng point, int order){
        mMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("This is point" + order)
                .snippet("Lat: " + point.latitude + " Lng: " + point.longitude)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
    }

    private void makeLoop (ArrayList<LatLng> circle){
        findDirections(circle, GMapV2Direction.MODE_WALKING);
        addMarker(circle.get(0), 1);
        addMarker(circle.get(1), 2);
        addMarker(circle.get(2), 3);
        addMarker(circle.get(3), 4);
        addMarker(circle.get(4), 5);
        //addMarker(circle.get(5), 6);
        //addMarker(circle.get(6), 7);
        //addMarker(circle.get(7), 8);
    }

    private void calcLoop(){
        clicks = 128;
        lat = location.getLatitude();
        lng = location.getLongitude();


        String input = mEditDistance.getText().toString();

        double distance = Double.parseDouble(input);
        //float diameter = (float) (circumference / Math.PI);
        changeInLat = (float) Math.toDegrees(distance / radOfEarth);
        changeInLng = (float) Math.toDegrees(distance / radOfEarth);


        if (mPolyline != null) {
            mGoogleMap.clear();
        }


        makeLoop(southSquare(lat, lng, changeInLat, changeInLng));

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicks++;
                mGoogleMap.clear();
                switch (clicks % 4) {
                    case 0:
                        makeLoop(southSquare(lat, lng, changeInLat, changeInLng));
                        break;
                    case 1:
                        makeLoop(eastSquare(lat, lng, changeInLat, changeInLng));
                        break;
                    case 2:
                        makeLoop(northSquare(lat, lng, changeInLat, changeInLng));
                        break;
                    case 3:
                        makeLoop(westSquare(lat, lng, changeInLat, changeInLng));
                }
            }
        });

        mPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clicks--;
                mGoogleMap.clear();
                switch (clicks % 4) {
                    case 0:
                        makeLoop(southSquare(lat, lng, changeInLat, changeInLng));
                        break;
                    case 1:
                        makeLoop(eastSquare(lat, lng, changeInLat, changeInLng));
                        break;
                    case 2:
                        makeLoop(northSquare(lat, lng, changeInLat, changeInLng));
                        break;
                    case 3:
                        makeLoop(westSquare(lat, lng, changeInLat, changeInLng));
                }
            }
        });
    }

    private final class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            LatLng curLocation = new LatLng(location.getLatitude(),location.getLongitude());
            if (mMarker!=null) {
                mMarker.remove();
                addMarker(curLocation, 10);
            }
            else
                addMarker(curLocation, 10);
        }

        @Override
        public void onProviderDisabled(String provider) {
            // called when the GPS provider is turned off (user turning off the GPS on the phone)
        }

        @Override
        public void onProviderEnabled(String provider) {
            // called when the GPS provider is turned on (user turning on the GPS on the phone)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // called when the status of the GPS provider changes
        }
    }

    private ArrayList<LatLng> southSquare (double lat, double lng, float changeInLat, float changeInLng) {
        ArrayList<LatLng> circle = new ArrayList<LatLng>();

        LatLng point1 = new LatLng(lat, lng);
        LatLng point2 = new LatLng(lat, lng - changeInLat/8);
        LatLng point3 = new LatLng(lat - changeInLat/4, lng - changeInLng/8);
        LatLng point4 = new LatLng(lat - changeInLat/4, lng + changeInLng/8);
        LatLng point5 = new LatLng(lat, lng + changeInLat/8);

        circle.add(0, point1);
        circle.add(1, point2);
        circle.add(2, point3);
        circle.add(3, point4);
        circle.add(4, point5);

        return circle;
    }
    private ArrayList<LatLng> northSquare (double lat, double lng, float changeInLat, float changeInLng) {
        ArrayList<LatLng> circle = new ArrayList<LatLng>();

        LatLng point1 = new LatLng(lat, lng);
        LatLng point2 = new LatLng(lat, lng - changeInLat/8);
        LatLng point3 = new LatLng(lat + changeInLat/4, lng - changeInLng/8);
        LatLng point4 = new LatLng(lat + changeInLat/4, lng + changeInLng/8);
        LatLng point5 = new LatLng(lat, lng+ changeInLng/8);

        circle.add(0, point1);
        circle.add(1, point2);
        circle.add(2, point3);
        circle.add(3, point4);
        circle.add(4, point5);

        return circle;
    }private ArrayList<LatLng> eastSquare (double lat, double lng, float changeInLat, float changeInLng) {
        ArrayList<LatLng> circle = new ArrayList<LatLng>();

        LatLng point1 = new LatLng(lat, lng);
        LatLng point2 = new LatLng(lat + changeInLat/8, lng);
        LatLng point3 = new LatLng(lat + changeInLat/8, lng + changeInLng/4);
        LatLng point4 = new LatLng(lat - changeInLat/8, lng + changeInLng/4);
        LatLng point5 = new LatLng(lat - changeInLat/8, lng);

        circle.add(0, point1);
        circle.add(1, point2);
        circle.add(2, point3);
        circle.add(3, point4);
        circle.add(4, point5);

        return circle;
    }private ArrayList<LatLng> westSquare (double lat, double lng, float changeInLat, float changeInLng) {
        ArrayList<LatLng> circle = new ArrayList<LatLng>();

        LatLng point1 = new LatLng(lat, lng);
        LatLng point2 = new LatLng(lat - changeInLat/8, lng);
        LatLng point3 = new LatLng(lat - changeInLat/8, lng - changeInLng/4);
        LatLng point4 = new LatLng(lat + changeInLat/8, lng - changeInLng/4);
        LatLng point5 = new LatLng(lat + changeInLat/8, lng);

        circle.add(0, point1);
        circle.add(1, point2);
        circle.add(2, point3);
        circle.add(3, point4);
        circle.add(4, point5);

        return circle;
    }


}