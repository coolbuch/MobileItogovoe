package com.example.googlemapphonefinding;


import android.Manifest;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Button;
import android.location.LocationListener;

import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;

import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.graphics.ImageFormat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.BreakIterator;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
    final static String URL = "http://192.168.43.252:25565/";
    final static String PREFSKEY = "UUID";
    final static String TAG = "MYTAG";
    String APP_UUID = "";
    SharedPreferences sp;

    Retrofit retrofit;
    Gson gson;
    APIService apiService;
    LocationManager locationManager;

    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();
    EditText uuidText;
    Button sendButton, testButton;
    TextView tvStatusGPS, tvStatusNet, tvLocationGPS, tvLocationNet, tvEnabledGPS, tvEnabledNet, AppIDTV;
    Activity thisActvity;

    double currentLat, currentLng;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = new GsonBuilder().create();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        apiService = retrofit.create(APIService.class);

        thisActvity = this;

        setContentView(R.layout.activity_main);
        uuidText = (EditText) findViewById(R.id.uuid_text);
        sendButton = (Button) findViewById(R.id.button2);
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!sp.contains(PREFSKEY) || APP_UUID == null || !APP_UUID.equals(""))
                {
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(PREFSKEY, uuidText.getText().toString());
                    editor.apply();
                    APP_UUID = sp.getString(PREFSKEY, null);
                }
                JSONObject json = new JSONObject();
                try {
                    json.accumulate("uuid", APP_UUID);
                    json.accumulate("lat", currentLat);
                    json.accumulate("lng", currentLng);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onClick: " + json.toString());
                apiService.reg(json.toString()).enqueue(new Callback<Response<String>>()
                {
                    @Override
                    public void onResponse(Call<Response<String>> call, Response<Response<String>> response) {
                        Log.d(TAG, "onFailure: " + call.request().toString());

                        Log.d(TAG, "onResponse: " + response.body());
                    }

                    @Override
                    public void onFailure(Call<Response<String>> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        });
        testButton = (Button) findViewById(R.id.test_button);
        testButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(thisActvity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
                }
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            1000 * 10, 10, locationListener);
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                            locationListener);
                    checkEnabled();
                }

            }
        });


        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);
        AppIDTV = (TextView) findViewById(R.id.AppID) ;

        sp = getSharedPreferences("settings", MODE_PRIVATE);
        if (sp.contains(PREFSKEY)) {
            APP_UUID = sp.getString(PREFSKEY, null);
            Log.d(TAG, "onCreate: current APP_UUID " + APP_UUID);

            if (APP_UUID != null || !APP_UUID.equals("")) {
                AppIDTV.setText(APP_UUID);
                uuidText.setVisibility(View.INVISIBLE);
                sendButton.setVisibility(View.INVISIBLE);
            }
        }
        /*SharedPreferences.Editor editor = sp.edit();
        editor.putString(PREFSKEY, null);
        editor.apply();*/
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(thisActvity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            ActivityCompat.requestPermissions(thisActvity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            } else if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };

    private void showLocation(Location location)
    {
        if (location == null)
            return;
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            Log.d(TAG, "showLocation:  GPS " + location.getLatitude() + " " + location.getLongitude());
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(
                LocationManager.NETWORK_PROVIDER)) {
            Log.d(TAG, "showLocation:  NET " + location.getLatitude() + " " + location.getLongitude());
            currentLat = location.getLatitude();
            currentLng = location.getLongitude();
            tvLocationNet.setText(formatLocation(location));
        }
        JSONObject json = new JSONObject();
        try {
            json.accumulate("uuid", APP_UUID);
            json.accumulate("lat", currentLat);
            json.accumulate("lng", currentLng);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "showLocation: " + json.toString());
        apiService.setCoords(json.toString()).enqueue(new Callback<Response<String>>()
        {
            @Override
            public void onResponse(Call<Response<String>> call, Response<Response<String>> response) {
                Log.d(TAG, "onFailure: " + call.request().toString());
                Log.d(TAG, "onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<Response<String>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private String formatLocation(Location location) {
        if (location == null)
            return "";
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: "
                + locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }


}


