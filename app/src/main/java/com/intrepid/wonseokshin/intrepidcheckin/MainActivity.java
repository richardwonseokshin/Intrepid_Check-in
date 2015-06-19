package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private EditText inputUsername;
    private TextView serviceRunningDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputUsername = ((EditText)findViewById(R.id.etName));
        String usernameFromPreferences = PreferenceManagerCustom.getString(this, Constants.PREF_KEY_USERNAME, getString(R.string.default_username));
        inputUsername.setText(usernameFromPreferences);

        serviceRunningDisplay = (TextView) findViewById(R.id.tv_service_running);
        serviceRunningDisplay.setVisibility(View.INVISIBLE);
        serviceRunningDisplay.postInvalidate();

        showInterfaceAnimation();
    }

    public void showInterfaceAnimation(){
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


    public void startService(View view) {
        //Used Extras and SharedPrefs to send data to service,
        //Using services alone caused issues for some reason
        //Should look into SharedPrefs more, how services, broadcastreceivers, etc. fit in
        Intent intent = new Intent(this, ServiceLocationTracker.class);
        String name = inputUsername.getText().toString();
        if (TextUtils.isEmpty(name)) {
            name = getString(R.string.default_username);
        }
        intent.putExtra(Constants.INTENT_EXTRA_USERNAME, name);

        PreferenceManagerCustom.putString(this, Constants.PREF_KEY_USERNAME, name);
        PreferenceManagerCustom.putBoolean(this, Constants.PREF_KEY_NOTIFICATION_SHOWING, false);
        PreferenceManagerCustom.putBoolean(this, Constants.PREF_KEY_SERVICE_RUNNING, true);

        startService(intent);

        serviceRunningDisplay.setVisibility(View.VISIBLE);
        serviceRunningDisplay.postInvalidate();
    }

    // Triggered when user taps on Stop Tracking button
    public void stopService(View view) {
        serviceRunningDisplay.setVisibility(View.INVISIBLE);
        serviceRunningDisplay.postInvalidate();

        Intent intent = new Intent(this, ServiceLocationTracker.class);
        stopService(intent);

        PreferenceManagerCustom.putBoolean(this, Constants.PREF_KEY_SERVICE_RUNNING, false);
        PreferenceManagerCustom.putBoolean(this, Constants.PREF_KEY_NOTIFICATION_SHOWING, false);

        serviceRunningDisplay.setVisibility(View.INVISIBLE);
        serviceRunningDisplay.postInvalidate();
    }


    @Override
    protected void onStop() {
        super.onStop();
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


}
