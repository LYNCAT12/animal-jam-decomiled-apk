package net.agasper.unitynotification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import com.kidoz.sdk.api.general.ContentExecutionHandler;
import com.unity3d.player.UnityPlayer;

public class UnityNotificationManager extends BroadcastReceiver {
    public static void SetNotification(int i, long j, String str, String str2, String str3, int i2, int i3, int i4, String str4, String str5, int i5, int i6, String str6) {
        int i7 = i;
        int i8 = i6;
        Activity activity = UnityPlayer.currentActivity;
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService("alarm");
        Intent intent = new Intent(activity, UnityNotificationManager.class);
        String str7 = str3;
        intent.putExtra("ticker", str3);
        String str8 = str;
        intent.putExtra("title", str);
        String str9 = str2;
        intent.putExtra("message", str2);
        intent.putExtra("id", i);
        intent.putExtra("color", i5);
        intent.putExtra("sound", i2 == 1);
        intent.putExtra("vibrate", i3 == 1);
        intent.putExtra("lights", i4 == 1);
        intent.putExtra("l_icon", str4);
        intent.putExtra("s_icon", str5);
        intent.putExtra("activity", str6);
        if (Build.VERSION.SDK_INT < 23) {
            alarmManager.set(0, System.currentTimeMillis() + j, PendingIntent.getBroadcast(activity, i, intent, 0));
        } else if (i8 == 2) {
            alarmManager.setExactAndAllowWhileIdle(0, System.currentTimeMillis() + j, PendingIntent.getBroadcast(activity, i, intent, 0));
        } else if (i8 == 1) {
            alarmManager.setExact(0, System.currentTimeMillis() + j, PendingIntent.getBroadcast(activity, i, intent, 0));
        } else {
            alarmManager.set(0, System.currentTimeMillis() + j, PendingIntent.getBroadcast(activity, i, intent, 0));
        }
    }

    public static void SetRepeatingNotification(int i, long j, String str, String str2, String str3, long j2, int i2, int i3, int i4, String str4, String str5, int i5, String str6) {
        int i6 = i;
        Activity activity = UnityPlayer.currentActivity;
        AlarmManager alarmManager = (AlarmManager) activity.getSystemService("alarm");
        Intent intent = new Intent(activity, UnityNotificationManager.class);
        String str7 = str3;
        intent.putExtra("ticker", str3);
        String str8 = str;
        intent.putExtra("title", str);
        String str9 = str2;
        intent.putExtra("message", str2);
        intent.putExtra("id", i);
        intent.putExtra("color", i5);
        boolean z = true;
        intent.putExtra("sound", i2 == 1);
        intent.putExtra("vibrate", i3 == 1);
        if (i4 != 1) {
            z = false;
        }
        intent.putExtra("lights", z);
        intent.putExtra("l_icon", str4);
        intent.putExtra("s_icon", str5);
        intent.putExtra("activity", str6);
        alarmManager.setRepeating(0, System.currentTimeMillis() + j, j2, PendingIntent.getBroadcast(activity, i, intent, 0));
    }

    public void onReceive(Context context, Intent intent) {
        Class<?> cls;
        Context context2 = context;
        Intent intent2 = intent;
        NotificationManager notificationManager = (NotificationManager) context2.getSystemService("notification");
        String stringExtra = intent2.getStringExtra("ticker");
        String stringExtra2 = intent2.getStringExtra("title");
        String stringExtra3 = intent2.getStringExtra("message");
        String stringExtra4 = intent2.getStringExtra("s_icon");
        String stringExtra5 = intent2.getStringExtra("l_icon");
        int intExtra = intent2.getIntExtra("color", 0);
        String stringExtra6 = intent2.getStringExtra("activity");
        Boolean valueOf = Boolean.valueOf(intent2.getBooleanExtra("sound", false));
        Boolean valueOf2 = Boolean.valueOf(intent2.getBooleanExtra("vibrate", false));
        Boolean valueOf3 = Boolean.valueOf(intent2.getBooleanExtra("lights", false));
        int intExtra2 = intent2.getIntExtra("id", 0);
        Resources resources = context.getResources();
        try {
            cls = Class.forName(stringExtra6);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            cls = null;
        }
        PendingIntent activity = PendingIntent.getActivity(context2, 0, new Intent(context2, cls), 0);
        Notification.Builder builder = new Notification.Builder(context2);
        NotificationManager notificationManager2 = notificationManager;
        builder.setContentIntent(activity).setWhen(System.currentTimeMillis()).setAutoCancel(true).setContentTitle(stringExtra2).setContentText(stringExtra3);
        if (Build.VERSION.SDK_INT >= 21) {
            builder.setColor(intExtra);
        }
        if (stringExtra != null && stringExtra.length() > 0) {
            builder.setTicker(stringExtra);
        }
        if (stringExtra4 != null && stringExtra4.length() > 0) {
            builder.setSmallIcon(resources.getIdentifier(stringExtra4, "drawable", context.getPackageName()));
        }
        if (stringExtra5 != null && stringExtra5.length() > 0) {
            builder.setLargeIcon(BitmapFactory.decodeResource(resources, resources.getIdentifier(stringExtra5, "drawable", context.getPackageName())));
        }
        if (valueOf.booleanValue()) {
            builder.setSound(RingtoneManager.getDefaultUri(2));
        }
        if (valueOf2.booleanValue()) {
            builder.setVibrate(new long[]{1000, 1000});
        }
        if (valueOf3.booleanValue()) {
            builder.setLights(-16711936, ContentExecutionHandler.PROMOTED_CONTENT_CLICK_RESTORE_TIMEOUT, ContentExecutionHandler.PROMOTED_CONTENT_CLICK_RESTORE_TIMEOUT);
        }
        notificationManager2.notify(intExtra2, builder.build());
    }

    public static void CancelNotification(int i) {
        Activity activity = UnityPlayer.currentActivity;
        ((AlarmManager) activity.getSystemService("alarm")).cancel(PendingIntent.getBroadcast(activity, i, new Intent(activity, UnityNotificationManager.class), 0));
    }

    public static void CancelAll() {
        ((NotificationManager) UnityPlayer.currentActivity.getApplicationContext().getSystemService("notification")).cancelAll();
    }
}
