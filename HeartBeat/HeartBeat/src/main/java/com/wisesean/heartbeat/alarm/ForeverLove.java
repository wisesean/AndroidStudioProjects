package com.wisesean.heartbeat.alarm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class ForeverLove extends Activity implements OnItemClickListener{
	private ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm_clock);
		listView = (ListView) findViewById(R.id.listView_alrm_list);
		SimpleAdapter adapter = new SimpleAdapter(this,getData(),R.layout.desk_clock,
				new String[]{"time","tag","alarm_ic_list"},
				new int[]{R.id.time,R.id.tag,R.id.alarm_ic_list});
		listView.setAdapter(adapter);
//		listView.setOnItemSelectedListener(new OnItemSelectedListener() {
//
//			@Override
//			public void onItemSelected(AdapterView<?> arg0, View arg1,
//					int arg2, long arg3) {
//				System.out.println(1);
//			}
//
//			@Override
//			public void onNothingSelected(AdapterView<?> arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//		});
		listView.setVerticalScrollBarEnabled(true);
		listView.setOnItemClickListener(this);
		listView.setOnCreateContextMenuListener(this);
	}
	
	private List<Map<String, Object>> getData() {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(Alarm.Columns.CONTENT_URI, 
				new String[] { "_id", "hour", "minutes", "message" }, null, null, "_id");
		
		
		if (cursor.moveToFirst()) {
            do {
            	int id = cursor.getInt(0);
            	int hour = cursor.getInt(1);
            	int minutes = cursor.getInt(2);
            	String message = cursor.getString(3);
    			Map<String, Object> map = new HashMap<String, Object>();
    			map.put("time", String.format("%02d",hour)+":"+String.format("%02d",minutes));
    			map.put("tag", message);
    			map.put("alarm_ic_list", R.drawable.ic_menu_alarms);
    			list.add(map);
            } while (cursor.moveToNext());
        }
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
		return list;
	}
	
	public void alarmAlert(View view) {
//		Alarm alarm = new Alarm();
//        
//        long time;
//        if (alarm.id == -1) {
//            time = Alarms.addAlarm(this, alarm);
//        } else {
//            time = Alarms.setAlarm(this, alarm);
//        }
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(time);
//        Log.v(c.getTime().toLocaleString());
		
		startActivity(new Intent(this, SetAlarm.class));
	}
	
	public void removeAlarm(View view) {
		listView.getSelectedItem();
	}
	
	@Override
    public boolean onContextItemSelected(final MenuItem item) {
//        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
//        final int id = (int) info.id;
//        switch (item.getItemId()) {
//            case R.id.delete_alarm:
//                // Confirm that the alarm will be deleted.
//                new AlertDialog.Builder(this)
//                        .setTitle(getString(R.string.delete_alarm))
//                        .setMessage(getString(R.string.delete_alarm_confirm))
//                        .setPositiveButton(android.R.string.ok,
//                                new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface d, int w) {
//                                        Alarms.deleteAlarm(AlarmClock.this, id);
//                                    }
//                                })
//                        .setNegativeButton(android.R.string.cancel, null)
//                        .show();
//                return true;
//
//            case R.id.enable_alarm:
//                final Cursor c = (Cursor) mAlarmsList.getAdapter().getItem(info.position);
//                final Alarm alarm = new Alarm(c);
//                //�޸�
//                Alarms.enableAlarm(this, alarm.id, !alarm.enabled);
//                if (!alarm.enabled) {
//                    SetAlarm.popAlarmSetToast(this, alarm.hour, alarm.minutes, alarm.daysOfWeek);
//                }
//                return true;
//
//            case R.id.edit_alarm:
//                Intent intent = new Intent(this, SetAlarm.class);
//                intent.putExtra(Alarms.ALARM_ID, id);
//                startActivity(intent);
//                return true;
//
//            default:
//                break;
//        }
        return super.onContextItemSelected(item);
    }

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Intent intent = new Intent(this, SetAlarm.class);
//        intent.putExtra(Alarms., (int) id);
        startActivity(intent);
		
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
/*        switch (item.getItemId()) {
            case R.id.menu_item_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_item_desk_clock:
                startActivity(new Intent(this, DeskClock.class));
                return true;
            case R.id.menu_item_add_alarm:
                addNewAlarm();
                return true;
            default:
                break;
        }*/
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
