package com.apres.gerber.loops;

        import java.util.ArrayList;
        import java.util.Map;
        import org.w3c.dom.Document;
        import com.google.android.gms.maps.model.LatLng;
        import android.app.ProgressDialog;
        import android.os.AsyncTask;
        import android.util.Log;
        import android.widget.Toast;

public class GetDirectionsAsyncTask extends AsyncTask<Map<String, String>, Object, ArrayList<LatLng>>
{
    public static final String USER_CURRENT_LAT = "user_current_lat";
    public static final String USER_CURRENT_LONG = "user_current_long";
    public static final String Waypoint1_Lat = "waypoint1_lat";
    public static final String Waypoint1_Long = "waypoint1_long";
    public static final String Waypoint2_Lat = "waypoint2_lat";
    public static final String Waypoint2_Long = "waypoint2_long";
    public static final String Waypoint3_Lat = "waypoint3_lat";
    public static final String Waypoint3_Long = "waypoint3_long";
    public static final String Waypoint4_Lat = "waypoint4_lat";
    public static final String Waypoint4_Long = "waypoint4_long";
    public static final String Waypoint5_Lat = "waypoint5_lat";
    public static final String Waypoint5_Long = "waypoint5_long";
    public static final String Waypoint6_Lat = "waypoint6_lat";
    public static final String Waypoint6_Long = "waypoint6_long";
    public static final String Waypoint7_Lat = "waypoint7_lat";
    public static final String Waypoint7_Long = "waypoint7_long";

    public static final String DIRECTIONS_MODE = "directions_mode";

    private MapsActivity activity;
    private Exception exception;
    private ProgressDialog progressDialog;
    public static int distance;

    public GetDirectionsAsyncTask(MapsActivity activity)
    {
        super();
        this.activity = activity;
    }

    public void onPreExecute()
    {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Calculating directions");
        progressDialog.show();
    }

    @Override
    public void onPostExecute(ArrayList result)
    {
        progressDialog.dismiss();
        if (exception == null)
        {
            MapsActivity.meters=distance;
            activity.handleGetDirectionsResult(result);
        }
        else
        {
            processException();
        }
    }

    @Override
    protected ArrayList<LatLng> doInBackground(Map<String, String>... params)
    {
        Map<String, String> paramMap = params[0];
        try
        {
            LatLng fromPosition = new LatLng(Double.valueOf(paramMap.get(USER_CURRENT_LAT)) , Double.valueOf(paramMap.get(USER_CURRENT_LONG)));
            LatLng waypoint1 = new LatLng(Double.valueOf(paramMap.get(Waypoint1_Lat)) , Double.valueOf(paramMap.get(Waypoint1_Long)));
            LatLng waypoint2 = new LatLng(Double.valueOf(paramMap.get(Waypoint2_Lat)) , Double.valueOf(paramMap.get(Waypoint2_Long)));
            LatLng waypoint3 = new LatLng(Double.valueOf(paramMap.get(Waypoint3_Lat)) , Double.valueOf(paramMap.get(Waypoint3_Long)));
            LatLng waypoint4 = new LatLng(Double.valueOf(paramMap.get(Waypoint4_Lat)) , Double.valueOf(paramMap.get(Waypoint4_Long)));
            LatLng waypoint5 = new LatLng(Double.valueOf(paramMap.get(Waypoint5_Lat)) , Double.valueOf(paramMap.get(Waypoint5_Long)));
            LatLng waypoint6 = new LatLng(Double.valueOf(paramMap.get(Waypoint6_Lat)) , Double.valueOf(paramMap.get(Waypoint6_Long)));
            LatLng waypoint7 = new LatLng(Double.valueOf(paramMap.get(Waypoint7_Lat)) , Double.valueOf(paramMap.get(Waypoint7_Long)));

            GMapV2Direction md = new GMapV2Direction();
            Document doc = md.getDocument(fromPosition, waypoint1, waypoint2, waypoint3, waypoint4, waypoint5, waypoint6, waypoint7, paramMap.get(DIRECTIONS_MODE));
            distance = md.getDistanceValue(doc);
            ArrayList<LatLng> directionPoints = md.getDirection(doc);
            return directionPoints;
        }
        catch (Exception e)
        {
            exception = e;
            return null;
        }
    }

    private void processException()
    {
        Toast.makeText(activity, "No Route Found. Press Next or Prev", Toast.LENGTH_LONG).show();
    }
}