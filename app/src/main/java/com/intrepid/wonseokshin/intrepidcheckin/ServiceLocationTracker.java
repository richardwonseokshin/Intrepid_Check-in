package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class ServiceLocationTracker extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public static final String TAG = ServiceLocationTracker.class.getSimpleName();

    public static final int LOCATION_UPDATE_INTERVAL = 10 * 1000;//10,000 ms, 10 sec
    public static final int LOCATION_UPDATE_FASTEST_INTERVAL = 1 * 1000;//1,000 ms, 1 sec

    public static final int TIMER_INTERVAL = 5 * 1000;//timer manually grabs the lastlocation

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private String personName = "";
    private String notificationMessage;
    private int mNotificationId = 1;
    private Timer timer;

    int hour;

    //handler to post toast messages
    private final Handler toastHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Toast.makeText(getApplicationContext(), notificationMessage, Toast.LENGTH_SHORT).show();
        }
    };


    public ServiceLocationTracker() {}

    //Service
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public void onCreate() {
        Toast.makeText(this, "Service was Created", Toast.LENGTH_LONG).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String usernameFromIntent = null;
        if(intent != null) {
            usernameFromIntent = intent.getStringExtra("username");
        }

        if(usernameFromIntent.compareTo("") != 0) {
            PreferenceManagerCustom.putString(this, "username", usernameFromIntent);
            personName = PreferenceManagerCustom.getString(this, "username", "Olaf");
        }

        PreferenceManagerCustom.putBoolean(this, "servicerunning", true);

        return startTracking();
    }

    private int startTracking(){
        hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if(mLocationRequest == null) {
            // Create the LocationRequest object
            mLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(LOCATION_UPDATE_INTERVAL)
                    .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL);
        }

        personName = PreferenceManagerCustom.getString(this, "username", "Olaf");
        //Just to be doubly sure that we are not using an empty string
        if(personName.compareTo("") == 0)
            personName = "Olaf";

        Toast.makeText(this, "Hello " + personName + "!\nService Started", Toast.LENGTH_LONG).show();

        mGoogleApiClient.connect();

        return START_STICKY;
    }

    private int stopTracking(){
        notificationMessage = "Service Destroyed";

        if (toastHandler != null) {
            toastHandler.sendEmptyMessage(0);
        }

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }

        if (timer != null) {
            timer.cancel();
            timer = null;
            System.gc();
        }

        PreferenceManagerCustom.putBoolean(this, "servicerunning", false);

        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopTracking();
    }

    //ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(location);
        }

        if(timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {


                @Override
                public void run() {
                    Location location = null;

                    if(mGoogleApiClient != null)
                        location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                    if(location != null)
                        handleNewLocation(location);


                    hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if(PreferenceManagerCustom.getBoolean(ServiceLocationTracker.this, "notificationshowing", true) && hour > 2 && hour < 4){
                        PreferenceManagerCustom.putBoolean(ServiceLocationTracker.this, "notificationshowing", false);

                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.cancel(1);
                        mNotificationManager.cancelAll();
                    }
                }
            }, 0, TIMER_INTERVAL);
        }
    }

    private void handleNewLocation(Location location) {
        //Intrepid: 42.366980, -71.080161
        Location locationIntrepid = new Location("");
        locationIntrepid.setLatitude(42.366980);
        locationIntrepid.setLongitude(-71.080161);

        //unit of measure is in meters
        float distanceToIntrepid = locationIntrepid.distanceTo(location);

        if(distanceToIntrepid < 100){
            if(!PreferenceManagerCustom.getBoolean(this, "notificationshowing", false) && (hour <=2 || hour >= 4)) {
                notificationMessage = personName + " is at Intrepid Pursuits!\n(" + ((int) distanceToIntrepid) + " meters)";

                Intent intentCancelCheckin = new Intent(this, CancelCheckInReceiver.class);
                PendingIntent pendingIntentCancelCheckin = PendingIntent.getBroadcast(this, 0, intentCancelCheckin, PendingIntent.FLAG_UPDATE_CURRENT);

                Intent intentCheckin = new Intent(this, CheckInReceiver.class);
                PendingIntent pendingIntentCheckin = PendingIntent.getBroadcast(this, 0, intentCheckin, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder =
                        (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.notification_icon)
                                .setContentTitle("Intrepid Check In")
                                .setContentIntent(pendingIntentCheckin)
                                .addAction(R.drawable.xmark, "Clear", pendingIntentCancelCheckin)
                                .addAction(R.drawable.checkmark, "Check In", pendingIntentCheckin)
                                .setContentText(notificationMessage);

                // Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                // Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

                PreferenceManagerCustom.putBoolean(this, "notificationshowing", true);
            }
        }
        else{
            notificationMessage = personName + " is " + ((int)distanceToIntrepid) + " meters from Intrepid Pursuits!";
        }
        toastHandler.sendEmptyMessage(0);
    }

    @Override
    public void onConnectionSuspended(int i) {}

    //LocationListener
    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
        Toast.makeText(this, "New Location Data Received", Toast.LENGTH_LONG).show();
    }

    //OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        notificationMessage = "Connection Failed! :(";
        toastHandler.sendEmptyMessage(0);
    }

}
