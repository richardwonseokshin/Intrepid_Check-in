package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {


    private Timer timer;
    private boolean preferenceServiceRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((EditText)findViewById(R.id.etName)).setText(PreferenceManagerCustom.getString(this, "username", "Olaf"));
        preferenceServiceRunning = PreferenceManagerCustom.getBoolean(MainActivity.this, "servicerunning", false);


        //Use a timer to check whether the service is running or stopped
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            boolean tickerShowing = false;

            @Override
            public void run() {
                if (preferenceServiceRunning) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tickerShowing) {
                                findViewById(R.id.tv_service_running).setVisibility(View.INVISIBLE);
                            } else {
                                findViewById(R.id.tv_service_running).setVisibility(View.VISIBLE);
                            }
                            findViewById(R.id.tv_service_running).postInvalidate();
                        }
                    });

                    tickerShowing = !tickerShowing;
                }
            }

        },0,200);

        //Add in animations for the interface from the activity
        //Basic Animation Resources: http://stackoverflow.com/questions/18147840/slide-right-to-left-android-animations
        Animation animationSlideInLeft = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_from_left);
        animationSlideInLeft.setDuration(1000);

        Animation animationSlideInRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fly_in_from_right);
        animationSlideInRight.setDuration(1000);

        findViewById(R.id.ivintrepidlogo).startAnimation(animationSlideInRight);
        findViewById(R.id.buttonStartTracking).startAnimation(animationSlideInRight);


        findViewById(R.id.tv_header).startAnimation(animationSlideInLeft);
        findViewById(R.id.etName).startAnimation(animationSlideInLeft);
        findViewById(R.id.buttonStopTracking).startAnimation(animationSlideInLeft);
    }

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
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
    }


    // Start the service
    public void startService(View view) {
        PreferenceManagerCustom.putBoolean(this, "servicerunning", true);
        preferenceServiceRunning = true;

        //Used Extras and SharedPrefs to send data to service,
        //Using services alone caused issues for some reason
        //Should look into SharedPrefs more, how services, broadcastreceivers, etc. fit in
        Intent intent = new Intent(this, ServiceLocationTracker.class);
        String name = ((EditText)findViewById(R.id.etName)).getText().toString();
        if(name.compareTo("") == 0){
            name = "Olaf";
        }
        intent.putExtra("username", name);

        PreferenceManagerCustom.putString(this, "username", name);
        PreferenceManagerCustom.putBoolean(this, "notificationshowing", false);

        startService(intent);
    }

    // Triggered when user taps on Stop Tracking button
    public void stopService(View view) {
        //Stop flicker of textview with text "Service is Running"
        timer.cancel();
        preferenceServiceRunning = false;
        PreferenceManagerCustom.putBoolean(this, "servicerunning", false);

        findViewById(R.id.tv_service_running).setVisibility(View.INVISIBLE);
        findViewById(R.id.tv_service_running).postInvalidate();

        Intent intent = new Intent(this, ServiceLocationTracker.class);

        PreferenceManagerCustom.putBoolean(this, "notificationshowing", false);

        stopService(intent);
    }



}
