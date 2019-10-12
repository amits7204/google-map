package com.example.google_map;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import static android.content.Context.LOCATION_SERVICE;
import static java.security.AccessController.getContext;

public class GetLocationReq extends Request implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String EXTENDED_DATA_LONGITUDE = "EXTENDED_DATA_LONGITUDE";
    private static final String EXTENDED_DATA_LATITUDE = "EXTENDED_DATA_LATITUDE";
    private static final String ACTION_RESP_LOCATION = "ACTION_RESP_LOCATION";
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public static final String Tag = "GeoLocationReq";
    public DetachableResultReceiver mResultReceiver;
    private Location mCurrentBestLocation;
    private Handler mWaitForOtherResponseHandler;
    private Runnable mLocationRunnable;
    private LocationManager mLocationManager;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;

    public GetLocationReq(
            Context aContext,
            DetachableResultReceiver aResultReceiver)
    {
        super(aContext, Tag);
        mResultReceiver = aResultReceiver;
    }

    @Override
    public void req()
    {
        try {
            Log.w(Tag,"Get Location Request");
            mWaitForOtherResponseHandler = new Handler(Looper.getMainLooper());
            mLocationRunnable = new Runnable() {
                @Override
                public void run() {
                    if(mResultReceiver != null){
                        if(mCurrentBestLocation != null){
                            Log.d(Tag, "TimeOut Sending Location: "+mCurrentBestLocation);
                            Bundle lBundle = new Bundle();
                            lBundle.putParcelable("Location", mCurrentBestLocation);
                            mResultReceiver.send(0, lBundle);
                            mResultReceiver = null;
                            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        }else{
                            Log.d(Tag, "TimeOut Location Null Sending result back to retry ");
                            Bundle lBundle = new Bundle();
                            mResultReceiver.send(1, lBundle);
                        }
                        mResultReceiver = null;
                        if(mLocationManager != null){
                            mLocationManager.removeUpdates(GetLocationReq.this);
                        }
                    }else{
                        Log.d(Tag,"Can't send resullt back ResutReceiver NULL");
                    }
                }
            };
            mWaitForOtherResponseHandler.postDelayed(mLocationRunnable,10000);

            mLocationCallback = new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    if(locationResult.getLastLocation() != null){
                        Log.w(Tag,"Location Received from fused client updates: "+locationResult.getLastLocation());
                        if(mCurrentBestLocation != null)
                        {
                            if(isBetterLocation(locationResult.getLastLocation(),mCurrentBestLocation))
                            {
                                mCurrentBestLocation = locationResult.getLastLocation();
                                if(mCurrentBestLocation.getAccuracy() <= 50){
                                    if(mResultReceiver != null){
                                        Log.d(Tag,"Found location with high accuracy "+mCurrentBestLocation);
                                        Bundle lBundle = new Bundle();
                                        lBundle.putParcelable("Location", mCurrentBestLocation);
                                        mResultReceiver.send(0, lBundle);
                                        mResultReceiver = null;
                                        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                        mLocationManager.removeUpdates(GetLocationReq.this);
                                        mWaitForOtherResponseHandler.removeCallbacks(mLocationRunnable);
                                    }else{
                                        Log.d(Tag,"Can't send location back result receiver null");
                                    }
                                }
                            }
                        } else {
                            mCurrentBestLocation = locationResult.getLastLocation();
                            if(mCurrentBestLocation.getAccuracy() <= 50){
                                if(mResultReceiver != null){
                                    Log.d(Tag,"Found location with high accuracy "+mCurrentBestLocation);
                                    Bundle lBundle = new Bundle();
                                    lBundle.putParcelable("Location", mCurrentBestLocation);
                                    mResultReceiver.send(0, lBundle);
                                    mResultReceiver = null;
                                    mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                                    mLocationManager.removeUpdates(GetLocationReq.this);
                                    mWaitForOtherResponseHandler.removeCallbacks(mLocationRunnable);
                                }else{
                                    Log.d(Tag,"Can't send location back result receiver null");
                                }
                            }
                        }
                    }
                }
            };

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(2000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            boolean lIsGpsAvailable = isGooglePlayServicesAvailable(getContext());
            if (lIsGpsAvailable) {
                handleActionFused();
            } else {
                handleActionNonFused();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public static boolean isGooglePlayServicesAvailable(Context aContext) {
        if (ConnectionResult.SUCCESS ==
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(aContext)) {
            return true;
        } else {
            // Send a broadcast saying location is not got.
            Log.w("Utils:", "No GooglePlayServices");
            return false;
        }
    }
    /**
     * Broadcast location.
     *
     * @param lLocation the l location
     */
    public void broadcastLocation(Location lLocation)
    {
        // Get the location passed to this service through an extra.
        Double lLongitude = lLocation.getLongitude();
        Double lLatitude = lLocation.getLatitude();

        Log.w(Tag, lLongitude.toString());
        Log.w(Tag, lLatitude.toString());

        Intent lIntent = new Intent(ACTION_RESP_LOCATION)
                .putExtra(EXTENDED_DATA_LONGITUDE, lLocation.getLongitude())
                .putExtra(EXTENDED_DATA_LATITUDE, lLocation.getLatitude());
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(getContext()).sendBroadcast(lIntent);
        Log.w(Tag, "SentBroadCast Location");
    }

    private void handleActionNonFused()
    {
        Log.w(Tag, "GooglePlayServices not available");
        Log.w(Tag, "mLocationManager init'd");
        mLocationManager = (LocationManager)getContext().getSystemService(LOCATION_SERVICE);

        if (mLocationManager == null) {
            return;
        }

        boolean isGPSProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkProviderEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        try{
            if(isNetworkProviderEnabled){
                Log.d(Tag,"NETWORK provider enabled");
                Criteria lCriteria = new Criteria();
                lCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
                mLocationManager.requestSingleUpdate(lCriteria,this,Looper.getMainLooper());
            }else if(isGPSProviderEnabled){
                Log.d(Tag,"GPS provider enabled");
                Criteria lCriteria = new Criteria();
                lCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                mLocationManager.requestSingleUpdate(lCriteria,this,Looper.getMainLooper());
            }else{
                Log.d(Tag,"No Providers enabled getting last known location");
                Criteria lCriteria = new Criteria();
                lCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                mLocationManager.getLastKnownLocation(mLocationManager.getBestProvider(lCriteria,true));
            }
        }catch (SecurityException e){
            Log.e(Tag,"Error while executing handle action non fused: "+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle action GetAddress in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFused()
    {
        Log.w(Tag, "Entered");
        /*Log.w(Tag, "RegisteredGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();*/
        Log.w(Tag, "mLocationClient connect");
        requestLocationFromProviders();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext().getApplicationContext());
        try{
            mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    Looper.getMainLooper());
        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(Bundle aConnectionHint)
    {
        Log.w(Tag, "onConnected");
        /*try {
            final FusedLocationProviderClient lFusedLocationClient =
                                LocationServices.getFusedLocationProviderClient(getContext());
            lFusedLocationClient.getLastLocation().addOnSuccessListener(
                new OnSuccessListener<Location>()
                {
                @Override
                public void onSuccess(Location lLocation) {
                    Log.w(Tag,"onSuccess: "+lLocation);
                    if (lLocation != null) {
                        Double lLongitude = lLocation.getLongitude();
                        Double lLatitude = lLocation.getLatitude();
                        Log.w(Tag, lLongitude.toString());
                        Log.w(Tag, lLatitude.toString());
                        mCurrentBestLocation = lLocation;
                        if (mResultReceiver != null) {
                            Log.d(Tag,"Sending Fused Location: "+mCurrentBestLocation);
                            mWaitForOtherResponseHandler.removeCallbacks(mLocationRunnable);
                            Bundle lBundle = new Bundle();
                            lBundle.putParcelable("Location", mCurrentBestLocation);
                            mResultReceiver.send(0, lBundle);
                            mResultReceiver = null;
                        }
                        //broadcastLocation(lLocation);
                        Log.w(Tag, "SentBroadCast");
                    } else {
                        Log.w(Tag, "Failed to get Fused Location requesting providers");
                        requestLocationFromProviders();
                    }
                }
            });
        } catch (SecurityException e) {
            Log.w(Tag, "On Connected: No Appropriate Services");
        }*/
    }

    @Override
    public void onConnectionSuspended(int flag)
    {
        // Nothing to be done, to be implemented
        Log.w(Tag, "Connection Suspended");
        requestLocationFromProviders();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult aResult)
    {
        // Nothing to be done for now, to be implemented.
        Log.w(Tag, "Failed in connection");
        requestLocationFromProviders();
    }

    private void requestLocationFromProviders()
    {
        Log.w(Tag,"requestLocationFromProviders");
        mLocationManager = (LocationManager)getContext().getSystemService(LOCATION_SERVICE);
        if (mLocationManager == null) {
            return;
        }

        Log.w(Tag,"Executing single update from GPS provider");
        try {
//            Criteria lNetworkCriteria = new Criteria();
//            lNetworkCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,this,Looper.getMainLooper());
        } catch (java.lang.SecurityException ex) {
            Log.d(Tag, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(Tag, "gps provider does not exist " + ex.getMessage());
        }

        Log.w(Tag,"Executing single update from Network provider");
        try {
//            Criteria lGpsCriteria = new Criteria();
//            lGpsCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
            mLocationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,this,Looper.getMainLooper());
        } catch (java.lang.SecurityException ex) {
            Log.d(Tag, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(Tag, "network provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onLocationChanged(Location aLocation)
    {
        Log.w(Tag,"OnLocationChanged");
        if (aLocation != null) {
            Log.w(Tag, "Location: "+aLocation);
            Double lLongitude = aLocation.getLongitude();
            Double lLatitude = aLocation.getLatitude();
            Log.w(Tag, lLongitude.toString());
            Log.w(Tag, lLatitude.toString());

            if(mCurrentBestLocation != null)
            {
                if(isBetterLocation(aLocation,mCurrentBestLocation))
                {
                    mCurrentBestLocation = aLocation;
                    if(mCurrentBestLocation.getAccuracy() <= 50){
                        if(mResultReceiver != null){
                            Log.d(Tag,"Found location with high accuracy "+mCurrentBestLocation);
                            Bundle lBundle = new Bundle();
                            lBundle.putParcelable("Location", mCurrentBestLocation);
                            mResultReceiver.send(0, lBundle);
                            mResultReceiver = null;
                            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                            mLocationManager.removeUpdates(this);
                            mWaitForOtherResponseHandler.removeCallbacks(mLocationRunnable);
                        }else{
                            Log.d(Tag,"Can't send location back result receiver null");
                        }
                    }
                }
            } else {
                mCurrentBestLocation = aLocation;
                if(mCurrentBestLocation.getAccuracy() <= 50){
                    if(mResultReceiver != null){
                        Log.d(Tag,"Found location with high accuracy "+mCurrentBestLocation);
                        Bundle lBundle = new Bundle();
                        lBundle.putParcelable("Location", mCurrentBestLocation);
                        mResultReceiver.send(0, lBundle);
                        mResultReceiver = null;
                        if(mFusedLocationClient != null){
                            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                        }
                        mLocationManager.removeUpdates(this);
                        mWaitForOtherResponseHandler.removeCallbacks(mLocationRunnable);
                    }else{
                        Log.d(Tag,"Can't send resullt back ResutReceiver NULL");
                    }
                }
            }
            //broadcastLocation(lLocation);
            Log.w(Tag, "SentBroadCast");
        }
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private boolean isBetterLocation(
            Location location,
            Location currentBestLocation)
    {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate)
        {
            return true;
        } else return isNewer && !isSignificantlyLessAccurate && isFromSameProvider;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2)
    {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle)
    {

    }

    @Override
    public void onProviderEnabled(String s)
    {

    }

    @Override
    public void onProviderDisabled(String s)
    {

    }

}
