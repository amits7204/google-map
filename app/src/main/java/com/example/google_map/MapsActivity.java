package com.example.google_map;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView mStartView, mEndView;
    private final int mStartCode = 1;
    private final static int MY_PERMITION_FINE_LOCATION = 101;
    private final int mEndCode = 2;
    private final static String TAG = "MainActivity";
    private Location mEndLocation = new Location(LocationManager.GPS_PROVIDER);
    private Marker mStartMarker = null;
    private Marker mEndMarker = null;
    private Location mStartLocation;
    private Location mCurrentLocation;
    private int mRetryCount = 0;
    private GoogleMap.OnCameraIdleListener onCameraIdleListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mStartView = findViewById(R.id.start_point_edittext);
        mEndView = findViewById(R.id.end_point_edittext);

        final AutocompleteFilter typeFilter = new AutocompleteFilter
                .Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .setCountry("IN")
                .build();


        mStartView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("MainActivity", "Click On Start Point");
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .setFilter(typeFilter)
                                    .build(MapsActivity.this);
                    startActivityForResult(intent, mStartCode);//PLACE_AUTOCOMPLETE_REQUEST_CODE is integer for request code
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        mEndView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("MainActivity", "Click On End Point");
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                    .setFilter(typeFilter)
                                    .build(MapsActivity.this);
                    startActivityForResult(intent, mEndCode);//PLACE_AUTOCOMPLETE_REQUEST_CODE is integer for request code
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    // TODO: Handle the error.
                }
            }
        });

        LocationManager lLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (lLocationManager != null && !lLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            GoogleApiClient lGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API).build();
            lGoogleApiClient.connect();
            LocationRequest lLocationRequest = LocationRequest.create();
            lLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            lLocationRequest.setInterval(10000);
            lLocationRequest.setFastestInterval(10000/2);


            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(lLocationRequest);
            builder.setAlwaysShow(true);

            Task<LocationSettingsResponse> lResult =
                    LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());

            // result callback to handle location settings request
            lResult.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                @Override
                public void onComplete(Task<LocationSettingsResponse> task) {
                    try {
                        LocationSettingsResponse response = task.getResult(ApiException.class);
                        // All location settings are satisfied. The client can initialize location
                        // requests here....
                    } catch (ApiException exception) {
                        switch (exception.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                Log.w(TAG,"All location setting are satisfied");
                                getLocationResult();
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.w(TAG,"Location settings are not satisfied, show the user a dialog to upgrade the location setting");
                                // Location settings are not satisfied. But could be fixed by showing the
                                // user a dialog.
                                try {
                                    // Cast to a resolvable exception.
                                    ResolvableApiException resolvable = (ResolvableApiException) exception;
                                    // start resolution for result will take only activity context
                                    // so results will be sent to activity's onActivityResult

                                    // status.startResolutionForResult(getReachUsActivity(),getPermRequestCode());

                                    // by using IntentSenderForResult will start intenet based on resolution acquired from status
                                    // this method gives result to this fragment's onActivityResult
                                    // works same as startActivityForResult...
                                    startIntentSenderForResult(resolvable.getResolution().getIntentSender(),PermissionRequestCodes.MapsActivity,null,0,0,0,null);
                                } catch (IntentSender.SendIntentException e) {
                                    // Ignore the error.
                                    e.printStackTrace();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Log.w(TAG,"Location settings are inadequate, and cannot be fixed here. Dialog not created");
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog....
                                break;
                        }
                    }
                }
            });

        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        displayLocation();
        mMap.setOnCameraIdleListener(onCameraIdleListener);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                /*
                 * GeoLatLng:Class will give us selected position latigude and
                 * longitude values
                 */
                setEndPointMarker(latLng);
                mEndLocation.setLatitude(latLng.latitude);
                mEndLocation.setLongitude(latLng.longitude);
//                requestEstimateApi();

                setEndPointAddress(mEndLocation);
                LatLng lStartLatLng = new LatLng(mStartLocation.getLatitude(),mStartLocation.getLongitude());
                LatLng lEndLatLng = new LatLng(mEndLocation.getLatitude(),mEndLocation.getLongitude());
                String lurl = getPolyLineUrl(lStartLatLng,lEndLatLng);
                Log.d("onMapClick", lurl);
                PolyLineUtils.getPolyLine(mMap,lurl);
            }
        });

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            mMap.setMyLocationEnabled(true);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMITION_FINE_LOCATION);
            }
        }
        //mMap.getUiSettings().isZoomControlsEnabled();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case mStartCode:
                switch (resultCode) {
                    case RESULT_OK :
                        Place place = PlaceAutocomplete.getPlace(this, data);
                        Log.w(TAG, "Place: " + place.getName());

                        final LatLng latLngLoc = place.getLatLng();

                        setStartPointMarker(latLngLoc);
                        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLngLoc, 15);
                        mMap.animateCamera(location);
                        mStartLocation.setLatitude(latLngLoc.latitude);
                        mStartLocation.setLongitude(latLngLoc.longitude);
                        setStartPointAddress(mStartLocation);

                        if(mEndLocation!=null){
                            LatLng lStartLatLng = new LatLng(mStartLocation.getLatitude(),mStartLocation.getLongitude());
                            LatLng lEndLatLng = new LatLng(mEndLocation.getLatitude(),mEndLocation.getLongitude());
                            String lurl = getPolyLineUrl(lStartLatLng,lEndLatLng);
                            Log.d("onMapClick", lurl);
                            PolyLineUtils.getPolyLine(mMap,lurl);
                        }
                        break;
                    case PlaceAutocomplete.RESULT_ERROR :
                        Status status = PlaceAutocomplete.getStatus(this, data);
                        // TODO: Handle the error.
                        Log.w(TAG, "Amit Singh: "+status.getStatusMessage());
                        break;
                    case RESULT_CANCELED :
                        // The user canceled the operation.
                        break;
                }
                break;
            case mEndCode:
                switch (resultCode) {
                    case RESULT_OK:
                        Place place = PlaceAutocomplete.getPlace(this, data);
                        Log.w(TAG, "Place: " + place.getName());

                        LatLng latLngLoc = place.getLatLng();

                        mEndLocation.setLatitude(latLngLoc.latitude);
                        mEndLocation.setLongitude(latLngLoc.longitude);

                        setEndPointMarker(latLngLoc);
                        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLngLoc, 15);
                        mMap.animateCamera(location);
                        mEndLocation.setLatitude(latLngLoc.latitude);
                        mEndLocation.setLongitude(latLngLoc.longitude);
                        setEndPointAddress(mEndLocation);
//                        requestEstimateApi();
                        LatLng lStartLatLng = new LatLng(mStartLocation.getLatitude(), mStartLocation.getLongitude());
                        LatLng lEndLatLng = new LatLng(mEndLocation.getLatitude(), mEndLocation.getLongitude());
                        String lurl = getPolyLineUrl(lStartLatLng, lEndLatLng);
                        Log.d("onMapClick", lurl);
                        PolyLineUtils.getPolyLine(mMap, lurl);
                        break;
                    case PlaceAutocomplete.RESULT_ERROR:
                        Status status = PlaceAutocomplete.getStatus(this, data);
                        // TODO: Handle the error.
                        Log.w(TAG, status.getStatusMessage());
                        break;

                    case RESULT_CANCELED:
                        // The user canceled the operation.
                        break;
                }
                break;
            case PermissionRequestCodes.MapsActivity:
                switch (resultCode){
                    case RESULT_OK:
                        Log.w(TAG,"Result_Ok");
//                        getLocationResult();
                        break;
                    case RESULT_CANCELED:
                        break;
                }
        }


    }



    private void setStartPointMarker(LatLng aLatLng){
        Log.w(TAG,"setStartMarker");
        if(mStartMarker!=null){
            mStartMarker.remove();
        }
        mStartMarker = mMap.addMarker(new MarkerOptions().position(aLatLng));
    }

    private void setStartPointAddress(Location aLocation){
        Log.w(TAG,"setStartPointAddress");
        Geocoder geocoder = new Geocoder(getApplicationContext());

        try {
            List<Address> addressList = geocoder.getFromLocation(aLocation.getLatitude(),
                    aLocation.getLongitude(),
                    1);
            if (addressList != null && addressList.size() > 0) {
                String locality = addressList.get(0).getAddressLine(0);
                String country = addressList.get(0).getCountryName();
                if (!locality.isEmpty() && !country.isEmpty()) {
                    mStartView.setText(this.getString(R.string.Empty_with_args,locality,country));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setEndPointMarker(LatLng aLatLng){
        Log.w(TAG,"setEndMarker");
        if(mEndMarker!=null){
            mEndMarker.remove();
        }
        mEndMarker = mMap.addMarker(new MarkerOptions().position(aLatLng));
    }

    private void setEndPointAddress(Location aLocation){
        Log.w(TAG,"setEndPointAddress");
        Geocoder geocoder = new Geocoder(getApplicationContext());

        try {
            List<Address> addressList = geocoder.getFromLocation(aLocation.getLatitude(),
                    aLocation.getLongitude(),
                    1);
            if (addressList != null && addressList.size() > 0) {
                String locality = addressList.get(0).getAddressLine(0);
                String country = addressList.get(0).getCountryName();
                if (!locality.isEmpty() && !country.isEmpty()) {
                    mEndView.setText(getApplicationContext().getString(R.string.Empty_with_args,locality,country));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getPolyLineUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
        Log.w(TAG,"Polyline URL: "+url);

        return url;
    }
    private void displayLocation() {
        Log.w(TAG,"displayLocation");
        Log.w(TAG,"Current Latitude: "+mCurrentLocation);
        if (mCurrentLocation != null) {
            Log.w(TAG,"location not null");
            final double latitude = mCurrentLocation.getLatitude();
            final double longitude = mCurrentLocation.getLongitude();
            mStartLocation = mCurrentLocation;

            LatLng lLatLng = new LatLng(mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());
            setStartPointMarker(lLatLng);
            setStartPointAddress(mStartLocation);

            //Animate camera to your position
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));
//            mProgressBar.cancel();
//            mProgressBar.dismiss();
        }
        else{
            Log.d(TAG,"Location is Null");
        }
    }
    public void getLocationResult()
    {
        final int MAX_RETRY_COUNT = 3;
        final DetachableResultReceiver lDetachableResultReceiver = new DetachableResultReceiver(new Handler());
        lDetachableResultReceiver.setReceiver(new DetachableResultReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                switch (resultCode) {
                    case 0 :
                        try {
                            mCurrentLocation = resultData.getParcelable("Location");
                            Log.w(TAG,"DetachableResultReceiver");
                            displayLocation();
                        }catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 1:
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (mRetryCount <= MAX_RETRY_COUNT){
                            Log.w(TAG, "if part Retry count: "+mRetryCount);
                            GetLocationReq lGetLocationReq = new GetLocationReq(getApplicationContext(), lDetachableResultReceiver);
                            lGetLocationReq.execute();
                            mRetryCount++;
                        }else {
                            Log.w(TAG, "else part Retry count: "+mRetryCount);
                            mRetryCount = 0;
                            Toast.makeText(getApplicationContext(),"Sorry: We Couldn't find your location!! Please try again later",Toast.LENGTH_SHORT).show();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        GetLocationReq lGetLocationReq = new GetLocationReq(getApplicationContext(), lDetachableResultReceiver);
        lGetLocationReq.execute();
    }
}
