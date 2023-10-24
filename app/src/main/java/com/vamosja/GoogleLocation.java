package com.vamosja;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.util.Log;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class GoogleLocation {

    /**
     * Provides the entry point to the Fused Location Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    protected Location mLastLocation;

    /* para locaction Update*/

    private LocationCallback mLocationCallback;

    /**
     * Represents a geographical location.
     */
    private Location mCurrentLocation;
    LocationUpdateListener locationUpdateListener;
    private Context context;
    private Activity activity;
    private static final String TAG = "APP_MAPP";


    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = (5*60)*1000;


    public GoogleLocation(Context context, Activity activity , LocationUpdateListener locationUpdateListener) {
        this.locationUpdateListener = locationUpdateListener;
        this.context = context;
        this.activity = activity;

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
        mSettingsClient = LocationServices.getSettingsClient(this.context);


        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();

    }


    //funcao chamada quando tem  ultima localizaçao.
    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(activity, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

                            //mLastLocation.getLatitude();
                            //mLastLocation.getLongitude();

                            locationUpdateListener.lastLocation(mLastLocation);

                        } else {
                            Log.w(TAG, "getLastLocation:exception", task.getException());

                        }
                    }
                });
    }

    //funcao callback quando localização e atualizada.
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {

                    //location.getLatitude();
                    //location.getLongitude();

                    locationUpdateListener.updateLocation(location);


                }
            };
        };
    }

    //Defino o intervalo de busca a localizaçao e tipo precisão da localização
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();


        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(10);
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(10000);


    }

    //Criando objeto LocationSettinsRequest
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    //start Localização
    @SuppressWarnings("MissingPermission")
    public void startLocationUpdates() {
        // Comece verificando se o dispositivo possui as configurações de localização necessárias.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "Todas as configurações de localização estão satisfeitas.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener(activity, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();


                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "As configurações de localização não estão satisfeitas. ");
                                locationUpdateListener.errorLocation(statusCode);

                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "As configurações de localização são inadequadas.";
                                locationUpdateListener.errorLocation(statusCode);
                                Log.e(TAG, errorMessage);
                        }



                    }
                });
    }

    //Remover localização
    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

}
