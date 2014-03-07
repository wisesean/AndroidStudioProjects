package com.wisesean.heartbeat.alarm;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * Manages each alarm
 */
public class SetAlarm extends PreferenceActivity implements
		TimePickerDialog.OnTimeSetListener,
		Preference.OnPreferenceChangeListener {
    
    
    private CheckBoxPreference mEnabledPref;

	private RadioButton RadioButton;
	private MenuItem mTestAlarmItem;

	private int mId = -1;
	private int mHour;
	private int mMinutes;
	private Preference mTimePref;
	private boolean mTimePickerCancelled;
	private Alarm mOriginalAlarm;

	/**
	 * Set an alarm. Requires an Alarms.ALARM_ID to be passed in as an extra.
	 * FIXME: Pass an Alarm object like every other Activity.
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.set_alarm);
		addPreferencesFromResource(R.xml.alarm_prefs);
		mTimePref = findPreference("time");
		
		Intent i = getIntent();
//        Alarm alarm = i.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
//
//        if (alarm == null) {
//            // No alarm means create a new alarm.
//            alarm = new Alarm();
//        }
//        mOriginalAlarm = alarm;
//
//        // Populate the prefs with the original alarm data.  updatePrefs also
//        // sets mId so it must be called before checking mId below.
//        updatePrefs(mOriginalAlarm);
		getListView().setItemsCanFocus(true);

		Button b = (Button) findViewById(R.id.alarm_save);
		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveAlarm();
				finish();
			}
		});
		final Button revert = (Button) findViewById(R.id.alarm_revert);
		revert.setEnabled(false);
		revert.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int newId = mId;
				if (mOriginalAlarm.id == -1) {
					Alarms.deleteAlarm(SetAlarm.this, newId);
				} else {
					saveAlarm();
				}
				revert.setEnabled(false);
			}
		});
		b = (Button) findViewById(R.id.alarm_delete);
//		if (mId == -1) {
//			b.setEnabled(false);
//		} else {
			b.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					deleteAlarm();
				}
			});
//		}
		mTimePickerCancelled = true;
		showTimePicker();
	}

	@Override
	public void onBackPressed() {
//		if (!mTimePickerCancelled) {
//			saveAlarm();
//		}
		finish();
	}
	
//	private void updatePrefs(Alarm alarm) {
//        mId = alarm.id;
//        mEnabledPref.setChecked(alarm.enabled);
//        mLabel.setText(alarm.label);
//        mHour = alarm.hour;
//        mMinute = alarm.minutes;
//        mRepeatPref.setDaysOfWeek(alarm.daysOfWeek);
//        mVibratePref.setChecked(alarm.vibrate);
//        // Give the alert uri to the preference.
//        mAlarmPref.setAlert(alarm.alert);
//        updateTime();
//    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.alarm_delete) {
            deleteAlarm();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
	
	private void showTimePicker() {
		new TimePickerDialog(this, this, mHour, mMinutes,
				DateFormat.is24HourFormat(this)).show();
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		mTimePickerCancelled = false;
		mHour = hourOfDay;
		mMinutes = minute;
		popAlarmSetToast(this, saveAlarmAndEnableRevert());
	}

	private long saveAlarmAndEnableRevert() {
		final Button revert = (Button) findViewById(R.id.alarm_revert);
		revert.setEnabled(true);
		return saveAlarm();
	}

	private long saveAlarm() {
		Alarm alarm = new Alarm();
		alarm.enabled = true;
		alarm.hour = mHour;
		alarm.minutes = mMinutes;

		alarm.interval = 0;

		long time;
		if (alarm.id == -1) {
			time = Alarms.addAlarm(this, alarm);
			mId = alarm.id;
		} else {
			time = Alarms.setAlarm(this, alarm);
		}
		return time;
	}

	private void deleteAlarm() {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.delete_alarm))
				.setMessage(getString(R.string.delete_alarm_confirm))
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface d, int w) {
								Alarms.deleteAlarm(SetAlarm.this, mId);
								finish();
							}
						}).setNegativeButton(android.R.string.cancel, null)
				.show();
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

	@Override
	protected void onResume() {
		super.onResume();
		Log.v("onResume\tonResume");
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

	public void setRadioButton(RadioButton radioButton) {
		RadioButton = radioButton;
	}

	public RadioButton getRadioButton() {
		return RadioButton;
	}

	public void setmTestAlarmItem(MenuItem mTestAlarmItem) {
		this.mTestAlarmItem = mTestAlarmItem;
	}

	public MenuItem getmTestAlarmItem() {
		return mTestAlarmItem;
	}
	private static final Handler sHandler = new Handler();
	@Override
	public boolean onPreferenceChange(final Preference p, Object newValue) {
		sHandler.post(new Runnable() {
            public void run() {
                // Editing any preference (except enable) enables the alarm.
                if (p != mEnabledPref) {
                    mEnabledPref.setChecked(true);
                }
                saveAlarmAndEnableRevert();
            }
        });
        return true;
	}
	
	@Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mTimePref) {
            showTimePicker();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
