package com.intrepid.wonseokshin.intrepidcheckin;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CancelCheckInReceiver extends BroadcastReceiver {

    public CancelCheckInReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceManagerCustom.putBoolean(context, Constants.PREF_KEY_NOTIFICATION_SHOWING, true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
        mNotificationManager.cancelAll();
    }

}
