package com.wisesean.heartbeat.alarm;

import android.app.TimePickerDialog;
import android.content.Context;

import java.util.Calendar;

/**
 * Created by wisesean on 14-3-6.
 */
public class MyTimePickerDialog extends TimePickerDialog {
    public MyTimePickerDialog(Context context, OnTimeSetListener callBack,
                              int hourOfDay, int minute, boolean is24HourView) {
        super(context, callBack,   hourOfDay,  minute,  is24HourView);
    }
    public MyTimePickerDialog(Context context, OnTimeSetListener callBack, Calendar c ) {
        super(context, callBack, c.get(Calendar.HOUR_OF_DAY),  c.get(Calendar.MINUTE),  true);
    }

    @Override
    protected void onStop() {
        //super.onStop();
    }
}