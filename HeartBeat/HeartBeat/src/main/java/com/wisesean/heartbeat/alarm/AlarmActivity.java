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
import android.os.Bundle;
import android.provider.Settings;

public class AlarmActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final AsyncPlayer ap = new AsyncPlayer("play"); 
	    ap.play(this, Settings.System.DEFAULT_RINGTONE_URI, true, AudioManager.STREAM_MUSIC);
		new AlertDialog.Builder(AlarmActivity.this).
		setTitle("太阳晒屁股了").
		setMessage("臭人，该起床了！").
		setPositiveButton("知道嘞", new OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				AlarmActivity.this.finish();
				ap.stop();
				AlarmManager am =	(AlarmManager) AlarmActivity.this.getSystemService(Context.ALARM_SERVICE);
				Intent intent = new Intent("android.alarm.demo.action");
				PendingIntent sender = PendingIntent.getBroadcast(AlarmActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
				System.out.println(sender.hashCode());
				am.cancel(sender);
			}
		}).create().show();
	}
}
