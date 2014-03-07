package com.wisesean.heartbeat.util;

import java.util.Calendar;
import java.util.Date;
/**
 * Created by wisesean on 14-2-27.
 */
public class Util {
    public static String getWeekOfDate() {
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        Date curDate = new Date(System.currentTimeMillis());
        cal.setTime(curDate);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) w = 0;
        String weekDay = weekDays[w];
        int mMonth=cal.get(Calendar.MONTH);
        int mDay=cal.get(Calendar.DAY_OF_MONTH);
        int Month=mMonth+1;
        return String.format("%1$s,%2$s日%3$s月",weekDay,mDay,Month);
    }
}
