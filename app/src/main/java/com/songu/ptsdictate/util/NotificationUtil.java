package com.songu.ptsdictate.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.widget.RemoteViews;

import com.songu.ptsdictate.R;
import com.songu.ptsdictate.activity.MainActivity;

/**
 * Created by Administrator on 4/27/2018.
 */

public class NotificationUtil {

    FragmentActivity myContext;

    // Notification Manager Object
    NotificationManager myNotificationManager;

    // Notification Object
    Notification myNotification;

    // RemoteView Object for showing Notification
    RemoteViews myContentView;

    // Id for the Notification
    int NOTIFICATION_ID = 1;

    @SuppressWarnings("deprecation")
    public NotificationUtil(FragmentActivity aContext) {

        this.myContext = aContext;

        myNotificationManager = (NotificationManager) myContext
                .getSystemService(Context.NOTIFICATION_SERVICE);

        int icon = R.drawable.icn_record;
        long when = System.currentTimeMillis();
        myNotification = new Notification(icon,
                myContext.getString(R.string.app_name), when);

    }

    public void ShowNotification(String aStatus) {

        myContentView = new RemoteViews(
                ((FragmentActivity) myContext).getPackageName(),
                R.layout.custom_notification);
        myContentView.setImageViewResource(R.id.notification_image,
                R.drawable.icn_record);
        myContentView.setTextViewText(R.id.notification_title,
                myContext.getString(R.string.Alert_App_name));

        myContentView.setTextViewText(R.id.notification_text, aStatus);

        myContentView.setTextViewText(R.id.notification_timer, "");
        myNotification.contentView = myContentView;

        Intent notificationIntent = new Intent(myContext, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(myContext, 0,
                notificationIntent, 0);

        myNotification.contentIntent = contentIntent;

        myNotification.flags |= Notification.FLAG_NO_CLEAR; // Do not clear the
        // notification

        // myNotification.defaults
        // |=
        // Notification.DEFAULT_LIGHTS;
        // // LED
        // myNotification.defaults |= Notification.DEFAULT_VIBRATE; // Vibration
        // myNotification.defaults |= Notification.DEFAULT_SOUND; // Sound

        myNotificationManager.notify(NOTIFICATION_ID, myNotification);

    }

    public void UpdateTimer(String aTime) {

        myContentView.setTextViewText(R.id.notification_timer, aTime);
        myNotificationManager.notify(NOTIFICATION_ID, myNotification);

    }

    public void dismissNotification() {

        myNotificationManager.cancel(NOTIFICATION_ID);

    }

}
