package com.wisesean.heartbeat.alarm;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.text.format.DateFormat;

public class Alarms {
	public static final String ALARM_ALERT_ACTION = "com.foxmail.wisesean.ALARM_ALERT";
	
	// This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

    // This string is used when passing an Alarm object through an intent.
    public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";
    
    // This string is used to indicate a silent alarm in the db.
    public static final String ALARM_ALERT_SILENT = "silent";

    private final static String M12 = "h:mm aa";
    // Shared with DigitalClock
    final static String M24 = "kk:mm";
	
	public static Object[] addAlarm(Context context,Alarm alarm) {
        ContentValues values = createContentValues(alarm);
        Uri uri = context.getContentResolver().insert(Alarm.Columns.CONTENT_URI, values);
        alarm.id = (int) ContentUris.parseId(uri);

        long timeInMillis = calculateAlarm(alarm);
        setNextAlert(context);
        return new Object[] { alarm.id,timeInMillis };
	}
	
	public static void deleteAlarm(Context context, int alarmId) {

        ContentResolver contentResolver = context.getContentResolver();
        setNextAlert(context);
        
        Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId);
        contentResolver.delete(uri, "", null);
    }
	
	/**
     * Return an Alarm object representing the alarm id in the database.
     * Returns null if no alarm exists.
     */
    public static Alarm getAlarm(ContentResolver contentResolver, int alarmId) {
        Cursor cursor = contentResolver.query(
                ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
                Alarm.Columns.ALARM_QUERY_COLUMNS,
                null, null, null);
        Alarm alarm = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                alarm = new Alarm(cursor);
            }
            cursor.close();
        }
        return alarm;
    }
    
    /**
     * A convenience method to set an alarm in the Alarms
     * content provider.
     * @return Time when the alarm will fire.
     */
    public static long setAlarm(Context context, Alarm alarm) {
        ContentValues values = createContentValues(alarm);
        ContentResolver resolver = context.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
                values, null, null);

        long timeInMillis = calculateAlarm(alarm);
        return timeInMillis;
    }
	
    public static void setNextAlert(final Context context) {
            Alarm alarm = calculateNextAlert(context);
            if (alarm != null) {
                enableAlert(context, alarm, alarm.time);
            } else {
                disableAlert(context);
            }
    }
    
    @SuppressLint("Recycle")
	private static void enableAlert(Context context, final Alarm alarm,
            final long atTimeInMillis) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Log.LOGV) {
            Log.v("** setAlert id " + alarm.id + " atTime " + atTimeInMillis);
        }

        Intent intent = new Intent(ALARM_ALERT_ACTION);

        Parcel out = Parcel.obtain();
        alarm.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(ALARM_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(atTimeInMillis);
        Log.v("enableAlert\talarm.id:"+alarm.id);
    }
    
    /**
     * Disables alert in AlarmManger and StatusBar.
     *
     */
    static void disableAlert(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, new Intent(ALARM_ALERT_ACTION),
                PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }
    
    public static void enableAlarm(final Context context, final int id, boolean enabled){
        enableAlarmInternal(context, id, enabled);
        setNextAlert(context);
    }
    
    public static Alarm calculateNextAlert(final Context context) {
        Alarm alarm = null;
        long minTime = Long.MAX_VALUE;
        long now = System.currentTimeMillis();
        Cursor cursor = getFilteredAlarmsCursor(context.getContentResolver());
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Alarm a = new Alarm(cursor);
                    if (a.time == 0) {
                        a.time = calculateAlarm(a);
                    } else if (a.time < now) {
                        // 过期的闹铃
                        enableAlarmInternal(context, a, false);
                        continue;
                    }
                    if (a.time < minTime) {
                        minTime = a.time;
                        alarm = a;
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return alarm;
    }
    
    private static void enableAlarmInternal(final Context context,
            final Alarm alarm, boolean enabled) {
        if (alarm == null) {
            return;
        }
        
        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues(2);
        values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

        // If we are enabling the alarm, calculate alarm time since the time
        // value in Alarm may be old.
        if (enabled) {
            long time = 0;
            if (!alarm.daysOfWeek.isRepeatSet()) {
                time = calculateAlarm(alarm);
            }
            values.put(Alarm.Columns.ALARM_TIME, time);
        }

        resolver.update(ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id), values, null, null);
    }
    
    private static void enableAlarmInternal(final Context context,
            final int id, boolean enabled) {
        enableAlarmInternal(context, getAlarm(context.getContentResolver(), id),
                enabled);
    }
    
    private static Cursor getFilteredAlarmsCursor(
            ContentResolver contentResolver) {
        return contentResolver.query(Alarm.Columns.CONTENT_URI,
                Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED,
                null, null);
    }
	
	private static long calculateAlarm(Alarm alarm) {
        return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek).getTimeInMillis();
    }
	
	/**
     * Given an alarm in hours and minutes, return a time suitable for
     * setting in AlarmManager.
     */
    static Calendar calculateAlarm(int hour, int minute,
            Alarm.DaysOfWeek daysOfWeek) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        int addDays = daysOfWeek.getNextAlarm(c);
        if (addDays > 0) c.add(Calendar.DAY_OF_WEEK, addDays);
        return c;
    }

    static String formatTime(final Context context, int hour, int minute,
                             Alarm.DaysOfWeek daysOfWeek) {
        Calendar c = calculateAlarm(hour, minute, daysOfWeek);
        return formatTime(context, c);
    }

    /* used by AlarmAlert */
    static String formatTime(final Context context, Calendar c) {
        String format = get24HourMode(context) ? M24 : M12;
        return (c == null) ? "" : (String) DateFormat.format(format, c);
    }

    /**
     * @return true if clock is set to 24-hour mode
     */
    public static boolean get24HourMode(final Context context) {
        return android.text.format.DateFormat.is24HourFormat(context);
    }
	
	public static ContentValues createContentValues(Alarm alarm) {
      ContentValues values = new ContentValues(10);
      
      if (!alarm.daysOfWeek.isRepeatSet()) {
      }

      values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
      values.put(Alarm.Columns.HOUR, alarm.hour);
      values.put(Alarm.Columns.MINUTES, alarm.minutes);
      values.put(Alarm.Columns.ALARM_TIME, alarm.time);
      values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
      values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
      values.put(Alarm.Columns.MESSAGE, alarm.label);

      // A null alert Uri indicates a silent alarm.
      values.put(Alarm.Columns.ALERT, alarm.alert == null ? ALARM_ALERT_SILENT
              : alarm.alert.toString());

      values.put(Alarm.Columns.TIMES, alarm.times);
      values.put(Alarm.Columns.INTERVAL, alarm.interval);
      
      return values;
  }
}
