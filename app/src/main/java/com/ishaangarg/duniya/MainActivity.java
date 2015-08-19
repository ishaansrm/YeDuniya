package com.ishaangarg.duniya;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private RecyclerView.Adapter mAdapter;
    ArrayList<JSONObject> myDataset = new ArrayList<>();
    RecyclerView rv;
    GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    String latitude, longitude;
    ProgressBar mProgressBar;
    int flag = 0;
    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    LocationRequest mLocationRequest;

    ImageView sadCloud;
    TextView noConnection;
    Button retryBtn, btn;
    final String url = "http://staging.couponapitest.com/task_data.txt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buildGoogleApiClient();
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        sadCloud = (ImageView) findViewById(R.id.sad_cloud);
        noConnection = (TextView) findViewById(R.id.no_connection);
        retryBtn = (Button) findViewById(R.id.retry_btn);
        btn = (Button) findViewById(R.id.settings_btn);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        rv = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        if (!isOnline()) {
            notOnline(false);
        } else
            fetchData(url);

        rv.setHasFixedSize(true);
        rv.setLayoutManager(llm);
        rv.setItemAnimator(new DefaultItemAnimator());

    }

    public boolean checkGPS() {
        Log.d("CHECK GPS", "CHECKING GPS");
        LocationManager lm;
        final Context context = MainActivity.this;
        boolean gps_enabled = false, network_enabled = false;

        LocationRequest mLocationRequestBalancedPowerAccuracy = new LocationRequest();
        mLocationRequestBalancedPowerAccuracy.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestBalancedPowerAccuracy);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                //final LocationSettingsStates = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(
                                    MainActivity.this,
                                    REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        //TODO
                        break;
                }
            }
        });

        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("checkGPS", "loc error");
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.d("checkGPS", "loc n/w error");
        }

        if (!gps_enabled && !network_enabled) {
            flag = 1;
            notOnline(true);
            return false;
        } else return true;
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        Log.d("ACTIVITY RESULT", "LOC DIALOG DISPLAYED");

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (responseCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        //Refresh whole list
                        sadCloud.setVisibility(View.GONE);
                        noConnection.setVisibility(View.GONE);
                        retryBtn.setVisibility(View.GONE);
                        retryBtn.setVisibility(View.GONE);
                        flag = 1;
                        connect();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to

                        break;
                    default:
                        break;
                }
                break;
        }
        super.onActivityResult(requestCode, responseCode, intent);
    }

    public void connect() {
        if (!isOnline()) {
            Log.d("CONNECT", "NOT ONLINE");
            notOnline(false);
            return;
        }

        if (!checkGPS()) {
            Log.d("CONNECT", "NO GPS FOUND");
            notOnline(true);
        } else {
            Log.d("CONNECT", "ONLINE");
            sadCloud.setVisibility(View.GONE);
            noConnection.setVisibility(View.GONE);
            retryBtn.setVisibility(View.GONE);

            mProgressBar.setVisibility(View.VISIBLE);

            if (!mGoogleApiClient.isConnecting()) {
                Log.d("CONNECT", "CONNECTING TO PLAY services");
                mGoogleApiClient.disconnect();
                mGoogleApiClient.connect();
            }
            if (myDataset.size() == 0)
                fetchData(url);
        }
    }

    public void notOnline(Boolean loc) {
        mProgressBar.setVisibility(View.GONE);
        rv.setVisibility(View.GONE);

        sadCloud.setVisibility(View.VISIBLE);
        noConnection.setVisibility(View.VISIBLE);
        retryBtn.setVisibility(View.VISIBLE);


        if (!loc) {
            //No Connection
            noConnection.setText("Check connection & try again");
            retryBtn.setText("Retry");
            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("NOT ONLINE", "CLICKED RETRY BTN");
                    mProgressBar.setVisibility(View.VISIBLE);
                    connect();
                }
            });
        } else {
            //No Location
            noConnection.setText("Please enable Location Services or GPS");
            retryBtn.setText("Enable Location");
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });
            retryBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connect();
                }
            });
        }
    }


    protected synchronized void buildGoogleApiClient() {
        Log.d("Play Services", "Building client");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public boolean isOnline() {
        Log.d("IS_ONLINE", "Checking connection..");
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Play Services", "Connected!");
        if (!checkGPS()) {
            Log.d("Play Services", "NO GPS");
            notOnline(true);
        } else {
            Log.d("Play Services", "GPS Locked & Confirmed!");
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (mLastLocation != null) {
                latitude = String.valueOf(mLastLocation.getLatitude());
                longitude = String.valueOf(mLastLocation.getLongitude());
                if (flag == 1) {
                    Log.d("Play Services", "Calculating distance, got user loc");
                    calcDistance(coordinates);
                } else {
                    Log.d("ON CONNECTED", "FLAG=0, not calculating dist");
                }
            } else {
                Log.d("ON CONNECTED", "LAST LOC IS NULL, retrying..");
                mLocationRequest = new LocationRequest();
                mLocationRequest.setNumUpdates(1);
                mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LOC", "location changed!");
        mLastLocation = location;
        latitude = String.valueOf(mLastLocation.getLatitude());
        longitude = String.valueOf(mLastLocation.getLongitude());
        if (flag == 1) {
            Log.d("Play Services", "Calculating distance, got user loc");
            calcDistance(coordinates);
        } else {
            Log.d("ON CONNECTED", "FLAG=0, not calculating dist");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Play Services", "Connection suspended");
        mGoogleApiClient.connect();
    }

    RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();
    String coordinates = "";

    public void fetchData(final String url) {
        Log.d("Fetch Data", "fetching data..");
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Fetch data", "Response OK");
                        try {
                            response = response.getJSONObject("data");

                            Iterator<String> iterator = response.keys();

                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                JSONObject value = response.getJSONObject(key);

                                if (coordinates.equals(""))
                                    coordinates = coordinates +
                                            value.get("Latitude").toString() + "," + value.get("Longitude").toString();
                                else
                                    coordinates = coordinates +
                                            "|" + value.get("Latitude").toString() + "," + value.get("Longitude").toString();

                                myDataset.add(value);
                            }
                            calcDistance(coordinates);
                            rv.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });


        queue.add(jsonRequest);

        //Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void calcDistance(final String coordinates) {


        Log.d("CALC DISTANCE", "Calculating distance..");

        if (latitude == null) {
            flag = 1;
            mGoogleApiClient.connect();
            return;
        }

        String url =
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins="
                        + latitude + "," + longitude + "&destinations=" + coordinates;

        final JsonObjectRequest distRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray rows = response.getJSONArray("rows");
                            JSONObject rowObj = rows.getJSONObject(0);
                            JSONArray elements = rowObj.getJSONArray("elements");
                            //Log.d("Coordinates", coordinates);
                            for (int i = 0; i < 29; i++) {

                                JSONObject obj = elements.getJSONObject(i);
                                String status = obj.get("status").toString();

                                if (status.equals("OK")) {
                                    JSONObject dist = obj.getJSONObject("distance");
                                    double distance = Double.parseDouble(dist.get("value").toString());
                                    String distText = dist.get("text").toString();
                                    myDataset.get(i).put("Distance", distance);
                                    myDataset.get(i).put("DistText", distText);
                                    //Log.d("DISTANCE", "" + distance);
                                } else
                                    Log.e("calcDistance", "STATUS NOT OK");
                            }
                            Collections.sort(myDataset, new Comparator<JSONObject>() {
                                @Override
                                public int compare(JSONObject lhs, JSONObject rhs) {
                                    double dis1 = 0, dis2 = 0;
                                    try {
                                        dis1 = (double) lhs.get("Distance");
                                        dis2 = (double) rhs.get("Distance");
                                        if (dis1 > dis2)
                                            return 1;
                                        if (dis1 < dis2)
                                            return -1;
                                        else
                                            return 0;
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    return 0;
                                }
                            });
                            mProgressBar.setVisibility(View.GONE);
                            rv.setVisibility(View.VISIBLE);
                            mAdapter = new ListAdapter(myDataset);
                            rv.setAdapter(mAdapter);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        queue.add(distRequest);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("Play Services", "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ON STOP", "STOPPED!");
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
