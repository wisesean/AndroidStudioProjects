package com.wisesean.heartbeat.alarm;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;

public class AalarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
        if (!Alarms.ALARM_ALERT_ACTION.equals(intent.getAction())) {
            return;
        }

        Alarm alarm = null;
        final byte[] data = intent.getByteArrayExtra(Alarms.ALARM_RAW_DATA);
        if (data != null) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            alarm = Alarm.CREATOR.createFromParcel(in);
        }

        if (alarm == null) {
            Alarms.setNextAlert(context);
            return;
        }

        if (!alarm.daysOfWeek.isRepeatSet()) {
            Alarms.enableAlarm(context, alarm.id, false);
        } else {
            Alarms.setNextAlert(context);
        }

        AlarmAlertWakeLock.acquireCpuWakeLock(context);

        Intent notify = new Intent(context, AlarmAlert.class);
        notify.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notify.putExtra(Alarms.ALARM_INTENT_EXTRA, alarm);
        context.startActivity(notify);
	}

    private NotificationManager getNotificationManager(Context context) {
        return (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
