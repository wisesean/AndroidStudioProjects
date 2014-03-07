package com.wisesean.heartbeat.alarm;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.wisesean.heartbeat.dslv.SlideCutListView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wisesean on 14-2-25.
 */
public class ActivityOne extends Activity implements
        TimePickerDialog.OnTimeSetListener, SlideCutListView.RemoveListener {

//   private ListView listView;
   private SlideCutListView slideCutListView;
   private SimpleAdapter adapter;
   private List<Map<String,Object>> listResult;
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       //在setContentView之前请求窗口特征
       requestWindowFeature(Window.FEATURE_RIGHT_ICON);
       setContentView(R.layout.main);

       init();

//       listView = (ListView) findViewById(R.id.listView_alarm_list);
//       SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.desk_clock,
//               new String[]{"time","tag","alarm_ic_list"},
//               new int[]{R.id.time,R.id.tag,R.id.alarm_ic_list});
//       listView.setAdapter(adapter);
//       listView.setVerticalScrollBarEnabled(true);
   }

    private void init() {
        slideCutListView = (SlideCutListView) findViewById(R.id.slideCutListView);
        slideCutListView.setRemoveListener(this);

        listResult = getData();
        adapter = new SimpleAdapter(this,listResult,R.layout.desk_clock,
                new String[]{"time","tag","alarm_ic_list"},
                new int[]{R.id.time,R.id.tag,R.id.alarm_ic_list});
        slideCutListView.setAdapter(adapter);

        slideCutListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }
        });
    }

    public void removeItem(SlideCutListView.RemoveDirection direction, int position) {
        Map<String,Object> selectItem = this.listResult.get(position);
        int id = Integer.parseInt(String.valueOf(selectItem.get("id")));
        Alarms.deleteAlarm(this, id);
        this.listResult.remove(position);
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "删除闹铃"+ position, Toast.LENGTH_SHORT).show();
    }

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Alarm.Columns.CONTENT_URI,
                Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, "_id");


        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(Alarm.Columns.ALARM_ID_INDEX);
                int hour = cursor.getInt(Alarm.Columns.ALARM_HOUR_INDEX);
                int minutes = cursor.getInt(Alarm.Columns.ALARM_MINUTES_INDEX);
                String message = cursor.getString(Alarm.Columns.ALARM_MESSAGE_INDEX);
                int enable = cursor.getInt(Alarm.Columns.ALARM_ENABLED_INDEX);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id",id);
                map.put("time", String.format("%02d",hour)+":"+String.format("%02d",minutes));
                map.put("tag", message);
                if(enable == 1){
                    map.put("alarm_ic_list", R.drawable.ic_lock_idle_alarm_saver);
                }else {
                    map.put("alarm_ic_list", R.drawable.ic_lock_idle_alarm);
                }
                list.add(map);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }

    private class CustomAdapter extends ArrayAdapter<String> {
        public CustomAdapter(Context context, int layout, int resId, String[] items) {
            super(context, layout, resId, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if(row == null) {
                row = getLayoutInflater().inflate(R.layout.custom_row,parent,false);
            }

            String item = getItem(position);
            ImageView left = (ImageView)row.findViewById(R.id.leftimage);
            ImageView right = (ImageView)row.findViewById(R.id.rightimage);
            TextView text = (TextView)row.findViewById(R.id.line1);

            left.setImageResource(R.drawable.ic_clock_alarm_on);
            right.setImageResource(R.drawable.ic_clock_alarm_off);
            text.setText(item);

            return row;
        }
    }

    public void addAlarm(View view) {
        showTimePicker();
    }

    private void showTimePicker() {
        new MyTimePickerDialog(this, Calendar.HOUR_OF_DAY , Calendar.MINUTE,
                DateFormat.is24HourFormat(this)).show();
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Alarm alarm = new Alarm();
        alarm.enabled = true;
        alarm.hour = hourOfDay;
        alarm.minutes = minute;

        alarm.interval = 0;

        long time = Alarms.addAlarm(this, alarm);

        popAlarmSetToast(this, time);
    }

    static void popAlarmSetToast(Context context, int hour, int minute,
                                 Alarm.DaysOfWeek daysOfWeek) {
        popAlarmSetToast(context,
                Alarms.calculateAlarm(hour, minute, daysOfWeek)
                        .getTimeInMillis());
    }

    private static void popAlarmSetToast(Context context, long timeInMillis) {
        String toastText = formatToast(context, timeInMillis);
        Toast toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG);
        ToastMaster.setToast(toast);
        toast.show();
    }

    static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / (1000 * 60 * 60);
        long minutes = delta / (1000 * 60) % 60;
        long days = hours / 24;
        hours = hours % 24;

        String daySeq = (days == 0) ? "" : (days == 1) ? context
                .getString(R.string.day) : context.getString(R.string.days,
                Long.toString(days));

        String minSeq = (minutes == 0) ? "" : (minutes == 1) ? context
                .getString(R.string.minute) : context.getString(
                R.string.minutes, Long.toString(minutes));

        String hourSeq = (hours == 0) ? "" : (hours == 1) ? context
                .getString(R.string.hour) : context.getString(R.string.hours,
                Long.toString(hours));

        boolean dispDays = days > 0;
        boolean dispHour = hours > 0;
        boolean dispMinute = minutes > 0;

        int index = (dispDays ? 1 : 0) | (dispHour ? 2 : 0)
                | (dispMinute ? 4 : 0);

        String[] formats = context.getResources().getStringArray(
                R.array.alarm_set);
        return String.format(formats[index], daySeq, hourSeq, minSeq);
    }
}
