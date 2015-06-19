package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;

public class ServiceLocationTracker extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final int LOCATION_UPDATE_INTERVAL = 10000; //10 Seconds
    public static final int LOCATION_UPDATE_FASTEST_INTERVAL = 1000;//1 Second
    public static final double INTREPID_LATITUDE = 42.366980; //Intrepid Lat.: 42.366980
    public static final double INTREPID_LONGITUDE = -71.080161; //Intrepid Long.: -71.080161
    public static final int CHECK_IN_RADIUS = 100; //100 meters

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private String personName = "";
    private String notificationMessage;

    private int hour;

    //handler to post toast messages, possible cause of memory leaks, better to remove?
    private ToastHandlerForService toastHandler;

    public ServiceLocationTracker() {}

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException(getString(R.string.not_yet_implemented_toast_message));
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, getString(R.string.service_created_toast_message), Toast.LENGTH_LONG).show();
        toastHandler = ToastHandlerForService.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String usernameFromIntent = "";
        if (intent != null) {
            usernameFromIntent = intent.getStringExtra(Constants.INTENT_EXTRA_USERNAME);
        }

        if (!TextUtils.isEmpty(usernameFromIntent)) {
            PreferenceManagerCustom.putString(this, Constants.PREF_KEY_USERNAME, usernameFromIntent);
            personName = PreferenceManagerCustom.getString(this,
                    Constants.PREF_KEY_USERNAME, getString(R.string.default_username));
        }

        PreferenceManagerCustom.putBoolean(this, Constants.PREF_KEY_SERVICE_RUNNING, true);

        return startTracking();
    }

    private int startTracking(){
        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (googleApiClient == null){
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (locationRequest == null) {
            locationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(LOCATION_UPDATE_INTERVAL)
                    .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL);
        }

        personName = PreferenceManagerCustom.getString(this, Constants.PREF_KEY_USERNAME,
                getString(R.string.default_username));

        personName = TextUtils.isEmpty(personName) ? getString(R.string.default_username) : personName;

        String greeting = getString(R.string.user_greeting, personName);
        Toast.makeText(this, greeting, Toast.LENGTH_LONG).show();

        googleApiClient.connect();

        return START_STICKY;
    }

    private int stopTracking(){
        notificationMessage = getString(R.string.service_destroyed_toast_message);

        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        if (toastHandler != null) {
            toastHandler.setToastMessage(notificationMessage);
            toastHandler.sendEmptyMessage(0);
        }

        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }

        PreferenceManagerCustom.putBoolean(this, Constants.PREF_KEY_SERVICE_RUNNING, false);
        stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopTracking();
    }

    @Override
    public void onConnected(Bundle bundle) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    private void handleNewLocation(Location location) {
        Location locationIntrepid = new Location("");
        locationIntrepid.setLatitude(INTREPID_LATITUDE);
        locationIntrepid.setLongitude(INTREPID_LONGITUDE);

        //unit of measure is in meters
        float distanceToIntrepid = locationIntrepid.distanceTo(location);

        if (distanceToIntrepid < CHECK_IN_RADIUS){
            boolean notificationShowing = PreferenceManagerCustom.getBoolean(this,
                    Constants.PREF_KEY_NOTIFICATION_SHOWING, false);

            boolean betweenTwoAndFourAM = hour > 2 && hour < 4;
            if (!notificationShowing && !betweenTwoAndFourAM) {

                notificationMessage = getString(R.string.user_arrived_toast_message, personName,
                        (int)distanceToIntrepid);

                Intent intentCancelCheckin = new Intent(this, CancelCheckInReceiver.class);
                PendingIntent pendingIntentCancelCheckin = PendingIntent.getBroadcast(this, 0,
                        intentCancelCheckin, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent intentCheckin = new Intent(this, CheckInReceiver.class);
                PendingIntent pendingIntentCheckin = PendingIntent.getBroadcast(this, 0,
                        intentCheckin, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Intrepid Check In")
                                .setContentIntent(pendingIntentCheckin)
                                .addAction(R.drawable.xmark, "Clear", pendingIntentCancelCheckin)
                                .addAction(R.drawable.checkmark, "Check In", pendingIntentCheckin)
                                .setContentText(notificationMessage);
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.notify(Constants.STATUS_BAR_NOTIFICATION_ID, mBuilder.build());

                PreferenceManagerCustom.putBoolean(this, Constants.PREF_KEY_NOTIFICATION_SHOWING, true);
            }
        }
        else{
            notificationMessage = getString(R.string.user_location_toast_message, personName,
                    (int)distanceToIntrepid);
        }
        toastHandler.setToastMessage(notificationMessage);
        toastHandler.sendEmptyMessage(0);    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
        String updatedLocationMessage = getString(R.string.location_update_toast_message,
                location.getLatitude(), location.getLongitude());

        Toast.makeText(this, updatedLocationMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        notificationMessage = getString(R.string.connection_failed_toast_message);
        toastHandler.setToastMessage(notificationMessage);
        toastHandler.sendEmptyMessage(0);
        stopTracking();
    }

}
