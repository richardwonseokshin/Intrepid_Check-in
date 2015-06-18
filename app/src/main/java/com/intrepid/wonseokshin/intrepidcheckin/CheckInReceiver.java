package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CheckInReceiver extends BroadcastReceiver {

    public CheckInReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String username = PreferenceManagerCustom.getString(context, "username", "Olaf");
        String stringCheckIn = Calendar.getInstance().getTime() + " - Richard's Check-in App - " + username + " has checked in";

        //if user "cancels" today's checkin, make app think that the notification is already showing so that it doesn't re-show the notification
        PreferenceManagerCustom.putBoolean(context, "notificationshowing", true);

        //Send message to slack using slack api http post, use retrofit
        //parameters that can be set (for future reference): setconverter(GsonConverter),setloglevel, review retrofit serializedname
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://hooks.slack.com").build();
        SlackWebhookService service = restAdapter.create(SlackWebhookService.class);

        SlackMessage message = new SlackMessage();
        message.text = stringCheckIn;
        service.sendCheckinMessage(BuildConfig.webhook_url ,message, new Callback<Void>() {
            @Override
            public void success(Void aVoid, Response response) {}

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("Retrofit", "Failed to send slack request.");
            }
        });

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
        mNotificationManager.cancelAll();
    }
}
