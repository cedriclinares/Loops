package com.apres.gerber.loops;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
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
    private int width, height;
    private boolean isTravelingToParis = false;
    private static final LatLng AMSTERDAM = new LatLng(52.37518, 4.895439);
    private static final LatLng PARIS = new LatLng(48.856132, 2.352448);
    private static final LatLng FRANKFURT = new LatLng(50.111772, 8.682632);

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
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);

        Location location = locationManager.getLastKnownLocation(provider);
        // Initialize the location fields
        if (locationManager!=null) {
            if (location != null) {
                Toast.makeText(this, "Selected Provider " + provider,
                        Toast.LENGTH_SHORT).show();
                mLocationListener.onLocationChanged(location);
            } else {
                Toast.makeText(this, "Location is null" + provider,
                        Toast.LENGTH_SHORT).show();
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
               mLocationListener.onLocationChanged(location);
            }
        }


        mEditDistance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditDistance.getText().clear();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Loops Button Pressed", Toast.LENGTH_LONG).show();
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
        if (mPolyline != null)
        {
            mPolyline.remove();
        }
        mPolyline = mGoogleMap.addPolyline(rectLine);
        mCameraPosition = new CameraPosition.Builder().target(AMSTERDAM)
                .zoom(15.5f)
                .bearing(0)
                .tilt(25)
                .build();
        if (isTravelingToParis)
        {
            mLatLngBounds = createLatLngBoundsObject(AMSTERDAM, PARIS);
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(mLatLngBounds, width, height, 150));
        }
        else
        {
            mLatLngBounds = createLatLngBoundsObject(AMSTERDAM, FRANKFURT);
            mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        }

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

private final class MyLocationListener implements LocationListener {

        private Marker mMarker;
        private Circle mCircle;
        CameraPosition mCameraPosition;
        private Polyline mPolyline;
        @Override
        public void onLocationChanged(Location location) {
            // called when the listener is notified with a location update from the GPS
           // Toast.makeText(this, "Location Changed", Toast.LENGTH_LONG).show();
            double lat = location.getLatitude();
            double lng = location.getLongitude();
          //  Toast.makeText(this, "Location " + lat + "," + lng,
          //          Toast.LENGTH_LONG).show();
            LatLng coordinate = new LatLng(lat, lng);
           // Toast.makeText(this, "Location " + coordinate.latitude + "," + coordinate.longitude,
             //       Toast.LENGTH_LONG).show();
            if (mCircle!=null) mCircle.remove();
            mCircle = mGoogleMap.addCircle(new CircleOptions()
                    .center(coordinate)
                    .radius(1000)
                    .fillColor(R.color.black));
            mCameraPosition = new CameraPosition.Builder().target(coordinate)
                    .zoom(15.5f)
                    .bearing(0)
                    .tilt(25)
                    .build();
            findDirections(52.37518, 4.895439, 48.856132, 2.352448, GMapV2Direction.MODE_WALKING );
           /* mPolyline = mGoogleMap.addPolyline(new PolylineOptions()
                    .add(coordinate,
                            new LatLng(coordinate.latitude + 0.02, coordinate.longitude + .02))
                    .geodesic(true));
           mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));*/
            /*mMarker = mGoogleMap.addMarker(new MarkerOptions()
                    .position(coordinate)
                    .title("Start")
                    .snippet("Your Location")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));*/
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


