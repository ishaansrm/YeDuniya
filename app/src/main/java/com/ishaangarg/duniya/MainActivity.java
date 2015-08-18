package com.ishaangarg.duniya;

import android.app.Application;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private RecyclerView.Adapter mAdapter;
    JSONObject[] myDataset = new JSONObject[29];
    RecyclerView rv;
    SwipeRefreshLayout mSwipeRefreshLayout;
    GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);

        rv = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        rv.setHasFixedSize(true);
        //haha
        rv.setLayoutManager(llm);

        final String url = "http://staging.couponapitest.com/task_data.txt";
        fetchData(url);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                fetchData(url);
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("USER LOCATION",String.valueOf(mLastLocation.getLatitude())+" \t"+String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    RequestQueue queue = VolleySingleton.getInstance().getRequestQueue();
    String coordinates="";

    public void fetchData(final String url) {
        JsonObjectRequest jsonRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            response = response.getJSONObject("data");

                            Iterator<String> iterator = response.keys();
                            int i = 0;
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                JSONObject value = response.getJSONObject(key);
                                if(coordinates.equals(""))
                                    coordinates=coordinates+value.get("Latitude").toString() + "," + value.get("Longitude").toString();
                                else
                                    coordinates=coordinates+"|"+value.get("Latitude").toString() + "," + value.get("Longitude").toString();

                                myDataset[i] = value;
                                i++;

                            }
                            //SwipeRefreshLayout mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
                            mSwipeRefreshLayout.setRefreshing(false);
                            mAdapter = new ListAdapter(myDataset);
                            rv.setAdapter(mAdapter);
                            calcDistance(coordinates);
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

        String url =
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=Seattle&destinations="+coordinates;

        JsonObjectRequest distRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONArray rows = response.getJSONArray("rows");
                            JSONObject rowObj = rows.getJSONObject(0);
                            JSONArray elements = rowObj.getJSONArray("elements");
                            Log.d("Coordinates", coordinates);
                            for (int i = 0; i < 29; i++) {

                                JSONObject obj = elements.getJSONObject(i);
                                String status = obj.get("status").toString();

                                if (status.equals("OK")) {
                                    JSONObject dist = obj.getJSONObject("distance");
                                    String distance = dist.get("text").toString();
                                    Log.d("DISTANCE", distance);
                                }
                            }
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
        //TODO
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
