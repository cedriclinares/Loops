package com.apres.gerber.loops;

import android.content.Context;
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
import java.util.List;
import java.util.Map;

public class MapsActivity extends AppCompatActivity {

    private EditText mEditDistance;
    private Spinner mSpinner;
    private Button mButton;
    private GoogleMap mGoogleMap;
    private String[] mAmountLoops = {"Amount Of Loops","1", "2", "4", "6", "8"};
    private String provider;
    private LocationManager locationManager;
    private GoogleApiClient mGoogleApiClient;
    private LocationListener mLocationListener;
    private Polyline mPolyline;
    private LatLngBounds mLatLngBounds;
    private CameraPosition mCameraPosition;
    private LatLng point1;
    private Map<String , Polyline> mHashMap = new HashMap<String , Polyline>();

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
        mSpinner = (Spinner) findViewById(R.id.loop_spinner);
        mButton = (Button) findViewById(R.id.loop_button);

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

        mEditDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditDistance.getText().clear();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
           Location location = locationManager.getLastKnownLocation(provider);
              // Initialize the location fields
                if (locationManager!=null) {
                    if (location != null) {
                      //  Toast.makeText(this, "Selected Provider " + provider,
                             //   Toast.LENGTH_SHORT).show();
                        mLocationListener.onLocationChanged(location);
                    } else {
                        //Toast.makeText(this, "Location is null" + provider,
                             //   Toast.LENGTH_SHORT).show();
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                        mLocationListener.onLocationChanged(location);
                    }
                }
                // Toast.makeText(MapsActivity.this, "Loops Button Pressed", Toast.LENGTH_LONG).show();
            }
        });

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                //context
                this,
                //view: layout you see when the spinner is closed
                R.layout.spinner_closed,
                //model: the array of Strings
                mAmountLoops
        );

        //view: layout you see when the spinner is open
        arrayAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        //assigns adapters to spinners
        mSpinner.setAdapter(arrayAdapter);

    }

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {

        PolylineOptions rectLine = new PolylineOptions().width(10).color(R.color.red);

        for(int i = 0 ; i < directionPoints.size() ; i++)
        {
            rectLine.add(directionPoints.get(i));
        }

        mPolyline = this.mGoogleMap.addPolyline(rectLine);

        mHashMap.put(mPolyline.getId() , mPolyline);
    }

    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode)
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(GetDirectionsAsyncTask.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(GetDirectionsAsyncTask.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
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

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.edt_text:
                //Log.w("edt_text", "clicked");
                break;
            case R.id.loop_spinner:
                //TODO Do something
                break;
            case R.id.loop_button:
                //TODO Do something
                break;
            default:
                break;
        }

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {

            case R.id.mnu_invert:
                //TODO add what to do
                break;

            case R.id.mnu_codes:
                //TODO add what to do
                break;

            case R.id.mnu_exit:
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
       Marker mMarker = mGoogleMap.addMarker(new MarkerOptions()
                .position(point)
                .title("This is point"+order)
                .snippet("Lat: "+ point.latitude+" Lng: "+ point.longitude)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
    }
    private final class MyLocationListener implements LocationListener {

        private Circle mCircle;
        private float radOfEarth = 3959;

        @Override
        public void onLocationChanged(Location location) {
            // called when the listener is notified with a location update from the GPS
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            String input = mEditDistance.getText().toString();
            double circumference = Double.parseDouble(input);

            float distance = (float) (circumference / Math.PI);
            float changeInLat = (float) Math.toDegrees(distance / radOfEarth);
            float changeInLng = (float) Math.toDegrees(.5 * distance / radOfEarth);

            point1 = new LatLng(lat, lng);
            LatLng point2 = new LatLng(lat - changeInLat / 2, lng - changeInLng);
            LatLng point3 = new LatLng(lat - changeInLat, lng);
            LatLng point4 = new LatLng(lat - changeInLat / 2, lng + changeInLng);

            if (mPolyline != null){
                mGoogleMap.clear();
             }
            else
                Log.d("Polyline", "null");

            addMarker(point1, 1);
            addMarker(point2, 2);
            addMarker(point3, 3);
            addMarker(point4, 4);

            findDirections(point1.latitude, point1.longitude,
                    point2.latitude, point2.longitude, GMapV2Direction.MODE_WALKING);
            findDirections(point2.latitude, point2.longitude,
                    point3.latitude, point3.longitude, GMapV2Direction.MODE_WALKING );
            findDirections(point3.latitude, point3.longitude,
                    point4.latitude, point4.longitude, GMapV2Direction.MODE_WALKING );
            findDirections(point4.latitude, point4.longitude,
                    point1.latitude, point1.longitude, GMapV2Direction.MODE_WALKING );

            mCameraPosition = new CameraPosition.Builder().target(point1)
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();

            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
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
}


