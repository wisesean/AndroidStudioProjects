package com.wisesean.heartbeat.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AalarmReceive extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if("com.foxmail.wisesean.ALARM_ALERT".equals(intent.getAction())) {
			Intent i=new Intent(context, AlarmActivity.class);
		    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.startActivity(i);
		}
	}

}
