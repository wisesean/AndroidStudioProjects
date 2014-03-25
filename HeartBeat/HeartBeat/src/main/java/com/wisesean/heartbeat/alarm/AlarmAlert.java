package com.wisesean.heartbeat.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.AsyncPlayer;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

public class AlarmAlert extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Alarm al = getIntent().getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
        Uri alert_uri = al.alert==null?Settings.System.DEFAULT_ALARM_ALERT_URI:al.alert;
		final AsyncPlayer ap = new AsyncPlayer("play"); 
	    ap.play(this, alert_uri, true, AudioManager.STREAM_MUSIC);

		new AlertDialog.Builder(AlarmAlert.this).
        setIcon(R.drawable.googley_eye_bird_pink).
		setTitle(R.string.dialog_title).
		setMessage(R.string.dialog_messge).
		setPositiveButton(R.string.dialog_cancel, new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				AlarmAlert.this.finish();
				ap.stop();
				AlarmManager am =	(AlarmManager) AlarmAlert.this.getSystemService(Context.ALARM_SERVICE);
				Intent intent = new Intent("android.alarm.demo.action");
				PendingIntent sender = PendingIntent.getBroadcast(AlarmAlert.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				am.cancel(sender);
			}
		}).create().show();
	}
}
