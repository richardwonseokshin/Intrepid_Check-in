package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
        String username = PreferenceManagerCustom.getString(context, Constants.PREF_KEY_USERNAME, context.getString(R.string.default_username));
        String stringCheckIn = context.getString(R.string.check_in_string, Calendar.getInstance().getTime(), username);

        //if user "cancels" today's checkin, make app think that the notification is already showing so that it doesn't re-show the notification
        PreferenceManagerCustom.putBoolean(context, Constants.PREF_KEY_NOTIFICATION_SHOWING, true);

        //Send message to slack using slack api http post, use retrofit
        //parameters that can be set (for future reference): setconverter(GsonConverter),setloglevel, review retrofit serializedname
        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Constants.URL_SLACK_WEBHOOK_ENDPOINT).build();
        SlackWebhookService service = restAdapter.create(SlackWebhookService.class);

        SlackMessage message = new SlackMessage();
        message.text = stringCheckIn;
        service.sendCheckinMessage(BuildConfig.webhook_url ,message, new Callback<Object>() {
            @Override
            public void success(Object object, Response response) {}

            @Override
            public void failure(RetrofitError retrofitError) {}
        });

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(Constants.STATUS_BAR_NOTIFICATION_ID);
        mNotificationManager.cancelAll();
    }
}
