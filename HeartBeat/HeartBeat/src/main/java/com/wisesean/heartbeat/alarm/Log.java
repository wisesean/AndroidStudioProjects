package com.wisesean.heartbeat.alarm;

public class Log {
    public final static String LOGTAG = "AlarmClock";
    static final boolean LOGV = true;
    public static void v(String logMe) {
        android.util.Log.v(LOGTAG, logMe);
    }

    static void i(String logMe) {
        android.util.Log.i(LOGTAG, logMe);
    }

    static void e(String logMe) {
        android.util.Log.e(LOGTAG, logMe);
    }

    static void e(String logMe, Exception ex) {
        android.util.Log.e(LOGTAG, logMe, ex);
    }
}